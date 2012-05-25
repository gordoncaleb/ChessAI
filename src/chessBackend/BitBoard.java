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
		loadMasks();

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

	}

	public static long getMask(int row, int col) {
		return bitMask[row][col];
	}

	public static void loadMasks() {
		long one = 1;

		bitMask = new long[8][8];

		for (int r = 0; r < 8; r++) {
			for (int c = 0; c < 8; c++) {
				bitMask[r][c] = (one << (r * 8 + c));
			}
		}

		loadFromToMask();
		loadKingFootPrints();

	}

	private static void loadKingFootPrints() {

		int[][] KINGMOVES = { { 1, 1, -1, -1, 1, -1, 0, 0 }, { 1, -1, 1, -1, 0, 0, 1, -1 } };

		kingFootPrint = new long[8][8];

		int nextr;
		int nextc;

		for (int r = 0; r < 8; r++) {
			for (int c = 0; c < 8; c++) {
				kingFootPrint[r][c] = 0;

				for (int m = 0; m < 8; m++) {
					nextr = r + KINGMOVES[0][m];
					nextc = c + KINGMOVES[1][m];

					if (nextr >= 0 && nextr < 8 && nextc >= 0 && nextc < 8) {
						kingFootPrint[r][c] |= bitMask[nextr][nextc];
					}
				}

				System.out.println("king foot print " + r + "," + c);
				System.out.println(printBitBoard(kingFootPrint[r][c]));
			}
		}
	}

	public static long getKingFootPrint(int row, int col) {
		return kingFootPrint[row][col];
	}

	private static void loadFromToMask() {
		slidFromToMask = new long[8][8][8][8];

		for (int fr = 0; fr < 8; fr++) {
			for (int fc = 0; fc < 8; fc++) {
				for (int tr = 0; tr < 8; tr++) {
					for (int tc = 0; tc < 8; tc++) {
						if (fr == tr || fc == tc || Math.abs(fc - tc) == Math.abs(fr - tr)) {

						}
					}
				}
			}
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
