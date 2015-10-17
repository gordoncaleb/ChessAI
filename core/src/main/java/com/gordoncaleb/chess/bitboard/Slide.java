package com.gordoncaleb.chess.bitboard;

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

    public static long slideSouth(final int r, final int c, final long friendOrFoe, final long friend) {
        final long a = slideSouth[r][c] & friendOrFoe | TOP_BIT;
        return slideSouth[r][c] & maskUpToBottomBit(a) & ~friend;
    }

    public static long slideSouthWest(final int r, final int c, final long friendOrFoe, final long friend) {
        final long a = slideSouthWest[r][c] & friendOrFoe | TOP_BIT;
        return slideSouthWest[r][c] & maskUpToBottomBit(a) & ~friend;
    }

    public static long slideSouthEast(final int r, final int c, final long friendOrFoe, final long friend) {
        final long a = slideSouthEast[r][c] & friendOrFoe | TOP_BIT;
        return slideSouthEast[r][c] & maskUpToBottomBit(a) & ~friend;
    }

    public static long slideEast(final int r, final int c, final long friendOrFoe, final long friend) {
        final long a = slideEast[r][c] & friendOrFoe | TOP_BIT;
        return slideEast[r][c] & maskUpToBottomBit(a) & ~friend;
    }

    public static long slideNorth(final int r, final int c, final long friendOrFoe, final long friend) {
        final long a = slideNorth[r][c] & friendOrFoe | BOT_BIT;
        return slideNorth[r][c] & maskBeyondTopBit(a) & ~friend;
    }

    public static long slideNorthWest(final int r, final int c, final long friendOrFoe, final long friend) {
        final long a = (slideNorthWest[r][c] & friendOrFoe) | BOT_BIT;
        return slideNorthWest[r][c] & maskBeyondTopBit(a) & ~friend;
    }

    public static long slideNorthEast(final int r, final int c, final long friendOrFoe, final long friend) {
        final long a = slideNorthEast[r][c] & friendOrFoe | BOT_BIT;
        return slideNorthEast[r][c] & maskBeyondTopBit(a) & ~friend;
    }

    public static long slideWest(final int r, final int c, final long friendOrFoe, final long friend) {
        final long a = slideWest[r][c] & friendOrFoe | BOT_BIT;
        return slideWest[r][c] & maskBeyondTopBit(a) & ~friend;
    }

    public static long south(final long mask, final long friendOrFoeNotMe, final long friend) {
        final long south = southFill(mask);
        final long a = south & friendOrFoeNotMe | TOP_BIT;
        return south & maskUpToBottomBit(a) & ~friend;
    }

    public static long southWest(final long mask, final long friendOrFoeNotMe, final long friend) {
        final long southWest = southWestFill(mask);
        final long a = southWest & friendOrFoeNotMe | TOP_BIT;
        return southWest & maskUpToBottomBit(a) & ~friend;
    }

    public static long southEast(final long mask, final long friendOrFoeNotMe, final long friend) {
        final long southEast = southEastFill(mask);
        final long a = southEast & friendOrFoeNotMe | TOP_BIT;
        return southEast & maskUpToBottomBit(a) & ~friend;
    }

    public static long east(final long mask, final long friendOrFoeNotMe, final long friend) {
        final long east = eastFill(mask);
        final long a = east & friendOrFoeNotMe | TOP_BIT;
        return east & maskUpToBottomBit(a) & ~friend;
    }

    public static long north(final long mask, final long friendOrFoeNotMe, final long friend) {
        final long north = northFill(mask);
        final long a = north & friendOrFoeNotMe | BOT_BIT;
        return north & maskBeyondTopBit(a) & ~friend;
    }

    public static long northWest(final long mask, final long friendOrFoeNotMe, final long friend) {
        final long northWest = northWestFill(mask);
        final long a = northWest & friendOrFoeNotMe | BOT_BIT;
        return northWest & maskBeyondTopBit(a) & ~friend;
    }

    public static long northEast(final long mask, final long friendOrFoeNotMe, final long friend) {
        final long northEast = northEastFill(mask);
        final long a = northEast & friendOrFoeNotMe | BOT_BIT;
        return northEast & maskBeyondTopBit(a) & ~friend;
    }

    public static long west(final long mask, final long friendOrFoeNotMe, final long friend) {
        final long west = westFill(mask);
        final long a = west & friendOrFoeNotMe | BOT_BIT;
        return west & maskBeyondTopBit(a) & ~friend;
    }

    //leadingZeros are high zeros
    //trailingZeros are low zeros
    public static long maskUpToBottomBit(long n) {
        return (n - 1) ^ n;
    }

    public static long maskBeyondTopBit(long n) {
        return (TOP_BIT >> Long.numberOfLeadingZeros(n));
    }

    public static long slideAllDirections(final int r, final int c, final long friendOrFoe, final long friend) {
        return slideNorth(r, c, friendOrFoe, friend) |
                slideSouth(r, c, friendOrFoe, friend) |
                slideWest(r, c, friendOrFoe, friend) |
                slideEast(r, c, friendOrFoe, friend) |
                slideNorthWest(r, c, friendOrFoe, friend) |
                slideNorthEast(r, c, friendOrFoe, friend) |
                slideSouthWest(r, c, friendOrFoe, friend) |
                slideSouthEast(r, c, friendOrFoe, friend);
    }

    public static long slideAllDirectionsGen(final int r, final int c, final long friendOrFoe, final long friend) {
        final long mask = getMask(r, c);
        final long friendOrFoeNotMe = friendOrFoe & ~mask;
        return north(mask, friendOrFoeNotMe, friend) |
                south(mask, friendOrFoeNotMe, friend) |
                west(mask, friendOrFoeNotMe, friend) |
                east(mask, friendOrFoeNotMe, friend) |
                northWest(mask, friendOrFoeNotMe, friend) |
                northEast(mask, friendOrFoeNotMe, friend) |
                southWest(mask, friendOrFoeNotMe, friend) |
                southEast(mask, friendOrFoeNotMe, friend);
    }

}
