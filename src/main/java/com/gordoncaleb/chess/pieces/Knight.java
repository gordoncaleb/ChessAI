package com.gordoncaleb.chess.pieces;

import java.util.ArrayList;

import com.gordoncaleb.chess.backend.BitBoard;
import com.gordoncaleb.chess.backend.Board;
import com.gordoncaleb.chess.backend.MoveNote;
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

	public static ArrayList<Long> generateValidMoves(Piece p, Board board, long[] nullMoveInfo, long[] posBitBoard, ArrayList<Long> validMoves) {
		int currentRow = p.getRow();
		int currentCol = p.getCol();
		int nextRow;
		int nextCol;
		int value;
		int bonus;
		int myValue = board.getPieceValue(p.getRow(), p.getCol());
		Piece.PositionStatus pieceStatus;
		Side player = p.getSide();
		Long moveLong;

		for (int i = 0; i < 8; i++) {
			nextRow = currentRow + KNIGHTMOVES[0][i];
			nextCol = currentCol + KNIGHTMOVES[1][i];
			pieceStatus = board.checkPiece(nextRow, nextCol, player);

			if (pieceStatus != Piece.PositionStatus.OFF_BOARD) {
				bonus = PositionBonus.getKnightMoveBonus(currentRow, currentCol, nextRow, nextCol, p.getSide());

				if (pieceStatus == Piece.PositionStatus.NO_PIECE) {
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

				if (pieceStatus == Piece.PositionStatus.ENEMY) {
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
		Piece.PositionStatus pieceStatus;

		for (int i = 0; i < 8; i++) {
			nextRow = currentRow + KNIGHTMOVES[0][i];
			nextCol = currentCol + KNIGHTMOVES[1][i];

			pieceStatus = board.checkPiece(nextRow, nextCol, p.getSide());

			if (pieceStatus != Piece.PositionStatus.OFF_BOARD) {

				if (board.getPieceID(nextRow, nextCol) == Piece.PieceID.KING && pieceStatus == Piece.PositionStatus.ENEMY) {
					nullMoveInfo[1] &= p.getBit();
				}

				nullMoveInfo[0] |= BitBoard.getMask(nextRow, nextCol);
			}
		}

	}

}
