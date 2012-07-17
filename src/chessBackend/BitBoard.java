package chessBackend;

public class BitBoard {

	public static final long ALL_ONES = -1;
	public static final long WHITE_CASTLE_NEAR = 0x6000000000000000L;
	public static final long WHITE_CASTLE_FAR = 0xe00000000000000L;
	public static final long BLACK_CASTLE_NEAR = 0x60L;
	public static final long BLACK_CASTLE_FAR = 0xeL;
	public static final long WHITE_CHECK_NEAR = 0x2000000000000000L;
	public static final long WHITE_CHECK_FAR = 0x800000000000000L;
	public static final long BLACK_CHECK_NEAR = 0x20L;
	public static final long BLACK_CHECK_FAR = 0x8L;

	public static final long noAG = 0x7E7E7E7E7E7E7E7EL;

	public static long[][] bitMask;
	public static long[][][][] slidFromToMask;

	public static long[] kingFootPrint;
	public static long[] knightFootPrint;

	public static void main(String args[]) {

		// long t1 = System.currentTimeMillis();
		// for (long i = 0; i < 0xFFFFFFF; i++) {
		// bitCountLongHardWay(i);
		// }
		//
		// System.out.println("Hardway takes " + (System.currentTimeMillis() -
		// t1) + " ms");
		//
		// t1 = System.currentTimeMillis();
		// for (long i = 0; i < 0xFFFFFFF; i++) {
		// bitCountLong(i);
		// }
		//
		// System.out.println("Easy takes " + (System.currentTimeMillis() - t1)
		// + " ms");

		long val;

		// System.out.println("white castle near");
		// System.out.println(BitBoard.printBitBoard(WHITE_CASTLE_NEAR));
		//
		// System.out.println("white check near");
		// System.out.println(BitBoard.printBitBoard(WHITE_CHECK_NEAR));
		//
		// System.out.println("white check near hardway");
		// System.out.println(BitBoard.printBitBoard(getCastleMask(4, 2,
		// Side.WHITE)));
		//
		// for (int r = 0; r < 8; r++) {
		// System.out.println("Row mask =\n" +
		// BitBoard.printBitBoard(getRowMask(r)));
		// }
		//
		// for (int c = 0; c < 8; c++) {
		// System.out.println("col mask = \n" +
		// BitBoard.printBitBoard(getColMask(c)));
		// }
		//
		// for (int r = 0; r < 8; r++) {
		// System.out.println("Bottom Row mask =\n" +
		// BitBoard.printBitBoard(getBottomRows(r)));
		// }
		//
		// for (int c = 0; c < 8; c++) {
		// System.out.println("Top Row mask = \n" +
		// BitBoard.printBitBoard(getTopRows(c)));
		// }
		//
		// for (int r = 0; r < 8; r++) {
		// for (int c = 0; c < 8; c++) {
		// System.out.println("White pawn forward mask =" + r + "," + c + "\n" +
		// BitBoard.printBitBoard(getWhitePawnForward(r, c)));
		// }
		// }
		//
		// for (int r = 0; r < 8; r++) {
		// for (int c = 0; c < 8; c++) {
		// System.out.println("Black pawn forward mask =" + r + "," + c + "\n" +
		// BitBoard.printBitBoard(getBlackPawnForward(r, c)));
		// }
		// }

		// loadKnightFootPrints();

		long t1 = System.currentTimeMillis();
		for (int i = 0; i < 1000000; i++) {
			for (int r = 0; r < 8; r++) {
				for (int c = 0; c < 8; c++) {
					// getKingFootPrint(r, c);
				}
			}
		}

		System.out.println("hardway " + (System.currentTimeMillis() - t1));

		t1 = System.currentTimeMillis();
		for (int i = 0; i < 1000000; i++) {
			for (int r = 0; r < 8; r++) {
				for (int c = 0; c < 8; c++) {
					// getKingAttacks(getMask(r, c));
				}
			}
		}

		System.out.println("safeway " + (System.currentTimeMillis() - t1));

		// for (int r = 0; r < 8; r++) {
		// for (int c = 0; c < 8; c++) {
		// System.out.println(r + "," + c);
		// System.out.println(BitBoard.printBitBoard(getKnightFootPrint(r, c)));
		//
		// if (getKnightFootPrint(r, c) != getKnightFootPrintMem(r, c)) {
		// System.out.println("Error");
		// }
		// }
		// }

		// System.out.println(BitBoard.printBitBoard(0x7F7F7F7F7F7F7F7FL));

		String bitBoard = "0,0,0,0,0,0,0,0,\n" + "1,1,1,0,1,1,1,1,\n" + "0,0,0,0,0,0,0,0,\n" + "0,0,0,0,0,0,0,0,\n" + "1,0,0,0,0,0,0,0,\n" + "0,0,0,0,0,0,0,0,\n"
				+ "0,0,0,0,0,0,0,0,\n" + "0,0,0,0,0,0,0,0,\n";

		long bb = parseBitBoard(bitBoard);

		// for (int r = 0; r < 8; r++) {
		// for (int c = 0; c < 8; c++) {
		// if (getKnightFootPrint(r, c) != getKnightAttacks(getMask(r, c))) {
		//
		// System.out.println(r + "," + c);
		// System.out.println(BitBoard.printBitBoard(getKnightFootPrint(r, c)));
		// System.out.println("!=");
		// System.out.println(BitBoard.printBitBoard(getKnightAttacks(getMask(r,
		// c))));
		//
		// System.out.println("Error!");
		// }
		// }
		// }

		for (int r = 0; r < 8; r++) {
			for (int c = 0; c < 8; c++) {
				System.out.println(r + "," + c);
				System.out.println(printBitBoard(getPosSlope(r, c)));
			}
		}

//		for (int r = 0; r < 8; r++) {
//			for (int c = 0; c < 8; c++) {
//				System.out.println(r + "," + c);
//				System.out.println(printBitBoard(getNegSlope(r, c)));
//			}
//		}

	}

	public static long getCastleMask(int col1, int col2, Side side) {
		int lowCol;
		int highCol;

		if (col1 >= col2) {
			lowCol = col2;
			highCol = col1;
		} else {
			lowCol = col1;
			highCol = col2;
		}

		if (side == Side.BLACK) {
			return ((0xFFL >> (7 - highCol + lowCol)) << (lowCol));
		} else {
			return ((0xFFL >> (7 - highCol + lowCol)) << (lowCol + 56));
		}
	}

	public static long getMask(int row, int col) {
		return (1L << ((row << 3) + col));
	}

	public static long getMaskSafe(int row, int col) {
		if (((row | col) & ~7) == 0) {
			return (1L << ((row << 3) + col));
		} else {
			return 0;
		}

	}

	public static long rotateLeft(long bb, int r) {
		return ((bb << r) | (bb >> (-r)));
	}

	public static long getColMask(int col) {
		return (0x0101010101010101L << col);
	}

	public static long getRowMask(int row) {
		return (0xFFL << (row * 8));
	}

	public static long getBottomRows(int r) {
		return (0xFF00000000000000L >> (r << 3));
	}

	public static long getNegSlope(int row, int col) {
		int s = row - col;
		if (s >= 0) {
			return ((0x8040201008040201L) << (s << 3));
		} else {
			return ((0x8040201008040201L) >>> (-s << 3));
		}
	}

	public static long getPosSlope(int row, int col) {
		int s = col + row - 7;
		if (s >= 0) {
			return ((0x0102040810204080L) << (s << 3));
		} else {
			return ((0x0102040810204080L) >>> (-s << 3));
		}
	}

	public static long getTopRows(int r) {
		return (0xFFFFFFFFFFFFFFFFL >>> ((7 - r) * 8));
	}

	private static long getWhitePawnPassedForward(int r, int c) {
		return (0x0080808080808080L >> ((7 - r) * 8 + (7 - c)));
	}

	private static long getBlackPawnPassedForward(int r, int c) {
		return (0x0101010101010100L << (r << 8 + c));
	}

	public static int getBackedPawns(long pawns) {
		return Long.bitCount(((pawns & 0x7F7F7F7F7F7F7F7FL) << 7) & pawns) + Long.bitCount(((pawns & 0xFEFEFEFEFEFEFEFEL) << 9));
	}

	public static long getPawnAttacks(long pawns, Side side) {
		if (side == Side.BLACK) {
			return ((pawns & 0x7F7F7F7F7F7F7F7FL) << 9) | ((pawns & 0xFEFEFEFEFEFEFEFEL) << 7);
		} else {
			return ((pawns & 0x7F7F7F7F7F7F7F7FL) >>> 7) | ((pawns & 0xFEFEFEFEFEFEFEFEL) >>> 9);
		}
	}

	private static void loadKingFootPrints() {

		int[][] KINGMOVES = { { 1, 1, -1, -1, 1, -1, 0, 0 }, { 1, -1, 1, -1, 0, 0, 1, -1 } };

		kingFootPrint = new long[64];

		int nextr;
		int nextc;

		for (int r = 0; r < 8; r++) {
			for (int c = 0; c < 8; c++) {
				kingFootPrint[r * 8 + c] = 0;

				for (int m = 0; m < 8; m++) {
					nextr = r + KINGMOVES[0][m];
					nextc = c + KINGMOVES[1][m];

					if (nextr >= 0 && nextr < 8 && nextc >= 0 && nextc < 8) {
						kingFootPrint[r * 8 + c] |= getMask(nextr, nextc);
					}
				}

				// System.out.println("king foot print " + r + "," + c);
				// System.out.println(printBitBoard(kingFootPrint[r][c]));
			}
		}
	}

	public static long getKingFootPrintMem(int row, int col) {

		return kingFootPrint[(row << 3) + col];

	}

	public static long getKingFootPrint(int row, int col) {

		int shift = ((row - 1) * 8 + col - 1);

		if (shift >= 0) {
			return (0x70507L << shift) & (~getColMask(col ^ 7) | (0x7E7E7E7E7E7E7E7EL));
		} else {
			return (0x70507L >> -shift) & (~getColMask(col ^ 7) | (0x7E7E7E7E7E7E7E7EL));
		}

	}

	public static long getKingAttacks(long king) {
		return (king << 8) | // down 1
				(king >>> 8) | // up 1
				((king & 0xFEFEFEFEFEFEFEFEL) >>> 1) | // left 1
				((king & 0x7F7F7F7F7F7F7F7FL) << 1) | // right 1
				((king & 0x7F7F7F7F7F7F7F7FL) >>> 7) | // up 1 right 1
				((king & 0x7F7F7F7F7F7F7F7FL) << 9) | // down 1 right 1
				((king & 0xFEFEFEFEFEFEFEFEL) >>> 9) | // up 1 left 1
				((king & 0xFEFEFEFEFEFEFEFEL) << 7); // down 1 left 1
	}

	private static void loadKnightFootPrints() {

		int[][] KNIGHTMOVES = { { 2, 2, -2, -2, 1, -1, 1, -1 }, { 1, -1, 1, -1, 2, -2, -2, 2 } };

		knightFootPrint = new long[64];

		int nextr;
		int nextc;

		for (int r = 0; r < 8; r++) {
			for (int c = 0; c < 8; c++) {
				knightFootPrint[r * 8 + c] = 0;

				for (int m = 0; m < 8; m++) {
					nextr = r + KNIGHTMOVES[0][m];
					nextc = c + KNIGHTMOVES[1][m];

					if (nextr >= 0 && nextr < 8 && nextc >= 0 && nextc < 8) {
						knightFootPrint[r * 8 + c] |= getMask(nextr, nextc);
					}
				}

				// System.out.println("king foot print " + r + "," + c);
				// System.out.println(printBitBoard(kingFootPrint[r][c]));
			}
		}
	}

	public static long getKnightFootPrintMem(int row, int col) {
		return knightFootPrint[(row << 3) + col];
	}

	public static long getKnightFootPrint(int row, int col) {
		int shift = (((row - 2) * 8) + col - 2);

		if (shift >= 0) {
			return (0x0A1100110AL << shift) & (~(0xC0C0C0C0C0C0C0C0L >>> (col & 6)) | (0x3c3c3c3c3c3c3c3cL));
		} else {
			return (0x0A1100110AL >> -shift) & (~(0xC0C0C0C0C0C0C0C0L >>> (col & 6)) | (0x3c3c3c3c3c3c3c3cL));
		}
	}

	public static long getKnightAttacks(long knights) {
		return ((knights & 0xFCFCFCFCFCFCFCFCL) << 6) | // down 1 left 2
				((knights & 0xFCFCFCFCFCFCFCFCL) >>> 10) | // up 1 left 2
				((knights & 0xFEFEFEFEFEFEFEFEL) << 15) | // down 2 left 1
				((knights & 0xFEFEFEFEFEFEFEFEL) >>> 17) | // up 2 left 1
				((knights & 0x3F3F3F3F3F3F3F3FL) >>> 6) | // up 1 right 2
				((knights & 0x3F3F3F3F3F3F3F3FL) << 10) | // down 1 right 2
				((knights & 0x7F7F7F7F7F7F7F7FL) >>> 15) | // up 2 right 1
				((knights & 0x7F7F7F7F7F7F7F7FL) << 17);
	}

	public static String printBitBoard(long bitBoard) {
		String bitBoardString = "";

		for (int r = 0; r < 8; r++) {
			for (int c = 0; c < 8; c++) {
				if ((bitBoard & getMask(r, c)) != 0) {
					bitBoardString += "1,";
				} else {
					bitBoardString += "0,";
				}
			}
			bitBoardString += "\n";
		}

		// System.out.println(bitBoardString);

		return bitBoardString;
	}

	public static long parseBitBoard(String bitBoard) {

		String[] tokens = bitBoard.split(",");

		long bb = 0;
		for (int i = 0; i < 64; i++) {
			if (tokens[i].trim().equals("1")) {
				bb |= (1L << i);
			}
		}

		return bb;

	}

}
