package com.gordoncaleb.chess.pieces;

import java.util.List;

import com.gordoncaleb.chess.bitboard.BitBoard;
import com.gordoncaleb.chess.backend.Board;
import com.gordoncaleb.chess.backend.Side;
import com.gordoncaleb.chess.backend.Move;

public class King {
	private static int[][] KINGMOVES = { { 1, 1, -1, -1, 1, -1, 0, 0 }, { 1, -1, 1, -1, 0, 0, 1, -1 } };

	public King() {
	}

	public static String getName() {
		return "King";
	}

	public static String getStringID() {
		return "K";
	}

	public static List<Long> generateValidMoves(Piece p, Board board, long[] nullMoveInfo, long[] posBitBoard, List<Long> validMoves) {
		int currentRow = p.getRow();
		int currentCol = p.getCol();
		Side player = p.getSide();
		int nextRow;
		int nextCol;
		Piece.PositionStatus pieceStatus;
		Long moveLong;

		for (int d = 0; d < 8; d++) {
			nextRow = currentRow + KINGMOVES[0][d];
			nextCol = currentCol + KINGMOVES[1][d];
			pieceStatus = board.checkPiece(nextRow, nextCol, player);

			if (pieceStatus == Piece.PositionStatus.NO_PIECE) {

				if (isValidMove(nextRow, nextCol, nullMoveInfo)) {
					if (!p.hasMoved() && (!board.farRookHasMoved(player) || !board.nearRookHasMoved(player))) {
						// The player loses points for losing the ability to
						// castle
						moveLong = Move.moveLong(currentRow, currentCol, nextRow, nextCol, Values.CASTLE_ABILITY_LOST_VALUE);
					} else {
						moveLong = Move.moveLong(currentRow, currentCol, nextRow, nextCol, 0, Move.MoveNote.NONE);
					}

					validMoves.add(moveLong);
				}
			}

			if (pieceStatus == Piece.PositionStatus.ENEMY) {
				if (isValidMove(nextRow, nextCol, nullMoveInfo)) {
					moveLong = Move.moveLong(currentRow, currentCol, nextRow, nextCol, 0, Move.MoveNote.NONE,
							board.getPiece(nextRow, nextCol));
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
						validMoves.add(Move.moveLong(currentRow, currentCol, currentRow, 2, Values.FAR_CASTLE_VALUE, Move.MoveNote.CASTLE_FAR));
					} else {
						validMoves.add(Move.moveLong(currentRow, board.getRookStartingCol(player, 0), currentRow, 3, Values.FAR_CASTLE_VALUE,
								Move.MoveNote.CASTLE_FAR));
					}
				}
			}

			if (canCastleNear(p, board, player, nullMoveInfo, allPosBitBoard)) {
				if (isValidMove(currentRow, 6, nullMoveInfo)) {
					if (currentCol < 5) {
						validMoves.add(Move.moveLong(currentRow, currentCol, currentRow, 6, Values.NEAR_CASTLE_VALUE, Move.MoveNote.CASTLE_NEAR));
					} else {
						validMoves.add(Move.moveLong(currentRow, board.getRookStartingCol(player, 1), currentRow, 5, Values.NEAR_CASTLE_VALUE,
								Move.MoveNote.CASTLE_NEAR));
					}
				}
			}
		}

		return validMoves;

	}

	public static long getKingCheckVectors(long king, long updown, long left, long right){
		long temp = king;

		long checkVectors = king;

		// up
		while ((temp = (temp >>> 8 & updown)) != 0) {
			checkVectors |= temp;
		}

		temp = king;

		// down
		while ((temp = (temp << 8 & updown)) != 0) {
			checkVectors |= temp;
		}

		temp = king;

		// going left
		if ((king & 0x0101010101010101L) == 0) {

			while ((temp = (temp >>> 1 & left)) != 0) {
				checkVectors |= temp;
			}

			temp = king;

			while ((temp = (temp >>> 9 & left)) != 0) {
				checkVectors |= temp;
			}

			temp = king;

			while ((temp = (temp << 7 & left)) != 0) {
				checkVectors |= temp;
			}

			temp = king;

		}

		// going right
		if ((king & 0x8080808080808080L) == 0) {

			while ((temp = (temp << 1 & right)) != 0) {
				checkVectors |= temp;
			}
			
			temp = king;

			while ((temp = (temp >> 7 & right)) != 0) {
				checkVectors |= temp;
			}

			temp = king;

			while ((temp = (temp << 9 & right)) != 0) {
				checkVectors |= temp;
			}

		}
		
		return checkVectors;
	}

	public static boolean isValidMove(int toRow, int toCol, long[] nullMoveInfo) {
		long mask = BitBoard.getMask(toRow, toCol);
		return  ((mask & (nullMoveInfo[0] | nullMoveInfo[2])) == 0);
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

		if ((kingToCastleMask & nullMoveInfo[0]) == 0) {
			if (((kingToCastleMask | rookToCastleMask) & allPosBitBoard) == 0) {
				return true;
			}
		}

		return false;
	}

}
