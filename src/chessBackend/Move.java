package chessBackend;

import chessPieces.Piece;
import chessPieces.PieceID;

public class Move {

	private static final int hadMovedMask = 1 << 15;
	private static final int hasPieceTakenMask = 1 << 16;
	private static final int pieceTakenHasMoved = 1 << 26;
	private static final int fromToMask = 0xFFF;
	private static final int notNoteMask = ~(0x7000);
	private static final int notPieceTaken = ~(0x7FF << 16);

	private int value;
	private int moveInt;

	public static void main(String[] args) {

		Move move;
		Piece pieceTaken1;
		Piece pieceTaken2;

		for (int n = 0; n < MoveNote.values().length; n++) {
			for (int ptid = 0; ptid < PieceID.values().length; ptid++) {
				for (int ptc = 0; ptc < 8; ptc++) {
					for (int ptr = 0; ptr < 8; ptr++) {
						for (int fr = 0; fr < 8; fr++) {
							for (int fc = 0; fc < 8; fc++) {
								for (int tr = 0; tr < 8; tr++) {
									for (int tc = 0; tc < 8; tc++) {
										pieceTaken1 = new Piece(PieceID.PAWN, Side.WHITE, 1, 2, false);
										move = new Move(fr, fc, tr, tc, 100, MoveNote.NONE, pieceTaken1, true);

										pieceTaken2 = new Piece(PieceID.values()[ptid], Side.WHITE, ptr, ptc, true);
										move.setPieceTaken(pieceTaken2);
										
										move.setNote(MoveNote.values()[n]);
										move.setHadMoved(false);

										if (move.getFromRow() != fr) {
											System.out.println("problem");
										}
										if (move.getFromCol() != fc) {
											System.out.println("problem");
										}
										if (move.getToRow() != tr) {
											System.out.println("problem");
										}
										if (move.getToCol() != tc) {
											System.out.println("problem");
										}
										if (move.getNote() != MoveNote.values()[n]) {
											System.out.println("problem");
										}

										if (move.getPieceTakenID() != PieceID.values()[ptid]) {
											System.out.println("problem");
										}

										if (move.getPieceTakenRow() != ptr) {
											System.out.println("problem");
										}

										if (move.getPieceTakenCol() != ptc) {
											System.out.println("problem");
										}

										if (!move.getPieceTakenHasMoved()) {
											System.out.println("problem");
										}

										if (move.hadMoved()) {
											System.out.println("problem");
										}

									}
								}
							}
						}
					}
				}
			}
		}

		System.out.println("Done");
	}

	public Move(int fromRow, int fromCol, int toRow, int toCol) {
		this(fromRow, fromCol, toRow, toCol, 0, MoveNote.NONE, null, false);
	}

	public Move(int fromRow, int fromCol, int toRow, int toCol, int value) {
		this(fromRow, fromCol, toRow, toCol, value, MoveNote.NONE, null, false);
	}

	public Move(int fromRow, int fromCol, int toRow, int toCol, int value, MoveNote note) {
		this(fromRow, fromCol, toRow, toCol, value, note, null, false);
	}

	public Move(int fromRow, int fromCol, int toRow, int toCol, int value, MoveNote note, Piece pieceTaken) {
		this(fromRow, fromCol, toRow, toCol, 0, MoveNote.NONE, pieceTaken, false);
	}

	public Move(int fromRow, int fromCol, int toRow, int toCol, int value, MoveNote note, Piece pieceTaken, boolean hadMoved) {

		this.value = value;

		moveInt = (note.ordinal() << 12) | (fromRow << 9) | (fromCol << 6) | (toRow << 3) | toCol;

		if (hadMoved) {
			moveInt |= hadMovedMask;
		}

		if (pieceTaken != null) {
			moveInt |= (pieceTaken.getPieceID().ordinal() << 23) | (pieceTaken.getRow() << 20) | (pieceTaken.getCol() << 17) | hasPieceTakenMask;

			if (pieceTaken.hasMoved()) {
				moveInt |= pieceTakenHasMoved;
			}
		}

	}

	public Move(int moveInt, int value) {
		this.moveInt = moveInt;
		this.value = value;
	}

	public boolean equals(Object moveObject) {
		if (moveObject instanceof Move) {
			return equals((Move) moveObject);
		} else {
			return false;
		}
	}

	public boolean equals(Move m) {

		if ((m.getMoveInt() & fromToMask) == (moveInt & fromToMask))
			return true;
		else
			return false;
	}

	public String toString() {
		String moveString;
		if (hasPieceTaken()) {
			Piece pieceTaken = new Piece(getPieceTakenID(), null, getPieceTakenRow(), getPieceTakenCol(), getPieceTakenHasMoved());
			moveString = "Moving from " + getFromRow() + "," + getFromCol() + " to " + getToRow() + "," + getToCol() + " Move Note: "
					+ getNote().toString() + " Value:" + value + " PieceTaken: " + pieceTaken.toString();
		} else {
			moveString = "Moving from " + getFromRow() + "," + getFromCol() + " to " + getToRow() + "," + getToCol() + " Move Note: "
					+ getNote().toString() + " Value:" + value;
		}
		return moveString;
	}

	public String toXML() {
		String xmlMove = "";

		xmlMove += "<move>\n";

		xmlMove += "<from>" + getFromRow() + "," + getFromCol() + "</from>\n";
		xmlMove += "<to>" + getToRow() + "," + getToCol() + "</to>\n";

		if (hadMoved()) {
			xmlMove += "<had_moved>" + hadMoved() + "</had_moved>\n";
		}

		MoveNote note = getNote();
		if (note != MoveNote.NONE) {
			xmlMove += "<note>" + note.toString() + "</note>\n";
		}

		if (hasPieceTaken()) {
			xmlMove += new Piece(getPieceTakenID(), null, getPieceTakenRow(), getPieceTakenCol(), getPieceTakenHasMoved()).toXML();
		}

		xmlMove += "</move>\n";

		return xmlMove;
	}

	public void setNote(MoveNote note) {
		moveInt &= notNoteMask;
		moveInt |= (note.ordinal() << 12);
	}

	public MoveNote getNote() {
		return MoveNote.values()[((moveInt >> 12) & 0x7)];
	}

	public int getFromRow() {
		return ((moveInt >> 9) & 0x7);
	}

	public int getFromCol() {
		return ((moveInt >> 6) & 0x7);
	}

	public int getToRow() {
		return ((moveInt >> 3) & 0x7);
	}

	public int getToCol() {
		return (moveInt & 0x7);
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public void setPieceTaken(Piece pieceTaken) {
		moveInt &= notPieceTaken;
		if (pieceTaken != null) {
			moveInt |= (pieceTaken.getPieceID().ordinal() << 23) | (pieceTaken.getRow() << 20) | (pieceTaken.getCol() << 17) | hasPieceTakenMask;

			if (pieceTaken.hasMoved()) {
				moveInt |= pieceTakenHasMoved;
			}
		}
	}

	public boolean hadMoved() {
		return ((moveInt & hadMovedMask) != 0);
	}

	public void setHadMoved(boolean hadMoved) {
		if (hadMoved) {
			moveInt |= hadMovedMask;
		} else {
			moveInt &= ~hadMovedMask;
		}
	}

	public boolean hasPieceTaken() {
		return ((moveInt & hasPieceTakenMask) != 0);
	}

	public boolean getPieceTakenHasMoved() {
		return ((moveInt & pieceTakenHasMoved) != 0);
	}

	public int getPieceTakenRow() {
		return (moveInt >> 20) & 0x7;
	}

	public int getPieceTakenCol() {
		return (moveInt >> 17) & 0x7;
	}

	public PieceID getPieceTakenID() {
		return PieceID.values()[(moveInt >> 23) & 0x7];
	}

	public Move getCopy() {
		return new Move(moveInt, value);
	}

	public int getMoveInt() {
		return moveInt;
	}

}
