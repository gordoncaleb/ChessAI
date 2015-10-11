package com.gordoncaleb.chess.backend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BitBoard {
    public static Logger logger = LoggerFactory.getLogger(BitBoard.class);

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

    public static final long[][] slideNorth = new long[8][8];
    public static final long[][] slideSouth = new long[8][8];
    public static final long[][] slideWest = new long[8][8];
    public static final long[][] slideEast = new long[8][8];
    public static final long[][] slideNorthWest = new long[8][8];
    public static final long[][] slideNorthEast = new long[8][8];
    public static final long[][] slideSouthWest = new long[8][8];
    public static final long[][] slideSouthEast = new long[8][8];

    public final static long[] kingFootPrint = new long[64];
    public final static long[][] knightFootPrint = new long[8][8];

    static {
        loadKnightFootPrints();
        loadKingFootPrints();
        loadSlidingBitBoards();
    }

    public static boolean isCastled(long king, long rook, Side side) {

        if (side == Side.WHITE) {
            king &= 0xFF00000000000000L;
            rook &= 0xFF00000000000000L;
        } else {
            king &= 0xFFL;
            rook &= 0xFFL;
        }

        return (king != 0 && rook != 0 && ((((removeBottomBit(king | rook, 1)) & king) == 0) || ((removeBottomBit(king | rook, 2)) & king) != 0));
    }

    public static long getPassedPawns(long pawns, long otherPawns, Side side) {
        if (side == Side.WHITE) {
            return (~southFill(pawns | getPawnAttacks(pawns, Side.WHITE)) & otherPawns);
        } else {
            return (~northFill(pawns | getPawnAttacks(pawns, Side.BLACK)) & otherPawns);
        }
    }

    public static long getIsolatedPawns(long pawns, Side side) {
        long pawnAttacks = getPawnAttacks(pawns, side);
        return ~(northFill(pawnAttacks) | southFill(pawnAttacks)) & pawns;
    }

    public static int canQueen(long p, long o, Side turn) {

        if (turn == Side.WHITE) {
            return Long.bitCount((((p >>> 8) & ~o) | (BitBoard.getPawnAttacks(p, turn) & o)) & 0xFFL);
        } else {
            return Long.bitCount((((p << 8) & ~o) | (BitBoard.getPawnAttacks(p, turn) & o)) & 0xFF00000000000000L);
        }
    }

    public static long removeBottomBit(long bb, int i) {
        for (; i > 0; i--) {
            bb &= bb - 1;
        }

        return bb;
    }

    public static long northFill(long gen) {
        gen |= (gen << 8);
        gen |= (gen << 16);
        gen |= (gen << 32);
        return gen;
    }

    public static long southFill(long gen) {
        gen |= (gen >> 8);
        gen |= (gen >> 16);
        gen |= (gen >> 32);
        return gen;
    }

    public static long fillUpOccluded(long g, long p) {

        g |= p & (g << 8);
        p &= (p << 8);
        g |= p & (g << 16);
        p &= (p << 16);
        g |= p & (g << 32);

        return g;
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

    public static void loadSlidingBitBoards() {
        loadSlideSouth();
        loadSlideNorth();
    }

    public static void loadSlideSouth() {
        for (int r = 0; r < 7; r++) {
            for (int c = 0; c < 8; c++) {
                slideSouth[r][c] = (0x0101010101010101L << ((r + 1) * 8 + c));
            }
        }
    }

    public static void loadSlideNorth() {
        for (int r = 1; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                slideNorth[r][c] = (0x0101010101010101L << c) >>> ((8 - r) * 8);
            }
        }
    }

    public static long slideSouth(int r, int c, long friend, long foe) {
        long a = slideSouth[r][c] & (friend | foe);
        int n = Long.numberOfTrailingZeros(a) + 1;
        return slideSouth[r][c] & ((1L << n) - 1L) & ~friend;
    }

    public static long slideNorth(int r, int c, long friend, long foe) {
        long a = slideNorth[r][c] & (friend | foe);
        int n = 64 - Long.numberOfLeadingZeros(a);
        return slideNorth[r][c] & ~((1L << n) - 1L) & ~friend;
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

    public static void loadKingFootPrints() {

        int[][] KINGMOVES = {{1, 1, -1, -1, 1, -1, 0, 0}, {1, -1, 1, -1, 0, 0, 1, -1}};

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

    public static void loadKnightFootPrints() {

        int[][] KNIGHT_MOVES = {{2, 2, -2, -2, 1, -1, 1, -1}, {1, -1, 1, -1, 2, -2, -2, 2}};

        int nextRow;
        int nextCol;

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                knightFootPrint[r][c] = 0;

                for (int m = 0; m < 8; m++) {
                    nextRow = r + KNIGHT_MOVES[0][m];
                    nextCol = c + KNIGHT_MOVES[1][m];

                    if (nextRow >= 0 && nextRow < 8 && nextCol >= 0 && nextCol < 8) {
                        knightFootPrint[r][c] |= getMask(nextRow, nextCol);
                    }
                }
            }
        }
    }

    public static long getKnightFootPrintMem(int row, int col) {
        return knightFootPrint[row][col];
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

    public static List<Integer> bitNumbers(long bb) {
        List<Integer> locations = new LinkedList<>();
        int i;
        while ((i = Long.numberOfTrailingZeros(bb)) < 64) {
            locations.add(i);
            bb ^= 1L << i;
        }
        return locations;
    }

    public static String printBitBoard(long bitBoard) {
        String bitBoardString = "";

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if ((bitBoard & getMask(r, c)) != 0) {
                    bitBoardString += "1,";
                } else {
                    bitBoardString += "_,";
                }
            }
            bitBoardString += "\n";
        }

        return bitBoardString;
    }

    public static long parseBitBoard(String[] bitBoard) {
        return parseBitBoard(Stream.of(bitBoard).collect(Collectors.joining()));
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
