package chessBackend;

import java.util.Stack;
import java.util.Vector;

import chessPieces.Piece;
import chessPieces.PieceID;
import chessPieces.PositionBonus;
import chessPieces.Values;
import chessPieces.PositionStatus;
import chessPieces.Pawn;
import chessPieces.Queen;

public class Board {
	private Piece[][] board;
	private int boardState;
	private Vector<Piece> blackPieces;
	private Vector<Piece> whitePieces;
	private Piece blackKing;
	private Piece whiteKing;
	private Player player;
	private RNGTable rngTable;
	private Stack<Move> moveHistory;
	private long hashCode;
	private Stack<Long> hashCodeHistory;
	private long[] nullMoveInfo;
	private long[] posBitBoard;

	public Board(Piece[][] board, Vector<Piece> blackPieces, Vector<Piece> whitePieces, long[] posBitBoard, Piece blackKing, Piece whiteKing,
			Player player, Stack<Move> moveHistory, Long hashCode, RNGTable rngTable) {

		this.board = board;
		this.blackPieces = blackPieces;
		this.whitePieces = whitePieces;
		this.blackKing = blackKing;
		this.whiteKing = whiteKing;
		this.posBitBoard = posBitBoard;
		this.moveHistory = moveHistory;
		this.hashCodeHistory = new Stack<Long>();
		this.rngTable = rngTable;
		this.player = player;
		this.nullMoveInfo = new long[3];

		if (hashCode != null) {
			this.hashCode = hashCode;
		} else {
			this.hashCode = generateHashCode();
		}

	}

	public boolean makeMove(Move move) {

		if (board[move.getFromRow()][move.getFromCol()].getPlayer() != player) {
			System.out.println("Problem with player ref");
			return false;
		}

		// save off hashCode
		hashCodeHistory.push(new Long(hashCode));

		// remove previous castle options
		hashCode ^= rngTable.getCastlingRightsRandom(this.farRookHasMoved(Player.BLACK), this.nearRookHasMoved(Player.BLACK),
				this.kingHasMoved(Player.BLACK), this.farRookHasMoved(Player.WHITE), this.nearRookHasMoved(Player.WHITE),
				this.kingHasMoved(Player.WHITE));

		Piece pieceTaken = move.getPieceTaken();

		// remove taken piece first
		if (pieceTaken != null) {

			// remove bit position from appropriate side
			posBitBoard[pieceTaken.getPlayer().ordinal()] ^= pieceTaken.getBit();

			// piceTaken is old ref, find new ref
			if (pieceTaken != board[pieceTaken.getRow()][pieceTaken.getCol()]) {
				pieceTaken = board[move.getToRow()][move.getToCol()];
				move.setPieceTaken(pieceTaken);
			}

			// remove pieceTaken from vectors
			if (pieceTaken.getPlayer() == Player.WHITE) {
				if (!whitePieces.remove(pieceTaken)) {
					System.out.println("Piece " + pieceTaken.toString() + " not found");
				}
			} else {
				if (!blackPieces.remove(pieceTaken)) {
					System.out.println("Piece " + pieceTaken.toString() + " not found");
				}
			}

			// remove ref to piecetaken on board
			board[pieceTaken.getRow()][pieceTaken.getCol()] = null;

			// remove old hash from piece that was taken, if any
			hashCode ^= rngTable.getPiecePerSquareRandom(pieceTaken.getPlayer(), pieceTaken.getPieceID(), pieceTaken.getRow(), pieceTaken.getCol());

		}

		if (move.getNote() != MoveNote.NEW_QUEEN) {

			movePiece(move);

		} else {

			// remove old hash from where pawn was
			hashCode ^= rngTable.getPiecePerSquareRandom(player, PieceID.PAWN, move.getFromRow(), move.getFromCol());

			// remove pawn from vector
			if (player == Player.WHITE) {
				whitePieces.remove(board[move.getFromRow()][move.getFromCol()]);
			} else {
				blackPieces.remove(board[move.getFromRow()][move.getFromCol()]);
			}

			// remove pawn from board
			board[move.getFromRow()][move.getFromCol()] = null;

			// put queen on board
			board[move.getToRow()][move.getToCol()] = new Queen(player, move.getToRow(), move.getToCol(), false);

			// add hash of piece at new location. Probably a queen.
			hashCode ^= rngTable.getPiecePerSquareRandom(player, board[move.getToRow()][move.getToCol()].getPieceID(), move.getToRow(),
					move.getToCol());

			// add queen to vectors
			if (player == Player.WHITE) {
				whitePieces.add(board[move.getToRow()][move.getToCol()]);
			} else {
				blackPieces.add(board[move.getToRow()][move.getToCol()]);
			}
		}

		if (move.getNote() == MoveNote.CASTLE_NEAR) {
			if (player == Player.BLACK) {
				movePiece(new Move(0, 7, 0, 5));
			} else {
				movePiece(new Move(7, 7, 7, 5));
			}
		}

		if (move.getNote() == MoveNote.CASTLE_FAR) {
			if (player == Player.BLACK) {
				movePiece(new Move(0, 0, 0, 3));
			} else {
				movePiece(new Move(7, 0, 7, 3));
			}
		}

		// if last move made is pawn leap, remove en passant file num
		if (getLastMoveMade() != null) {
			if (getLastMoveMade().getNote() == MoveNote.PAWN_LEAP) {
				hashCode ^= rngTable.getEnPassantFile(getLastMoveMade().getToCol());
			}
		}

		// if new move is pawn leap, add en passant file num
		if (move.getNote() == MoveNote.PAWN_LEAP) {
			hashCode ^= rngTable.getEnPassantFile(move.getToCol());
		}

		// add new castle options
		hashCode ^= rngTable.getCastlingRightsRandom(this.farRookHasMoved(Player.BLACK), this.nearRookHasMoved(Player.BLACK),
				this.kingHasMoved(Player.BLACK), this.farRookHasMoved(Player.WHITE), this.nearRookHasMoved(Player.WHITE),
				this.kingHasMoved(Player.WHITE));

		// either remove black and add white or reverse. Same operation.
		hashCode ^= rngTable.getBlackToMoveRandom();

		// show that this move is now the last move made
		moveHistory.push(move);

		// move was made, next player's turn
		player = getNextPlayer();

		return true;

	}

	private void movePiece(Move move) {
		Piece pieceMoving = board[move.getFromRow()][move.getFromCol()];

		// remove bit position from where piece was
		posBitBoard[pieceMoving.getPlayer().ordinal()] ^= pieceMoving.getBit();

		// remove old hash from where piece was
		hashCode ^= rngTable.getPiecePerSquareRandom(player, pieceMoving.getPieceID(), move.getFromRow(), move.getFromCol());

		// tell piece its new position
		pieceMoving.move(move);
		// update board to reflect piece's new position
		board[move.getToRow()][move.getToCol()] = pieceMoving;
		// remove pieces old position
		board[move.getFromRow()][move.getFromCol()] = null;

		// add bit position of where piece is now
		posBitBoard[pieceMoving.getPlayer().ordinal()] |= pieceMoving.getBit();

		// add hash of piece at new location
		hashCode ^= rngTable.getPiecePerSquareRandom(player, pieceMoving.getPieceID(), move.getToRow(), move.getToCol());
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
		player = getNextPlayer();

		if (lastMove.getNote() != MoveNote.NEW_QUEEN) {

			undoMovePiece(lastMove);

		} else {

			// remove queen from vectors
			if (player == Player.WHITE) {
				whitePieces.remove(board[lastMove.getToRow()][lastMove.getToCol()]);
			} else {
				blackPieces.remove(board[lastMove.getToRow()][lastMove.getToCol()]);
			}

			// remove queen from board
			board[lastMove.getToRow()][lastMove.getToCol()] = null;

			// add pawn back to board
			board[lastMove.getFromRow()][lastMove.getFromCol()] = new Pawn(player, lastMove.getFromRow(), lastMove.getFromCol(), true);

			// add pawn to vectors
			if (player == Player.WHITE) {
				whitePieces.add(board[lastMove.getFromRow()][lastMove.getFromCol()]);
			} else {
				blackPieces.add(board[lastMove.getFromRow()][lastMove.getFromCol()]);
			}
		}

		// add taken piece back to vectors and board
		Piece pieceTaken = lastMove.getPieceTaken();
		if (pieceTaken != null) {

			// add piece taken to position bit board
			posBitBoard[pieceTaken.getPlayer().ordinal()] |= pieceTaken.getBit();

			board[pieceTaken.getRow()][pieceTaken.getCol()] = pieceTaken;

			if (pieceTaken.getPlayer() == Player.WHITE) {
				whitePieces.add(pieceTaken);
			} else {
				blackPieces.add(pieceTaken);
			}

		}

		if (lastMove.getNote() == MoveNote.CASTLE_NEAR) {
			if (player == Player.BLACK) {
				undoMovePiece(new Move(0, 7, 0, 5));
			} else {
				undoMovePiece(new Move(7, 7, 7, 5));
			}
		}

		if (lastMove.getNote() == MoveNote.CASTLE_FAR) {
			if (player == Player.BLACK) {
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
		posBitBoard[pieceMoving.getPlayer().ordinal()] ^= pieceMoving.getBit();

		// tell piece where it was
		pieceMoving.move(move.reverse());
		// put piece in old position
		board[move.getFromRow()][move.getFromCol()] = pieceMoving;
		// remove old position
		board[move.getToRow()][move.getToCol()] = null;

		// show whether piece had moved before this move was made
		board[move.getFromRow()][move.getFromCol()].setMoved(move.hadMoved());

		// add bit position of where piece is now
		posBitBoard[pieceMoving.getPlayer().ordinal()] |= pieceMoving.getBit();

	}

	public Vector<Move> generateValidMoves() {

		// recalculating check info
		clearBoardState();

		// find in check details. i.e. left and right castle info
		makeNullMove();

		// System.out.println("Not safe areas");
		// BitBoard.printBitBoard(nullMoveInfo[0]);
		//
		// System.out.println("in check vector");
		// BitBoard.printBitBoard(nullMoveInfo[1]);

		Vector<Move> validMoves = new Vector<Move>(30);

		Vector<Piece> pieces = getPlayerPieces(player);
		Piece piece;
		Vector<Move> moves;
		Move move;
		for (int p = 0; p < pieces.size(); p++) {
			piece = pieces.elementAt(p);

			moves = pieces.elementAt(p).generateValidMoves(this, nullMoveInfo, posBitBoard);
			for (int m = 0; m < moves.size(); m++) {
				move = moves.elementAt(m);

				move.setHadMoved(hasMoved(move.getFromRow(), move.getFromCol()));
				addSortValidMove(validMoves, moves.elementAt(m));
			}

			piece.clearBlocking();

		}

		if (validMoves.size() == 0) {
			if (this.isInCheck()) {
				this.setIsInCheckMate();
			} else {
				this.setIsInStaleMate();
			}
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

		Vector<Piece> pieces = getPlayerPieces(getNextPlayer());
		for (int p = 0; p < pieces.size(); p++) {
			pieces.elementAt(p).getNullMoveInfo(this, nullMoveInfo);
		}

		if ((getPlayerKing().getBit() & nullMoveInfo[0]) != 0) {
			setInCheck();
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
	
	public Vector<Move> getMoveHistory(){
		return moveHistory;
	}

	public int getPieceValue(int row, int col) {
		int value;
		Player player = board[row][col].getPlayer();

		switch (board[row][col].getPieceID()) {
		case KNIGHT:
			int piecesMissing = 32 - (blackPieces.size() + whitePieces.size());
			value = Values.KNIGHT_VALUE - piecesMissing * Values.KNIGHT_ENDGAME_INC;
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
		if (player == Player.BLACK) {
			return Player.WHITE;
		} else {
			return Player.BLACK;
		}
	}

	public Piece getPlayerKing() {
		if (player == Player.BLACK) {
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

	public Vector<Piece> getPlayerPieces(Player player) {
		if (player == Player.WHITE) {
			return whitePieces;
		} else {
			return blackPieces;
		}
	}

	public boolean isInCheck() {
		return ((boardState & 1) != 0);
	}

	public boolean isInCheckMate() {
		return ((boardState & 2) != 0);
	}

	public boolean isInStaleMate() {
		return ((boardState & 4) != 0);
	}

	public void setIsInCheckMate() {
		boardState |= 2;
	}

	public void setIsInStaleMate() {
		boardState |= 4;
	}

	public void setInCheck() {
		this.boardState |= 1;
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

	public boolean farRookHasMoved(Player player) {
		if (player == Player.BLACK) {
			return hasMoved(0, 0);
		} else {
			return hasMoved(7, 0);
		}
	}

	public boolean nearRookHasMoved(Player player) {
		if (player == Player.BLACK) {
			return hasMoved(0, 7);
		} else {
			return hasMoved(7, 7);
		}
	}

	public boolean kingHasMoved(Player player) {
		if (player == Player.BLACK) {
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

					copyBoard[row][col] = board[row][col].getCopy(this);

					if (copyBoard[row][col].getPlayer() == Player.BLACK) {
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

		return new Board(copyBoard, copyBlackPieces, copyWhitePieces, posBitBoard, blackKing, whiteKing, this.player, new Stack<Move>(),
				this.hashCode, this.rngTable);

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

					if (p.getPieceID() == PieceID.ROOK && kingHasMoved(p.getPlayer())) {
						pieceDetails |= 1;
					}

					if (p.getPieceID() == PieceID.KING && nearRookHasMoved(p.getPlayer()) && farRookHasMoved(p.getPlayer())) {
						pieceDetails |= 1;
					}

					if (getLastMoveMade() != null) {
						if (getLastMoveMade().getToRow() == row && getLastMoveMade().getToCol() == col
								&& getLastMoveMade().getNote() == MoveNote.PAWN_LEAP) {
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

	public String toXML() {
		String xmlBoard = "<board>\n";

		xmlBoard += "<setup>\n" + this.toString() + "</setup>\n";
		xmlBoard += "<turn>" + player.toString() + "</turn>\n";

		for (int i = 0; i < moveHistory.size(); i++) {
			xmlBoard += moveHistory.elementAt(i).toXML();
		}

		xmlBoard += "</board>";
		return xmlBoard;
	}

	public long generateHashCode() {
		long hashCode = 0;

		if (player == Player.BLACK) {
			hashCode = rngTable.getBlackToMoveRandom();
		}

		Piece p;
		for (int r = 0; r < 8; r++) {
			for (int c = 0; c < 8; c++) {
				p = board[r][c];
				if (p != null) {
					hashCode ^= rngTable.getPiecePerSquareRandom(p.getPlayer(), p.getPieceID(), r, c);
				}
			}
		}

		hashCode ^= rngTable.getCastlingRightsRandom(this.farRookHasMoved(Player.BLACK), this.nearRookHasMoved(Player.BLACK),
				this.kingHasMoved(Player.BLACK), this.farRookHasMoved(Player.WHITE), this.nearRookHasMoved(Player.WHITE),
				this.kingHasMoved(Player.WHITE));

		if (this.getLastMoveMade() != null) {
			if (this.getLastMoveMade().getNote() == MoveNote.PAWN_LEAP) {
				hashCode ^= rngTable.getEnPassantFile(this.getLastMoveMade().getToCol());
			}
		}

		return hashCode;
	}

	public long getHashCode() {
		return hashCode;
	}

}
