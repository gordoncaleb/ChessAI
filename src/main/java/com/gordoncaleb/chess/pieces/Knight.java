package com.gordoncaleb.chess.pieces;

import java.util.List;

import com.gordoncaleb.chess.backend.BitBoard;
import com.gordoncaleb.chess.backend.Board;
import com.gordoncaleb.chess.backend.Side;
import com.gordoncaleb.chess.backend.Move;

public class Knight {
	private static int[][] KNIGHTMOVES = { { 2, 2, -2, -2, 1, -1, 1, -1 }, { 1, -1, 1, -1, 2, 2, -2, -2 } };

	public Knight() {
	}

	public static Piece.PieceID getPieceID() {
		return Piece.PieceID.KNIGHT;
	}

	public static String getName() {
		return "Knight";
	}

	public static String getStringID() {
		return "N";
	}

	public static List<Long> generateValidMoves(Piece p, Board board, long[] nullMoveInfo, long[] posBitBoard, List<Long> validMoves) {
		int currentRow = p.getRow();
		int currentCol = p.getCol();
		int nextRow;
		int nextCol;
		Piece.PositionStatus pieceStatus;
		Side player = p.getSide();
		Long moveLong;

		for (int i = 0; i < 8; i++) {
			nextRow = currentRow + KNIGHTMOVES[0][i];
			nextCol = currentCol + KNIGHTMOVES[1][i];
			pieceStatus = board.checkPiece(nextRow, nextCol, player);

			if (pieceStatus != Piece.PositionStatus.OFF_BOARD) {

				if (pieceStatus == Piece.PositionStatus.NO_PIECE) {
					if (p.isValidMove(nextRow, nextCol, nullMoveInfo)) {
						moveLong = Move.moveLong(currentRow, currentCol, nextRow, nextCol, 0);
						validMoves.add(moveLong);
					}
				}

				if (pieceStatus == Piece.PositionStatus.ENEMY) {
					if (p.isValidMove(nextRow, nextCol, nullMoveInfo)) {
						moveLong = Move.moveLong(currentRow, currentCol, nextRow, nextCol, 0, Move.MoveNote.NONE, board.getPiece(nextRow, nextCol));
						validMoves.add(moveLong);
					}
				}

			}
		}

		return validMoves;
	}

}
