package chessBackend;

import java.util.ArrayList;
import java.util.Stack;
import java.util.Vector;

import chessIO.FileIO;
import chessIO.XMLParser;
import chessPieces.*;

public class Board {
	private Piece[][] board;
	private GameStatus boardStatus;
	private ArrayList<Piece> blackPieces;
	private Stack<Piece> blackPiecesTaken;
	private ArrayList<Piece> whitePieces;
	private Stack<Piece> whitePiecesTaken;
	private int castleRights;
	private Stack<Integer> castleRightsHistory;
	private Piece blackKing;
	private Piece whiteKing;
	private Side turn;
	private RNGTable rngTable;
	private Stack<Move> moveHistory;
	private long hashCode;
	private Stack<Long> hashCodeHistory;
	private long[] nullMoveInfo;
	private long[] posBitBoard;

	public static void main(String[] args) {
		BitBoard.loadMasks();
		Board board = Game.getDefaultBoard();

		ArrayList<Long> moves = board.generateValidMoves(true);

		long m = moves.get(0);

		int its = 1000000;

		long t1 = System.currentTimeMillis();
		for (int i = 0; i < its; i++) {
			board.makeMove(m);
			board.makeNullMove();
			moves = board.generateValidMoves(true);
			board.undoMove();
		}

		long A = System.currentTimeMillis() - t1;

		t1 = System.currentTimeMillis();
		for (int i = 0; i < its; i++) {
			board.makeMove(m);
			board.undoMove();
		}

		long B = System.currentTimeMillis() - t1;

		System.out.println("A= " + A + ", B= " + B + " A/B=" + (double) A / (double) B);
	}

	// public Board(Piece[][] board, Vector<Piece> blackPieces, Vector<Piece>
	// whitePieces, long[] posBitBoard, Piece blackKing, Piece whiteKing,
	// Side turn, Stack<Move> moveHistory, Long hashCode) {
	//
	// this.board = board;
	// this.blackPieces = blackPieces;
	// this.whitePieces = whitePieces;
	// this.blackPiecesTaken = new Stack<Piece>();
	// this.whitePiecesTaken = new Stack<Piece>();
	// this.blackKing = blackKing;
	// this.whiteKing = whiteKing;
	// this.posBitBoard = posBitBoard;
	// this.moveHistory = moveHistory;
	// this.hashCodeHistory = new Stack<Long>();
	// this.rngTable = RNGTable.getSingleton();
	// this.turn = turn;
	// this.nullMoveInfo = new long[3];
	//
	// if (hashCode != null) {
	// this.hashCode = hashCode;
	// } else {
	// this.hashCode = generateHashCode();
	// }
	//
	// }

	// private Board(ArrayList<Piece> blackPieces, ArrayList<Piece> whitePieces,
	// Side turn) {
	// this(blackPieces, whitePieces, turn, new Stack<Move>());
	// }

	public Board(ArrayList<Piece> blackPieces, ArrayList<Piece> whitePieces, Side turn, Stack<Move> moveHistory, Stack<Integer> castelingRightsHistory, int castlingRights) {
		this.board = new Piece[8][8];
		this.blackPieces = new ArrayList<Piece>(blackPieces.size());
		this.whitePieces = new ArrayList<Piece>(whitePieces.size());
		this.blackPiecesTaken = new Stack<Piece>();
		this.whitePiecesTaken = new Stack<Piece>();
		this.moveHistory = new Stack<Move>();
		this.castleRightsHistory = new Stack<Integer>();
		this.hashCodeHistory = new Stack<Long>();
		this.rngTable = RNGTable.getSingleton();
		this.turn = turn;
		this.nullMoveInfo = new long[3];

		long[] posBitBoard = { 0, 0 };

		Piece temp;
		for (int p = 0; p < blackPieces.size(); p++) {
			temp = blackPieces.get(p).getCopy();
			this.blackPieces.add(temp);
			board[temp.getRow()][temp.getCol()] = temp;
			posBitBoard[Side.BLACK.ordinal()] |= temp.getBit();

			if (temp.getPieceID() == PieceID.PAWN) {
				if (temp.getRow() != 1) {
					temp.setMoved(true);
				}
			}

			if (temp.getPieceID() == PieceID.KING) {
				blackKing = temp;
				if (blackKing.getRow() != 0 || blackKing.getCol() != 4) {
					blackKing.setMoved(true);
				}
			}
		}

		for (int p = 0; p < whitePieces.size(); p++) {
			temp = whitePieces.get(p).getCopy();
			this.whitePieces.add(temp);
			board[temp.getRow()][temp.getCol()] = temp;
			posBitBoard[Side.WHITE.ordinal()] |= temp.getBit();

			if (temp.getPieceID() == PieceID.PAWN) {
				if (temp.getRow() != 6) {
					temp.setMoved(true);
				}
			}

			if (temp.getPieceID() == PieceID.KING) {
				whiteKing = temp;
				if (whiteKing.getRow() != 7 || whiteKing.getCol() != 4) {
					whiteKing.setMoved(true);
				}
			}
		}

		this.posBitBoard = posBitBoard;

		this.hashCode = generateHashCode();

		if (moveHistory.size() > 0) {
			Long move;
			Side moveSide;
			if (moveHistory.size() % 2 == 0) {
				moveSide = turn;
			} else {
				moveSide = turn.otherSide();
			}

			for (int i = 0; i < moveHistory.size(); i++) {
				move = moveHistory.elementAt(i).getMoveLong();
				this.moveHistory.push(new Move(move));
				if (Move.hasPieceTaken(move)) {
					if (moveSide == Side.WHITE) {
						blackPiecesTaken.push(new Piece(Move.getPieceTakenID(move), Side.BLACK, Move.getPieceTakenRow(move), Move.getPieceTakenCol(move), Move.getPieceTakenHasMoved(move)));
					} else {
						whitePiecesTaken.push(new Piece(Move.getPieceTakenID(move), Side.WHITE, Move.getPieceTakenRow(move), Move.getPieceTakenCol(move), Move.getPieceTakenHasMoved(move)));
					}
				}

				moveSide = moveSide.otherSide();
			}

		} else {
			loadPiecesTaken();
		}

		for (int i = 0; i < castelingRightsHistory.size(); i++) {
			this.castleRightsHistory.push(castelingRightsHistory.elementAt(i).intValue());
		}

		this.castleRights = castleRights;
	}

	public boolean makeMove(long move) {

		int fromRow = Move.getFromRow(move);
		int fromCol = Move.getFromCol(move);
		int toRow = Move.getToRow(move);
		int toCol = Move.getToCol(move);
		MoveNote note = Move.getNote(move);

		if (board[fromRow][fromCol].getSide() != turn) {
			System.out.println("Problem with player ref");
			return false;
		}

		castleRightsHistory.push(castleRights);

		// save off hashCode
		hashCodeHistory.push(new Long(hashCode));

		// remove previous castle options
		hashCode ^= rngTable.getCastlingRightsRandom(this.farRookHasMoved(Side.BLACK), this.nearRookHasMoved(Side.BLACK), this.kingHasMoved(Side.BLACK), this.farRookHasMoved(Side.WHITE),
				this.nearRookHasMoved(Side.WHITE), this.kingHasMoved(Side.WHITE));

		// remove taken piece first
		if (Move.hasPieceTaken(move)) {

			Piece pieceTaken = board[Move.getPieceTakenRow(move)][Move.getPieceTakenCol(move)];

			// remove pieceTaken from vectors
			if (turn == Side.WHITE) {
				blackPieces.remove(pieceTaken);
				blackPiecesTaken.push(pieceTaken);
			} else {
				whitePieces.remove(pieceTaken);
				whitePiecesTaken.push(pieceTaken);
			}

			// remove bit position from appropriate side
			posBitBoard[pieceTaken.getSide().ordinal()] ^= pieceTaken.getBit();

			// remove ref to piecetaken on board
			board[pieceTaken.getRow()][pieceTaken.getCol()] = null;

			// remove old hash from piece that was taken, if any
			hashCode ^= rngTable.getPiecePerSquareRandom(pieceTaken.getSide(), pieceTaken.getPieceID(), pieceTaken.getRow(), pieceTaken.getCol());

		}

		movePiece(fromRow, fromCol, toRow, toCol, note);

		if (note == MoveNote.CASTLE_NEAR) {
			if (turn == Side.BLACK) {
				movePiece(0, 7, 0, 5, MoveNote.NONE);
			} else {
				movePiece(7, 7, 7, 5, MoveNote.NONE);
			}

			setCastleRights(turn, 0x4);
		}

		if (note == MoveNote.CASTLE_FAR) {
			if (turn == Side.BLACK) {
				movePiece(0, 0, 0, 3, MoveNote.NONE);
			} else {
				movePiece(7, 0, 7, 3, MoveNote.NONE);
			}

			setCastleRights(turn, 0x4);
		}

		// if last move made is pawn leap, remove en passant file num
		if (getLastMoveMade() != 0) {
			if (Move.getNote(getLastMoveMade()) == MoveNote.PAWN_LEAP) {
				hashCode ^= rngTable.getEnPassantFile(Move.getToCol(getLastMoveMade()));
			}
		}

		// if new move is pawn leap, add en passant file num
		if (note == MoveNote.PAWN_LEAP) {
			hashCode ^= rngTable.getEnPassantFile(Move.getToCol(move));
		}

		// add new castle options
		hashCode ^= rngTable.getCastlingRightsRandom(this.farRookHasMoved(Side.BLACK), this.nearRookHasMoved(Side.BLACK), this.kingHasMoved(Side.BLACK), this.farRookHasMoved(Side.WHITE),
				this.nearRookHasMoved(Side.WHITE), this.kingHasMoved(Side.WHITE));

		// either remove black and add white or reverse. Same operation.
		hashCode ^= rngTable.getBlackToMoveRandom();

		// show that this move is now the last move made
		moveHistory.push(new Move(move));

		// move was made, next player's turn
		turn = turn.otherSide();

		return true;

	}

	private void movePiece(int fromRow, int fromCol, int toRow, int toCol, MoveNote note) {
		Piece pieceMoving = board[fromRow][fromCol];

		// remove bit position from where piece was
		posBitBoard[pieceMoving.getSide().ordinal()] ^= pieceMoving.getBit();

		// remove old hash from where piece was
		hashCode ^= rngTable.getPiecePerSquareRandom(turn, pieceMoving.getPieceID(), fromRow, fromCol);

		// tell piece its new position
		pieceMoving.setPos(toRow, toCol);
		pieceMoving.setMoved(true);
		// update board to reflect piece's new position
		board[toRow][toCol] = pieceMoving;
		// remove pieces old position
		board[fromRow][fromCol] = null;

		// add bit position of where piece is now
		posBitBoard[pieceMoving.getSide().ordinal()] |= pieceMoving.getBit();

		if (note == MoveNote.NEW_QUEEN) {
			pieceMoving.setPieceID(PieceID.QUEEN);
		}

		if (pieceMoving == getKing(turn)) {
			setCastleRights(turn, 0);
		}

		// add hash of piece at new location
		hashCode ^= rngTable.getPiecePerSquareRandom(turn, pieceMoving.getPieceID(), toRow, toCol);
	}

	public long undoMove() {

		// if no there is no last move then undoMove is impossible
		if (moveHistory.empty()) {
			System.out.println("Can not undo move");
			return 0;
		}

		// retrieve last move made
		long lastMove = getLastMoveMade();

		int fromRow = Move.getFromRow(lastMove);
		int fromCol = Move.getFromCol(lastMove);
		int toRow = Move.getToRow(lastMove);
		int toCol = Move.getToCol(lastMove);
		MoveNote note = Move.getNote(lastMove);

		// last move made was made by previous player, which is also the next
		// player
		turn = turn.otherSide();

		undoMovePiece(fromRow, fromCol, toRow, toCol, note, Move.hadMoved(lastMove));

		if (Move.hasPieceTaken(lastMove)) {

			// add taken piece back to vectors and board
			Piece pieceTaken;

			if (turn == Side.WHITE) {
				pieceTaken = blackPiecesTaken.pop();
				blackPieces.add(pieceTaken);
			} else {
				pieceTaken = whitePiecesTaken.pop();
				whitePieces.add(pieceTaken);
			}

			// add piece taken to position bit board
			posBitBoard[pieceTaken.getSide().ordinal()] |= pieceTaken.getBit();

			board[pieceTaken.getRow()][pieceTaken.getCol()] = pieceTaken;

		}

		if (note == MoveNote.CASTLE_NEAR) {
			if (turn == Side.BLACK) {
				undoMovePiece(0, 7, 0, 5, MoveNote.NONE, false);
			} else {
				undoMovePiece(7, 7, 7, 5, MoveNote.NONE, false);
			}
		}

		if (note == MoveNote.CASTLE_FAR) {
			if (turn == Side.BLACK) {
				undoMovePiece(0, 0, 0, 3, MoveNote.NONE, false);
			} else {
				undoMovePiece(7, 0, 7, 3, MoveNote.NONE, false);
			}
		}

		// move was undone so show move made before that as the last move made
		moveHistory.pop();

		if (hashCodeHistory.empty()) {
			// if no hashCode was saved then generate it the hard way
			hashCode = generateHashCode();
		} else {
			// retrieve what the hashCode was before move was made
			hashCode = hashCodeHistory.pop();
		}

		return lastMove;

	}

	private void undoMovePiece(int fromRow, int fromCol, int toRow, int toCol, MoveNote note, boolean hadMoved) {

		Piece pieceMoving = board[toRow][toCol];

		// remove bit position from where piece was
		posBitBoard[pieceMoving.getSide().ordinal()] ^= pieceMoving.getBit();

		// tell piece where it was
		pieceMoving.setPos(fromRow, fromCol);

		// put piece in old position
		board[fromRow][fromCol] = pieceMoving;
		// remove old position
		board[toRow][toCol] = null;

		// show whether piece had moved before this move was made
		board[fromRow][fromCol].setMoved(hadMoved);

		// add bit position of where piece is now
		posBitBoard[pieceMoving.getSide().ordinal()] |= pieceMoving.getBit();

		if (note == MoveNote.NEW_QUEEN) {
			pieceMoving.setPieceID(PieceID.PAWN);
		}

	}

	public boolean canUndo() {
		return (moveHistory.size() != 0);
	}

	public boolean checkPosBitBoard() {

		long[] pos = new long[2];

		pos[0] = 0;
		pos[1] = 0;

		for (int r = 0; r < 8; r++) {
			for (int c = 0; c < 8; c++) {
				if (board[r][c] != null) {
					pos[board[r][c].getSide().ordinal()] |= BitBoard.getMask(r, c);
				}
			}
		}

		boolean match = true;

		if (pos[Side.WHITE.ordinal()] != posBitBoard[Side.WHITE.ordinal()]) {
			match = false;
			FileIO.log("White error");
		}

		if (pos[Side.BLACK.ordinal()] != posBitBoard[Side.BLACK.ordinal()]) {
			match = false;
			FileIO.log("Black error");
		}

		return match;
	}

	public ArrayList<Long> generateValidMoves(boolean sort) {

		// find in check details. i.e. left and right castle info
		// makeNullMove();

		// System.out.println("Not safe areas");
		// BitBoard.printBitBoard(nullMoveInfo[0]);
		//
		// System.out.println("in check vector");
		// BitBoard.printBitBoard(nullMoveInfo[1]);

		ArrayList<Long> validMoves = new ArrayList<Long>(30);

		ArrayList<Piece> pieces = getPiecesFor(turn);
		ArrayList<Long> moves;
		Long move;
		for (int p = 0; p < pieces.size(); p++) {

			moves = pieces.get(p).generateValidMoves(this, nullMoveInfo, posBitBoard);

			for (int m = 0; m < moves.size(); m++) {
				move = moves.get(m);

				if (Move.getPieceTakenID(move) == PieceID.KING) {

					FileIO.writeFile("bug522.xml", this.toXML(true), false);
					System.out.println("WTF");
				}

				move = Move.setHadMoved(move, hasMoved(Move.getFromRow(move), Move.getFromCol(move)));

				if (sort) {
					addSortValidMove(validMoves, move);
				} else {
					validMoves.add(move);
				}
			}

		}

		if (validMoves.size() == 0) {
			if (isInCheck()) {
				setBoardStatus(GameStatus.CHECKMATE);
			} else {
				setBoardStatus(GameStatus.STALEMATE);
			}
		}

		if (!hasSufficientMaterial() || drawByThreeRule()) {
			setBoardStatus(GameStatus.DRAW);
		}

		return validMoves;
	}

	private void addSortValidMove(ArrayList<Long> validMoves, Long move) {
		int moveValue = Move.getValue(move);
		for (int m = 0; m < validMoves.size(); m++) {
			if (moveValue >= Move.getValue(validMoves.get(m))) {
				validMoves.add(m, move);
				return;
			}
		}

		validMoves.add(move);
	}

	public void makeNullMove() {
		long nullMoveAttacks = 0;
		long inCheckVector = BitBoard.ALL_ONES;
		long bitAttackCompliment = 0;

		nullMoveInfo[0] = nullMoveAttacks;
		nullMoveInfo[1] = inCheckVector;
		nullMoveInfo[2] = bitAttackCompliment;

		// recalculating check info
		clearBoardStatus();

		ArrayList<Piece> pieces = getPiecesFor(turn);
		for (int p = 0; p < pieces.size(); p++) {
			pieces.get(p).clearBlocking();
		}

		pieces = getPiecesFor(turn.otherSide());
		for (int p = 0; p < pieces.size(); p++) {
			pieces.get(p).getNullMoveInfo(this, nullMoveInfo);
		}

		if ((getKing(turn).getBit() & nullMoveInfo[0]) != 0) {
			setBoardStatus(GameStatus.CHECK);
		}

	}

	public boolean isVoi(Move[] voi) {

		int historySize = moveHistory.size();

		if (historySize < voi.length) {
			return false;
		}

		for (int i = 0; i < voi.length; i++) {
			if (!voi[i].equals(moveHistory.elementAt(historySize - i - 1))) {
				return false;
			}
		}

		return true;
	}

	public PositionStatus checkPiece(int row, int col, Side player) {

		if (row > 7 || row < 0 || col > 7 || col < 0)
			return PositionStatus.OFF_BOARD;

		if (board[row][col] != null) {
			if (board[row][col].getSide() == player)
				return PositionStatus.FRIEND;
			else
				return PositionStatus.ENEMY;
		} else {
			return PositionStatus.NO_PIECE;
		}

	}

	public long getLastMoveMade() {
		if (!moveHistory.empty()) {
			return moveHistory.peek().getMoveLong();
		} else {
			return 0;
		}
	}

	public Vector<Move> getMoveHistory() {
		return moveHistory;
	}

	public int getPieceValue(int row, int col) {
		return getPieceValue(board[row][col]);
	}

	public int getPieceValue(Piece piece) {
		int value;
		Side player = piece.getSide();
		int row = piece.getRow();
		int col = piece.getCol();

		switch (piece.getPieceID()) {
		case KNIGHT:
			int piecesMissing = 32 - (blackPieces.size() + whitePieces.size());
			value = Values.KNIGHT_VALUE - piecesMissing * Values.KNIGHT_ENDGAME_INC + PositionBonus.getKnightPositionBonus(row, col, player);
			break;
		case PAWN:
			value = Values.PAWN_VALUE + PositionBonus.getPawnPositionBonus(row, col, player);
			break;
		case BISHOP:
			value = Values.BISHOP_VALUE;
			break;
		case KING:
			value = Values.KING_VALUE + PositionBonus.getKingPositionBonus(row, col, player);
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

	public int staticScore() {
		int ptDiff = 0;

		ArrayList<Piece> myPieces = getPiecesFor(turn);
		ArrayList<Piece> otherPieces = getPiecesFor(turn.otherSide());

		for (int i = 0; i < myPieces.size(); i++) {
			ptDiff += getPieceValue(myPieces.get(i));
		}

		for (int i = 0; i < otherPieces.size(); i++) {
			ptDiff -= getPieceValue(otherPieces.get(i));
		}

		return ptDiff;
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

	public Side getPieceSide(int row, int col) {
		if (board[row][col] != null) {
			return board[row][col].getSide();
		} else {
			System.out.println("Error: requested player on null piece");
			return null;
		}

	}

	public Side getTurn() {
		return turn;
	}

	public void setTurn(Side turn) {
		this.turn = turn;
	}

	private Piece getKing(Side side) {
		if (side == Side.BLACK) {
			return blackKing;
		} else {
			return whiteKing;
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

	public ArrayList<Piece> getPiecesFor(Side player) {
		if (player == Side.WHITE) {
			return whitePieces;
		} else {
			return blackPieces;
		}
	}

	public Stack<Piece> getPiecesTakenFor(Side player) {
		if (player == Side.WHITE) {
			return whitePiecesTaken;
		} else {
			return blackPiecesTaken;
		}
	}

	public void placePiece(Piece piece, int toRow, int toCol) {

		if (piece.getRow() >= 0) {
			// remove where piece was if it was on board
			posBitBoard[piece.getSide().ordinal()] ^= piece.getBit();
			board[piece.getRow()][piece.getCol()] = null;
		} else {

			// piece is new
			if (piece.getSide() == Side.WHITE) {
				whitePieces.add(piece);
			} else {
				blackPieces.add(piece);
			}
		}

		if (toRow >= 0) {
			// remove where piece taken was
			if (board[toRow][toCol] != null) {

				// remove bit position of piece taken
				posBitBoard[board[toRow][toCol].getSide().ordinal()] ^= board[toRow][toCol].getBit();

				// remove ref to piece taken
				if (board[toRow][toCol].getSide() == Side.WHITE) {
					whitePieces.remove(board[toRow][toCol]);
				} else {
					blackPieces.remove(board[toRow][toCol]);
				}
			}

			// tell piece where it is now
			piece.setPos(toRow, toCol);
			// reflect new piece in position bitboard
			posBitBoard[piece.getSide().ordinal()] |= piece.getBit();
			// update board ref to show piece there
			board[toRow][toCol] = piece;
		} else {
			// piece is being taken off the board. Remove
			if (piece.getSide() == Side.WHITE) {
				whitePieces.remove(piece);
			} else {
				blackPieces.remove(piece);
			}
		}

		// basically start over with new board
		this.moveHistory.clear();
		this.hashCodeHistory.clear();

		this.hashCode = generateHashCode();

	}

	public boolean isInCheck() {
		return (boardStatus == GameStatus.CHECK);
	}

	public boolean isInCheckMate() {
		return (boardStatus == GameStatus.CHECKMATE);
	}

	public boolean isInStaleMate() {
		return (boardStatus == GameStatus.STALEMATE);
	}

	public boolean isTimeUp() {
		return (boardStatus == GameStatus.TIMES_UP);
	}

	public boolean isDraw() {
		return (boardStatus == GameStatus.DRAW);
	}

	public boolean isGameOver() {
		return (isInCheckMate() || isInStaleMate() || isTimeUp() || isDraw());
	}

	public void clearBoardStatus() {
		boardStatus = GameStatus.IN_PLAY;
	}

	public GameStatus getBoardStatus() {
		return boardStatus;
	}

	public void setBoardStatus(GameStatus boardStatus) {
		this.boardStatus = boardStatus;
	}

	public int calculateCastleRights(Side side) {
		int rights = 0;

		if (!kingHasMoved(side)) {
			if (farRookHasMoved(side)) {
				rights |= 0x2;
			}

			if (nearRookHasMoved(side)) {
				rights |= 0x1;
			}
		}

		return rights;
	}

	public void setCastleRights(Side side, int rights) {
		if (side == Side.BLACK) {
			castleRights &= 0x7;
			castleRights |= (rights << 3);
		} else {
			castleRights &= (0x7 << 3);
			castleRights |= rights;
		}
	}

	public boolean farRookHasMoved(Side player) {
		if (player == Side.BLACK) {
			return hasMoved(0, 0);
		} else {
			return hasMoved(7, 0);
		}
	}

	public boolean nearRookHasMoved(Side player) {
		if (player == Side.BLACK) {
			return hasMoved(0, 7);
		} else {
			return hasMoved(7, 7);
		}
	}

	public boolean kingHasMoved(Side player) {
		if (player == Side.BLACK) {
			return blackKing.hasMoved();
		} else {
			return whiteKing.hasMoved();
		}
	}

	public Board getCopy() {
		return new Board(blackPieces, whitePieces, this.turn, moveHistory, castleRightsHistory, castleRights);
	}

	public String toString() {
		String stringBoard = "";
		int pieceDetails = 0;
		Piece p;

		for (int row = 0; row < 8; row++) {
			for (int col = 0; col < 8; col++) {
				if (board[row][col] != null) {

					p = board[row][col];

					if (p.hasMoved() && (p.getPieceID() == PieceID.PAWN || p.getPieceID() == PieceID.KING || p.getPieceID() == PieceID.ROOK)) {
						pieceDetails |= 1;
					}

					if (p.getPieceID() == PieceID.ROOK && kingHasMoved(p.getSide())) {
						pieceDetails |= 1;
					}

					if (p.getPieceID() == PieceID.KING && nearRookHasMoved(p.getSide()) && farRookHasMoved(p.getSide())) {
						pieceDetails |= 1;
					}

					long lastMove = getLastMoveMade();

					if (getLastMoveMade() != 0) {
						if (Move.getToRow(lastMove) == row && Move.getToCol(lastMove) == col && Move.getNote(lastMove) == MoveNote.PAWN_LEAP) {
							pieceDetails |= 2;
						}
					}

					stringBoard += board[row][col].toString() + pieceDetails + ",";
					pieceDetails = 0;

				} else {
					stringBoard += "__,";
				}

			}
			stringBoard += "\n";
		}

		return stringBoard;
	}

	public String toXML(boolean includeHistory) {
		String xmlBoard = "<board>\n";

		xmlBoard += "<setup>\n" + this.toString() + "</setup>\n";
		xmlBoard += "<turn>" + turn.toString() + "</turn>\n";

		if (includeHistory) {
			for (int i = 0; i < moveHistory.size(); i++) {
				xmlBoard += Move.toXML(moveHistory.elementAt(i).getMoveLong());
			}
		}

		xmlBoard += "</board>";
		return xmlBoard;
	}

	public long generateHashCode() {
		long hashCode = 0;

		if (turn == Side.BLACK) {
			hashCode = rngTable.getBlackToMoveRandom();
		}

		Piece p;
		for (int r = 0; r < 8; r++) {
			for (int c = 0; c < 8; c++) {
				p = board[r][c];
				if (p != null) {
					hashCode ^= rngTable.getPiecePerSquareRandom(p.getSide(), p.getPieceID(), r, c);
				}
			}
		}

		hashCode ^= rngTable.getCastlingRightsRandom(this.farRookHasMoved(Side.BLACK), this.nearRookHasMoved(Side.BLACK), this.kingHasMoved(Side.BLACK), this.farRookHasMoved(Side.WHITE),
				this.nearRookHasMoved(Side.WHITE), this.kingHasMoved(Side.WHITE));

		if (getLastMoveMade() != 0) {
			if (Move.getNote(getLastMoveMade()) == MoveNote.PAWN_LEAP) {
				hashCode ^= rngTable.getEnPassantFile(Move.getToCol(getLastMoveMade()));
			}
		}

		return hashCode;
	}

	public int getHashIndex() {
		return (int) (hashCode & BoardHashEntry.hashIndexMask);
	}

	public long getHashCode() {
		return hashCode;
	}

	public static Long getHashCode(String xmlBoard) {
		Board board = XMLParser.XMLToBoard(xmlBoard);

		if (board != null) {
			return board.getHashCode();
		} else {
			return null;
		}
	}

	private boolean drawByThreeRule() {

		int count = 0;

		if (hashCodeHistory.size() > 600) {
			return true;
		}

		for (int i = hashCodeHistory.size() - 1; i >= 0; i--) {
			if (hashCode == hashCodeHistory.elementAt(i)) {
				count++;
			}

			if (count > 2) {
				return true;
			}
		}

		return false;
	}

	private boolean hasSufficientMaterial() {

		boolean sufficient = true;

		if (whitePieces.size() <= 2 && blackPieces.size() <= 2) {

			sufficient = false;

			for (int i = 0; i < whitePieces.size(); i++) {
				if ((whitePieces.get(i).getPieceID() == PieceID.PAWN) || (whitePieces.get(i).getPieceID() == PieceID.QUEEN) || (whitePieces.get(i).getPieceID() == PieceID.ROOK)) {
					sufficient = true;
				}
			}

			for (int i = 0; i < blackPieces.size(); i++) {
				if ((blackPieces.get(i).getPieceID() == PieceID.PAWN) || (blackPieces.get(i).getPieceID() == PieceID.QUEEN) || (blackPieces.get(i).getPieceID() == PieceID.ROOK)) {
					sufficient = true;
				}
			}

		}

		return sufficient;
	}

	private void loadPiecesTaken() {

		whitePiecesTaken = getFullPieceSet(Side.WHITE);

		Piece piecePresent;
		for (int p = 0; p < whitePieces.size(); p++) {
			piecePresent = whitePieces.get(p);

			for (int t = 0; t < whitePiecesTaken.size(); t++) {
				if (whitePiecesTaken.elementAt(t).getPieceID() == piecePresent.getPieceID()) {
					whitePiecesTaken.remove(t);
					break;
				}
			}
		}

		blackPiecesTaken = getFullPieceSet(Side.BLACK);

		for (int p = 0; p < blackPieces.size(); p++) {
			piecePresent = blackPieces.get(p);

			for (int t = 0; t < blackPiecesTaken.size(); t++) {
				if (blackPiecesTaken.elementAt(t).getPieceID() == piecePresent.getPieceID()) {
					blackPiecesTaken.remove(t);
					break;
				}
			}
		}
	}

	public static Stack<Piece> getFullPieceSet(Side player) {
		Stack<Piece> pieces = new Stack<Piece>();

		for (int i = 0; i < 8; i++) {
			pieces.add(new Piece(PieceID.PAWN, player, 0, 0, false));
		}

		for (int i = 0; i < 2; i++) {
			pieces.add(new Piece(PieceID.BISHOP, player, 0, 0, false));
		}

		for (int i = 0; i < 2; i++) {
			pieces.add(new Piece(PieceID.ROOK, player, 0, 0, false));
		}

		for (int i = 0; i < 2; i++) {
			pieces.add(new Piece(PieceID.KNIGHT, player, 0, 0, false));
		}

		pieces.add(new Piece(PieceID.KING, player, 0, 0, false));
		pieces.add(new Piece(PieceID.QUEEN, player, 0, 0, false));

		return pieces;
	}
}
