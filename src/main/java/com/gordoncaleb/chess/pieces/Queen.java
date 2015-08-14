package com.gordoncaleb.chess.pieces;

import java.util.ArrayList;

import com.gordoncaleb.chess.backend.BitBoard;
import com.gordoncaleb.chess.backend.Board;
import com.gordoncaleb.chess.backend.Side;
import com.gordoncaleb.chess.backend.Move;

public class Queen {

	public static int[][] QUEENMOVES = { { 1, 1, -1, -1, 1, -1, 0, 0 }, { 1, -1, 1, -1, 0, 0, 1, -1 } };

	public Queen() {
	}

	public static Piece.PieceID getPieceID() {
		return Piece.PieceID.QUEEN;
	}

	public static String getName() {
		return "Queen";
	}

	public static String getStringID() {
		return "Q";
	}

	public static void generateMoves(Piece p, Board board, ArrayList<Long> moves) {
		int currentRow = p.getRow();
		int currentCol = p.getCol();
		int nextRow;
		int nextCol;
		Piece.PositionStatus pieceStatus;

		int i = 1;
		for (int d = 0; d < 8; d++) {
			nextRow = currentRow + i * QUEENMOVES[0][d];
			nextCol = currentCol + i * QUEENMOVES[1][d];
			pieceStatus = board.checkPiece(nextRow, nextCol, p.getSide());

			while (pieceStatus == Piece.PositionStatus.NO_PIECE) {

				moves.add(Move.moveLong(currentRow, currentCol, nextRow, nextCol, 0, Move.MoveNote.NONE));

				i++;
				nextRow = currentRow + i * QUEENMOVES[0][d];
				nextCol = currentCol + i * QUEENMOVES[1][d];
				pieceStatus = board.checkPiece(nextRow, nextCol, p.getSide());

			}

			if (pieceStatus == Piece.PositionStatus.ENEMY) {
				moves.add(Move.moveLong(currentRow, currentCol, nextRow, nextCol, board.getPieceValue(nextRow, nextCol), Move.MoveNote.NONE, board.getPiece(nextRow, nextCol)));
			}

			i = 1;
		}
	}

	public static ArrayList<Long> generateValidMoves(Piece p, Board board, long[] nullMoveInfo, long[] posBitBoard, ArrayList<Long> validMoves) {
		int currentRow = p.getRow();
		int currentCol = p.getCol();
		Side player = p.getSide();
		int nextRow;
		int nextCol;
		int value;
		Piece.PositionStatus pieceStatus;

		int i = 1;
		for (int d = 0; d < 8; d++) {
			nextRow = currentRow + i * QUEENMOVES[0][d];
			nextCol = currentCol + i * QUEENMOVES[1][d];
			pieceStatus = board.checkPiece(nextRow, nextCol, player);

			while (pieceStatus == Piece.PositionStatus.NO_PIECE) {

				if (p.isValidMove(nextRow, nextCol, nullMoveInfo)) {

					if ((nullMoveInfo[0] & BitBoard.getMask(nextRow, nextCol)) != 0) {
						value = -Values.QUEEN_VALUE >> 1;
					} else {
						value = 0;
					}

					validMoves.add(Move.moveLong(currentRow, currentCol, nextRow, nextCol, value, Move.MoveNote.NONE));
				}

				i++;
				nextRow = currentRow + i * QUEENMOVES[0][d];
				nextCol = currentCol + i * QUEENMOVES[1][d];
				pieceStatus = board.checkPiece(nextRow, nextCol, player);

			}

			if (pieceStatus == Piece.PositionStatus.ENEMY) {
				if (p.isValidMove(nextRow, nextCol, nullMoveInfo)) {

					value = board.getPieceValue(nextRow, nextCol);

					if ((nullMoveInfo[0] & BitBoard.getMask(nextRow, nextCol)) != 0) {
						value -= Values.QUEEN_VALUE >> 1;
					}

					Long moveLong = Move.moveLong(currentRow, currentCol, nextRow, nextCol, value, Move.MoveNote.NONE, board.getPiece(nextRow, nextCol));
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

		while ((temp2 = (temp2 >>> 8 & updown)) != 0) {
			attackVector |= temp2;
			temp = temp2;
			r--;
		}

		temp = temp >>> 8;
		nullMoveInfo[0] |= attackVector | temp;

		// check to see if king collision is possible
		if ((BitBoard.getColMask(c) & kingBitBoard) != 0) {

			if ((temp & kingBitBoard) != 0) {
				nullMoveInfo[1] &= attackVector | bitPiece;
				nullMoveInfo[2] |= temp >>> 8;
			} else {
				if ((temp & friendly) != 0) {
					temp = temp >>> 8;
					if ((temp & kingCheckVectors) != 0) {
						board.getPiece(r - 1, c).setBlockingVector(BitBoard.getColMask(c));
					}
				}
			}
		}

		// down-----------------------------------------------------------
		temp = bitPiece;
		temp2 = bitPiece;
		r = piece.getRow();
		attackVector = 0;

		while ((temp2 = (temp2 << 8 & updown)) != 0) {
			attackVector |= temp2;
			temp = temp2;
			r++;
		}

		temp = temp << 8;
		nullMoveInfo[0] |= attackVector | temp;

		// check to see if king collision is possible
		if ((BitBoard.getColMask(c) & kingBitBoard) != 0) {

			if ((temp & kingBitBoard) != 0) {
				nullMoveInfo[1] &= attackVector | bitPiece;
				nullMoveInfo[2] |= temp << 8;
			} else {
				if ((temp & friendly) != 0) {
					temp = temp << 8;
					if ((temp & kingCheckVectors) != 0) {
						board.getPiece(r + 1, c).setBlockingVector(BitBoard.getColMask(c));
					}
				}
			}
		}
		
		// going westward -----------------------------------------------------
		if ((bitPiece & 0x0101010101010101L) == 0) {

			// west
			temp = bitPiece;
			temp2 = bitPiece;
			r = piece.getRow();
			attackVector = 0;

			while ((temp2 = (temp2 >>> 1 & left)) != 0) {
				attackVector |= temp2;
				temp = temp2;
				c--;
			}

			temp = temp >>> 1;
			nullMoveInfo[0] |= attackVector | temp;

			// check to see if king collision is possible
			if ((BitBoard.getRowMask(r) & kingBitBoard) != 0) {

				if ((temp & kingBitBoard) != 0) {
					nullMoveInfo[1] &= attackVector | bitPiece;
					nullMoveInfo[2] |= temp >>> 1;
				} else {
					if ((temp & friendly) != 0) {
						temp = temp >>> 1;
						if ((temp & kingCheckVectors) != 0) {
							board.getPiece(r, c - 1).setBlockingVector(BitBoard.getRowMask(r));
						}
					}
				}
			}

			// northwest
			temp2 = bitPiece;
			temp = bitPiece;
			c = piece.getCol();
			attackVector = 0;

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

			// east
			temp = bitPiece;
			temp2 = bitPiece;
			r = piece.getRow();
			c = piece.getCol();
			attackVector = 0;

			while ((temp2 = (temp2 << 1 & right)) != 0) {
				attackVector |= temp2;
				temp = temp2;
				c++;
			}

			temp = temp << 1;
			nullMoveInfo[0] |= attackVector | temp;

			// check to see if king collision is possible
			if ((BitBoard.getRowMask(r) & kingBitBoard) != 0) {

				if ((temp & kingBitBoard) != 0) {
					nullMoveInfo[1] &= attackVector | bitPiece;
					nullMoveInfo[2] |= temp << 1;
				} else {
					if ((temp & friendly) != 0) {
						temp = temp << 1;
						if ((temp & kingCheckVectors) != 0) {
							board.getPiece(r, c + 1).setBlockingVector(BitBoard.getRowMask(r));
						}
					}
				}
			}

			// northeast
			temp = bitPiece;
			temp2 = bitPiece;
			c = piece.getCol();
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
		Piece.PositionStatus pieceStatus;
		Side player = p.getSide();

		long bitPosition = p.getBit();

		int i = 1;
		for (int d = 0; d < 8; d++) {
			nextRow = currentRow + i * QUEENMOVES[0][d];
			nextCol = currentCol + i * QUEENMOVES[1][d];
			pieceStatus = board.checkPiece(nextRow, nextCol, player);

			if (pieceStatus == Piece.PositionStatus.OFF_BOARD) {
				continue;
			}

			while (pieceStatus == Piece.PositionStatus.NO_PIECE) {
				bitAttackVector |= BitBoard.getMask(nextRow, nextCol);
				i++;
				nextRow = currentRow + i * QUEENMOVES[0][d];
				nextCol = currentCol + i * QUEENMOVES[1][d];
				pieceStatus = board.checkPiece(nextRow, nextCol, player);
			}

			if (pieceStatus != Piece.PositionStatus.OFF_BOARD) {
				bitAttackVector |= BitBoard.getMask(nextRow, nextCol);
			}

			if (pieceStatus == Piece.PositionStatus.ENEMY) {

				blockingPiece = board.getPiece(nextRow, nextCol);

				if (blockingPiece.getPieceID() == Piece.PieceID.KING) {
					nullMoveInfo[1] &= (bitAttackVector | bitPosition);
					inCheck = true;
				}

				i++;
				nextRow = currentRow + i * QUEENMOVES[0][d];
				nextCol = currentCol + i * QUEENMOVES[1][d];
				pieceStatus = board.checkPiece(nextRow, nextCol, player);

				while (pieceStatus == Piece.PositionStatus.NO_PIECE) {
					bitAttackCompliment |= BitBoard.getMask(nextRow, nextCol);
					i++;
					nextRow = currentRow + i * QUEENMOVES[0][d];
					nextCol = currentCol + i * QUEENMOVES[1][d];
					pieceStatus = board.checkPiece(nextRow, nextCol, player);
				}

				if (pieceStatus != Piece.PositionStatus.OFF_BOARD) {
					if (board.getPieceID(nextRow, nextCol) == Piece.PieceID.KING && board.getPiece(nextRow, nextCol).getSide() != player) {
						blockingPiece.setBlockingVector(bitAttackCompliment | bitAttackVector | bitPosition);
					}
				}

				if (pieceStatus == Piece.PositionStatus.FRIEND) {
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
