package chessPieces;

import java.util.Vector;

import chessBackend.BitBoard;
import chessBackend.Board;
import chessBackend.MoveNote;
import chessBackend.Side;
import chessBackend.Move;

public class King{
	private static int[][] KINGMOVES = { { 1, 1, -1, -1, 1, -1, 0, 0 }, { 1, -1, 1, -1, 0, 0, 1, -1 } };

	public King() {
	}

	public static PieceID getPieceID() {
		return PieceID.KING;
	}

	public static String getName() {
		return "King";
	}

	public static String getStringID() {
		return "K";
	}

	public static Vector<Move> generateValidMoves(Piece p, Board board, long[] nullMoveInfo, long[] posBitBoard) {
		Vector<Move> validMoves = new Vector<Move>();
		int currentRow = p.getRow();
		int currentCol = p.getCol();
		Side player = p.getSide();
		int nextRow;
		int nextCol;
		PositionStatus pieceStatus;
		Move move;

		for (int d = 0; d < 8; d++) {
			nextRow = currentRow + KINGMOVES[0][d];
			nextCol = currentCol + KINGMOVES[1][d];
			pieceStatus = board.checkPiece(nextRow, nextCol, player);

			if (pieceStatus == PositionStatus.NO_PIECE) {

				if (isValidMove(nextRow, nextCol, nullMoveInfo)) {
					if (!p.hasMoved() && (!board.farRookHasMoved(player) || !board.nearRookHasMoved(player))) {
						// The player loses points for losing the ability to
						// castle
						move = new Move(currentRow, currentCol, nextRow, nextCol, Values.CASTLE_ABILITY_LOST_VALUE);
						validMoves.add(move);
					} else {
						move = new Move(currentRow, currentCol, nextRow, nextCol, 0, MoveNote.NONE);
						validMoves.add(move);
					}
				}
			}

			if (pieceStatus == PositionStatus.ENEMY) {
				if (isValidMove(nextRow, nextCol, nullMoveInfo)) {
					move = new Move(currentRow, currentCol, nextRow, nextCol, board.getPieceValue(nextRow, nextCol));
					move.setPieceTaken(board.getPiece(nextRow, nextCol));
					validMoves.add(move);
				}
			}

		}

		long allPosBitBoard = posBitBoard[0] | posBitBoard[1];

		// add possible castle move
		if (canCastleFar(board, player, nullMoveInfo, allPosBitBoard) && !board.isInCheck()) {
			if (isValidMove(currentRow, currentCol - 2, nullMoveInfo)) {
				validMoves.add(new Move(currentRow, currentCol, currentRow, currentCol - 2, Values.CASTLE_VALUE, MoveNote.CASTLE_FAR));
			} else {
			}
		}

		if (canCastleNear(board, player, nullMoveInfo, allPosBitBoard) && !board.isInCheck()) {
			if (isValidMove(currentRow, currentCol + 2, nullMoveInfo)) {
				validMoves.add(new Move(currentRow, currentCol, currentRow, currentCol + 2, Values.CASTLE_VALUE, MoveNote.CASTLE_NEAR));
			} else {
			}
		}

		return validMoves;

	}

	public static void getNullMoveInfo(Piece p, Board board, long[] nullMoveInfo) {

		int currentRow = p.getRow();
		int currentCol = p.getCol();

		for (int i = 0; i < 8; i++) {
			if (board.checkPiece(currentRow + KINGMOVES[0][i], currentCol + KINGMOVES[1][i], p.getSide()) != PositionStatus.OFF_BOARD) {
				nullMoveInfo[0] |= BitBoard.getMask(currentRow + KINGMOVES[0][i], currentCol + KINGMOVES[1][i]);
			}
		}

	}

	public static boolean isValidMove(int toRow, int toCol, long[] nullMoveInfo) {
		long mask = BitBoard.getMask(toRow, toCol);

		if ((mask & (nullMoveInfo[0] | nullMoveInfo[2])) == 0) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean canCastleFar(Board board, Side player, long[] nullMoveInfo, long allPosBitBoard) {

		if (board.kingHasMoved(player) || board.farRookHasMoved(player)) {
			return false;
		}

		long posClearMask;
		long checkFar;
		if (player == Side.BLACK) {
			posClearMask = BitBoard.BLACK_CASTLE_FAR;
			checkFar = BitBoard.BLACK_CHECK_FAR;
		} else {
			posClearMask = BitBoard.WHITE_CASTLE_FAR;
			checkFar = BitBoard.WHITE_CHECK_FAR;
		}

		if ((posClearMask & allPosBitBoard) == 0) {
			if ((checkFar & nullMoveInfo[0]) == 0) {
				return true;
			}
		}

		return false;

	}

	public static boolean canCastleNear(Board board, Side player, long[] nullMoveInfo, long allPosBitBoard) {

		if (board.kingHasMoved(player) || board.nearRookHasMoved(player)) {
			return false;
		}

		long posClearMask;
		long checkNear;
		if (player == Side.BLACK) {
			posClearMask = BitBoard.BLACK_CASTLE_NEAR;
			checkNear = BitBoard.BLACK_CHECK_NEAR;
		} else {
			posClearMask = BitBoard.WHITE_CASTLE_NEAR;
			checkNear = BitBoard.WHITE_CHECK_NEAR;
		}

		if ((posClearMask & allPosBitBoard) == 0) {
			if ((checkNear & nullMoveInfo[0]) == 0) {
				return true;
			}
		}

		return false;
	}

}
