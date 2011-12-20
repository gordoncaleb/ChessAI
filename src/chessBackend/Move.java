package chessBackend;

import chessAI.DecisionNode;
import chessPieces.PieceID;
import chessPieces.Values;

public class Move {
	private DecisionNode node;
	private MoveNote note;
	private PieceID pieceTaken;
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
	}

	public Move(int fromRow, int fromCol, int toRow, int toCol, int value, MoveNote note) {
		this.fromRow = fromRow;
		this.fromCol = fromCol;
		this.toRow = toRow;
		this.toCol = toCol;
		this.note = note;
		this.value = value;
	}

	public Move(int fromRow, int fromCol, int toRow, int toCol, int value, PieceID id) {
		this.fromRow = fromRow;
		this.fromCol = fromCol;
		this.toRow = toRow;
		this.toCol = toCol;
		this.note = MoveNote.TAKE_PIECE;
		this.value = value;
		this.pieceTaken = id;
	}

	public boolean equals(Move m) {
		if (m.toRow == this.toRow && m.toCol == this.toCol && this.fromRow == m.fromRow && this.fromCol == m.fromCol)
			return true;
		else
			return false;
	}

	public String toString() {
		return ("Moving from " + fromRow + "," + fromCol + " to " + toRow + "," + toCol + " Move Note: " + note.toString());
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

	public DecisionNode getNode() {
		return node;
	}

	public void setNode(DecisionNode node) {
		this.node = node;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public boolean isValidated() {
		return note != MoveNote.INVALIDATED;
	}

	public PieceID getPieceTaken() {
		return pieceTaken;
	}

	public void setPieceTaken(PieceID pieceTaken) {
		this.pieceTaken = pieceTaken;
	}

}
