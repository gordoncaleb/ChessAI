package com.gordoncaleb.chess.bitboard;

import com.gordoncaleb.chess.backend.Side;
import com.gordoncaleb.chess.pieces.Pawn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.gordoncaleb.chess.bitboard.Slide.northFill;
import static com.gordoncaleb.chess.bitboard.Slide.southFill;

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

    public static final long NOT_LEFT1_RIGHT1 = 0x7E7E7E7E7E7E7E7EL;

    public static final long NOT_LEFT1 = 0xFEFEFEFEFEFEFEFEL;
    public static final long NOT_LEFT2 = 0xFCFCFCFCFCFCFCFCL;
    public static final long NOT_LEFT4 = 0xF0F0F0F0F0F0F0F0L;
    public static final long NOT_RIGHT1 = 0x7F7F7F7F7F7F7F7FL;
    public static final long NOT_RIGHT2 = 0x3F3F3F3F3F3F3F3FL;
    public static final long NOT_RIGHT4 = 0x0F0F0F0F0F0F0F0FL;

    public static final long TOP_BIT = 0x8000000000000000L;
    public static final long BOT_BIT = 0x1L;

    public final static long[] kingFootPrint = new long[64];
    public final static long[][] knightFootPrint = new long[8][8];

    static {
        loadKnightFootPrints();
        loadKingFootPrints();
    }

    public static long getPassedPawns(long pawns, long otherPawns, int side) {
        if (side == Side.WHITE) {
            return (~northFill(pawns | Pawn.getPawnAttacks(pawns, Side.WHITE)) & otherPawns);
        } else {
            return (~southFill(pawns | Pawn.getPawnAttacks(pawns, Side.BLACK)) & otherPawns);
        }
    }

    public static long getIsolatedPawns(long pawns, int side) {
        long pawnAttacks = Pawn.getPawnAttacks(pawns, side);
        return ~(southFill(pawnAttacks) | northFill(pawnAttacks)) & pawns;
    }

    public static long getCastleMask(int col1, int col2, int side) {
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

    public static boolean hasOneBitOrLess(long bb) {
        return (bb & (bb - 1)) == 0;
    }

    public static long getMask(int row, int col) {
        return (1L << ((row << 3) + col));
    }

    public static long getColMask(int col) {
        return (0x0101010101010101L << col);
    }

    public static long getRowMask(int row) {
        return (0xFFL << (row * 8));
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

    public static int getBackedPawns(long pawns) {
        return Long.bitCount(((pawns & NOT_RIGHT1) << 7) & pawns) + Long.bitCount(((pawns & NOT_LEFT1) << 9));
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
            return (0x70507L << shift) & (~getColMask(col ^ 7) | NOT_LEFT1_RIGHT1);
        } else {
            return (0x70507L >> -shift) & (~getColMask(col ^ 7) | NOT_LEFT1_RIGHT1);
        }

    }

    public static void loadKnightFootPrints() {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                knightFootPrint[r][c] = getKnightFootPrint(r, c);
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
        String bitBoardString = "\"";

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if ((bitBoard & getMask(r, c)) != 0) {
                    bitBoardString += "1,";
                } else {
                    bitBoardString += "_,";
                }
            }

            bitBoardString += "\"";
            if (r < 7) {
                bitBoardString += ",\n\"";
            }
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
