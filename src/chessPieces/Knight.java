package chessPieces;

import java.util.ArrayList;

import chessBackend.BitBoard;
import chessBackend.Board;
import chessBackend.MoveNote;
import chessBackend.Side;
import chessBackend.Move;

public class Knight {
	private static int[][] KNIGHTMOVES = { { 2, 2, -2, -2, 1, -1, 1, -1 }, { 1, -1, 1, -1, 2, 2, -2, -2 } };

	public Knight() {
	}

	public static PieceID getPieceID() {
		return PieceID.KNIGHT;
	}

	public static String getName() {
		return "Knight";
	}

	public static String getStringID() {
		return "N";
	}

	public static ArrayList<Long> generateValidMoves(Piece p, Board board, long[] nullMoveInfo, long[] posBitBoard) {
		ArrayList<Long> validMoves = new ArrayList<Long>();
		int currentRow = p.getRow();
		int currentCol = p.getCol();
		int nextRow;
		int nextCol;
		int value;
		int bonus;
		int myValue = board.getPieceValue(p.getRow(), p.getCol());
		PositionStatus pieceStatus;
		Side player = p.getSide();
		Long moveLong;

		for (int i = 0; i < 8; i++) {
			nextRow = currentRow + KNIGHTMOVES[0][i];
			nextCol = currentCol + KNIGHTMOVES[1][i];
			pieceStatus = board.checkPiece(nextRow, nextCol, player);

			if (pieceStatus != PositionStatus.OFF_BOARD) {
				bonus = PositionBonus.getKnightMoveBonus(currentRow, currentCol, nextRow, nextCol, p.getSide());

				if (pieceStatus == PositionStatus.NO_PIECE) {
					if (p.isValidMove(nextRow, nextCol, nullMoveInfo)) {

						if ((nullMoveInfo[0] & BitBoard.getMask(nextRow, nextCol)) != 0) {
							value = -myValue >> 1;
						} else {
							value = bonus;
						}

						moveLong = Move.moveLong(currentRow, currentCol, nextRow, nextCol, value);
						validMoves.add(moveLong);
					}
				}

				if (pieceStatus == PositionStatus.ENEMY) {
					if (p.isValidMove(nextRow, nextCol, nullMoveInfo)) {
						value = board.getPieceValue(nextRow, nextCol);

						if ((nullMoveInfo[0] & BitBoard.getMask(nextRow, nextCol)) != 0) {
							value -= myValue >> 1;
						} else {
							value += bonus;
						}

						moveLong = Move.moveLong(currentRow, currentCol, nextRow, nextCol, value, MoveNote.NONE, board.getPiece(nextRow, nextCol));
						validMoves.add(moveLong);
					}
				}

			}
		}

		return validMoves;

	}

	public static void getNullMoveInfo(Piece p, Board board, long[] nullMoveInfo) {

		int currentRow = p.getRow();
		int currentCol = p.getCol();
		int nextRow;
		int nextCol;
		PositionStatus pieceStatus;

		for (int i = 0; i < 8; i++) {
			nextRow = currentRow + KNIGHTMOVES[0][i];
			nextCol = currentCol + KNIGHTMOVES[1][i];

			pieceStatus = board.checkPiece(nextRow, nextCol, p.getSide());

			if (pieceStatus != PositionStatus.OFF_BOARD) {

				if (board.getPieceID(nextRow, nextCol) == PieceID.KING && pieceStatus == PositionStatus.ENEMY) {
					nullMoveInfo[1] &= p.getBit();
				}

				nullMoveInfo[0] |= BitBoard.getMask(nextRow, nextCol);
			}
		}

	}

}
