package chessBackend;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Stack;
import java.util.Vector;

import chessIO.FileIO;
import chessIO.XMLParser;
import chessPieces.*;

public class Board {
	private Piece[][] board;
	private GameStatus boardStatus;
	private ArrayList<Piece>[] pieces = new ArrayList[2];
	private Stack<Piece>[] piecesTaken = new Stack[2];
	private int castleRights;
	private Stack<Integer> castleRightsHistory;
	private Piece[] kings = new Piece[2];
	private Side turn;
	private RNGTable rngTable;
	private Stack<Move> moveHistory;
	private long hashCode;
	private Stack<Long> hashCodeHistory;
	private long[] nullMoveInfo;

	private long[] allPosBitBoard;
	private long[] pawnPosBitBoard;
	private long[] kingPosBitBoard;

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

	public Board(ArrayList<Piece>[] pieces, Side turn, Stack<Move> moveHistory, Stack<Integer> castelingRightsHistory, int castlingRights) {
		this.board = new Piece[8][8];
		this.pieces[Side.WHITE.ordinal()] = new ArrayList<Piece>(pieces[Side.WHITE.ordinal()].size());
		this.pieces[Side.BLACK.ordinal()] = new ArrayList<Piece>(pieces[Side.BLACK.ordinal()].size());

		this.piecesTaken[Side.WHITE.ordinal()] = new Stack<Piece>();
		this.piecesTaken[Side.BLACK.ordinal()] = new Stack<Piece>();

		this.moveHistory = new Stack<Move>();
		this.castleRightsHistory = new Stack<Integer>();
		this.hashCodeHistory = new Stack<Long>();
		this.rngTable = RNGTable.getSingleton();
		this.turn = turn;
		this.nullMoveInfo = new long[3];

		long[] posBitBoard = { 0, 0 };
		long[] pawnPosBitboard = { 0, 0 };
		long[] kingPosBitboard = { 0, 0 };

		int[] pawnRow = new int[2];
		pawnRow[Side.BLACK.ordinal()] = 1;
		pawnRow[Side.WHITE.ordinal()] = 6;

		int[] kingRow = new int[2];
		kingRow[Side.BLACK.ordinal()] = 0;
		kingRow[Side.WHITE.ordinal()] = 7;

		Piece temp;

		for (int i = 0; i < pieces.length; i++) {

			for (int p = 0; p < pieces[i].size(); p++) {
				temp = pieces[i].get(p).getCopy();

				this.pieces[i].add(temp);

				board[temp.getRow()][temp.getCol()] = temp;

				posBitBoard[i] |= temp.getBit();

				if (temp.getPieceID() == PieceID.PAWN) {

					pawnPosBitboard[i] |= temp.getBit();

					if (temp.getRow() != pawnRow[i]) {
						temp.setMoved(true);
					}
				}

				if (temp.getPieceID() == PieceID.KING) {

					kingPosBitboard[i] |= temp.getBit();
					kings[i] = temp;

					if (temp.getRow() != kingRow[i] || temp.getCol() != 4) {
						temp.setMoved(true);
					}
				}
			}

		}

		this.allPosBitBoard = posBitBoard;
		this.pawnPosBitBoard = pawnPosBitboard;
		this.kingPosBitBoard = kingPosBitboard;

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
					piecesTaken[moveSide.otherSide().ordinal()].push(new Piece(Move.getPieceTakenID(move), moveSide.otherSide(), Move.getPieceTakenRow(move), Move.getPieceTakenCol(move), Move.getPieceTakenHasMoved(move)));
				}

				moveSide = moveSide.otherSide();
			}

		} else {
			loadPiecesTaken();
		}

		for (int i = 0; i < castelingRightsHistory.size(); i++) {
			this.castleRightsHistory.push(castelingRightsHistory.elementAt(i).intValue());
		}

		// this.castleRights = castleRights;
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
			pieces[turn.otherSide().ordinal()].remove(pieceTaken);
			piecesTaken[turn.otherSide().ordinal()].push(pieceTaken);

			// remove bit position from appropriate side
			allPosBitBoard[pieceTaken.getSide().ordinal()] ^= pieceTaken.getBit();

			if (pieceTaken.getPieceID() == PieceID.PAWN) {
				pawnPosBitBoard[pieceTaken.getSide().ordinal()] ^= pieceTaken.getBit();
				;
			}

			if (pieceTaken.getPieceID() == PieceID.KING) {
				kingPosBitBoard[pieceTaken.getSide().ordinal()] ^= pieceTaken.getBit();
				;
			}

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

		// verifyBitBoards();

		return true;

	}

	private void movePiece(int fromRow, int fromCol, int toRow, int toCol, MoveNote note) {
		Piece pieceMoving = board[fromRow][fromCol];

		long bitMove = BitBoard.getMask(fromRow, fromCol) | BitBoard.getMask(toRow, toCol);

		// remove bit position from where piece was and add where it is now
		allPosBitBoard[pieceMoving.getSide().ordinal()] ^= bitMove;

		// remove old hash from where piece was
		hashCode ^= rngTable.getPiecePerSquareRandom(turn, pieceMoving.getPieceID(), fromRow, fromCol);

		// tell piece its new position
		pieceMoving.setPos(toRow, toCol);
		pieceMoving.setMoved(true);
		// update board to reflect piece's new position
		board[toRow][toCol] = pieceMoving;
		// remove pieces old position
		board[fromRow][fromCol] = null;

		if (pieceMoving.getPieceID() == PieceID.PAWN) {
			pawnPosBitBoard[pieceMoving.getSide().ordinal()] ^= bitMove;
		}

		if (pieceMoving.getPieceID() == PieceID.KING) {
			kingPosBitBoard[pieceMoving.getSide().ordinal()] ^= bitMove;
		}

		if (note == MoveNote.NEW_QUEEN) {
			pieceMoving.setPieceID(PieceID.QUEEN);
			pawnPosBitBoard[pieceMoving.getSide().ordinal()] ^= pieceMoving.getBit();
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

			pieceTaken = piecesTaken[turn.otherSide().ordinal()].pop();
			pieces[turn.otherSide().ordinal()].add(pieceTaken);

			// add piece taken to position bit board
			allPosBitBoard[pieceTaken.getSide().ordinal()] |= pieceTaken.getBit();

			if (pieceTaken.getPieceID() == PieceID.PAWN) {
				pawnPosBitBoard[pieceTaken.getSide().ordinal()] |= pieceTaken.getBit();
				;
			}

			if (pieceTaken.getPieceID() == PieceID.KING) {
				kingPosBitBoard[pieceTaken.getSide().ordinal()] |= pieceTaken.getBit();
				;
			}

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

		if (!castleRightsHistory.empty()) {
			castleRights = castleRightsHistory.pop();
		}

		// verifyBitBoards();

		return lastMove;

	}

	private void undoMovePiece(int fromRow, int fromCol, int toRow, int toCol, MoveNote note, boolean hadMoved) {

		Piece pieceMoving = board[toRow][toCol];

		long bitMove = BitBoard.getMask(fromRow, fromCol) | BitBoard.getMask(toRow, toCol);

		// remove bit position from where piece was and add where it is now
		allPosBitBoard[pieceMoving.getSide().ordinal()] ^= bitMove;

		if (pieceMoving.getPieceID() == PieceID.PAWN) {
			pawnPosBitBoard[pieceMoving.getSide().ordinal()] ^= bitMove;
		}

		if (pieceMoving.getPieceID() == PieceID.KING) {
			kingPosBitBoard[pieceMoving.getSide().ordinal()] ^= bitMove;
		}

		// tell piece where it was
		pieceMoving.setPos(fromRow, fromCol);

		// put piece in old position
		board[fromRow][fromCol] = pieceMoving;
		// remove old position
		board[toRow][toCol] = null;

		// show whether piece had moved before this move was made
		board[fromRow][fromCol].setMoved(hadMoved);

		if (note == MoveNote.NEW_QUEEN) {
			pieceMoving.setPieceID(PieceID.PAWN);
			pawnPosBitBoard[pieceMoving.getSide().ordinal()] |= pieceMoving.getBit();
		}

	}

	private void verifyBitBoards() {

		Piece piece;

		long[] kingBitBoard = new long[2];
		kingBitBoard[0] = kingPosBitBoard[0];
		kingBitBoard[1] = kingPosBitBoard[1];

		long[] pawnBitBoard = new long[2];
		pawnBitBoard[0] = pawnPosBitBoard[0];
		pawnBitBoard[1] = pawnPosBitBoard[1];

		long[] allBitBoard = new long[2];
		allBitBoard[0] = allPosBitBoard[0];
		allBitBoard[1] = allPosBitBoard[1];

		for (int r = 0; r < 8; r++) {
			for (int c = 0; c < 8; c++) {
				piece = board[r][c];

				if (piece != null) {
					allBitBoard[piece.getSide().ordinal()] ^= BitBoard.getMask(r, c);

					if (piece.getPieceID() == PieceID.PAWN) {
						pawnBitBoard[piece.getSide().ordinal()] ^= BitBoard.getMask(r, c);
					}

					if (piece.getPieceID() == PieceID.KING) {
						kingBitBoard[piece.getSide().ordinal()] ^= BitBoard.getMask(r, c);
					}

				}

			}
		}

		if (kingBitBoard[0] != 0 || kingBitBoard[1] != 0 || pawnBitBoard[0] != 0 || pawnBitBoard[1] != 0 || allBitBoard[0] != 0 || allBitBoard[1] != 0) {
			System.out.println("BitBoard Problem!!!");
		}
	}

	public boolean canUndo() {
		return (moveHistory.size() != 0);
	}

	private boolean checkPosBitBoard() {

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

		if (pos[Side.WHITE.ordinal()] != allPosBitBoard[Side.WHITE.ordinal()]) {
			match = false;
			FileIO.log("White error");
		}

		if (pos[Side.BLACK.ordinal()] != allPosBitBoard[Side.BLACK.ordinal()]) {
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
		ArrayList<Long> moves;
		Long move;
		for (int p = 0; p < pieces[turn.ordinal()].size(); p++) {

			moves = pieces[turn.ordinal()].get(p).generateValidMoves(this, nullMoveInfo, allPosBitBoard);

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

		for (int p = 0; p < pieces[turn.ordinal()].size(); p++) {
			pieces[turn.ordinal()].get(p).clearBlocking();
		}

		for (int p = 0; p < pieces[turn.otherSide().ordinal()].size(); p++) {
			pieces[turn.otherSide().ordinal()].get(p).getNullMoveInfo(this, nullMoveInfo);
		}

		if ((kings[turn.ordinal()].getBit() & nullMoveInfo[0]) != 0) {
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
		
		if (((row | col) & (~0x7)) != 0){
			return PositionStatus.OFF_BOARD;
		}
		
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
		return Values.getOpeningPieceValue(board[row][col].getPieceID()) + getOpeningPositionValue(board[row][col]);
	}

	private int getOpeningPositionValue(Piece piece) {

		if (piece == null) {
			return 0;
		}

		int value;
		Side player = piece.getSide();
		int row = piece.getRow();
		int col = piece.getCol();

		switch (piece.getPieceID()) {
		case KNIGHT:
			value = PositionBonus.getKnightPositionBonus(row, col, player);
			break;
		case PAWN:
			value = PositionBonus.getOpeningPawnPositionBonus(row, col, player);
			break;
		case BISHOP:
			value = 0;
			break;
		case KING:
			value = 0;
			value = PositionBonus.getKingOpeningPositionBonus(row, col, player);
			break;
		case QUEEN:
			value = 0;
			break;
		case ROOK:
			value = 0;
			break;
		default:
			value = 0;
			System.out.println("Error: invalid piece value request!");
		}

		return value;

	}

	private int getEndGamePositionValue(Piece piece) {
		int value;
		Side player = piece.getSide();
		int row = piece.getRow();
		int col = piece.getCol();

		switch (piece.getPieceID()) {
		case KNIGHT:
			value = PositionBonus.getKnightPositionBonus(row, col, player);
			break;
		case PAWN:
			value = PositionBonus.getEndGamePawnPositionBonus(row, col, player);
			break;
		case BISHOP:
			value = 0;
			break;
		case KING:
			value = PositionBonus.getKingEndGamePositionBonus(row, col, player);
			break;
		case QUEEN:
			value = 0;
			break;
		case ROOK:
			value = 0;
			break;
		default:
			value = 0;
			System.out.println("Error: invalid piece value request!");
		}

		return value;

	}

	private int openingPositionScore(Side side) {
		int score = 0;

		for (int i = 0; i < pieces[side.ordinal()].size(); i++) {
			score += getOpeningPositionValue(pieces[side.ordinal()].get(i));
		}

		return score;
	}

	private int endGamePositionScore(Side side) {
		int score = 0;

		for (int i = 0; i < pieces[side.ordinal()].size(); i++) {
			score += getEndGamePositionValue(pieces[side.ordinal()].get(i));
		}

		return score;
	}

	private int openingMaterialScore(Side side) {
		int score = 0;

		for (int i = 0; i < pieces[side.ordinal()].size(); i++) {
			score += Values.getOpeningPieceValue(pieces[side.ordinal()].get(i).getPieceID());
		}

		return score;
	}

	private int endGameMaterialScore(Side side) {
		int score = 0;

		for (int i = 0; i < pieces[side.ordinal()].size(); i++) {
			score += Values.getEndGamePieceValue(pieces[side.ordinal()].get(i).getPieceID());
		}

		return score;
	}

	private int castleScore(Side side) {
		int score = 0;
		int castleRights = 0;

		if (getCastleRights(side) == 0x4) {
			score += Values.CASTLE_VALUE;
		} else {
			castleRights = calculateCastleRights(side);
			if (castleRights == 0x2) {
				score -= Values.CASTLE_ABILITY_LOST_VALUE;
			}

			if (castleRights == 0x1) {
				score -= Values.CASTLE_ABILITY_LOST_VALUE;
			}

			if (castleRights == 0) {
				score -= Values.CASTLE_VALUE;
			}
		}

		return score;
	}

	private int pawnStructureScore(Side side, int phase) {

		long pawns = pawnPosBitBoard[side.ordinal()];

		int backedPawns;

		if (side == Side.WHITE) {
			backedPawns = BitBoard.bitCountLong(pawns & (pawns >> 7)) + BitBoard.bitCountLong(pawns & (pawns >> 9));
		} else {
			backedPawns = BitBoard.bitCountLong(pawns & (pawns << 7)) + BitBoard.bitCountLong(pawns & (pawns << 9));
		}

		int occupiedCol = 0;
		for (int c = 0; c < 8; c++) {
			if ((BitBoard.getColMask(c) & pawns) != 0) {
				occupiedCol++;
			}
		}

		int doubledPawns = BitBoard.bitCountLong(pawns) - occupiedCol;

		for (int i = 0; i < pieces[side.ordinal()].size(); i++) {

		}

		return backedPawns * Values.BACKED_PAWN_BONUS + doubledPawns * Values.DOUBLED_PAWN_BONUS;
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

		int openingMyScore = openingMaterialScore(turn);
		int openingYourScore = openingMaterialScore(turn.otherSide());

		openingMyScore += castleScore(turn) + openingPositionScore(turn);
		openingYourScore += castleScore(turn.otherSide()) + openingPositionScore(turn.otherSide());

		int endGameMyScore = endGameMaterialScore(turn);
		int endGameYourScore = endGameMaterialScore(turn.otherSide());

		endGameMyScore += endGamePositionScore(turn);
		endGameYourScore += endGamePositionScore(turn.otherSide());

		int myScore = (((openingMyScore * (256 - phase)) + (endGameMyScore * phase)) / 256) + myPawnScore;
		int yourScore = (((openingYourScore * (256 - phase)) + (endGameYourScore * phase)) / 256) + yourPawnScore;

		ptDiff = myScore - yourScore;

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

	// private Piece getKing(Side side) {
	// if (side == Side.BLACK) {
	// return blackKing;
	// } else {
	// return whiteKing;
	// }
	// }

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

	// public ArrayList<Piece> getPiecesFor(Side player) {
	//
	// }

	public Stack<Piece> getPiecesTakenFor(Side player) {
		return piecesTaken[player.ordinal()];
	}

	public void placePiece(Piece piece, int toRow, int toCol) {

		if (piece.getRow() >= 0) {
			// remove where piece was if it was on board
			allPosBitBoard[piece.getSide().ordinal()] ^= piece.getBit();
			board[piece.getRow()][piece.getCol()] = null;

			if (piece.getPieceID() == PieceID.PAWN) {
				pawnPosBitBoard[piece.getSide().ordinal()] ^= piece.getBit();
			}

			if (piece.getPieceID() == PieceID.KING) {
				kingPosBitBoard[piece.getSide().ordinal()] ^= piece.getBit();
			}

		} else {

			pieces[piece.getSide().ordinal()].add(piece);
		}

		if (toRow >= 0) {
			// remove where piece taken was
			if (board[toRow][toCol] != null) {

				Piece pieceTaken = board[toRow][toCol];

				// remove bit position of piece taken
				allPosBitBoard[pieceTaken.getSide().ordinal()] ^= pieceTaken.getBit();

				if (pieceTaken.getPieceID() == PieceID.PAWN) {
					pawnPosBitBoard[pieceTaken.getSide().ordinal()] ^= pieceTaken.getBit();
				}

				if (pieceTaken.getPieceID() == PieceID.KING) {
					kingPosBitBoard[pieceTaken.getSide().ordinal()] ^= pieceTaken.getBit();
				}

				// remove ref to piece taken
				pieces[pieceTaken.getSide().ordinal()].remove(pieceTaken);
			}

			// tell piece where it is now
			piece.setPos(toRow, toCol);

			// reflect new piece in position bitboard
			allPosBitBoard[piece.getSide().ordinal()] |= piece.getBit();

			if (piece.getPieceID() == PieceID.PAWN) {
				pawnPosBitBoard[piece.getSide().ordinal()] |= piece.getBit();
			}

			if (piece.getPieceID() == PieceID.KING) {
				kingPosBitBoard[piece.getSide().ordinal()] |= piece.getBit();
			}

			// update board ref to show piece there
			board[toRow][toCol] = piece;
		} else {
			// piece is being taken off the board. Remove
			pieces[piece.getSide().ordinal()].remove(piece);
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

	public boolean isInvalid() {
		return (boardStatus == GameStatus.INVALID);
	}

	public boolean isGameOver() {
		return (isInCheckMate() || isInStaleMate() || isTimeUp() || isDraw() || isInvalid());
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
			if (!farRookHasMoved(side)) {
				rights |= 0x2;
			}

			if (!nearRookHasMoved(side)) {
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

	public int getCastleRights(Side side) {
		if (side == Side.BLACK) {
			return (castleRights >> 3);
		} else {
			return (castleRights & 0x7);
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
		return kings[player.ordinal()].hasMoved();
	}

	public Board getCopy() {
		return new Board(pieces, this.turn, moveHistory, castleRightsHistory, castleRights);
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
		int fiftyMove = 0;

		for (int i = hashCodeHistory.size() - 1; i >= 0; i--) {

			if (fiftyMove >= 101) {
				return true;
			}

			if (hashCode == hashCodeHistory.elementAt(i)) {
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
					if ((pieces[i].get(p).getPieceID() == PieceID.PAWN) || (pieces[i].get(p).getPieceID() == PieceID.QUEEN) || (pieces[i].get(p).getPieceID() == PieceID.ROOK)) {
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
