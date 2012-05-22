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

	private long moveLong;

	public static void main(String[] args) {

		long moveLong;
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
										moveLong = Move.moveLong(fr, fc, tr, tc, 0xFFFF, MoveNote.NONE, pieceTaken1, true);

										pieceTaken2 = new Piece(PieceID.values()[ptid], Side.WHITE, ptr, ptc, true);
										moveLong = Move.setPieceTaken(moveLong, pieceTaken2);

										moveLong = Move.setNote(moveLong, MoveNote.values()[n]);
										
										moveLong = Move.setHadMoved(moveLong, false);

										if (Move.getFromRow(moveLong) != fr) {
											System.out.println("getFromRow");
										}
										if (Move.getFromCol(moveLong) != fc) {
											System.out.println("getFromCol");
										}
										if (Move.getToRow(moveLong) != tr) {
											System.out.println("getToRow");
										}
										if (Move.getToCol(moveLong) != tc) {
											System.out.println("getToCol");
										}
										if (Move.getNote(moveLong) != MoveNote.values()[n]) {
											System.out.println("getNote");
										}

										if (Move.getPieceTakenID(moveLong) != PieceID.values()[ptid]) {
											System.out.println("getPieceTakenID");
										}

										if (Move.getPieceTakenRow(moveLong) != ptr) {
											System.out.println("getPieceTakenRow");
										}

										if (Move.getPieceTakenCol(moveLong) != ptc) {
											System.out.println("getPieceTakenCol");
										}

										if (!Move.getPieceTakenHasMoved(moveLong)) {
											System.out.println("getPieceTakenHasMoved");
										}

										if (Move.hadMoved(moveLong)) {
											System.out.println("hadMoved");
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
	
	//----------------------
	
	public static long moveLong(int fromRow, int fromCol, int toRow, int toCol) {
		return moveLong(fromRow, fromCol, toRow, toCol, 0, MoveNote.NONE, null, false);
	}

	public static long moveLong(int fromRow, int fromCol, int toRow, int toCol, int value) {
		return moveLong(fromRow, fromCol, toRow, toCol, value, MoveNote.NONE, null, false);
	}

	public static long moveLong(int fromRow, int fromCol, int toRow, int toCol, int value, MoveNote note) {
		return moveLong(fromRow, fromCol, toRow, toCol, value, note, null, false);
	}

	public static long moveLong(int fromRow, int fromCol, int toRow, int toCol, int value, MoveNote note, Piece pieceTaken) {
		return moveLong(fromRow, fromCol, toRow, toCol, 0, MoveNote.NONE, pieceTaken, false);
	}

	
	/**
	 * Bit-field
	 * 0-2 = toCol
	 * 3-5 = toRow
	 * 6-8 = fromCol
	 * 9-11 = fromRow
	 * 12-14 = move note
	 * 15 = hadMoved
	 * 16 = has piece taken
	 * 17-19 = pieceTaken col
	 * 20-22 = pieceTaken row
	 * 23-25 = pieceTaken id
	 * 26 = pieceTaken has moved
	 * 32-48 = moveValue
	 */
	
	public Move(int fromRow, int fromCol, int toRow, int toCol, int value, MoveNote note, Piece pieceTaken, boolean hadMoved) {

		moveLong = (note.ordinal() << 12) | (fromRow << 9) | (fromCol << 6) | (toRow << 3) | toCol;

		if (hadMoved) {
			moveLong |= hadMovedMask;
		}

		if (pieceTaken != null) {
			moveLong |= (pieceTaken.getPieceID().ordinal() << 23) | (pieceTaken.getRow() << 20) | (pieceTaken.getCol() << 17) | hasPieceTakenMask;

			if (pieceTaken.hasMoved()) {
				moveLong |= pieceTakenHasMoved;
			}
		}

		//moveLong |= ((long) value) << 32;

	}

	public static long moveLong(int fromRow, int fromCol, int toRow, int toCol, int value, MoveNote note, Piece pieceTaken, boolean hadMoved) {
		long moveLong;

		moveLong = (note.ordinal() << 12) | (fromRow << 9) | (fromCol << 6) | (toRow << 3) | toCol;

		if (hadMoved) {
			moveLong |= hadMovedMask;
		}

		if (pieceTaken != null) {
			moveLong |= (pieceTaken.getPieceID().ordinal() << 23) | (pieceTaken.getRow() << 20) | (pieceTaken.getCol() << 17) | hasPieceTakenMask;

			if (pieceTaken.hasMoved()) {
				moveLong |= pieceTakenHasMoved;
			}
		}

		moveLong |= ((long) value) << 32;

		return moveLong;
	}

	public Move(long moveInt) {
		this.moveLong = moveInt;
	}

	public boolean equals(Object moveObject) {
		if (moveObject instanceof Move) {
			return equals((Move) moveObject);
		} else {
			return false;
		}
	}

	public static boolean equals(long moveLongA, long moveLongB) {

		if ((moveLongA & fromToMask) == (moveLongB & fromToMask))
			return true;
		else
			return false;
	}
	
	public String toString(){
		return toString(moveLong);
	}

	public static String toString(long moveLong) {
		String moveString;
		if (hasPieceTaken(moveLong)) {
			Piece pieceTaken = new Piece(getPieceTakenID(moveLong), null, getPieceTakenRow(moveLong), getPieceTakenCol(moveLong),
					getPieceTakenHasMoved(moveLong));
			moveString = "Moving from " + getFromRow(moveLong) + "," + getFromCol(moveLong) + " to " + getToRow(moveLong) + "," + getToCol(moveLong)
					+ " Move Note: " + getNote(moveLong).toString() + " Value:" + getValue(moveLong) + " PieceTaken: " + pieceTaken.toString();
		} else {
			moveString = "Moving from " + getFromRow(moveLong) + "," + getFromCol(moveLong) + " to " + getToRow(moveLong) + "," + getToCol(moveLong)
					+ " Move Note: " + getNote(moveLong).toString() + " Value:" + getValue(moveLong);
		}
		return moveString;
	}
	
	public String toXML(){
		return toXML(moveLong);
	}

	public static String toXML(long moveLong) {
		String xmlMove = "";

		xmlMove += "<move>\n";

		xmlMove += "<from>" + getFromRow(moveLong) + "," + getFromCol(moveLong) + "</from>\n";
		xmlMove += "<to>" + getToRow(moveLong) + "," + getToCol(moveLong) + "</to>\n";

		if (hadMoved(moveLong)) {
			xmlMove += "<had_moved>" + hadMoved(moveLong) + "</had_moved>\n";
		}

		MoveNote note = getNote(moveLong);
		if (note != MoveNote.NONE) {
			xmlMove += "<note>" + note.toString() + "</note>\n";
		}

		if (hasPieceTaken(moveLong)) {
			xmlMove += new Piece(getPieceTakenID(moveLong), null, getPieceTakenRow(moveLong), getPieceTakenCol(moveLong),
					getPieceTakenHasMoved(moveLong)).toXML();
		}

		xmlMove += "</move>\n";

		return xmlMove;
	}

	public static long setNote(long moveLong, MoveNote note) {
		moveLong &= notNoteMask;
		moveLong |= (note.ordinal() << 12);
		
		return moveLong;
	}

	public static MoveNote getNote(long moveLong) {
		return MoveNote.values()[(int) ((moveLong >> 12) & 0x7)];
	}

	public static int getFromRow(long moveLong) {
		return (int) ((moveLong >> 9) & 0x7);
	}

	public static int getFromCol(long moveLong) {
		return (int) ((moveLong >> 6) & 0x7);
	}

	public static int getToRow(long moveLong) {
		return (int) ((moveLong >> 3) & 0x7);
	}

	public static int getToCol(long moveLong) {
		return (int) (moveLong & 0x7);
	}

	public static int getValue(long moveLong) {
		return (int) (moveLong >> 32);
	}

	public static long setValue(long moveLong, int value) {
		moveLong = moveLong & 0xFFFFFFFF;
		moveLong |= ((long) value) << 32;
		
		return moveLong;
	}

	public static long setPieceTaken(long moveLong, Piece pieceTaken) {
		moveLong &= notPieceTaken;
		if (pieceTaken != null) {
			moveLong |= (pieceTaken.getPieceID().ordinal() << 23) | (pieceTaken.getRow() << 20) | (pieceTaken.getCol() << 17) | hasPieceTakenMask;

			if (pieceTaken.hasMoved()) {
				moveLong |= pieceTakenHasMoved;
			}
		}
		
		return moveLong;
	}

	public static boolean hadMoved(long moveLong) {
		return ((moveLong & hadMovedMask) != 0);
	}

	public static long setHadMoved(long moveLong, boolean hadMoved) {
		if (hadMoved) {
			moveLong |= hadMovedMask;
		} else {
			moveLong &= ~hadMovedMask;
		}
		
		return moveLong;
	}

	public static boolean hasPieceTaken(long moveLong) {
		return ((moveLong & hasPieceTakenMask) != 0);
	}

	public static boolean getPieceTakenHasMoved(long moveLong) {
		return ((moveLong & pieceTakenHasMoved) != 0);
	}

	public static int getPieceTakenRow(long moveLong) {
		return (int) ((moveLong >> 20) & 0x7);
	}

	public static int getPieceTakenCol(long moveLong) {
		return (int) ((moveLong >> 17) & 0x7);
	}

	public static PieceID getPieceTakenID(long moveLong) {
		return PieceID.values()[(int) ((moveLong >> 23) & 0x7)];
	}

	public Move getCopy() {
		return new Move(moveLong);
	}

	public long getMoveLong() {
		return moveLong;
	}

}
