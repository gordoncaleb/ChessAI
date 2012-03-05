package chessPieces;

import java.util.Vector;

import chessBackend.Board;
import chessBackend.Player;
import chessBackend.Move;

public abstract class Piece {
	private int row;
	private int col;
	private Player player;
	private boolean moved;

	public Piece(Player player, int row, int col, boolean moved) {

		this.moved = moved;
		this.player = player;
		this.row = row;
		this.col = col;

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

	public void setMoved(boolean moved) {
		this.moved = moved;
	}

	public String toString() {
		String moved;
		String id;

		if (this.hasMoved()) {
			moved = "1";
		} else {
			moved = "0";
		}

		if (this.getPlayer() == Player.AI) {
			id = this.getStringID();
		} else {
			id = this.getStringID().toLowerCase();
		}

		return id + moved;
	}

	public static Piece fromString(String stringPiece, int row, int col) {
		boolean hasMoved;
		Player player;
		Piece piece = null;

		if (stringPiece.charAt(1) == '1') {
			hasMoved = true;
		} else {
			hasMoved = false;
		}

		if (stringPiece.charAt(0) < 'a') {
			player = Player.AI;
		} else {
			player = Player.USER;
		}

		char type = stringPiece.toLowerCase().charAt(0);

		switch (type) {
		case 'r':
			piece = new Rook(player, row, col, hasMoved);
			break;
		case 'n':
			piece = new Knight(player, row, col, hasMoved);
			break;
		case 'b':
			piece = new Bishop(player, row, col, hasMoved);
			break;
		case 'q':
			piece = new Queen(player, row, col, hasMoved);
			break;
		case 'k':
			piece = new King(player, row, col, hasMoved);
			break;
		case 'p':
			piece = new Pawn(player, row, col, hasMoved);
			break;
		}

		return piece;

	}

	public boolean equals(Piece piece) {

		if (piece == null) {
			return false;
		}

		if (piece.getRow() == row && piece.getCol() == col && piece.getPlayer() == player && piece.getPieceID() == this.getPieceID()) {
			return true;
		} else {
			return false;
		}
	}

	public abstract String getStringID();

	public abstract PieceID getPieceID();

	public abstract String getName();

	public abstract Vector<Move> generateValidMoves(Board board, Move lastMoveMade);

	public abstract Piece getCopy(Board board);

}
