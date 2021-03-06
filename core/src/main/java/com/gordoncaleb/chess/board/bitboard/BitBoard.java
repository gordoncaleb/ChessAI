package com.gordoncaleb.chess.board.bitboard;

import com.gordoncaleb.chess.board.Board;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.gordoncaleb.chess.board.Side.BLACK;
import static com.gordoncaleb.chess.board.Side.WHITE;

public class BitBoard {
    public static Logger logger = LoggerFactory.getLogger(BitBoard.class);

    public static final long ALL_ONES = -1;

    public static final long NOT_LEFT1 = 0xFEFEFEFEFEFEFEFEL;
    public static final long NOT_LEFT2 = 0xFCFCFCFCFCFCFCFCL;
    public static final long NOT_LEFT4 = 0xF0F0F0F0F0F0F0F0L;
    public static final long NOT_RIGHT1 = 0x7F7F7F7F7F7F7F7FL;
    public static final long NOT_RIGHT2 = 0x3F3F3F3F3F3F3F3FL;
    public static final long NOT_RIGHT4 = 0x0F0F0F0F0F0F0F0FL;

    public static final long COL1 = 0x0101010101010101L;

    public static final long TOP_BIT = 0x8000000000000000L;
    public static final long BOT_BIT = 0x1L;

    private static final long[][] knightFootPrint = new long[8][8];

    static {
        loadKnightFootPrints();
    }

    public static long[][] buildKingToCastleMasks(long[] kings, long[][] rooks, int[] materialRows) {
        long[][] kingsToCastleMasks = new long[2][2];
        for (int side : Arrays.asList(BLACK, WHITE)) {
            long king = kings[side];
            long near = getMask(materialRows[side], 6);
            long far = getMask(materialRows[side], 2);

            kingsToCastleMasks[side][Board.NEAR] = Slide.eastFillAndSlide(
                    furthestLeft(king, near),
                    furthestRight(king, near)) &
                    ~rooks[side][Board.NEAR]; //for chess 960 case where castling rook can be in the way

            kingsToCastleMasks[side][Board.FAR] = Slide.eastFillAndSlide(
                    furthestLeft(king, far),
                    furthestRight(king, far)) &
                    ~rooks[side][Board.FAR]; //for chess 960 case where castling rook can be in the way
        }
        return kingsToCastleMasks;
    }

    public static long[][] buildRookToCastleMasks(long[] kings, long[][] rooks, int[] materialRows) {
        long[][] rookToCastleMasks = new long[2][2];
        for (int side : Arrays.asList(BLACK, WHITE)) {
            long farRook = rooks[side][Board.FAR];
            long nearRook = rooks[side][Board.NEAR];
            long farPos = getMask(materialRows[side], 3);
            long nearPos = getMask(materialRows[side], 5);

            rookToCastleMasks[side][Board.FAR] = Slide.eastFillAndSlide(
                    furthestLeft(farRook, farPos),
                    furthestRight(farRook, farPos)) &
                    ~farRook &
                    ~kings[side];

            rookToCastleMasks[side][Board.NEAR] = Slide.eastFillAndSlide(
                    furthestLeft(nearRook, nearPos),
                    furthestRight(nearRook, nearPos)) &
                    ~nearRook &
                    ~kings[side];
        }
        return rookToCastleMasks;
    }

    public static long furthestRight(long a, long b) {
        return (Long.numberOfTrailingZeros(a) > Long.numberOfTrailingZeros(b)) ? a : b;
    }

    public static long furthestLeft(long a, long b) {
        return (Long.numberOfTrailingZeros(a) > Long.numberOfTrailingZeros(b)) ? b : a;
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
            if (!tokens[i].trim().equals("_")) {
                bb |= (1L << i);
            }
        }

        return bb;
    }

}
