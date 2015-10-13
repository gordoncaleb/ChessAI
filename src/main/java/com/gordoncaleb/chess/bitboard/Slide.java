package com.gordoncaleb.chess.bitboard;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.gordoncaleb.chess.bitboard.BitBoard.*;

public class Slide {

    public static final long[][] slideNorth = new long[8][8];
    public static final long[][] slideSouth = new long[8][8];
    public static final long[][] slideWest = new long[8][8];
    public static final long[][] slideEast = new long[8][8];
    public static final long[][] slideNorthWest = new long[8][8];
    public static final long[][] slideNorthEast = new long[8][8];
    public static final long[][] slideSouthWest = new long[8][8];
    public static final long[][] slideSouthEast = new long[8][8];

    static {
        loadSlidingBitBoards();
    }

    public static void loadSlidingBitBoards() {
        loadSlideSouth();
        loadSlideNorth();
        loadSlideEast();
        loadSlideWest();
        loadSlideNorthEast();
        loadSlideNorthWest();
        loadSlideSouthEast();
        loadSlideSouthWest();
    }

    public static long southFill(long gen) {
        gen |= (gen << 8);
        gen |= (gen << 16);
        gen |= (gen << 32);
        return gen;
    }

    public static long southWestFill(long gen) {
        gen |= ((gen & NOT_LEFT1) << 7);
        gen |= ((gen & NOT_LEFT2) << 14);
        gen |= ((gen & NOT_LEFT4) << 28);
        return gen;
    }

    public static long southEastFill(long gen) {
        gen |= ((gen & NOT_RIGHT1) << 9);
        gen |= ((gen & NOT_RIGHT2) << 18);
        gen |= ((gen & NOT_RIGHT4) << 36);
        return gen;
    }

    public static long northWestFill(long gen) {
        gen |= ((gen & NOT_LEFT1) >>> 9);
        gen |= ((gen & NOT_LEFT2) >>> 18);
        gen |= ((gen & NOT_LEFT4) >>> 36);
        return gen;
    }

    public static long northEastFill(long gen) {
        gen |= ((gen & NOT_RIGHT1) >>> 7);
        gen |= ((gen & NOT_RIGHT2) >>> 14);
        gen |= ((gen & NOT_RIGHT4) >>> 28);
        return gen;
    }

    public static long northFill(long gen) {
        gen |= (gen >>> 8);
        gen |= (gen >>> 16);
        gen |= (gen >>> 32);
        return gen;
    }

    public static long westFill(long gen) {
        gen |= ((gen & NOT_LEFT1) >>> 1);
        gen |= ((gen & NOT_LEFT2) >>> 2);
        gen |= ((gen & NOT_LEFT4) >>> 4);
        return gen;
    }

    public static long eastFill(long gen) {
        gen |= ((gen & NOT_RIGHT1) << 1);
        gen |= ((gen & NOT_RIGHT2) << 2);
        gen |= ((gen & NOT_RIGHT4) << 4);
        return gen;
    }

    public static void loadSlideSouth() {
        for (int r = 0; r < 7; r++) {
            for (int c = 0; c < 8; c++) {
                slideSouth[r][c] = getColMask(c) << ((r + 1) * 8);
            }
        }
    }

    public static void loadSlideNorth() {
        for (int r = 1; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                slideNorth[r][c] = getColMask(c) >>> ((8 - r) * 8);
            }
        }
    }

    public static void loadSlideEast() {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 7; c++) {
                long rowMask = getRowMask(r);
                slideEast[r][c] = (rowMask << (c + 1)) & rowMask;
            }
        }
    }

    public static void loadSlideWest() {
        for (int r = 0; r < 8; r++) {
            for (int c = 1; c < 8; c++) {
                long rowMask = getRowMask(r);
                slideWest[r][c] = (rowMask >>> (8 - c)) & rowMask;
            }
        }
    }

    public static void loadSlideNorthWest() {
        for (int r = 1; r < 8; r++) {
            for (int c = 1; c < 8; c++) {
                slideNorthWest[r][c] = northWestFill(getMask(r - 1, c - 1));
            }
        }
    }

    public static void loadSlideNorthEast() {
        for (int r = 1; r < 8; r++) {
            for (int c = 0; c < 7; c++) {
                slideNorthEast[r][c] = northEastFill(getMask(r - 1, c + 1));
            }
        }
    }

    public static void loadSlideSouthWest() {
        for (int r = 0; r < 7; r++) {
            for (int c = 1; c < 8; c++) {
                slideSouthWest[r][c] = southWestFill(getMask(r + 1, c - 1));
            }
        }
    }

    public static void loadSlideSouthEast() {
        for (int r = 0; r < 7; r++) {
            for (int c = 0; c < 7; c++) {
                slideSouthEast[r][c] = southEastFill(getMask(r + 1, c + 1));
            }
        }
    }

    public static long slideSouth(int r, int c, long friendOrFoe, long friend) {
        long a = slideSouth[r][c] & friendOrFoe | TOP_BIT;
        int n = Long.numberOfTrailingZeros(a);
        long mask = (1L << n);
        return slideSouth[r][c] & ((mask - 1L) | mask) & ~friend;
    }

    public static long slideSouthWest(int r, int c, long friendOrFoe, long friend) {
        long a = slideSouthWest[r][c] & friendOrFoe | TOP_BIT;
        int n = Long.numberOfTrailingZeros(a);
        long mask = (1L << n);
        return slideSouthWest[r][c] & ((mask - 1L) | mask) & ~friend;
    }

    public static long slideSouthEast(int r, int c, long friendOrFoe, long friend) {
        long a = slideSouthEast[r][c] & friendOrFoe | TOP_BIT;
        int n = Long.numberOfTrailingZeros(a);
        long mask = (1L << n);
        return slideSouthEast[r][c] & ((mask - 1L) | mask) & ~friend;
    }

    public static long slideEast(int r, int c, long friendOrFoe, long friend) {
        long a = slideEast[r][c] & friendOrFoe | TOP_BIT;
        int n = Long.numberOfTrailingZeros(a);
        long mask = (1L << n);
        return slideEast[r][c] & ((mask - 1L) | mask) & ~friend;
    }

    public static long slideNorth(int r, int c, long friendOrFoe, long friend) {
        long a = slideNorth[r][c] & friendOrFoe | 1L;
        int n = Long.numberOfLeadingZeros(a);
        return slideNorth[r][c] & (TOP_BIT >> n) & ~friend;
    }

    public static long slideNorthWest(int r, int c, long friendOrFoe, long friend) {
        long a = (slideNorthWest[r][c] & friendOrFoe) | 1L;
        int n = Long.numberOfLeadingZeros(a);
        return slideNorthWest[r][c] & (TOP_BIT >> n) & ~friend;
    }

    public static long slideNorthEast(int r, int c, long friendOrFoe, long friend) {
        long a = slideNorthEast[r][c] & friendOrFoe | 1L;
        int n = Long.numberOfLeadingZeros(a);
        return slideNorthEast[r][c] & (TOP_BIT >> n) & ~friend;
    }

    public static long slideWest(int r, int c, long friendOrFoe, long friend) {
        long a = slideWest[r][c] & friendOrFoe | 1L;
        int n = Long.numberOfLeadingZeros(a);
        return slideWest[r][c] & (TOP_BIT >> n) & ~friend;
    }

}
