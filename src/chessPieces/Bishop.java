package chessPieces;

import java.util.ArrayList;

import chessBackend.BitBoard;
import chessBackend.Board;
import chessBackend.MoveNote;
import chessBackend.Side;
import chessBackend.Move;

public class Bishop {
	private static final int[][] BISHOPMOVES = { { 1, 1, -1, -1 }, { 1, -1, 1, -1 } };

	public Bishop() {
	}

	public static PieceID getPieceID() {
		return PieceID.BISHOP;
	}

	public static String getName() {
		return "Bishop";
	}

	public static String getStringID() {
		return "B";
	}

	public static void generateMoves(Piece p, Board board, ArrayList<Long> moves) {
		int currentRow = p.getRow();
		int currentCol = p.getCol();
		int nextRow;
		int nextCol;
		Long moveLong;
		int value;
		PositionStatus pieceStatus;
		Side player = p.getSide();

		int i = 1;
		for (int d = 0; d < 4; d++) {
			nextRow = currentRow + i * BISHOPMOVES[0][d];
			nextCol = currentCol + i * BISHOPMOVES[1][d];
			pieceStatus = board.checkPiece(nextRow, nextCol, player);

			while (pieceStatus == PositionStatus.NO_PIECE) {

				moveLong = Move.moveLong(currentRow, currentCol, nextRow, nextCol, 0, MoveNote.NONE);
				moves.add(moveLong);

				i++;
				nextRow = currentRow + i * BISHOPMOVES[0][d];
				nextCol = currentCol + i * BISHOPMOVES[1][d];
				pieceStatus = board.checkPiece(nextRow, nextCol, player);

			}

			if (pieceStatus == PositionStatus.ENEMY) {
				value = board.getPieceValue(nextRow, nextCol);
				moveLong = Move.moveLong(currentRow, currentCol, nextRow, nextCol, value, MoveNote.NONE, board.getPiece(nextRow, nextCol));
				moves.add(moveLong);
			}

			i = 1;
		}
	}

	public static ArrayList<Long> generateValidMoves(Piece p, Board board, long[] nullMoveInfo, long[] posBitBoard, ArrayList<Long> validMoves) {
		int currentRow = p.getRow();
		int currentCol = p.getCol();
		int nextRow;
		int nextCol;
		Long moveLong;
		int value;
		PositionStatus pieceStatus;
		Side player = p.getSide();

		int i = 1;
		for (int d = 0; d < 4; d++) {
			nextRow = currentRow + i * BISHOPMOVES[0][d];
			nextCol = currentCol + i * BISHOPMOVES[1][d];
			pieceStatus = board.checkPiece(nextRow, nextCol, player);

			while (pieceStatus == PositionStatus.NO_PIECE) {

				if (p.isValidMove(nextRow, nextCol, nullMoveInfo)) {

					if ((nullMoveInfo[0] & BitBoard.getMask(nextRow, nextCol)) != 0) {
						value = -Values.BISHOP_VALUE >> 1;
					} else {
						value = 0;
					}

					moveLong = Move.moveLong(currentRow, currentCol, nextRow, nextCol, value, MoveNote.NONE);

					validMoves.add(moveLong);
				}

				i++;
				nextRow = currentRow + i * BISHOPMOVES[0][d];
				nextCol = currentCol + i * BISHOPMOVES[1][d];
				pieceStatus = board.checkPiece(nextRow, nextCol, player);

			}

			if (pieceStatus == PositionStatus.ENEMY) {
				if (p.isValidMove(nextRow, nextCol, nullMoveInfo)) {
					value = board.getPieceValue(nextRow, nextCol);

					if ((nullMoveInfo[0] & BitBoard.getMask(nextRow, nextCol)) != 0) {
						value -= Values.BISHOP_VALUE >> 1;
					}

					moveLong = Move.moveLong(currentRow, currentCol, nextRow, nextCol, value, MoveNote.NONE, board.getPiece(nextRow, nextCol));
					validMoves.add(moveLong);
				}
			}

			i = 1;
		}

		return validMoves;

	}
	
	public static void getNullMoveInfo(Piece piece, Board board, long[] nullMoveInfo, long updown, long left, long right, long kingBitBoard, long kingCheckVectors,
			long friendly) {

		long bitPiece = piece.getBit();

		// up ------------------------------------------------------------
		long temp = bitPiece;
		long temp2 = bitPiece;
		int r = piece.getRow();
		int c = piece.getCol();
		long attackVector = 0;

		
		// going westward -----------------------------------------------------
		if ((bitPiece & 0x0101010101010101L) == 0) {

			// northwest
			while ((temp2 = (temp2 >>> 9 & left)) != 0) {
				attackVector |= temp2;
				temp = temp2;
				r--;
				c--;
			}
			temp = temp >>> 9;
			nullMoveInfo[0] |= attackVector | temp;

			// check to see if king collision is possible
			if ((BitBoard.getNegSlope(r, c) & kingBitBoard) != 0) {

				if ((temp & kingBitBoard) != 0) {
					nullMoveInfo[1] &= attackVector | bitPiece;
					nullMoveInfo[2] |= temp >>> 9;
				} else {
					if ((temp & friendly) != 0) {
						temp = temp >>> 9;
						if ((temp & kingCheckVectors) != 0) {
							board.getPiece(r - 1, c - 1).setBlockingVector(BitBoard.getNegSlope(r, c));
						}
					}
				}
			}

			// south west
			temp = bitPiece;
			temp2 = bitPiece;
			r = piece.getRow();
			c = piece.getCol();
			attackVector = 0;

			while ((temp2 = (temp2 << 7 & left)) != 0) {
				attackVector |= temp2;
				temp = temp2;
				r++;
				c--;
			}

			temp = temp << 7;
			nullMoveInfo[0] |= attackVector | temp;

			// check to see if king collision is possible
			if ((BitBoard.getPosSlope(r, c) & kingBitBoard) != 0) {

				if ((temp & kingBitBoard) != 0) {
					nullMoveInfo[1] &= attackVector | bitPiece;
					nullMoveInfo[2] |= temp << 7;
				} else {
					if ((temp & friendly) != 0) {
						temp = temp << 7;
						if ((temp & kingCheckVectors) != 0) {
							board.getPiece(r + 1, c - 1).setBlockingVector(BitBoard.getPosSlope(r, c));
						}
					}
				}
			}

		}

		// going eastward
		if ((bitPiece & 0x8080808080808080L) == 0) {

			// northeast
			temp = bitPiece;
			temp2 = bitPiece;
			c = piece.getCol();
			r = piece.getRow();
			attackVector = 0;

			while ((temp2 = (temp2 >> 7 & right)) != 0) {
				attackVector |= temp2;
				temp = temp2;
				c++;
				r--;
			}

			temp = temp >> 7;
			nullMoveInfo[0] |= attackVector | temp;

			// check to see if king collision is possible
			if ((BitBoard.getPosSlope(r, c) & kingBitBoard) != 0) {

				if ((temp & kingBitBoard) != 0) {
					nullMoveInfo[1] &= attackVector | bitPiece;
					nullMoveInfo[2] |= temp >> 7;
				} else {
					if ((temp & friendly) != 0) {
						temp = temp >> 7;
						if ((temp & kingCheckVectors) != 0) {
							board.getPiece(r - 1, c + 1).setBlockingVector(BitBoard.getPosSlope(r, c));
						}
					}
				}
			}

			// southeast
			temp = bitPiece;
			temp2 = bitPiece;
			c = piece.getCol();
			r = piece.getRow();
			attackVector = 0;

			while ((temp2 = (temp2 << 9 & right)) != 0) {
				attackVector |= temp2;
				temp = temp2;
				c++;
				r++;
			}

			temp = temp << 9;
			nullMoveInfo[0] |= attackVector | temp;

			// check to see if king collision is possible
			if ((BitBoard.getNegSlope(r, c) & kingBitBoard) != 0) {

				if ((temp & kingBitBoard) != 0) {
					nullMoveInfo[1] &= attackVector | bitPiece;
					nullMoveInfo[2] |= temp << 9;
				} else {
					if ((temp & friendly) != 0) {
						temp = temp << 9;
						if ((temp & kingCheckVectors) != 0) {
							board.getPiece(r + 1, c + 1).setBlockingVector(BitBoard.getNegSlope(r, c));
						}
					}
				}
			}

		}

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
			nextRow = currentRow + i * BISHOPMOVES[0][d];
			nextCol = currentCol + i * BISHOPMOVES[1][d];
			pieceStatus = board.checkPiece(nextRow, nextCol, player);

			if (pieceStatus == PositionStatus.OFF_BOARD) {
				continue;
			}

			while (pieceStatus == PositionStatus.NO_PIECE) {
				bitAttackVector |= BitBoard.getMask(nextRow, nextCol);
				i++;
				nextRow = currentRow + i * BISHOPMOVES[0][d];
				nextCol = currentCol + i * BISHOPMOVES[1][d];
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
				nextRow = currentRow + i * BISHOPMOVES[0][d];
				nextCol = currentCol + i * BISHOPMOVES[1][d];
				pieceStatus = board.checkPiece(nextRow, nextCol, player);

				while (pieceStatus == PositionStatus.NO_PIECE) {
					bitAttackCompliment |= BitBoard.getMask(nextRow, nextCol);
					i++;
					nextRow = currentRow + i * BISHOPMOVES[0][d];
					nextCol = currentCol + i * BISHOPMOVES[1][d];
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
				inCheck = false;
			}

			bitAttackCompliment = 0;
			bitAttackVector = 0;

			i = 1;
		}

	}

}
