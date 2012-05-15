package chessPieces;

import java.util.Vector;

import chessBackend.BitBoard;
import chessBackend.Board;
import chessBackend.Side;
import chessBackend.Move;

public class Knight{
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

	public static Vector<Move> generateValidMoves(Piece p, Board board, long[] nullMoveInfo, long[] posBitBoard) {
		Vector<Move> validMoves = new Vector<Move>();
		int currentRow = p.getRow();
		int currentCol = p.getCol();
		int nextRow;
		int nextCol;
		int bonus;
		PositionStatus pieceStatus;
		Side player = p.getSide();
		Move move;

		for (int i = 0; i < 8; i++) {
			nextRow = currentRow + KNIGHTMOVES[0][i];
			nextCol = currentCol + KNIGHTMOVES[1][i];
			pieceStatus = board.checkPiece(nextRow, nextCol, player);

			if (pieceStatus != PositionStatus.OFF_BOARD) {
				bonus = PositionBonus.getKnightMoveBonus(currentRow, currentCol, nextRow, nextCol, p.getSide());

				if (pieceStatus == PositionStatus.NO_PIECE) {
					if (p.isValidMove(nextRow, nextCol, nullMoveInfo)) {
						move = new Move(currentRow, currentCol, nextRow, nextCol, bonus);
						validMoves.add(move);
					}
				}

				if (pieceStatus == PositionStatus.ENEMY) {
					if (p.isValidMove(nextRow, nextCol, nullMoveInfo)) {
						int pieceValue = board.getPieceValue(nextRow, nextCol);
						move = new Move(currentRow, currentCol, nextRow, nextCol, pieceValue + bonus);
						move.setPieceTaken(board.getPiece(nextRow, nextCol));
						validMoves.add(move);
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
