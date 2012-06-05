package chessPieces;

import java.util.ArrayList;

import chessBackend.BitBoard;
import chessBackend.Board;
import chessBackend.MoveNote;
import chessBackend.Side;
import chessBackend.Move;

public class Rook {
	private static int[][] ROOKMOVES = { { 1, -1, 0, 0 }, { 0, 0, 1, -1 } };

	public Rook() {
	}

	public static PieceID getPieceID() {
		return PieceID.ROOK;
	}

	public static String getName() {
		return "Rook";
	}

	public static String getStringID() {
		return "R";
	}

	public static ArrayList<Long> generateValidMoves(Piece p, Board board, long[] nullMoveInfo, long[] posBitBoard) {
		ArrayList<Long> validMoves = new ArrayList<Long>();
		int currentRow = p.getRow();
		int currentCol = p.getCol();
		Side player = p.getSide();
		int nextRow;
		int nextCol;
		int value;
		PositionStatus pieceStatus;
		Long moveLong;

		int i = 1;
		for (int d = 0; d < 4; d++) {
			nextRow = currentRow + i * ROOKMOVES[0][d];
			nextCol = currentCol + i * ROOKMOVES[1][d];
			pieceStatus = board.checkPiece(nextRow, nextCol, player);

			while (pieceStatus == PositionStatus.NO_PIECE) {

				if (p.isValidMove(nextRow, nextCol, nullMoveInfo)) {

					if (!p.hasMoved() && !board.kingHasMoved(player)) {
						value = Values.CASTLE_ABILITY_LOST_VALUE;
					} else {
						value = 0;
					}

					if ((nullMoveInfo[0] & BitBoard.getMask(nextRow, nextCol)) != 0) {
						value -= Values.ROOK_VALUE >> 1;
					}

					moveLong = Move.moveLong(currentRow, currentCol, nextRow, nextCol, value);

					validMoves.add(moveLong);
				}

				i++;
				nextRow = currentRow + i * ROOKMOVES[0][d];
				nextCol = currentCol + i * ROOKMOVES[1][d];
				pieceStatus = board.checkPiece(nextRow, nextCol, player);

			}

			if (pieceStatus == PositionStatus.ENEMY) {
				if (p.isValidMove(nextRow, nextCol, nullMoveInfo)) {

					if (!p.hasMoved() && !board.kingHasMoved(player)) {
						value = board.getPieceValue(nextRow, nextCol) + Values.CASTLE_ABILITY_LOST_VALUE;
					} else {
						value = board.getPieceValue(nextRow, nextCol);
					}

					if ((nullMoveInfo[0] & BitBoard.getMask(nextRow, nextCol)) != 0) {
						value -= Values.ROOK_VALUE >> 1;
					}

					moveLong = Move.moveLong(currentRow, currentCol, nextRow, nextCol, value, MoveNote.NONE, board.getPiece(nextRow, nextCol));

					validMoves.add(moveLong);
				}
			}

			i = 1;
		}

		return validMoves;

	}

	public static void getNullMoveInfo(Piece p, Board board, long[] nullMoveInfo) {
		long bitAttackVector = 0;
		long bitAttackCompliment = 0;
		boolean inCheck = false;
		Piece blockingPiece;

		int currentRow = p.getRow();
		int currentCol = p.getCol();
		int nextRow;
		int nextCol;
		PositionStatus pieceStatus;
		Side player = p.getSide();

		long bitPosition = p.getBit();

		int i = 1;
		for (int d = 0; d < 4; d++) {
			nextRow = currentRow + i * ROOKMOVES[0][d];
			nextCol = currentCol + i * ROOKMOVES[1][d];
			pieceStatus = board.checkPiece(nextRow, nextCol, player);

			if (pieceStatus == PositionStatus.OFF_BOARD) {
				continue;
			}

			while (pieceStatus == PositionStatus.NO_PIECE) {
				bitAttackVector |= BitBoard.getMask(nextRow, nextCol);
				i++;
				nextRow = currentRow + i * ROOKMOVES[0][d];
				nextCol = currentCol + i * ROOKMOVES[1][d];
				pieceStatus = board.checkPiece(nextRow, nextCol, player);
			}

			if (pieceStatus != PositionStatus.OFF_BOARD) {
				bitAttackVector |= BitBoard.getMask(nextRow, nextCol);
			}

			if (pieceStatus == PositionStatus.ENEMY) {
				blockingPiece = board.getPiece(nextRow, nextCol);

				if (blockingPiece.getPieceID() == PieceID.KING) {
					nullMoveInfo[1] &= (bitAttackVector | bitPosition);
					inCheck = true;
				}

				i++;
				nextRow = currentRow + i * ROOKMOVES[0][d];
				nextCol = currentCol + i * ROOKMOVES[1][d];
				pieceStatus = board.checkPiece(nextRow, nextCol, player);

				while (pieceStatus == PositionStatus.NO_PIECE) {
					bitAttackCompliment |= BitBoard.getMask(nextRow, nextCol);
					i++;
					nextRow = currentRow + i * ROOKMOVES[0][d];
					nextCol = currentCol + i * ROOKMOVES[1][d];
					pieceStatus = board.checkPiece(nextRow, nextCol, player);
				}

				if (pieceStatus != PositionStatus.OFF_BOARD) {
					if (board.getPieceID(nextRow, nextCol) == PieceID.KING && board.getPiece(nextRow, nextCol).getSide() != player) {
						blockingPiece.setBlockingVector(bitAttackCompliment | bitAttackVector | bitPosition);
					}
				}

				if (pieceStatus == PositionStatus.FRIEND) {
					bitAttackCompliment |= BitBoard.getMask(nextRow, nextCol);
				}

			}

			nullMoveInfo[0] |= bitAttackVector;

			if (inCheck) {
				nullMoveInfo[2] |= bitAttackCompliment;
			}

			bitAttackCompliment = 0;
			bitAttackVector = 0;

			i = 1;
		}

	}

}
