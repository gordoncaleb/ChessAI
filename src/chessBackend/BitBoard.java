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

	public static void main(String args[]) {
		loadMasks();
		long base = 1;
		long temp = WHITE_CASTLE_NEAR;

		System.out.println(Long.toHexString(temp));
		System.out.println(printBitBoard(temp));
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
}
