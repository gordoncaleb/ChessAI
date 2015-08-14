package com.gordoncaleb.chess.backend;

import java.util.*;
import java.util.ArrayList;

import com.gordoncaleb.chess.ai.AI;
import com.gordoncaleb.chess.ai.AISettings;
import com.gordoncaleb.chess.io.XMLParser;
import com.gordoncaleb.chess.pieces.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Board {
	private static final Logger logger = LoggerFactory.getLogger(Board.class);

	private Piece[][] board;
	private Game.GameStatus boardStatus;
	private ArrayList<Long> validMoves = new ArrayList<>(100);
	private ArrayList<Piece>[] pieces = new ArrayList[2];
	private Stack<Piece>[] piecesTaken = new Stack[2];
	private int[] castleRights = new int[2];

	private Piece[] kings = new Piece[2];
	private int[][] rookStartCols = new int[2][2];
	private int[] kingStartCols = new int[2];
	private int[] materialRow = { 0, 7 };

	private long openFiles = 0;
	private boolean revisitedState = false;

	private Side turn;
	private RNGTable rngTable;
	private Stack<Move> moveHistory;
	private long hashCode;
	private Stack<Long> hashCodeHistory;
	private long[] nullMoveInfo = { 0, BitBoard.ALL_ONES, 0 };

	// private long[] allPosBitBoard = { 0, 0 };
	// private long[] pawnPosBitBoard = { 0, 0 };
	// private long[] kingPosBitBoard = { 0, 0 };

	private long[][] posBitBoard = new long[Piece.PieceID.values().length][2];
	private long[] allPosBitBoard = new long[2];

	public Board() {
		this.board = new Piece[8][8];

		this.pieces[Side.WHITE.ordinal()] = new ArrayList<>();
		this.pieces[Side.BLACK.ordinal()] = new ArrayList<>();

		this.piecesTaken[Side.WHITE.ordinal()] = new Stack<>();
		this.piecesTaken[Side.BLACK.ordinal()] = new Stack<>();

		this.moveHistory = new Stack<>();
		this.hashCodeHistory = new Stack<>();
		this.rngTable = RNGTable.instance;
		this.turn = Side.WHITE;
		this.nullMoveInfo = new long[3];

		kings[Side.BLACK.ordinal()] = new Piece(Piece.PieceID.KING, Side.BLACK, -1, -1, false);
		kings[Side.WHITE.ordinal()] = new Piece(Piece.PieceID.KING, Side.WHITE, -1, -1, false);

		placePiece(kings[Side.BLACK.ordinal()], 0, 0);
		placePiece(kings[Side.WHITE.ordinal()], 7, 0);

	}

	public Board(ArrayList<Piece>[] pieces, Side turn, Stack<Move> moveHistory, int[][] rookStartCols, int[] kingCols) {
		this.board = new Piece[8][8];
		this.pieces[Side.WHITE.ordinal()] = new ArrayList<>(pieces[Side.WHITE.ordinal()].size());
		this.pieces[Side.BLACK.ordinal()] = new ArrayList<>(pieces[Side.BLACK.ordinal()].size());

		this.piecesTaken[Side.WHITE.ordinal()] = new Stack<>();
		this.piecesTaken[Side.BLACK.ordinal()] = new Stack<>();

		this.moveHistory = new Stack<>();
		this.hashCodeHistory = new Stack<>();
		this.rngTable = RNGTable.instance;
		this.turn = turn;
		this.nullMoveInfo = new long[3];

		long[][] posBitBoard = new long[Piece.PieceID.values().length][2];
		// long[] pawnPosBitboard = { 0, 0 };
		// long[] kingPosBitboard = { 0, 0 };

		int[] pawnRow = new int[2];
		pawnRow[Side.BLACK.ordinal()] = 1;
		pawnRow[Side.WHITE.ordinal()] = 6;

		Piece temp;

		for (int i = 0; i < pieces.length; i++) {

			for (int p = 0; p < pieces[i].size(); p++) {
				temp = pieces[i].get(p).getCopy();

				this.pieces[i].add(temp);

				board[temp.getRow()][temp.getCol()] = temp;

				posBitBoard[temp.getPieceID().ordinal()][i] |= temp.getBit();

				if (temp.getPieceID() == Piece.PieceID.PAWN) {

					if (temp.getRow() != pawnRow[i]) {
						temp.setMoved(true);
					}
				}

				if (temp.getPieceID() == Piece.PieceID.KING) {

					kings[i] = temp;

					if (temp.getRow() != materialRow[i]) {
						temp.setMoved(true);
					}
				}
			}

		}

		this.posBitBoard = posBitBoard;

		for (int i = 0; i < Piece.PieceID.values().length; i++) {
			this.allPosBitBoard[0] |= posBitBoard[i][0];
			this.allPosBitBoard[1] |= posBitBoard[i][1];
		}

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
					piecesTaken[moveSide.otherSide().ordinal()].push(new Piece(Move.getPieceTakenID(move), moveSide.otherSide(), Move.getPieceTakenRow(move), Move
							.getPieceTakenCol(move), Move.getPieceTakenHasMoved(move)));
				}

				moveSide = moveSide.otherSide();
			}

		} else {
			loadPiecesTaken();
		}

		if (kingCols == null || rookStartCols == null) {
			initializeCastleSetup();
		} else {
			this.kingStartCols = kingCols;
			this.rookStartCols = rookStartCols;
		}

		// this.castleRights = castleRights;
	}

	public boolean makeMove(long move) {

		int fromRow = Move.getFromRow(move);
		int fromCol = Move.getFromCol(move);
		int toRow = Move.getToRow(move);
		int toCol = Move.getToCol(move);
		Move.MoveNote note = Move.getNote(move);

		if (board[fromRow][fromCol].getSide() != turn) {
			logger.debug("Problem with player ref");
			return false;
		}

		// save off hashCode
		hashCodeHistory.push(new Long(hashCode));

		// remove previous castle options
		hashCode ^= rngTable.getCastlingRightsRandom(this.farRookHasMoved(Side.BLACK), this.nearRookHasMoved(Side.BLACK), this.kingHasMoved(Side.BLACK),
				this.farRookHasMoved(Side.WHITE), this.nearRookHasMoved(Side.WHITE), this.kingHasMoved(Side.WHITE));

		// remove taken piece first
		if (Move.hasPieceTaken(move)) {

			Piece pieceTaken = board[Move.getPieceTakenRow(move)][Move.getPieceTakenCol(move)];

			// remove pieceTaken from vectors
			pieces[turn.otherSide().ordinal()].remove(pieceTaken);
			piecesTaken[turn.otherSide().ordinal()].push(pieceTaken);

			// // remove bit position from appropriate side
			// allPosBitBoard[pieceTaken.getSide().ordinal()] ^=
			// pieceTaken.getBit();
			//
			// if (pieceTaken.getPieceID() == PieceID.PAWN) {
			// pawnPosBitBoard[pieceTaken.getSide().ordinal()] ^=
			// pieceTaken.getBit();
			//
			// }
			//
			// if (pieceTaken.getPieceID() == PieceID.KING) {
			// kingPosBitBoard[pieceTaken.getSide().ordinal()] ^=
			// pieceTaken.getBit();
			// }

			posBitBoard[pieceTaken.getPieceID().ordinal()][pieceTaken.getSide().ordinal()] ^= pieceTaken.getBit();
			allPosBitBoard[pieceTaken.getSide().ordinal()] ^= pieceTaken.getBit();

			// remove ref to piecetaken on board
			board[pieceTaken.getRow()][pieceTaken.getCol()] = null;

			// remove old hash from piece that was taken, if any
			hashCode ^= rngTable.getPiecePerSquareRandom(pieceTaken.getSide(), pieceTaken.getPieceID(), pieceTaken.getRow(), pieceTaken.getCol());

		}

		if (note == Move.MoveNote.CASTLE_NEAR || note == Move.MoveNote.CASTLE_FAR) {

			Piece king = kings[turn.ordinal()];
			Piece rook;

			if (note == Move.MoveNote.CASTLE_NEAR) {
				rook = board[materialRow[turn.ordinal()]][rookStartCols[turn.ordinal()][1]];

				movePiece(king, materialRow[turn.ordinal()], 6, Move.MoveNote.NONE);
				movePiece(rook, materialRow[turn.ordinal()], 5, Move.MoveNote.NONE);

				board[materialRow[turn.ordinal()]][6] = king;
				board[materialRow[turn.ordinal()]][5] = rook;

				castleRights[turn.ordinal()] = 2;
			} else {
				rook = board[materialRow[turn.ordinal()]][rookStartCols[turn.ordinal()][0]];

				movePiece(king, materialRow[turn.ordinal()], 2, Move.MoveNote.NONE);
				movePiece(rook, materialRow[turn.ordinal()], 3, Move.MoveNote.NONE);

				board[materialRow[turn.ordinal()]][2] = king;
				board[materialRow[turn.ordinal()]][3] = rook;

				castleRights[turn.ordinal()] = 1;
			}

		} else {

			movePiece(board[fromRow][fromCol], toRow, toCol, note);

		}

		// if last move made is pawn leap, remove en passant file num
		if (getLastMoveMade() != 0) {
			if (Move.getNote(getLastMoveMade()) == Move.MoveNote.PAWN_LEAP) {
				hashCode ^= rngTable.getEnPassantFile(Move.getToCol(getLastMoveMade()));
			}
		}

		// if new move is pawn leap, add en passant file num
		if (note == Move.MoveNote.PAWN_LEAP) {
			hashCode ^= rngTable.getEnPassantFile(Move.getToCol(move));
		}

		// add new castle options
		hashCode ^= rngTable.getCastlingRightsRandom(this.farRookHasMoved(Side.BLACK), this.nearRookHasMoved(Side.BLACK), this.kingHasMoved(Side.BLACK),
				this.farRookHasMoved(Side.WHITE), this.nearRookHasMoved(Side.WHITE), this.kingHasMoved(Side.WHITE));

		// either remove black and add white or reverse. Same operation.
		hashCode ^= rngTable.getBlackToMoveRandom();

		// show that this move is now the last move made
		moveHistory.push(new Move(move));

		// move was made, next player's turn
		turn = turn.otherSide();

		// verifyBitBoards();

		return true;

	}

	private void movePiece(Piece pieceMoving, int toRow, int toCol, Move.MoveNote note) {

		long bitMove = BitBoard.getMask(pieceMoving.getRow(), pieceMoving.getCol()) ^ BitBoard.getMask(toRow, toCol);

		// remove bit position from where piece was and add where it is now
		posBitBoard[pieceMoving.getPieceID().ordinal()][pieceMoving.getSide().ordinal()] ^= bitMove;
		allPosBitBoard[pieceMoving.getSide().ordinal()] ^= bitMove;

		// remove old hash from where piece was
		hashCode ^= rngTable.getPiecePerSquareRandom(turn, pieceMoving.getPieceID(), pieceMoving.getRow(), pieceMoving.getCol());

		// remove pieces old position
		board[pieceMoving.getRow()][pieceMoving.getCol()] = null;
		// update board to reflect piece's new position
		board[toRow][toCol] = pieceMoving;

		// tell piece its new position
		pieceMoving.setPos(toRow, toCol);
		pieceMoving.setMoved(true);

		if (note == Move.MoveNote.NEW_QUEEN) {
			pieceMoving.setPieceID(Piece.PieceID.QUEEN);
			posBitBoard[Piece.PieceID.PAWN.ordinal()][pieceMoving.getSide().ordinal()] ^= pieceMoving.getBit();
			posBitBoard[Piece.PieceID.QUEEN.ordinal()][pieceMoving.getSide().ordinal()] ^= pieceMoving.getBit();
		}

		// if (pieceMoving == getKing(turn)) {
		// setCastleRights(turn, 0);
		// }

		// add hash of piece at new location
		hashCode ^= rngTable.getPiecePerSquareRandom(turn, pieceMoving.getPieceID(), toRow, toCol);
	}

	public long undoMove() {

		// if no there is no last move then undoMove is impossible
		if (moveHistory.empty()) {
			// logger.debug("Can not undo move");
			return 0;
		}

		// retrieve last move made
		long lastMove = getLastMoveMade();

		int fromRow = Move.getFromRow(lastMove);
		int fromCol = Move.getFromCol(lastMove);
		int toRow = Move.getToRow(lastMove);
		int toCol = Move.getToCol(lastMove);
		Move.MoveNote note = Move.getNote(lastMove);

		// last move made was made by previous player, which is also the next
		// player
		turn = turn.otherSide();

		if (note == Move.MoveNote.CASTLE_NEAR || note == Move.MoveNote.CASTLE_FAR) {

			Piece king = kings[turn.ordinal()];
			Piece rook;

			if (note == Move.MoveNote.CASTLE_FAR) {
				rook = board[materialRow[turn.ordinal()]][3];

				undoMovePiece(king, materialRow[turn.ordinal()], kingStartCols[turn.ordinal()], Move.MoveNote.NONE, false);
				undoMovePiece(rook, materialRow[turn.ordinal()], rookStartCols[turn.ordinal()][0], Move.MoveNote.NONE, false);

				board[materialRow[turn.ordinal()]][kingStartCols[turn.ordinal()]] = king;
				board[materialRow[turn.ordinal()]][rookStartCols[turn.ordinal()][0]] = rook;

			} else {

				rook = board[materialRow[turn.ordinal()]][5];

				undoMovePiece(king, materialRow[turn.ordinal()], kingStartCols[turn.ordinal()], Move.MoveNote.NONE, false);
				undoMovePiece(rook, materialRow[turn.ordinal()], rookStartCols[turn.ordinal()][1], Move.MoveNote.NONE, false);

				board[materialRow[turn.ordinal()]][kingStartCols[turn.ordinal()]] = king;
				board[materialRow[turn.ordinal()]][rookStartCols[turn.ordinal()][1]] = rook;

			}

			castleRights[turn.ordinal()] = 0;

		} else {
			undoMovePiece(board[toRow][toCol], fromRow, fromCol, note, Move.hadMoved(lastMove));
		}

		if (Move.hasPieceTaken(lastMove)) {

			// add taken piece back to vectors and board
			Piece pieceTaken = piecesTaken[turn.otherSide().ordinal()].pop();

			pieces[turn.otherSide().ordinal()].add(pieceTaken);

			// add piece taken to position bit board
			posBitBoard[pieceTaken.getPieceID().ordinal()][pieceTaken.getSide().ordinal()] |= pieceTaken.getBit();
			allPosBitBoard[pieceTaken.getSide().ordinal()] |= pieceTaken.getBit();

			board[pieceTaken.getRow()][pieceTaken.getCol()] = pieceTaken;

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

		// verifyBitBoards();

		return lastMove;

	}

	private void undoMovePiece(Piece pieceMoving, int fromRow, int fromCol, Move.MoveNote note, boolean hadMoved) {

		long bitMove = BitBoard.getMask(pieceMoving.getRow(), pieceMoving.getCol()) ^ BitBoard.getMask(fromRow, fromCol);

		// remove bit position from where piece was and add where it is now
		posBitBoard[pieceMoving.getPieceID().ordinal()][pieceMoving.getSide().ordinal()] ^= bitMove;
		allPosBitBoard[pieceMoving.getSide().ordinal()] ^= bitMove;

		// remove old position
		board[pieceMoving.getRow()][pieceMoving.getCol()] = null;
		// put piece in old position
		board[fromRow][fromCol] = pieceMoving;

		// tell piece where it was
		pieceMoving.setPos(fromRow, fromCol);

		// show whether piece had moved before this move was made
		pieceMoving.setMoved(hadMoved);

		if (note == Move.MoveNote.NEW_QUEEN) {
			pieceMoving.setPieceID(Piece.PieceID.PAWN);
			posBitBoard[Piece.PieceID.PAWN.ordinal()][pieceMoving.getSide().ordinal()] |= pieceMoving.getBit();
			posBitBoard[Piece.PieceID.QUEEN.ordinal()][pieceMoving.getSide().ordinal()] ^= pieceMoving.getBit();
		}

	}

	private void verifyBitBoards() {

		Piece piece;

		long[][] allBitBoard = new long[Piece.PieceID.values().length][2];

		for (int i = 0; i < Piece.PieceID.values().length; i++) {
			allBitBoard[i][0] = posBitBoard[i][0];
			allBitBoard[i][1] = posBitBoard[i][1];
		}

		for (int r = 0; r < 8; r++) {
			for (int c = 0; c < 8; c++) {
				piece = board[r][c];

				if (piece != null) {
					allBitBoard[piece.getPieceID().ordinal()][piece.getSide().ordinal()] ^= BitBoard.getMask(r, c);
				}

			}
		}

		for (int i = 0; i < Piece.PieceID.values().length; i++) {
			if (allBitBoard[i][0] != 0) {
				logger.debug("BitBoard Problem!!!");
			}

			if (allBitBoard[i][1] != 0) {
				logger.debug("BitBoard Problem!!!");
			}
		}

	}

	public boolean canUndo() {
		return (moveHistory.size() != 0);
	}

	public ArrayList<Long> generateValidMoves() {
		return generateValidMoves(false, 0, AI.noKillerMoves);
	}

	public ArrayList<Long> generateValidMoves(boolean sort, long hashMove, long[] killerMoves) {

		// find in check details. i.e. left and right castle info
		// makeNullMove();

		// logger.debug("Not safe areas");
		// BitBoard.printBitBoard(nullMoveInfo[0]);
		//
		// logger.debug("in check vector");
		// BitBoard.printBitBoard(nullMoveInfo[1]);

		// ArrayList<Long> validMoves = new ArrayList<Long>(50);
		validMoves.clear();

		if (!hasSufficientMaterial() || drawByThreeRule()) {
			setBoardStatus(Game.GameStatus.DRAW);
			return validMoves;
		}

		int prevMovesSize = 0;

		Long move;
		for (int p = 0; p < pieces[turn.ordinal()].size(); p++) {

			pieces[turn.ordinal()].get(p).generateValidMoves(this, nullMoveInfo, allPosBitBoard, validMoves);

			for (int m = prevMovesSize; m < validMoves.size(); m++) {
				move = validMoves.get(m);

				move = Move.setHadMoved(move, pieces[turn.ordinal()].get(p).hasMoved());

				if (move == hashMove) {
					move = Move.setValue(move, 10000);
					validMoves.set(m, move);
					continue;
				}

				for (int k = 0; k < killerMoves.length; k++) {
					if (move == killerMoves[k]) {
						move = Move.setValue(move, 9999 - k);
						break;
					}
				}

				validMoves.set(m, move);

			}

			prevMovesSize = validMoves.size();

		}

		if (sort) {
			Collections.sort(validMoves, Collections.reverseOrder());
		}

		if (validMoves.size() == 0) {
			if (isInCheck()) {
				setBoardStatus(Game.GameStatus.CHECKMATE);
			} else {
				setBoardStatus(Game.GameStatus.STALEMATE);
			}
		}

		return validMoves;
	}

	public long[] getAllPosBitBoard() {
		return allPosBitBoard;
	}

	public long[][] getPosBitBoard() {
		return posBitBoard;
	}

	public long[] makeNullMove() {
		// long nullMoveAttacks = 0;
		// long inCheckArrayList = BitBoard.ALL_ONES;
		// long bitAttackCompliment = 0;
		//
		// nullMoveInfo[0] = nullMoveAttacks;
		// nullMoveInfo[1] = inCheckArrayList;
		// nullMoveInfo[2] = bitAttackCompliment;

		// recalculating check info
		clearBoardStatus();

		for (int p = 0; p < pieces[turn.ordinal()].size(); p++) {
			pieces[turn.ordinal()].get(p).clearBlocking();
		}

		nullMoveInfo[0] = BitBoard.getPawnAttacks(posBitBoard[Piece.PieceID.PAWN.ordinal()][turn.otherSide().ordinal()], turn.otherSide());
		nullMoveInfo[0] |= BitBoard.getKnightAttacks(posBitBoard[Piece.PieceID.KNIGHT.ordinal()][turn.otherSide().ordinal()]);
		nullMoveInfo[0] |= BitBoard.getKingAttacks(posBitBoard[Piece.PieceID.KING.ordinal()][turn.otherSide().ordinal()]);

		nullMoveInfo[1] = BitBoard.getPawnAttacks(posBitBoard[Piece.PieceID.KING.ordinal()][turn.ordinal()], turn) & posBitBoard[Piece.PieceID.PAWN.ordinal()][turn.otherSide().ordinal()];

		nullMoveInfo[1] |= BitBoard.getKnightAttacks(posBitBoard[Piece.PieceID.KING.ordinal()][turn.ordinal()]) & posBitBoard[Piece.PieceID.KNIGHT.ordinal()][turn.otherSide().ordinal()];

		if (nullMoveInfo[1] == 0) {
			nullMoveInfo[1] = BitBoard.ALL_ONES;
		}

		nullMoveInfo[2] = 0;

		long updown = ~(allPosBitBoard[0] | allPosBitBoard[1]);
		long left = 0xFEFEFEFEFEFEFEFEL & updown;
		long right = 0x7F7F7F7F7F7F7F7FL & updown;

		for (int p = 0; p < pieces[turn.otherSide().ordinal()].size(); p++) {

			pieces[turn.otherSide().ordinal()].get(p).getNullMoveInfo(this, nullMoveInfo, updown, left, right, posBitBoard[Piece.PieceID.KING.ordinal()][turn.ordinal()],
					King.getKingCheckVectors(posBitBoard[Piece.PieceID.KING.ordinal()][turn.ordinal()], updown, left, right), allPosBitBoard[turn.ordinal()]);

		}

		// for (int i = 0; i < 3; i++) {
		// logger.debug(BitBoard.printBitBoard(nullMoveInfo[i]));
		// }

		if ((kings[turn.ordinal()].getBit() & nullMoveInfo[0]) != 0) {
			setBoardStatus(Game.GameStatus.CHECK);
		}

		return nullMoveInfo;

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

	public Piece.PositionStatus checkPiece(int row, int col, Side player) {

		if (((row | col) & (~0x7)) != 0) {
			return Piece.PositionStatus.OFF_BOARD;
		}

		if (board[row][col] != null) {
			if (board[row][col].getSide() == player)
				return Piece.PositionStatus.FRIEND;
			else
				return Piece.PositionStatus.ENEMY;
		} else {
			return Piece.PositionStatus.NO_PIECE;
		}

	}

	public long getLastMoveMade() {
		if (!moveHistory.empty()) {
			return moveHistory.peek().getMoveLong();
		} else {
			return 0;
		}
	}

	public List<Move> getMoveHistory() {
		return moveHistory;
	}

	public int getPieceValue(int row, int col) {
		return Values.getPieceValue(board[row][col].getPieceID()) + getOpeningPositionValue(board[row][col]);
	}

	public int getOpeningPositionValue(Piece piece) {

		if (piece == null) {
			return 0;
		}

		switch (piece.getPieceID()) {
		case KNIGHT:
			return PositionBonus.getKnightPositionBonus(piece.getRow(), piece.getCol(), piece.getSide());
		case PAWN:
			return PositionBonus.getPawnPositionBonus(piece.getRow(), piece.getCol(), piece.getSide());
		case BISHOP:
			return 0;
		case KING:
			return PositionBonus.getKingOpeningPositionBonus(piece.getRow(), piece.getCol(), piece.getSide());
		case QUEEN:
			return ((piece.getBit() & openFiles) != 0) ? PositionBonus.QUEEN_ON_OPENFILE : 0;
		case ROOK:
			return ((piece.getBit() & openFiles) != 0) ? PositionBonus.ROOK_ON_OPENFILE : 0;
		default:
			logger.debug("Error: invalid piece value request!");
			return 0;
		}

	}

	public int getEndGamePositionValue(Piece piece) {

		switch (piece.getPieceID()) {
		case KNIGHT:
			return PositionBonus.getKnightPositionBonus(piece.getRow(), piece.getCol(), piece.getSide());
		case PAWN:
			return PositionBonus.getPawnPositionBonus(piece.getRow(), piece.getCol(), piece.getSide());
		case BISHOP:
			return 50;
		case KING:
			return PositionBonus.getKingEndGamePositionBonus(piece.getRow(), piece.getCol(), piece.getSide());
		case QUEEN:
			return ((piece.getBit() & openFiles) != 0) ? PositionBonus.QUEEN_ON_OPENFILE + 100 : 0;
		case ROOK:
			return ((piece.getBit() & openFiles) != 0) ? PositionBonus.ROOK_ON_OPENFILE + 50 : 0;
		default:
			logger.debug("Error: invalid piece value request!");
			return 0;
		}

	}

	public int openingPositionScore(Side side) {
		int score = 0;

		for (int i = 0; i < pieces[side.ordinal()].size(); i++) {
			score += getOpeningPositionValue(pieces[side.ordinal()].get(i));
		}

		return score;
	}

	public int endGamePositionScore(Side side) {
		int score = 0;

		for (int i = 0; i < pieces[side.ordinal()].size(); i++) {
			score += getEndGamePositionValue(pieces[side.ordinal()].get(i));
		}

		return score;
	}

	public int materialScore(Side side) {
		int score = 0;

		for (int i = 0; i < pieces[side.ordinal()].size(); i++) {
			score += Values.getPieceValue(pieces[side.ordinal()].get(i).getPieceID());
		}

		return score;
	}

	public int castleScore(Side side) {
		int score = 0;
		int castleRights = this.castleRights[side.ordinal()];

		if (castleRights != 0) {
			if (castleRights == 1) {
				score += Values.FAR_CASTLE_VALUE;
			} else {
				score += Values.NEAR_CASTLE_VALUE;
			}
		} else {
			if (!kingHasMoved(side)) {
				if (farRookHasMoved(side)) {
					score -= Values.FAR_CASTLE_ABILITY_LOST_VALUE;
				}

				if (nearRookHasMoved(side)) {
					score -= Values.NEAR_CASTLE_ABILITY_LOST_VALUE;
				}
			} else {
				score -= Values.CASTLE_ABILITY_LOST_VALUE;
			}
		}

		return score;
	}

	public int pawnStructureScore(Side side, int phase) {

		long pawns = posBitBoard[Piece.PieceID.PAWN.ordinal()][side.ordinal()];
		long otherPawns = posBitBoard[Piece.PieceID.PAWN.ordinal()][side.otherSide().ordinal()];

		long files = 0x0101010101010101L;

		int occupiedCol = 0;
		for (int c = 0; c < 8; c++) {
			if ((files & pawns) != 0) {

				occupiedCol++;

				if ((files & otherPawns) == 0) {
					openFiles |= files;
				}
			}
			files = files << 1;
		}

		int doubledPawns = Long.bitCount(pawns) - occupiedCol;

		long passedBB = BitBoard.getPassedPawns(pawns, otherPawns, side);

		int passedPawns = 0;

		for (int i = 0; i < 8; i++) {
			passedPawns += Long.bitCount(passedBB & BitBoard.getRowMask(i)) * Values.PASSED_PAWN_BONUS[side.ordinal()][i];
		}
		
		int isolatedPawns = Long.bitCount(BitBoard.getIsolatedPawns(pawns, side)) * Values.ISOLATED_PAWN_BONUS;
		
		return BitBoard.getBackedPawns(pawns) * Values.BACKED_PAWN_BONUS + doubledPawns * Values.DOUBLED_PAWN_BONUS + ((passedPawns * phase) / 256) + isolatedPawns;
	}

	public int calcGamePhase() {

		int phase = Values.TOTALPHASE;

		for (int i = 0; i < 2; i++) {
			for (int p = 0; p < pieces[i].size(); p++) {
				phase -= Values.PIECE_PHASE_VAL[pieces[i].get(p).getPieceID().ordinal()];
			}
		}

		phase = (phase * 256 + (Values.TOTALPHASE / 2)) / Values.TOTALPHASE;

		return phase;
	}

	public int staticScore() {
		int ptDiff = 0;

		int phase = calcGamePhase();

		int myPawnScore = pawnStructureScore(turn, phase);
		int yourPawnScore = pawnStructureScore(turn.otherSide(), phase);

		int openingMyScore = castleScore(turn) + openingPositionScore(turn);
		int openingYourScore = castleScore(turn.otherSide()) + openingPositionScore(turn.otherSide());

		int endGameMyScore = endGamePositionScore(turn);
		int endGameYourScore = endGamePositionScore(turn.otherSide());

		int myScore = (openingMyScore * (256 - phase) + endGameMyScore * phase) / 256 + materialScore(turn) + myPawnScore;
		int yourScore = (openingYourScore * (256 - phase) + endGameYourScore * phase) / 256 + materialScore(turn.otherSide()) + yourPawnScore;

		ptDiff = myScore - yourScore;

		return ptDiff;
	}

	public boolean canQueen() {
		long p = posBitBoard[Piece.PieceID.PAWN.ordinal()][turn.ordinal()];
		long o = allPosBitBoard[turn.otherSide().ordinal()];

		if (turn == Side.WHITE) {
			return (((((p >>> 8) & ~o) | (BitBoard.getPawnAttacks(p, turn) & o)) & 0xFFL) != 0);
		} else {
			return (((((p << 8) & ~o) | (BitBoard.getPawnAttacks(p, turn) & o)) & 0xFF00000000000000L) != 0);
		}
	}

	public Piece getPiece(int row, int col) {
		return board[row][col];
	}

	public Piece.PieceID getPieceID(int row, int col) {
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
			logger.debug("Error: requested player on null piece");
			return null;
		}

	}

	public Side getTurn() {
		return turn;
	}

	public void setTurn(Side turn) {
		this.turn = turn;
	}

	public boolean hasPiece(int row, int col) {
		if ((row & ~0x7) != 0 || (col & ~7) != 0) {
			return false;
		} else {
			return (board[row][col] != null);
		}
	}

	public boolean hasMoved(int row, int col) {
		if (hasPiece(row, col)) {
			return board[row][col].hasMoved();
		} else {
			return true;
		}
	}

	public Stack<Piece> getPiecesTakenFor(Side player) {
		return piecesTaken[player.ordinal()];
	}

	private void initializeCastleSetup() {

		int[][] rookCols = { { -1, -1 }, { -1, -1 } };
		this.rookStartCols = rookCols;

		for (int s = 0; s < 2; s++) {
			for (int c = kings[s].getCol() - 1; c >= 0; c--) {

				if (board[materialRow[s]][c] != null) {
					if (board[materialRow[s]][c].getPieceID() == Piece.PieceID.ROOK) {
						rookCols[s][0] = c;
						break;
					}
				}
			}

			for (int c = kings[s].getCol() + 1; c < 8; c++) {
				if (board[materialRow[s]][c] != null) {
					if (board[materialRow[s]][c].getPieceID() == Piece.PieceID.ROOK) {
						rookCols[s][1] = c;
						break;
					}
				}
			}
		}

		kingStartCols[Side.BLACK.ordinal()] = kings[Side.BLACK.ordinal()].getCol();
		kingStartCols[Side.WHITE.ordinal()] = kings[Side.WHITE.ordinal()].getCol();

	}

	public boolean placePiece(Piece piece, int toRow, int toCol) {

		if (toRow >= 0 && toRow < 8 && toCol >= 0 && toCol < 8) {
			if (board[toRow][toCol] != null) {
				if (board[toRow][toCol].getPieceID() == Piece.PieceID.KING) {
					return false;
				}
			}
		}

		if (piece.getPieceID() == Piece.PieceID.KING) {
			if (toRow < 0 || toCol < 0) {
				return false;
			} else {
				kings[piece.getSide().ordinal()] = piece;
			}
		}

		piece.setMoved(false);

		if (piece.getRow() >= 0) {
			// remove where piece was if it was on board
			posBitBoard[piece.getPieceID().ordinal()][piece.getSide().ordinal()] ^= piece.getBit();
			allPosBitBoard[piece.getSide().ordinal()] ^= piece.getBit();
			board[piece.getRow()][piece.getCol()] = null;

		} else {

			pieces[piece.getSide().ordinal()].add(piece);
		}

		if (toRow >= 0) {
			// remove where piece taken was
			if (board[toRow][toCol] != null) {

				Piece pieceTaken = board[toRow][toCol];

				// remove bit position of piece taken
				posBitBoard[pieceTaken.getPieceID().ordinal()][pieceTaken.getSide().ordinal()] ^= pieceTaken.getBit();
				allPosBitBoard[pieceTaken.getSide().ordinal()] ^= pieceTaken.getBit();

				// remove ref to piece taken
				pieces[pieceTaken.getSide().ordinal()].remove(pieceTaken);
			}

			// tell piece where it is now
			piece.setPos(toRow, toCol);

			// reflect new piece in position bitboard
			posBitBoard[piece.getPieceID().ordinal()][piece.getSide().ordinal()] |= piece.getBit();
			allPosBitBoard[piece.getSide().ordinal()] |= piece.getBit();

			// update board ref to show piece there
			board[toRow][toCol] = piece;
		} else {
			// piece is being taken off the board. Remove
			if (piece.getPieceID() != Piece.PieceID.KING) {
				pieces[piece.getSide().ordinal()].remove(piece);
			}
		}

		// basically start over with new board
		this.moveHistory.clear();
		this.hashCodeHistory.clear();

		this.hashCode = generateHashCode();

		initializeCastleSetup();

		verifyBitBoards();

		return true;

	}

	public boolean isInCheck() {
		return (boardStatus == Game.GameStatus.CHECK);
	}

	public boolean isInCheckMate() {
		return (boardStatus == Game.GameStatus.CHECKMATE);
	}

	public boolean isInStaleMate() {
		return (boardStatus == Game.GameStatus.STALEMATE);
	}

	public boolean isTimeUp() {
		return (boardStatus == Game.GameStatus.TIMES_UP);
	}

	public boolean isDraw() {
		return (boardStatus == Game.GameStatus.DRAW);
	}

	public boolean isInvalid() {
		return (boardStatus == Game.GameStatus.INVALID);
	}

	public boolean isGameOver() {
		return (isInCheckMate() || isInStaleMate() || isTimeUp() || isDraw() || isInvalid());
	}

	public void clearBoardStatus() {
		boardStatus = Game.GameStatus.IN_PLAY;
	}

	public Game.GameStatus getBoardStatus() {
		return boardStatus;
	}

	public void setBoardStatus(Game.GameStatus boardStatus) {
		this.boardStatus = boardStatus;
	}

	public boolean farRookHasMoved(Side player) {
		return hasMoved(materialRow[player.ordinal()], rookStartCols[player.ordinal()][0]);
	}

	public boolean nearRookHasMoved(Side player) {
		return hasMoved(materialRow[player.ordinal()], rookStartCols[player.ordinal()][1]);
	}

	public int getRookStartingCol(Side side, int near) {
		return rookStartCols[side.ordinal()][near];
	}

	public int getKingStartingCol(Side side) {
		return kingStartCols[side.ordinal()];
	}

	public boolean kingHasMoved(Side player) {
		return kings[player.ordinal()].hasMoved();
	}

	public long farCastleMask(Side player) {
		return BitBoard.getCastleMask(Math.min(rookStartCols[player.ordinal()][0], 2), kings[player.ordinal()].getCol(), player);
	}

	public long nearCastleMask(Side player) {
		return BitBoard.getCastleMask(kings[player.ordinal()].getCol(), Math.max(rookStartCols[player.ordinal()][1], 6), player);
	}

	public Board getCopy() {
		return new Board(pieces, this.turn, moveHistory, rookStartCols, kingStartCols);
	}

	public String toString() {
		String stringBoard = "";
		int pieceDetails = 0;
		Piece p;

		for (int row = 0; row < 8; row++) {
			for (int col = 0; col < 8; col++) {
				if (board[row][col] != null) {

					p = board[row][col];

					if (p.hasMoved() && (p.getPieceID() == Piece.PieceID.PAWN || p.getPieceID() == Piece.PieceID.KING || p.getPieceID() == Piece.PieceID.ROOK)) {
						pieceDetails |= 1;
					}

					if (p.getPieceID() == Piece.PieceID.ROOK && kingHasMoved(p.getSide())) {
						pieceDetails |= 1;
					}

					if (p.getPieceID() == Piece.PieceID.KING && nearRookHasMoved(p.getSide()) && farRookHasMoved(p.getSide())) {
						pieceDetails |= 1;
					}

					long lastMove = getLastMoveMade();

					if (getLastMoveMade() != 0) {
						if (Move.getToRow(lastMove) == row && Move.getToCol(lastMove) == col && Move.getNote(lastMove) == Move.MoveNote.PAWN_LEAP) {
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

		if (includeHistory) {

			Stack<Move> movesToRedo = new Stack<Move>();
			long m;
			while ((m = undoMove()) != 0) {
				movesToRedo.push(new Move(m));
			}

			xmlBoard += "<setup>\n" + this.toString() + "</setup>\n";
			xmlBoard += "<turn>" + turn.toString() + "</turn>\n";

			while (!movesToRedo.isEmpty()) {
				makeMove(movesToRedo.pop().getMoveLong());
			}

			for (int i = 0; i < moveHistory.size(); i++) {
				xmlBoard += Move.toXML(moveHistory.elementAt(i).getMoveLong());
			}

		} else {
			xmlBoard += "<setup>\n" + this.toString() + "</setup>\n";
			xmlBoard += "<turn>" + turn.toString() + "</turn>\n";
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

		hashCode ^= rngTable.getCastlingRightsRandom(this.farRookHasMoved(Side.BLACK), this.nearRookHasMoved(Side.BLACK), this.kingHasMoved(Side.BLACK),
				this.farRookHasMoved(Side.WHITE), this.nearRookHasMoved(Side.WHITE), this.kingHasMoved(Side.WHITE));

		if (getLastMoveMade() != 0) {
			if (Move.getNote(getLastMoveMade()) == Move.MoveNote.PAWN_LEAP) {
				hashCode ^= rngTable.getEnPassantFile(Move.getToCol(getLastMoveMade()));
			}
		}

		return hashCode;
	}

	public int getHashIndex() {
		return (int) (hashCode & AISettings.hashIndexMask);
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

	public boolean isRevisitedState() {
		return revisitedState;
	}

	public boolean drawByThreeRule() {

		int count = 0;
		int fiftyMove = 0;

		revisitedState = false;

		for (int i = hashCodeHistory.size() - 1; i >= 0; i--) {

			if (fiftyMove >= 101) {
				return true;
			}

			if (hashCode == hashCodeHistory.elementAt(i)) {
				revisitedState = true;
				count++;
			}

			if (count > 2) {
				return true;
			}

			if (moveHistory.elementAt(i).hasPieceTaken()) {
				return false;
			}

			fiftyMove++;
		}

		return false;
	}

	private boolean hasSufficientMaterial() {

		boolean sufficient = true;

		if (pieces[0].size() <= 2 && pieces[1].size() <= 2) {

			sufficient = false;

			for (int i = 0; i < pieces.length; i++) {
				for (int p = 0; p < pieces[i].size(); p++) {
					if ((pieces[i].get(p).getPieceID() == Piece.PieceID.PAWN) || (pieces[i].get(p).getPieceID() == Piece.PieceID.QUEEN) || (pieces[i].get(p).getPieceID() == Piece.PieceID.ROOK)) {
						sufficient = true;
					}
				}
			}

		}

		return sufficient;
	}

	private void loadPiecesTaken() {

		for (int i = 0; i < pieces.length; i++) {

			piecesTaken[i] = getFullPieceSet(Side.values()[i]);

			Piece piecePresent;
			for (int p = 0; p < pieces[i].size(); p++) {
				piecePresent = pieces[i].get(p);

				for (int t = 0; t < piecesTaken[i].size(); t++) {
					if (piecesTaken[i].elementAt(t).getPieceID() == piecePresent.getPieceID()) {
						piecesTaken[i].remove(t);
						break;
					}
				}
			}
		}

	}



	public static Stack<Piece> getFullPieceSet(Side player) {
		Stack<Piece> pieces = new Stack<Piece>();

		for (int i = 0; i < 8; i++) {
			pieces.add(new Piece(Piece.PieceID.PAWN, player, 0, 0, false));
		}

		for (int i = 0; i < 2; i++) {
			pieces.add(new Piece(Piece.PieceID.BISHOP, player, 0, 0, false));
		}

		for (int i = 0; i < 2; i++) {
			pieces.add(new Piece(Piece.PieceID.ROOK, player, 0, 0, false));
		}

		for (int i = 0; i < 2; i++) {
			pieces.add(new Piece(Piece.PieceID.KNIGHT, player, 0, 0, false));
		}

		pieces.add(new Piece(Piece.PieceID.KING, player, 0, 0, false));
		pieces.add(new Piece(Piece.PieceID.QUEEN, player, 0, 0, false));

		return pieces;
	}
}
