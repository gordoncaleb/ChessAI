package chessBackend;

import java.util.Stack;
import java.util.Vector;

import chessPieces.*;

public class Board {
	private Piece[][] board;
	private GameStatus boardStatus;
	private Vector<Piece> blackPieces;
	private Vector<Piece> whitePieces;
	private Piece blackKing;
	private Piece whiteKing;
	private Side turn;
	private RNGTable rngTable;
	private Stack<Move> moveHistory;
	private long hashCode;
	private Stack<Long> hashCodeHistory;
	private long[] nullMoveInfo;
	private long[] posBitBoard;

	public Board(Piece[][] board, Vector<Piece> blackPieces,
			Vector<Piece> whitePieces, long[] posBitBoard, Piece blackKing,
			Piece whiteKing, Side turn, Stack<Move> moveHistory, Long hashCode,
			RNGTable rngTable) {

		this.board = board;
		this.blackPieces = blackPieces;
		this.whitePieces = whitePieces;
		this.blackKing = blackKing;
		this.whiteKing = whiteKing;
		this.posBitBoard = posBitBoard;
		this.moveHistory = moveHistory;
		this.hashCodeHistory = new Stack<Long>();
		this.rngTable = rngTable;
		this.turn = turn;
		this.nullMoveInfo = new long[3];

		if (hashCode != null) {
			this.hashCode = hashCode;
		} else {
			this.hashCode = generateHashCode();
		}

	}

	public boolean makeMove(Move move) {

		if (board[move.getFromRow()][move.getFromCol()].getSide() != turn) {
			System.out.println("Problem with player ref");
			return false;
		}

		// save off hashCode
		hashCodeHistory.push(new Long(hashCode));

		// remove previous castle options
		hashCode ^= rngTable.getCastlingRightsRandom(
				this.farRookHasMoved(Side.BLACK),
				this.nearRookHasMoved(Side.BLACK),
				this.kingHasMoved(Side.BLACK),
				this.farRookHasMoved(Side.WHITE),
				this.nearRookHasMoved(Side.WHITE),
				this.kingHasMoved(Side.WHITE));

		Piece pieceTaken = move.getPieceTaken();

		// remove taken piece first
		if (pieceTaken != null) {

			// remove bit position from appropriate side
			posBitBoard[pieceTaken.getSide().ordinal()] ^= pieceTaken.getBit();

			// piceTaken is old ref, find new ref
			if (pieceTaken != board[pieceTaken.getRow()][pieceTaken.getCol()]) {

				if (board[move.getToRow()][move.getToCol()] == null) {
					System.out.println("What?");
				}

				pieceTaken = board[move.getToRow()][move.getToCol()];
				move.setPieceTaken(pieceTaken);
			}

			// remove pieceTaken from vectors
			if (pieceTaken.getSide() == Side.WHITE) {
				whitePieces.remove(pieceTaken);
			} else {
				blackPieces.remove(pieceTaken);

			}

			// remove ref to piecetaken on board
			board[pieceTaken.getRow()][pieceTaken.getCol()] = null;

			// remove old hash from piece that was taken, if any
			hashCode ^= rngTable.getPiecePerSquareRandom(pieceTaken.getSide(),
					pieceTaken.getPieceID(), pieceTaken.getRow(),
					pieceTaken.getCol());

		}

		if (move.getNote() != MoveNote.NEW_QUEEN) {

			movePiece(move);

		} else {

			// remove old hash from where pawn was
			hashCode ^= rngTable.getPiecePerSquareRandom(turn, PieceID.PAWN,
					move.getFromRow(), move.getFromCol());

			// remove pawn from vector
			if (turn == Side.WHITE) {
				whitePieces.remove(board[move.getFromRow()][move.getFromCol()]);
			} else {
				blackPieces.remove(board[move.getFromRow()][move.getFromCol()]);
			}

			// remove pawn from board
			board[move.getFromRow()][move.getFromCol()] = null;

			// put queen on board
			board[move.getToRow()][move.getToCol()] = new Queen(turn,
					move.getToRow(), move.getToCol(), false);

			// add hash of piece at new location. Probably a queen.
			hashCode ^= rngTable.getPiecePerSquareRandom(turn,
					board[move.getToRow()][move.getToCol()].getPieceID(),
					move.getToRow(), move.getToCol());

			// add queen to vectors
			if (turn == Side.WHITE) {
				whitePieces.add(board[move.getToRow()][move.getToCol()]);
			} else {
				blackPieces.add(board[move.getToRow()][move.getToCol()]);
			}
		}

		if (move.getNote() == MoveNote.CASTLE_NEAR) {
			if (turn == Side.BLACK) {
				movePiece(new Move(0, 7, 0, 5));
			} else {
				movePiece(new Move(7, 7, 7, 5));
			}
		}

		if (move.getNote() == MoveNote.CASTLE_FAR) {
			if (turn == Side.BLACK) {
				movePiece(new Move(0, 0, 0, 3));
			} else {
				movePiece(new Move(7, 0, 7, 3));
			}
		}

		// if last move made is pawn leap, remove en passant file num
		if (getLastMoveMade() != null) {
			if (getLastMoveMade().getNote() == MoveNote.PAWN_LEAP) {
				hashCode ^= rngTable.getEnPassantFile(getLastMoveMade()
						.getToCol());
			}
		}

		// if new move is pawn leap, add en passant file num
		if (move.getNote() == MoveNote.PAWN_LEAP) {
			hashCode ^= rngTable.getEnPassantFile(move.getToCol());
		}

		// add new castle options
		hashCode ^= rngTable.getCastlingRightsRandom(
				this.farRookHasMoved(Side.BLACK),
				this.nearRookHasMoved(Side.BLACK),
				this.kingHasMoved(Side.BLACK),
				this.farRookHasMoved(Side.WHITE),
				this.nearRookHasMoved(Side.WHITE),
				this.kingHasMoved(Side.WHITE));

		// either remove black and add white or reverse. Same operation.
		hashCode ^= rngTable.getBlackToMoveRandom();

		// show that this move is now the last move made
		moveHistory.push(move);

		// move was made, next player's turn
		turn = turn.otherSide();

		return true;

	}

	private void movePiece(Move move) {
		Piece pieceMoving = board[move.getFromRow()][move.getFromCol()];

		// remove bit position from where piece was
		posBitBoard[pieceMoving.getSide().ordinal()] ^= pieceMoving.getBit();

		// remove old hash from where piece was
		hashCode ^= rngTable.getPiecePerSquareRandom(turn,
				pieceMoving.getPieceID(), move.getFromRow(), move.getFromCol());

		// tell piece its new position
		pieceMoving.move(move);
		// update board to reflect piece's new position
		board[move.getToRow()][move.getToCol()] = pieceMoving;
		// remove pieces old position
		board[move.getFromRow()][move.getFromCol()] = null;

		// add bit position of where piece is now
		posBitBoard[pieceMoving.getSide().ordinal()] |= pieceMoving.getBit();

		// add hash of piece at new location
		hashCode ^= rngTable.getPiecePerSquareRandom(turn,
				pieceMoving.getPieceID(), move.getToRow(), move.getToCol());
	}

	public Move undoMove() {

		// if no there is no last move then undoMove is impossible
		if (moveHistory.empty()) {
			System.out.println("Can not undo move");
			return null;
		}

		// retrieve last move made
		Move lastMove = getLastMoveMade();

		// last move made was made by previous player, which is also the next
		// player
		turn = turn.otherSide();

		if (lastMove.getNote() != MoveNote.NEW_QUEEN) {

			undoMovePiece(lastMove);

		} else {

			// remove queen from vectors
			if (turn == Side.WHITE) {
				whitePieces.remove(board[lastMove.getToRow()][lastMove
						.getToCol()]);
			} else {
				blackPieces.remove(board[lastMove.getToRow()][lastMove
						.getToCol()]);
			}

			// remove queen from board
			board[lastMove.getToRow()][lastMove.getToCol()] = null;

			// add pawn back to board
			board[lastMove.getFromRow()][lastMove.getFromCol()] = new Pawn(
					turn, lastMove.getFromRow(), lastMove.getFromCol(), true);

			// add pawn to vectors
			if (turn == Side.WHITE) {
				whitePieces.add(board[lastMove.getFromRow()][lastMove
						.getFromCol()]);
			} else {
				blackPieces.add(board[lastMove.getFromRow()][lastMove
						.getFromCol()]);
			}
		}

		// add taken piece back to vectors and board
		Piece pieceTaken = lastMove.getPieceTaken();
		if (pieceTaken != null) {

			// add piece taken to position bit board
			posBitBoard[pieceTaken.getSide().ordinal()] |= pieceTaken.getBit();

			board[pieceTaken.getRow()][pieceTaken.getCol()] = pieceTaken;

			if (pieceTaken.getSide() == Side.WHITE) {
				whitePieces.add(pieceTaken);
			} else {
				blackPieces.add(pieceTaken);
			}

		}

		if (lastMove.getNote() == MoveNote.CASTLE_NEAR) {
			if (turn == Side.BLACK) {
				undoMovePiece(new Move(0, 7, 0, 5));
			} else {
				undoMovePiece(new Move(7, 7, 7, 5));
			}
		}

		if (lastMove.getNote() == MoveNote.CASTLE_FAR) {
			if (turn == Side.BLACK) {
				undoMovePiece(new Move(0, 0, 0, 3));
			} else {
				undoMovePiece(new Move(7, 0, 7, 3));
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

	private void undoMovePiece(Move move) {

		Piece pieceMoving = board[move.getToRow()][move.getToCol()];

		// remove bit position from where piece was
		posBitBoard[pieceMoving.getSide().ordinal()] ^= pieceMoving.getBit();

		// tell piece where it was
		pieceMoving.move(move.reverse());
		// put piece in old position
		board[move.getFromRow()][move.getFromCol()] = pieceMoving;
		// remove old position
		board[move.getToRow()][move.getToCol()] = null;

		// show whether piece had moved before this move was made
		board[move.getFromRow()][move.getFromCol()].setMoved(move.hadMoved());

		// add bit position of where piece is now
		posBitBoard[pieceMoving.getSide().ordinal()] |= pieceMoving.getBit();

	}

	public Vector<Move> generateValidMoves() {

		// recalculating check info
		clearBoardStatus();

		// find in check details. i.e. left and right castle info
		makeNullMove();

		// System.out.println("Not safe areas");
		// BitBoard.printBitBoard(nullMoveInfo[0]);
		//
		// System.out.println("in check vector");
		// BitBoard.printBitBoard(nullMoveInfo[1]);

		Vector<Move> validMoves = new Vector<Move>(30);

		Vector<Piece> pieces = getPiecesFor(turn);
		Piece piece;
		Vector<Move> moves;
		Move move;
		for (int p = 0; p < pieces.size(); p++) {
			piece = pieces.elementAt(p);

			moves = pieces.elementAt(p).generateValidMoves(this, nullMoveInfo,
					posBitBoard);
			for (int m = 0; m < moves.size(); m++) {
				move = moves.elementAt(m);

				move.setHadMoved(hasMoved(move.getFromRow(), move.getFromCol()));
				addSortValidMove(validMoves, moves.elementAt(m));
			}

			piece.clearBlocking();

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

	public void makeNullMove() {
		long nullMoveAttacks = 0;
		long inCheckVector = BitBoard.ALL_ONES;
		long bitAttackCompliment = 0;

		nullMoveInfo[0] = nullMoveAttacks;
		nullMoveInfo[1] = inCheckVector;
		nullMoveInfo[2] = bitAttackCompliment;

		Vector<Piece> pieces = getPiecesFor(turn.otherSide());
		for (int p = 0; p < pieces.size(); p++) {
			pieces.elementAt(p).getNullMoveInfo(this, nullMoveInfo);
		}

		if ((getMovingSidesKing().getBit() & nullMoveInfo[0]) != 0) {
			setBoardStatus(GameStatus.CHECK);
		}

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

	public Move getLastMoveMade() {
		if (!moveHistory.empty()) {
			return moveHistory.peek();
		} else {
			return null;
		}
	}

	public Vector<Move> getMoveHistory() {
		return moveHistory;
	}

	public int getPieceValue(int row, int col) {
		int value;
		Side player = board[row][col].getSide();

		switch (board[row][col].getPieceID()) {
		case KNIGHT:
			int piecesMissing = 32 - (blackPieces.size() + whitePieces.size());
			value = Values.KNIGHT_VALUE - piecesMissing
					* Values.KNIGHT_ENDGAME_INC;
			break;
		case PAWN:
			value = Values.PAWN_VALUE
					+ PositionBonus.getPawnPositionBonus(row, col, player);
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

	public Piece getMovingSidesKing() {
		if (turn == Side.BLACK) {
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

	public Vector<Piece> getPiecesFor(Side player) {
		if (player == Side.WHITE) {
			return whitePieces;
		} else {
			return blackPieces;
		}
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
			return hasMoved(0, 4);
		} else {
			return hasMoved(7, 4);
		}
	}

	public Board getCopy() {
		Piece[][] copyBoard = new Piece[8][8];
		Vector<Piece> copyBlackPieces = new Vector<Piece>(16);
		Vector<Piece> copyWhitePieces = new Vector<Piece>(16);
		Piece blackKing = null;
		Piece whiteKing = null;
		long[] posBitBoard = this.posBitBoard.clone();

		for (int row = 0; row < 8; row++) {
			for (int col = 0; col < 8; col++) {

				if (board[row][col] != null) {

					copyBoard[row][col] = board[row][col].getCopy();

					if (copyBoard[row][col].getSide() == Side.BLACK) {
						copyBlackPieces.add(copyBoard[row][col]);

						if (copyBoard[row][col].getPieceID() == PieceID.KING)
							blackKing = copyBoard[row][col];

					} else {
						copyWhitePieces.add(copyBoard[row][col]);

						if (copyBoard[row][col].getPieceID() == PieceID.KING)
							whiteKing = copyBoard[row][col];
					}
				}
			}
		}

		return new Board(copyBoard, copyBlackPieces, copyWhitePieces,
				posBitBoard, blackKing, whiteKing, this.turn,
				new Stack<Move>(), this.hashCode, this.rngTable);

	}

	public String toString() {
		String stringBoard = "";
		int pieceDetails = 0;
		Piece p;

		for (int row = 0; row < 8; row++) {
			for (int col = 0; col < 8; col++) {
				if (board[row][col] != null) {

					p = board[row][col];

					if (p.hasMoved()
							&& (p.getPieceID() == PieceID.PAWN
									|| p.getPieceID() == PieceID.KING || p
									.getPieceID() == PieceID.ROOK)) {
						pieceDetails |= 1;
					}

					if (p.getPieceID() == PieceID.ROOK
							&& kingHasMoved(p.getSide())) {
						pieceDetails |= 1;
					}

					if (p.getPieceID() == PieceID.KING
							&& nearRookHasMoved(p.getSide())
							&& farRookHasMoved(p.getSide())) {
						pieceDetails |= 1;
					}

					if (getLastMoveMade() != null) {
						if (getLastMoveMade().getToRow() == row
								&& getLastMoveMade().getToCol() == col
								&& getLastMoveMade().getNote() == MoveNote.PAWN_LEAP) {
							pieceDetails |= 2;
						}
					}

					stringBoard += board[row][col].toString() + pieceDetails
							+ ",";
					pieceDetails = 0;

				} else {
					stringBoard += "__,";
				}

			}
			stringBoard += "\n";
		}

		return stringBoard;
	}

	public String toXML() {
		String xmlBoard = "<board>\n";

		xmlBoard += "<setup>\n" + this.toString() + "</setup>\n";
		xmlBoard += "<turn>" + turn.toString() + "</turn>\n";

		for (int i = 0; i < moveHistory.size(); i++) {
			xmlBoard += moveHistory.elementAt(i).toXML();
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
					hashCode ^= rngTable.getPiecePerSquareRandom(p.getSide(),
							p.getPieceID(), r, c);
				}
			}
		}

		hashCode ^= rngTable.getCastlingRightsRandom(
				this.farRookHasMoved(Side.BLACK),
				this.nearRookHasMoved(Side.BLACK),
				this.kingHasMoved(Side.BLACK),
				this.farRookHasMoved(Side.WHITE),
				this.nearRookHasMoved(Side.WHITE),
				this.kingHasMoved(Side.WHITE));

		if (this.getLastMoveMade() != null) {
			if (this.getLastMoveMade().getNote() == MoveNote.PAWN_LEAP) {
				hashCode ^= rngTable.getEnPassantFile(this.getLastMoveMade()
						.getToCol());
			}
		}

		return hashCode;
	}

	public long getHashCode() {
		return hashCode;
	}

	private boolean drawByThreeRule() {

		int count = 0;

		for (int i = hashCodeHistory.size()-1; i >= 0; i--) {
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
				if ((whitePieces.elementAt(i) instanceof Pawn)
						|| (whitePieces.elementAt(i) instanceof Queen)
						|| (whitePieces.elementAt(i) instanceof Rook)) {
					sufficient = true;
				}
			}

			for (int i = 0; i < blackPieces.size(); i++) {
				if ((blackPieces.elementAt(i) instanceof Pawn)
						|| (blackPieces.elementAt(i) instanceof Queen)
						|| (blackPieces.elementAt(i) instanceof Rook)) {
					sufficient = true;
				}
			}

		}

		return sufficient;
	}
}
