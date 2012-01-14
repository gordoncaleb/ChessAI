package chessPieces;

import chessBackend.Player;
import chessBackend.Move;

public class PieceBase {
	private int row;
	private int col;
	private Player player;
	private boolean moved;
	private int value;

	public PieceBase(Player player, int row, int col) {

		moved = false;

		this.player = player;
		this.row = row;
		this.col = col;

	}

	public PieceBase(Player player, int row, int col, boolean moved, int value) {
		this.row = row;
		this.col = col;
		this.player = player;
		this.moved = moved;
		this.value = value;
	}

	public int getRow() {
		return row;
	}

	public int getCol() {
		return col;
	}

	public void setPos(int row, int col) {
		this.row = row;
		this.col = col;
	}

	public void setPieceValue(int value) {
		this.value = value;
	}

	public int getPieceValue() {
		return value;
	}

	public void move(Move newMove) {
		setPos(newMove.getToRow(), newMove.getToCol());
		moved = true;
	}

	public Player getPlayer() {
		return player;
	}

	public boolean hasMoved() {
		return moved;
	}

	public void updateValue() {
		// only Knight and Pawn use this
	}

}
