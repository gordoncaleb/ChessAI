package chessBackend;

import chessPieces.Piece;

public class Move {
	private MoveNote note;
	private Piece pieceTaken;
	private boolean hadMoved;
	private boolean validated;
	private int value;
	private int fromRow;
	private int fromCol;
	private int toRow;
	private int toCol;

	public Move(int fromRow, int fromCol, int toRow, int toCol) {
		this.fromRow = fromRow;
		this.fromCol = fromCol;
		this.toRow = toRow;
		this.toCol = toCol;
		this.note = MoveNote.NONE;
		this.pieceTaken = null;
		this.hadMoved = false;
		this.validated = true;
	}

	public Move(int fromRow, int fromCol, int toRow, int toCol, int value, MoveNote note) {
		this.fromRow = fromRow;
		this.fromCol = fromCol;
		this.toRow = toRow;
		this.toCol = toCol;
		this.note = note;
		this.value = value;
		this.pieceTaken = null;
		this.hadMoved = false;
		this.validated = true;
	}

	public Move(int fromRow, int fromCol, int toRow, int toCol, int value) {
		this.fromRow = fromRow;
		this.fromCol = fromCol;
		this.toRow = toRow;
		this.toCol = toCol;
		this.note = MoveNote.NONE;
		this.value = value;
		this.hadMoved = false;
		this.validated = true;
	}

	public boolean equals(Move m) {
		if (m.toRow == this.toRow && m.toCol == this.toCol && this.fromRow == m.fromRow && this.fromCol == m.fromCol)
			return true;
		else
			return false;
	}

	public String toString() {
		return ("Moving from " + fromRow + "," + fromCol + " to " + toRow + "," + toCol + " Move Note: " + note.toString() + " Value:" + value);
	}

	public void setNote(MoveNote note) {
		this.note = note;
	}

	public MoveNote getNote() {
		return this.note;
	}

	public int getFromRow() {
		return fromRow;
	}

	public void setFromRow(int fromRow) {
		this.fromRow = fromRow;
	}

	public int getFromCol() {
		return fromCol;
	}

	public void setFromCol(int fromCol) {
		this.fromCol = fromCol;
	}

	public int getToRow() {
		return toRow;
	}

	public void setToRow(int toRow) {
		this.toRow = toRow;
	}

	public int getToCol() {
		return toCol;
	}

	public void setToCol(int toCol) {
		this.toCol = toCol;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}
	
	public void invalidate(){
		this.validated = false;
	}

	public boolean isValidated() {
		return validated;
	}

	public Piece getPieceTaken() {
		return pieceTaken;
	}

	public void setPieceTaken(Piece pieceTaken) {
		this.pieceTaken = pieceTaken;
	}

	public boolean hadMoved() {
		return hadMoved;
	}

	public void setHadMoved(boolean hadMoved) {
		this.hadMoved = hadMoved;
	}

	public Move reverse(){
		return new Move(toRow, toCol, fromRow, fromCol);
	}

}
