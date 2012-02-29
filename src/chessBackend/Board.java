package chessBackend;

import java.util.Vector;

import chessPieces.Bishop;
import chessPieces.Piece;
import chessPieces.PieceID;
import chessPieces.PositionBonus;
import chessPieces.Values;
import chessPieces.PositionStatus;
import chessPieces.King;
import chessPieces.Knight;
import chessPieces.Pawn;
import chessPieces.Queen;
import chessPieces.Rook;

public class Board {
	private Piece[][] board;
//	private boolean inCheck;
//	private boolean noCheckNear;
//	private boolean noCheckFar;
	private int inCheck;
	private Vector<Piece> aiPieces;
	private Vector<Piece> userPieces;

	public Board(Piece[][] board, Vector<Piece> aiPieces, Vector<Piece> playerPieces) {
		this.board = board;
		this.aiPieces = aiPieces;
		this.userPieces = playerPieces;
	}

	public Board() {
		buildBoard();
	}

	public void buildBoard() {
		int rowOne;
		int rowTwo;
		Player player;

		// Build AI side of the board
		player = Player.AI;
		rowOne = 0;
		rowTwo = 1;

		aiPieces = new Vector<Piece>();
		aiPieces.add(new Rook(player, rowOne, 0, false));
		aiPieces.add(new Knight(player, rowOne, 1, false));
		aiPieces.add(new Bishop(player, rowOne, 2, false));
		aiPieces.add(new Queen(player, rowOne, 3, false));
		aiPieces.add(new King(player, rowOne, 4, false));
		aiPieces.add(new Bishop(player, rowOne, 5, false));
		aiPieces.add(new Knight(player, rowOne, 6, false));
		aiPieces.add(new Rook(player, rowOne, 7, false));

		for (int pawn = 0; pawn < 8; pawn++) {
			aiPieces.add(new Pawn(player, rowTwo, pawn, false));
		}

		// Build User side of the board
		player = Player.USER;
		rowOne = 7;
		rowTwo = 6;

		userPieces = new Vector<Piece>();
		userPieces.add(new Rook(player, rowOne, 0, false));
		userPieces.add(new Knight(player, rowOne, 1, false));
		userPieces.add(new Bishop(player, rowOne, 2, false));
		userPieces.add(new Queen(player, rowOne, 3, false));
		userPieces.add(new King(player, rowOne, 4, false));
		userPieces.add(new Bishop(player, rowOne, 5, false));
		userPieces.add(new Knight(player, rowOne, 6, false));
		userPieces.add(new Rook(player, rowOne, 7, false));

		for (int pawn = 0; pawn < 8; pawn++) {
			userPieces.add(new Pawn(player, rowTwo, pawn, false));
		}

		Piece aiPiece;
		Piece userPiece;
		board = new Piece[8][8];

		for (int i = 0; i < userPieces.size(); i++) {
			userPiece = userPieces.elementAt(i);
			board[userPiece.getRow()][userPiece.getCol()] = userPiece;
		}

		for (int i = 0; i < aiPieces.size(); i++) {
			aiPiece = aiPieces.elementAt(i);
			board[aiPiece.getRow()][aiPiece.getCol()] = aiPiece;
		}

	}

	public void makeMove(Move move, Player player) {

		if (hasPiece(move.getToRow(), move.getToCol())) {
			if (board[move.getToRow()][move.getToCol()].getPlayer() == Player.USER) {
				userPieces.remove(board[move.getToRow()][move.getToCol()]);
			} else {
				aiPieces.remove(board[move.getToRow()][move.getToCol()]);
			}
		}

		board[move.getFromRow()][move.getFromCol()].move(move);
		board[move.getToRow()][move.getToCol()] = board[move.getFromRow()][move.getFromCol()];
		board[move.getFromRow()][move.getFromCol()] = null;

		if (move.getNote() == MoveNote.NEW_QUEEN) {
			board[move.getToRow()][move.getToCol()] = new Queen(player, move.getToRow(), move.getToCol(), false);
		}

		if (move.getNote() == MoveNote.CASTLE_NEAR) {
			if (player == Player.AI) {
				makeMove(new Move(0, 7, 0, 5), player);
			} else {
				makeMove(new Move(7, 7, 7, 5), player);
			}
		}

		if (move.getNote() == MoveNote.CASTLE_FAR) {
			if (player == Player.AI) {
				makeMove(new Move(0, 0, 0, 3), player);
			} else {
				makeMove(new Move(7, 0, 7, 3), player);
			}
		}

	}

	public void undoMove(Move move, Player player) {

	}

	public Vector<Move> generateValidMoves(Player player) {
		Vector<Move> validMoves = new Vector<Move>(30);

		Vector<Piece> pieces = getPlayerPieces(player);
		Vector<Move> moves;
		for (int p = 0; p < pieces.size(); p++) {
			moves = pieces.elementAt(p).generateValidMoves(this);
			for (int m = 0; m < moves.size(); m++) {
				addSortValidMove(validMoves, moves.elementAt(m));
				// validMoves.add(moves.elementAt(m));
			}
		}

		return validMoves;
	}

	private void addSortValidMove(Vector<Move> validMoves, Move move) {
		for (int m = 0; m < validMoves.size(); m++) {
			if (move.getValue() >= validMoves.elementAt(m).getValue()) {
				validMoves.add(m, move);
				return;
			}
		}

		validMoves.add(move);
	}

	public PositionStatus checkPiece(int row, int col, Player player) {

		if (row > 7 || row < 0 || col > 7 || col < 0)
			return PositionStatus.OFF_BOARD;

		if (board[row][col] != null) {
			if (board[row][col].getPlayer() == player)
				return PositionStatus.FRIEND;
			else
				return PositionStatus.ENEMY;
		} else {
			return PositionStatus.NO_PIECE;
		}

	}

	public int getPieceValue(int row, int col) {
		int value;
		Player player = board[row][col].getPlayer();

		switch (board[row][col].getPieceID()) {
		case KNIGHT:
			value = this.getKnightValue();
			break;
		case PAWN:
			value = Values.PAWN_VALUE + PositionBonus.getPawnPositionBonus(row, col, player);
			break;
		case BISHOP:
			value = Values.BISHOP_VALUE;
			break;
		case KING:
			value = Values.KING_VALUE;
			break;
		case QUEEN:
			value = Values.QUEEN_VALUE;
			break;
		case ROOK:
			value = Values.ROOK_VALUE;
			break;
		default:
			value = 0;
			System.out.println("Error: invalid piece value request!");
		}

		return value;

	}

	public Piece getPiece(int row, int col) {
		return board[row][col];
	}

	public PieceID getPieceID(int row, int col) {
		if (board[row][col] != null) {
			return board[row][col].getPieceID();
		} else {
			return PieceID.NONE;
		}

	}

	public Player getPlayer(int row, int col) {
		if(board[row][col] != null){
			return board[row][col].getPlayer(); 
		}else{
			System.out.println("Error: requested player on null piece");
			return null;
		}
		
	}

	public boolean hasPiece(int row, int col) {
		return (board[row][col] != null);
	}

	public boolean hasMoved(int row, int col) {
		if (hasPiece(row, col)) {
			return board[row][col].hasMoved();
		} else {
			return true;
		}
	}

	public Vector<Piece> getPlayerPieces(Player player) {
		if (player == Player.USER) {
			return userPieces;
		} else {
			return aiPieces;
		}
	}
	
	public boolean isInCheckFar() {
		return ((inCheck & 4) != 0);
	}
	
	public boolean isInCheckNear() {
		return ((inCheck & 1) != 0);
	}

	public boolean isInCheck() {
		return ((inCheck & 2) != 0);
	}
	
	public void setInCheck(int inCheck){
		this.inCheck = inCheck;
	}

	public boolean canCastleFar(Player player) {

		if (player == Player.AI) {

			if (!hasMoved(0, 4)) {
				if (!hasPiece(0, 3) && !hasPiece(0, 2) && !hasPiece(0, 1)) {
					if (!hasMoved(0, 0) && !isInCheckFar()) {
						return true;
					}
				}
			}

		} else {

			if (!hasMoved(7, 4)) {
				if (!hasPiece(7, 3) && !hasPiece(7, 2) && !hasPiece(7, 1)) {
					if (!hasMoved(7, 0) && !isInCheckFar()) {
						return true;
					}
				}
			}

		}

		return false;

	}

	public boolean canCastleNear(Player player) {

		if (player == Player.AI) {

			if (!hasMoved(0, 4)) {
				if (!hasPiece(0, 5) && !hasPiece(0, 6)) {
					if (!hasMoved(0, 7) && !isInCheckNear()) {
						return true;
					}
				}
			}

		} else {
			if (!hasMoved(7, 4)) {
				if (!hasPiece(7, 5) && !hasPiece(7, 6)) {
					if (!hasMoved(7, 7) && !isInCheckNear()) {
						return true;
					}
				}
			}
		}

		return false;
	}

	public boolean farRookHasMoved(Player player) {
		if (player == Player.AI) {
			return hasMoved(0, 0);
		} else {
			return hasMoved(7, 0);
		}
	}

	public boolean nearRookHasMoved(Player player) {
		if (player == Player.AI) {
			return hasMoved(0, 7);
		} else {
			return hasMoved(7, 7);
		}
	}

	public boolean kingHasMoved(Player player) {
		if (player == Player.AI) {
			return hasMoved(0, 4);
		} else {
			return hasMoved(7, 4);
		}
	}

	public int getKnightValue() {
		int piecesMissing = 32 - (aiPieces.size() + userPieces.size());
		return Values.KNIGHT_VALUE - piecesMissing * Values.KNIGHT_ENDGAME_INC;
	}

	public Board getCopy() {
		Piece[][] copyBoard = new Piece[8][8];
		Vector<Piece> copyAiPieces = new Vector<Piece>(16);
		Vector<Piece> copyPlayerPieces = new Vector<Piece>(16);
		Board newBoard = new Board(copyBoard, copyAiPieces, copyPlayerPieces);

		for (int row = 0; row < 8; row++) {
			for (int col = 0; col < 8; col++) {

				if (board[row][col] != null) {

					copyBoard[row][col] = board[row][col].getCopy(this);

					if (copyBoard[row][col].getPlayer() == Player.AI) {
						copyAiPieces.add(copyBoard[row][col]);
					} else {
						copyPlayerPieces.add(copyBoard[row][col]);
					}
				}
			}
		}

		return newBoard;
	}

	public String toString() {
		String stringBoard = "";

		for (int row = 0; row < 8; row++) {
			for (int col = 0; col < 8; col++) {
				if (board[row][col] != null) {
					stringBoard += board[row][col].toString();
				} else {
					stringBoard += "E0,";
				}
			}
		}

		return stringBoard;
	}

	public static Board fromString(String stringBoard) {
		Piece[][] board = new Piece[8][8];
		Vector<Piece> aiPieces = new Vector<Piece>();
		Vector<Piece> userPieces = new Vector<Piece>();

		String[] stringPieces = stringBoard.split(",");
		Piece piece;
		int i = 0;
		for (int row = 0; row < 8; row++) {
			for (int col = 0; col < 8; col++) {

				piece = Piece.fromString(stringPieces[i].trim(), row, col);

				board[row][col] = piece;

				if (piece != null) {
					if (piece.getPlayer() == Player.AI) {
						aiPieces.add(piece);
					} else {
						userPieces.add(piece);
					}
				}

				i++;
			}
		}

		return new Board(board, aiPieces, userPieces);
	}

	public static Board fromFile(String fileName) {
		String stringBoard = ReadWriteTextFile.getContents(fileName);

		return fromString(stringBoard);
	}
}
