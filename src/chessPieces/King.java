package chessPieces;

import java.util.ArrayList;

import chessBackend.BitBoard;
import chessBackend.Board;
import chessBackend.MoveNote;
import chessBackend.Side;
import chessBackend.Move;

public class King {
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

	public static ArrayList<Long> generateValidMoves(Piece p, Board board, long[] nullMoveInfo, long[] posBitBoard, ArrayList<Long> validMoves) {
		int currentRow = p.getRow();
		int currentCol = p.getCol();
		Side player = p.getSide();
		int nextRow;
		int nextCol;
		PositionStatus pieceStatus;
		Long moveLong;

		for (int d = 0; d < 8; d++) {
			nextRow = currentRow + KINGMOVES[0][d];
			nextCol = currentCol + KINGMOVES[1][d];
			pieceStatus = board.checkPiece(nextRow, nextCol, player);

			if (pieceStatus == PositionStatus.NO_PIECE) {

				if (isValidMove(nextRow, nextCol, nullMoveInfo)) {
					if (!p.hasMoved() && (!board.farRookHasMoved(player) || !board.nearRookHasMoved(player))) {
						// The player loses points for losing the ability to
						// castle
						moveLong = Move.moveLong(currentRow, currentCol, nextRow, nextCol, Values.CASTLE_ABILITY_LOST_VALUE);
					} else {
						moveLong = Move.moveLong(currentRow, currentCol, nextRow, nextCol, 0, MoveNote.NONE);
					}

					validMoves.add(moveLong);
				}
			}

			if (pieceStatus == PositionStatus.ENEMY) {
				if (isValidMove(nextRow, nextCol, nullMoveInfo)) {
					moveLong = Move.moveLong(currentRow, currentCol, nextRow, nextCol, board.getPieceValue(nextRow, nextCol), MoveNote.NONE, board.getPiece(nextRow, nextCol));
					validMoves.add(moveLong);
				}
			}

		}

		long allPosBitBoard = posBitBoard[0] | posBitBoard[1];

		if (!board.isInCheck()) {
			// add possible castle move
			if (canCastleFar(p, board, player, nullMoveInfo, allPosBitBoard)) {
				if (isValidMove(currentRow, 2, nullMoveInfo)) {
					if (currentCol > 3) {
						validMoves.add(Move.moveLong(currentRow, currentCol, currentRow, 2, Values.FAR_CASTLE_VALUE, MoveNote.CASTLE_FAR));
					} else {
						validMoves.add(Move.moveLong(currentRow, board.getRookStartingCol(player, 0), currentRow, 3, Values.FAR_CASTLE_VALUE, MoveNote.CASTLE_FAR));
					}
				}
			}

			if (canCastleNear(p, board, player, nullMoveInfo, allPosBitBoard)) {
				if (isValidMove(currentRow, 6, nullMoveInfo)) {
					if (currentCol < 5) {
						validMoves.add(Move.moveLong(currentRow, currentCol, currentRow, 6, Values.NEAR_CASTLE_VALUE, MoveNote.CASTLE_NEAR));
					} else {
						validMoves.add(Move.moveLong(currentRow, board.getRookStartingCol(player, 1), currentRow, 5, Values.NEAR_CASTLE_VALUE, MoveNote.CASTLE_NEAR));
					}
				}
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

		// String nullmove0 = BitBoard.printBitBoard(nullMoveInfo[0]);
		// String nullmove1 = BitBoard.printBitBoard(nullMoveInfo[1]);
		// String nullmove2 = BitBoard.printBitBoard(nullMoveInfo[2]);

		if ((mask & (nullMoveInfo[0] | nullMoveInfo[2])) == 0) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean canCastleFar(Piece king, Board board, Side player, long[] nullMoveInfo, long allPosBitBoard) {

		if (board.kingHasMoved(player) || board.farRookHasMoved(player)) {
			return false;
		}

		long kingToCastleMask = BitBoard.getCastleMask(king.getCol(), 2, player);

		int rookCol = board.getRookStartingCol(player, 0);
		long rookToCastleMask = BitBoard.getCastleMask(rookCol, 3, player);

		allPosBitBoard ^= BitBoard.getMask(king.getRow(), rookCol) | king.getBit();

		if ((kingToCastleMask & nullMoveInfo[0]) == 0) {
			if (((kingToCastleMask | rookToCastleMask) & allPosBitBoard) == 0) {
				return true;
			}
		}

		return false;

	}

	public static boolean canCastleNear(Piece king, Board board, Side player, long[] nullMoveInfo, long allPosBitBoard) {

		if (board.kingHasMoved(player) || board.nearRookHasMoved(player)) {
			return false;
		}

		long kingToCastleMask = BitBoard.getCastleMask(king.getCol(), 6, player);

		int rookCol = board.getRookStartingCol(player, 1);
		long rookToCastleMask = BitBoard.getCastleMask(rookCol, 5, player);

		allPosBitBoard ^= BitBoard.getMask(king.getRow(), rookCol) | king.getBit();

		// System.out.println(BitBoard.printBitBoard(kingToCastleMask));
		// System.out.println(BitBoard.printBitBoard(rookToCastleMask));
		// System.out.println(BitBoard.printBitBoard(allPosBitBoard));

		if ((kingToCastleMask & nullMoveInfo[0]) == 0) {
			if (((kingToCastleMask | rookToCastleMask) & allPosBitBoard) == 0) {
				return true;
			}
		}

		return false;
	}

}
