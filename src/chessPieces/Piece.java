package chessPieces;

import java.util.Vector;

import chessBackend.BitBoard;
import chessBackend.Board;
import chessBackend.Side;
import chessBackend.Move;

public class Piece {
	private int row;
	private int col;
	private Side player;
	private boolean moved;
	private long blockingVector;
	private PieceID id;

	public Piece(PieceID id, Side player, int row, int col, boolean moved) {
		this.id = id;
		this.moved = moved;
		this.player = player;
		this.row = row;
		this.col = col;
		this.blockingVector = BitBoard.ALL_ONES;

		if (id == null) {
			System.out.println("WTF");
		}

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
	
	public void reverseMove(Move newMove){
		setPos(newMove.getFromRow(), newMove.getFromCol());
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

	public long getBlockingVector() {
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

		PieceID id;
		char type = stringPiece.toLowerCase().charAt(0);

		switch (type) {
		case 'r':
			id = PieceID.ROOK;
			break;
		case 'n':
			id = PieceID.KNIGHT;
			break;
		case 'b':
			id = PieceID.BISHOP;
			break;
		case 'q':
			id = PieceID.QUEEN;
			break;
		case 'k':
			id = PieceID.KING;
			break;
		case 'p':
			id = PieceID.PAWN;
			break;
		default:
			id = null;
		}

		if (id != null) {
			return new Piece(id, player, row, col, hasMoved);
		} else {
			return null;
		}

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
		long mask = BitBoard.getMask(toRow, toCol);

		if ((mask & nullMoveInfo[1] & blockingVector) != 0) {
			return true;
		} else {
			return false;
		}
	}

	public String getStringID() {
		switch (id) {
		case ROOK:
			return Rook.getStringID();
		case KNIGHT:
			return Knight.getStringID();
		case BISHOP:
			return Bishop.getStringID();
		case QUEEN:
			return Queen.getStringID();
		case KING:
			return King.getStringID();
		case PAWN:
			return Pawn.getStringID();
		default:
			return "";
		}

	}

	public PieceID getPieceID() {
		return id;
	}

	public void setPieceID(PieceID id) {
		this.id = id;
	}

	public String getName() {
		switch (id) {
		case ROOK:
			return Rook.getName();
		case KNIGHT:
			return Knight.getName();
		case BISHOP:
			return Bishop.getName();
		case QUEEN:
			return Queen.getName();
		case KING:
			return King.getName();
		case PAWN:
			return Pawn.getName();
		default:
			return "";
		}
	}

	public Vector<Move> generateValidMoves(Board board, long[] nullMoveInfo, long[] posBitBoard) {

		switch (id) {
		case ROOK:
			return Rook.generateValidMoves(this, board, nullMoveInfo, posBitBoard);
		case KNIGHT:
			return Knight.generateValidMoves(this, board, nullMoveInfo, posBitBoard);
		case BISHOP:
			return Bishop.generateValidMoves(this, board, nullMoveInfo, posBitBoard);
		case QUEEN:
			return Queen.generateValidMoves(this, board, nullMoveInfo, posBitBoard);
		case KING:
			return King.generateValidMoves(this, board, nullMoveInfo, posBitBoard);
		case PAWN:
			return Pawn.generateValidMoves(this, board, nullMoveInfo, posBitBoard);
		default:
			return null;
		}

	}

	public void getNullMoveInfo(Board board, long[] nullMoveBitBoards) {
		switch (id) {
		case ROOK:
			Rook.getNullMoveInfo(this, board, nullMoveBitBoards);
			break;
		case KNIGHT:
			Knight.getNullMoveInfo(this, board, nullMoveBitBoards);
			break;
		case BISHOP:
			Bishop.getNullMoveInfo(this, board, nullMoveBitBoards);
			break;
		case QUEEN:
			Queen.getNullMoveInfo(this, board, nullMoveBitBoards);
			break;
		case KING:
			King.getNullMoveInfo(this, board, nullMoveBitBoards);
			break;
		case PAWN:
			Pawn.getNullMoveInfo(this, board, nullMoveBitBoards);
			break;
		}
	}

	public Piece getCopy() {
		return new Piece(id, player, row, col, moved);
	}

}
