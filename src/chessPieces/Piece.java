package chessPieces;

import java.util.Vector;

import chessBackend.BitBoard;
import chessBackend.Board;
import chessBackend.Side;
import chessBackend.Move;

public abstract class Piece {
	private int row;
	private int col;
	private Side player;
	private boolean moved;
	private long blockingVector;

	public Piece(Side player, int row, int col, boolean moved) {

		this.moved = moved;
		this.player = player;
		this.row = row;
		this.col = col;
		this.blockingVector = BitBoard.ALL_ONES;

	}

	public int getRow() {
		return row;
	}

	public int getCol() {
		return col;
	}

	public long getBit() {
		return BitBoard.getMask(row, col);
	}

	public void setPos(int row, int col) {
		this.row = row;
		this.col = col;
	}

	public void move(Move newMove) {
		setPos(newMove.getToRow(), newMove.getToCol());
		moved = true;
	}

	public Side getSide() {
		return player;
	}

	public boolean hasMoved() {
		return moved;
	}

	public void setMoved(boolean moved) {
		this.moved = moved;
	}

	public void setBlockingVector(long blockingVector) {
		this.blockingVector = blockingVector;
	}

	public void clearBlocking() {
		blockingVector = BitBoard.ALL_ONES;
	}
	
	public long getBlockingVector(){
		return blockingVector;
	}

	public String toString() {
		String id;

		if (this.getSide() == Side.BLACK) {
			id = this.getStringID();
		} else {
			id = this.getStringID().toLowerCase();
		}

		return id;
	}

	public String toXML() {
		String xmlPiece = "";

		xmlPiece += "<piece>\n";
		xmlPiece += "<id>" + toString() + "</id>\n";
		xmlPiece += "<has_moved>" + hasMoved() + "</has_moved>\n";
		xmlPiece += "<position>" + getRow() + "," + getCol() + "</position>\n";
		xmlPiece += "</piece>\n";

		return xmlPiece;
	}

	public static Piece fromString(String stringPiece, int row, int col) {
		Side player;
		Piece piece = null;

		boolean hasMoved = false;

		try {
			if (Integer.parseInt(stringPiece.substring(1, 2)) % 2 != 0) {
				hasMoved = true;
			}
		} catch (Exception e) {

		}

		if (stringPiece.charAt(0) < 'a') {
			player = Side.BLACK;
		} else {
			player = Side.WHITE;
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

		if (piece.getRow() == row && piece.getCol() == col && piece.getSide() == player && piece.getPieceID() == this.getPieceID()) {
			return true;
		} else {
			return false;
		}
	}

	public boolean isValidMove(int toRow, int toCol, long[] nullMoveInfo) {
		long mask = BitBoard.getMask(toRow,toCol);
		
		if((mask & nullMoveInfo[1] & blockingVector) != 0){
			return true;
		}else{
			return false;
		}
	}

	public abstract String getStringID();

	public abstract PieceID getPieceID();

	public abstract String getName();

	public abstract Vector<Move> generateValidMoves(Board board, long[] nullMoveInfo, long[] posBitBoard);

	public abstract void getNullMoveInfo(Board board, long[] nullMoveBitBoards);

	public abstract Piece getCopy();

}
