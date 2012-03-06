package chessBackend;

import java.util.Stack;
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
	private int boardState;
	private Vector<Piece> aiPieces;
	private Vector<Piece> userPieces;
	private Player player;
	private Stack<Move> moveHistory;
	private RNGTable rngTable;
	private Stack<Long> hashCodeHistory;

	public Board(Piece[][] board, Vector<Piece> aiPieces, Vector<Piece> playerPieces, Player player, Stack<Move> moveHistory,
			Stack<Long> hashCodeHistory, RNGTable rngTable) {

		this.board = board;
		this.aiPieces = aiPieces;
		this.userPieces = playerPieces;
		this.moveHistory = moveHistory;
		this.hashCodeHistory = hashCodeHistory;
		this.rngTable = rngTable;
		this.player = player;

		if (hashCodeHistory.empty()) {
			this.generateHashCode();
		}

	}

	public Board(Player player) {
		this.moveHistory = new Stack<Move>();
		this.hashCodeHistory = new Stack<Long>();
		this.rngTable = new RNGTable();
		this.player = player;
		buildBoard();
		this.generateHashCode();
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

	public void makeMove(Move move) {
		
		if(board[move.getFromRow()][move.getFromCol()].getPlayer()!=player){
			System.out.println("Problem with player ref");
		}
		
		makeMove(move, player);

		updateHashCode(move);

		moveHistory.push(move);
		player = getNextPlayer();
	}

	private void makeMove(Move move, Player player) {

		Piece pieceTaken = move.getPieceTaken();

		// remove taken piece first
		if (pieceTaken != null) {

			// piceTaken is old ref, find new ref
			if (pieceTaken != board[pieceTaken.getRow()][pieceTaken.getCol()]) {
				pieceTaken = board[move.getToRow()][move.getToCol()];
				move.setPieceTaken(pieceTaken);
			}

			// remove pieceTaken from vectors
			if (pieceTaken.getPlayer() == Player.USER) {
				if (!userPieces.remove(pieceTaken)) {
					System.out.println("Piece " + pieceTaken.toString() + " not found");
				}
			} else {
				if (!aiPieces.remove(pieceTaken)) {
					System.out.println("Piece " + pieceTaken.toString() + " not found");
				}
			}

			// remove ref to piecetaken on board
			board[pieceTaken.getRow()][pieceTaken.getCol()] = null;

		}

		if (move.getNote() != MoveNote.NEW_QUEEN) {

			// tell piece its new position
			board[move.getFromRow()][move.getFromCol()].move(move);
			// update board to reflect piece's new position
			board[move.getToRow()][move.getToCol()] = board[move.getFromRow()][move.getFromCol()];
			// remove pieces old position
			board[move.getFromRow()][move.getFromCol()] = null;

		} else {

			// remove pawn from vector
			if (player == Player.USER) {
				userPieces.remove(board[move.getFromRow()][move.getFromCol()]);
			} else {
				aiPieces.remove(board[move.getFromRow()][move.getFromCol()]);
			}

			// remove pawn from board
			board[move.getFromRow()][move.getFromCol()] = null;

			// put queen on board
			board[move.getToRow()][move.getToCol()] = new Queen(player, move.getToRow(), move.getToCol(), false);

			// add queen to vectors
			if (player == Player.USER) {
				userPieces.add(board[move.getToRow()][move.getToCol()]);
			} else {
				aiPieces.add(board[move.getToRow()][move.getToCol()]);
			}
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

	public void undoMove() {

		player = getNextPlayer();

		undoMove(this.getLastMoveMade(), this.player);

		moveHistory.pop();
		hashCodeHistory.pop();
	}

	private void undoMove(Move move, Player player) {

		if (move.getNote() != MoveNote.NEW_QUEEN) {

			// tell piece where it was
			board[move.getToRow()][move.getToCol()].move(move.reverse());
			// put piece in old position
			board[move.getFromRow()][move.getFromCol()] = board[move.getToRow()][move.getToCol()];
			// remove old position
			board[move.getToRow()][move.getToCol()] = null;

		} else {

			// remove queen from vectors
			if (player == Player.USER) {
				userPieces.remove(board[move.getToRow()][move.getToCol()]);
			} else {
				aiPieces.remove(board[move.getToRow()][move.getToCol()]);
			}

			// remove queen from board
			board[move.getToRow()][move.getToCol()] = null;

			// add old pawn to board or create new one

			board[move.getFromRow()][move.getFromCol()] = new Pawn(player, move.getFromRow(), move.getFromCol(), true);

			// add old pawn to vectors
			if (player == Player.USER) {
				userPieces.add(board[move.getFromRow()][move.getFromCol()]);
			} else {
				aiPieces.add(board[move.getFromRow()][move.getFromCol()]);
			}
		}

		// add taken piece back to vectors and board
		Piece pieceTaken = move.getPieceTaken();
		if (pieceTaken != null) {
			board[pieceTaken.getRow()][pieceTaken.getCol()] = pieceTaken;

			if (pieceTaken.getPlayer() == Player.USER) {
				userPieces.add(pieceTaken);
			} else {
				aiPieces.add(pieceTaken);
			}

		}

		if (move.getNote() == MoveNote.CASTLE_NEAR) {
			if (player == Player.AI) {
				undoMove(new Move(0, 7, 0, 5), player);
			} else {
				undoMove(new Move(7, 7, 7, 5), player);
			}
		}

		if (move.getNote() == MoveNote.CASTLE_FAR) {
			if (player == Player.AI) {
				undoMove(new Move(0, 0, 0, 3), player);
			} else {
				undoMove(new Move(7, 0, 7, 3), player);
			}
		}

		board[move.getFromRow()][move.getFromCol()].setMoved(move.hadMoved());

		// System.out.println("Undid " + move.toString());
		// System.out.println(this.toString());

	}

	public Vector<Move> generateValidMoves() {
		return generateValidMoves(this.player);
	}

	public Vector<Move> generateValidMoves(Player player) {
		Vector<Move> validMoves = new Vector<Move>(30);

		Vector<Piece> pieces = getPlayerPieces(player);
		Vector<Move> moves;
		Move move;
		for (int p = 0; p < pieces.size(); p++) {
			moves = pieces.elementAt(p).generateValidMoves(this);
			for (int m = 0; m < moves.size(); m++) {
				move = moves.elementAt(m);

				move.setHadMoved(hasMoved(move.getFromRow(), move.getFromCol()));
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

	public Move getLastMoveMade() {
		if (!moveHistory.empty()) {
			return moveHistory.peek();
		} else {
			return null;
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
			return null;
		}

	}

	public Player getPiecePlayer(int row, int col) {
		if (board[row][col] != null) {
			return board[row][col].getPlayer();
		} else {
			System.out.println("Error: requested player on null piece");
			return null;
		}

	}

	public Player getPlayer() {
		return player;
	}

	public Player getNextPlayer() {
		if (player == Player.AI) {
			return Player.USER;
		} else {
			return Player.AI;
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

	public boolean isInCheckNear() {
		return ((boardState & 1) != 0);
	}

	public boolean isInCheck() {
		return ((boardState & 2) != 0);
	}

	public boolean isInCheckFar() {
		return ((boardState & 4) != 0);
	}

	public boolean isLastMoveInvalid() {
		return ((boardState & 8) != 0);
	}

	public boolean isInCheckMate() {
		return ((boardState & 16) != 0);
	}

	public boolean isInStaleMate() {
		return ((boardState & 32) != 0);
	}

	public void setLastMoveInvalid() {
		boardState |= 8;
	}

	public void setIsInCheckMate() {
		boardState |= 16;
	}

	public void setIsInStaleMate() {
		boardState |= 32;
	}

	public void setInCheckDetails(int inCheck) {
		this.boardState |= inCheck;
	}

	public void clearBoardState() {
		boardState = 0;
	}

	public int getBoardState() {
		return boardState;
	}

	public void setBoardState(int boardState) {
		this.boardState = boardState;
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
		Stack<Move> moveHistory = new Stack<Move>();
		Stack<Long> hashCodeHistory = new Stack<Long>();

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

		Stack<Move> tempMoves = new Stack<Move>();

		while (!this.moveHistory.empty()) {
			tempMoves.push(this.moveHistory.pop());
		}

		Move m;
		while (!tempMoves.empty()) {
			m = tempMoves.pop();
			this.moveHistory.push(m);
			moveHistory.push(m);
		}

		Stack<Long> temp = new Stack<Long>();

		while (!this.hashCodeHistory.empty()) {
			temp.push(this.hashCodeHistory.pop());
		}

		Long l;
		while (!temp.empty()) {
			l = temp.pop();
			this.hashCodeHistory.push(l);
			hashCodeHistory.push(l);
		}

		return new Board(copyBoard, copyAiPieces, copyPlayerPieces, this.player, moveHistory, hashCodeHistory, this.rngTable);

	}

	public boolean verifyBoard() {
		int r;
		int c;

		boolean verified = true;

		for (int i = 0; i < aiPieces.size(); i++) {
			r = aiPieces.elementAt(i).getRow();
			c = aiPieces.elementAt(i).getCol();

			if (board[r][c] != aiPieces.elementAt(i)) {
				System.out.println("Piece " + aiPieces.elementAt(i).toString() + " in ai vector not on board");
				verified = false;
			}
		}

		for (int i = 0; i < userPieces.size(); i++) {
			r = userPieces.elementAt(i).getRow();
			c = userPieces.elementAt(i).getCol();

			if (board[r][c] != userPieces.elementAt(i)) {
				System.out.println("Piece " + userPieces.elementAt(i).toString() + " in user vector not on board");
				verified = false;
			}
		}

		for (int row = 0; row < 8; row++) {
			for (int col = 0; col < 8; col++) {
				if (board[row][col] != null) {

					if (board[row][col].getRow() != row && board[row][col].getCol() != col) {
						System.out.println("Board coord mismatch " + row + " " + col);
						verified = false;
					}

					if (!aiPieces.contains(board[row][col]) && !userPieces.contains(board[row][col])) {
						System.out.println(row + "," + col + " piece not found in vectors");
						verified = false;
					}

				} else {

				}
			}
		}

		return verified;
	}

	public String toString() {
		String stringBoard = "";

		for (int row = 0; row < 8; row++) {
			for (int col = 0; col < 8; col++) {
				if (board[row][col] != null) {
					stringBoard += board[row][col].toString() + ",";

				} else {
					stringBoard += "__,";
				}
			}
			stringBoard += "\n";
		}

		if (!verifyBoard()) {
			System.out.println("Problem with board");
		}

		return stringBoard;
	}

	public static Board fromString(String stringBoard, Player player) {
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

		return new Board(board, aiPieces, userPieces, player, new Stack<Move>(), new Stack<Long>(), new RNGTable());
	}

	public static Board fromFile(String fileName, Player player) {
		String stringBoard = ReadWriteTextFile.getContents(fileName);
		return fromString(stringBoard, player);
	}

	public long generateHashCode() {
		long hashCode = rngTable.getSideToMoveRandom(player);

		Piece p;
		for (int r = 0; r < 8; r++) {
			for (int c = 0; c < 8; c++) {
				p = board[r][c];
				if (p != null) {
					hashCode ^= rngTable.getPiecePerSquareRandom(p.getPlayer(), p.getPieceID(), r, c);
				}
			}
		}

		hashCode ^= rngTable.getCastlingRightsRandom(this.farRookHasMoved(Player.AI), this.nearRookHasMoved(Player.AI), this.kingHasMoved(Player.AI),
				this.farRookHasMoved(Player.USER), this.nearRookHasMoved(Player.USER), this.kingHasMoved(Player.USER));

		if (this.getLastMoveMade() != null) {
			if (this.getLastMoveMade().getNote() == MoveNote.PAWN_LEAP) {
				hashCode ^= rngTable.getEnPassantFile(this.getLastMoveMade().getToCol());
			}
		}

		hashCodeHistory.push(new Long(hashCode));

		return hashCode;
	}

	private void updateHashCode(Move move) {
		
		//remove old player
		//add new player
		
		//remove old piece per square
		//add new piece per square
		
		//if rook or king moved, update castling rules
		
		//if last move made is pawn leap, remove en passant file num
		//if new move is pawn leap, add en passant file num

	}

	public long getHashCode() {
		return this.hashCodeHistory.peek().longValue();
	}
}
