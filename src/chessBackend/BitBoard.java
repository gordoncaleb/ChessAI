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

	public static long[][] bitMask;
	public static long[][][][] slidFromToMask;

	public static long[][] kingFootPrint;

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

		for (int r = 0; r < 8; r++) {
			for (int c = 0; c < 8; c++) {
				System.out.println("R=" + r + " C=" + c);
				System.out.println(BitBoard.printBitBoard(getKingFootPrint(r, c)));
			}
		}

		System.out.println(BitBoard.printBitBoard(0x70507L >> 1));

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
		return (1L << (row * 8 + col));
	}

	public static long getColMask(int col) {
		return (0x0101010101010101L << col);
	}

	public static long getRowMask(int row) {
		return (0xFFL << (row * 8));
	}

	public static long getBottomRows(int r) {
		return (0xFF00000000000000L >> (r * 8));
	}

	public static long getTopRows(int r) {
		return (0xFFFFFFFFFFFFFFFFL >>> ((7 - r) * 8));
	}

	public static long getWhitePawnForward(int r, int c) {
		return (0x0080808080808080L >> ((7 - r) * 8 + (7 - c)));
	}

	public static long getBlackPawnForward(int r, int c) {
		return (0x0101010101010100L << (r * 8 + c));
	}

	// public static void loadMasks() {
	//
	// loadKingFootPrints();
	//
	// }

	// private static void loadKingFootPrints() {
	//
	// int[][] KINGMOVES = { { 1, 1, -1, -1, 1, -1, 0, 0 }, { 1, -1, 1, -1, 0,
	// 0, 1, -1 } };
	//
	// kingFootPrint = new long[8][8];
	//
	// int nextr;
	// int nextc;
	//
	// for (int r = 0; r < 8; r++) {
	// for (int c = 0; c < 8; c++) {
	// kingFootPrint[r][c] = 0;
	//
	// for (int m = 0; m < 8; m++) {
	// nextr = r + KINGMOVES[0][m];
	// nextc = c + KINGMOVES[1][m];
	//
	// if (nextr >= 0 && nextr < 8 && nextc >= 0 && nextc < 8) {
	// kingFootPrint[r][c] |= bitMask[nextr][nextc];
	// }
	// }
	//
	// // System.out.println("king foot print " + r + "," + c);
	// // System.out.println(printBitBoard(kingFootPrint[r][c]));
	// }
	// }
	// }

//	public static long getKingFootPrint(int row, int col) {
//		// return kingFootPrint[row][col];
//
//		return (0x70507L << (8 * (row - 1) + col - 1));// & (~getColMask(col ^
//														// 7) |
//														// (0x7E7E7E7E7E7E7E7EL));
//	}

	public static long getKingFootPrint(int row, int col) {
		// return kingFootPrint[row][col];

		int shift = (8 * (row - 1) + col - 1);

		if (shift >= 0){
			return (0x70507L << shift) & (~getColMask(col ^ 7) | (0x7E7E7E7E7E7E7E7EL));
		}else{
			return (0x70507L >> -shift) & (~getColMask(col ^ 7) | (0x7E7E7E7E7E7E7E7EL));
		}
		
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

	// public static void loadBitCount() {
	// bitCount = new int[256];
	//
	// for (int i = 0; i < bitCount.length; i++) {
	// bitCount[i] = bitCountByte(i);
	// }
	// }

	// public static int bitCountLong(long val) {
	//
	// int count = 0;
	//
	// for (int i = 0; i < 64; i += 8) {
	// count += bitCount[(int) ((val >> i) & 0xFF)];
	// }
	//
	// return count;
	// }

	public static int bitCountLong(long val) {
		int count = 0;
		int size = 64;

		for (int i = 0; i < size; i++) {
			if ((val & (1L << i)) != 0) {
				count++;
			}
		}

		return count;
	}

	public static int bitCountByte(int val) {

		int count = 0;
		int size = 8;

		for (int i = 0; i < size; i++) {
			if ((val & (1L << i)) != 0) {
				count++;
			}
		}

		return count;
	}
}
