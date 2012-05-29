package chessPieces;

import java.util.ArrayList;

import chessBackend.BitBoard;
import chessBackend.Board;
import chessBackend.MoveNote;
import chessBackend.Side;
import chessBackend.Move;

public class Queen {
	private static int[][] QUEENMOVES = { { 1, 1, -1, -1, 1, -1, 0, 0 }, { 1, -1, 1, -1, 0, 0, 1, -1 } };

	public Queen() {
	}

	public static PieceID getPieceID() {
		return PieceID.QUEEN;
	}

	public static String getName() {
		return "Queen";
	}

	public static String getStringID() {
		return "Q";
	}

	public static ArrayList<Long> generateValidMoves(Piece p, Board board, long[] nullMoveInfo, long[] posBitBoard) {
		ArrayList<Long> validMoves = new ArrayList<Long>();
		int currentRow = p.getRow();
		int currentCol = p.getCol();
		Side player = p.getSide();
		int nextRow;
		int nextCol;
		PositionStatus pieceStatus;

		int i = 1;
		for (int d = 0; d < 8; d++) {
			nextRow = currentRow + i * QUEENMOVES[0][d];
			nextCol = currentCol + i * QUEENMOVES[1][d];
			pieceStatus = board.checkPiece(nextRow, nextCol, player);

			while (pieceStatus == PositionStatus.NO_PIECE) {

				if (p.isValidMove(nextRow, nextCol, nullMoveInfo)) {
					validMoves.add(Move.moveLong(currentRow, currentCol, nextRow, nextCol, Values.RISK_QUEEN, MoveNote.NONE));
				}

				i++;
				nextRow = currentRow + i * QUEENMOVES[0][d];
				nextCol = currentCol + i * QUEENMOVES[1][d];
				pieceStatus = board.checkPiece(nextRow, nextCol, player);

			}

			if (pieceStatus == PositionStatus.ENEMY) {
				if (p.isValidMove(nextRow, nextCol, nullMoveInfo)) {
					Long moveLong = Move.moveLong(currentRow, currentCol, nextRow, nextCol, board.getPieceValue(nextRow, nextCol), MoveNote.NONE,
							board.getPiece(nextRow, nextCol));
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
		for (int d = 0; d < 8; d++) {
			nextRow = currentRow + i * QUEENMOVES[0][d];
			nextCol = currentCol + i * QUEENMOVES[1][d];
			pieceStatus = board.checkPiece(nextRow, nextCol, player);

			if (pieceStatus == PositionStatus.OFF_BOARD) {
				continue;
			}

			while (pieceStatus == PositionStatus.NO_PIECE) {
				bitAttackVector |= BitBoard.getMask(nextRow, nextCol);
				i++;
				nextRow = currentRow + i * QUEENMOVES[0][d];
				nextCol = currentCol + i * QUEENMOVES[1][d];
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
				nextRow = currentRow + i * QUEENMOVES[0][d];
				nextCol = currentCol + i * QUEENMOVES[1][d];
				pieceStatus = board.checkPiece(nextRow, nextCol, player);

				while (pieceStatus == PositionStatus.NO_PIECE) {
					bitAttackCompliment |= BitBoard.getMask(nextRow, nextCol);
					i++;
					nextRow = currentRow + i * QUEENMOVES[0][d];
					nextCol = currentCol + i * QUEENMOVES[1][d];
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
