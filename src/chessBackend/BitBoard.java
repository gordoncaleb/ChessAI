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

	public static void main(String args[]) {
		long base = 1;
		long temp = WHITE_CHECK_FAR;

		System.out.println(Long.toHexString(temp));
		printBitBoard(temp);
	}

	public static long getMask(int row, int col) {
		long one = 1;

		if (row >= 0 && row < 8 && col >= 0 && col < 8) {
			return (one << (row * 8 + col));
		} else {
			return 0;
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

		//System.out.println(bitBoardString);

		return bitBoardString;
	}
}
