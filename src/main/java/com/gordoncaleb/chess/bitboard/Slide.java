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

    public static long genSouth(int r, int c) {
        final long mask = getMask(r, c);
        return southFill(mask) & ~mask;
    }

    public static void loadSlideSouth() {
        for (int r = 0; r < 7; r++) {
            for (int c = 0; c < 8; c++) {
                slideSouth[r][c] = getColMask(c) << ((r + 1) * 8);
            }
        }
    }

    public static long genNorth(int r, int c) {
        final long mask = getMask(r, c);
        return northFill(mask) & ~mask;
    }

    public static void loadSlideNorth() {
        for (int r = 1; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                slideNorth[r][c] = getColMask(c) >>> ((8 - r) * 8);
            }
        }
    }

    public static long genEast(int r, int c) {
        final long mask = getMask(r, c);
        return eastFill(mask) & ~mask;
    }

    public static void loadSlideEast() {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 7; c++) {
                long rowMask = getRowMask(r);
                slideEast[r][c] = (rowMask << (c + 1)) & rowMask;
            }
        }
    }

    public static long genWest(int r, int c) {
        final long mask = getMask(r, c);
        return westFill(mask) & ~mask;
    }

    public static void loadSlideWest() {
        for (int r = 0; r < 8; r++) {
            for (int c = 1; c < 8; c++) {
                long rowMask = getRowMask(r);
                slideWest[r][c] = (rowMask >>> (8 - c)) & rowMask;
            }
        }
    }

    public static long genNorthWest(int r, int c) {
        final long mask = getMask(r, c);
        return northWestFill(mask) & ~mask;
    }

    public static void loadSlideNorthWest() {
        for (int r = 1; r < 8; r++) {
            for (int c = 1; c < 8; c++) {
                slideNorthWest[r][c] = northWestFill(getMask(r - 1, c - 1));
            }
        }
    }

    public static long genNorthEast(int r, int c) {
        final long mask = getMask(r, c);
        return northEastFill(mask) & ~mask;
    }

    public static void loadSlideNorthEast() {
        for (int r = 1; r < 8; r++) {
            for (int c = 0; c < 7; c++) {
                slideNorthEast[r][c] = northEastFill(getMask(r - 1, c + 1));
            }
        }
    }

    public static long genSouthWest(int r, int c) {
        final long mask = getMask(r, c);
        return southWestFill(mask) & ~mask;
    }

    public static void loadSlideSouthWest() {
        for (int r = 0; r < 7; r++) {
            for (int c = 1; c < 8; c++) {
                slideSouthWest[r][c] = southWestFill(getMask(r + 1, c - 1));
            }
        }
    }

    public static long genSouthEast(int r, int c) {
        final long mask = getMask(r, c);
        return southEastFill(mask) & ~mask;
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
        final int n = Long.numberOfTrailingZeros(a);
        final long mask = (1L << n);
        return slideSouth[r][c] & ((mask - 1L) | mask) & ~friend;
    }

    public static long slideSouthWest(final int r, final int c, final long friendOrFoe, final long friend) {
        final long a = slideSouthWest[r][c] & friendOrFoe | TOP_BIT;
        final int n = Long.numberOfTrailingZeros(a);
        final long mask = (1L << n);
        return slideSouthWest[r][c] & ((mask - 1L) | mask) & ~friend;
    }

    public static long slideSouthEast(final int r, final int c, final long friendOrFoe, final long friend) {
        final long a = slideSouthEast[r][c] & friendOrFoe | TOP_BIT;
        final int n = Long.numberOfTrailingZeros(a);
        final long mask = (1L << n);
        return slideSouthEast[r][c] & ((mask - 1L) | mask) & ~friend;
    }

    public static long slideEast(final int r, final int c, final long friendOrFoe, final long friend) {
        final long a = slideEast[r][c] & friendOrFoe | TOP_BIT;
        final int n = Long.numberOfTrailingZeros(a);
        final long mask = (1L << n);
        return slideEast[r][c] & ((mask - 1L) | mask) & ~friend;
    }

    public static long slideNorth(final int r, final int c, final long friendOrFoe, final long friend) {
        final long a = slideNorth[r][c] & friendOrFoe | BOT_BIT;
        final int n = Long.numberOfLeadingZeros(a);
        return slideNorth[r][c] & (TOP_BIT >> n) & ~friend;
    }

    public static long slideNorthWest(final int r, final int c, final long friendOrFoe, final long friend) {
        final long a = (slideNorthWest[r][c] & friendOrFoe) | BOT_BIT;
        final int n = Long.numberOfLeadingZeros(a);
        return slideNorthWest[r][c] & (TOP_BIT >> n) & ~friend;
    }

    public static long slideNorthEast(final int r, final int c, final long friendOrFoe, final long friend) {
        final long a = slideNorthEast[r][c] & friendOrFoe | BOT_BIT;
        final int n = Long.numberOfLeadingZeros(a);
        return slideNorthEast[r][c] & (TOP_BIT >> n) & ~friend;
    }

    public static long slideWest(final int r, final int c, final long friendOrFoe, final long friend) {
        final long a = slideWest[r][c] & friendOrFoe | BOT_BIT;
        final int n = Long.numberOfLeadingZeros(a);
        return slideWest[r][c] & (TOP_BIT >> n) & ~friend;
    }

    public static long south(final int r, final int c, final long friendOrFoe, final long friend) {
        final long south = genSouth(r, c);
        final long a = south & friendOrFoe | TOP_BIT;
        final int n = Long.numberOfTrailingZeros(a);
        final long mask = (1L << n);
        return south & ((mask - 1L) | mask) & ~friend;
    }

    public static long southWest(final int r, final int c, final long friendOrFoe, final long friend) {
        final long southWest = genSouthWest(r, c);
        final long a = southWest & friendOrFoe | TOP_BIT;
        final int n = Long.numberOfTrailingZeros(a);
        final long mask = (1L << n);
        return southWest & ((mask - 1L) | mask) & ~friend;
    }

    public static long southEast(final int r, final int c, final long friendOrFoe, final long friend) {
        final long southEast = genSouthEast(r, c);
        final long a = southEast & friendOrFoe | TOP_BIT;
        final int n = Long.numberOfTrailingZeros(a);
        final long mask = (1L << n);
        return southEast & ((mask - 1L) | mask) & ~friend;
    }

    public static long east(final int r, final int c, final long friendOrFoe, final long friend) {
        final long east = genEast(r, c);
        final long a = east & friendOrFoe | TOP_BIT;
        final int n = Long.numberOfTrailingZeros(a);
        final long mask = (1L << n);
        return east & ((mask - 1L) | mask) & ~friend;
    }

    public static long north(final int r, final int c, final long friendOrFoe, final long friend) {
        final long north = genNorth(r, c);
        final long a = north & friendOrFoe | BOT_BIT;
        final int n = Long.numberOfLeadingZeros(a);
        return north & (TOP_BIT >> n) & ~friend;
    }

    public static long northWest(final int r, final int c, final long friendOrFoe, final long friend) {
        final long northWest = genNorthWest(r, c);
        final long a = northWest & friendOrFoe | BOT_BIT;
        final int n = Long.numberOfLeadingZeros(a);
        return northWest & (TOP_BIT >> n) & ~friend;
    }

    public static long northEast(final int r, final int c, final long friendOrFoe, final long friend) {
        final long northEast = genNorthEast(r, c);
        final long a = northEast & friendOrFoe | BOT_BIT;
        final int n = Long.numberOfLeadingZeros(a);
        return northEast & (TOP_BIT >> n) & ~friend;
    }

    public static long west(final int r, final int c, final long friendOrFoe, final long friend) {
        final long west = genWest(r, c);
        final long a = west & friendOrFoe | BOT_BIT;
        final int n = Long.numberOfLeadingZeros(a);
        return west & (TOP_BIT >> n) & ~friend;
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

        //North
        final long north = genNorth(r, c);
        final long aN = north & friendOrFoe | BOT_BIT;
        final int nN = Long.numberOfLeadingZeros(aN);
        long allDir = north & (TOP_BIT >> nN) & ~friend;

        //South
        final long south = genSouth(r, c);
        final long aS = south & friendOrFoe | TOP_BIT;
        final int nS = Long.numberOfTrailingZeros(aS);
        final long maskS = (1L << nS);
        allDir |= south & ((maskS - 1L) | maskS) & ~friend;

        //East
        final long east = genEast(r, c);
        final long aE = east & friendOrFoe | TOP_BIT;
        final int nE = Long.numberOfTrailingZeros(aE);
        final long maskE = (1L << nE);
        allDir |= east & ((maskE - 1L) | maskE) & ~friend;

        //West
        final long west = genWest(r, c);
        final long aW = west & friendOrFoe | BOT_BIT;
        final int nW = Long.numberOfLeadingZeros(aW);
        allDir |= west & (TOP_BIT >> nW) & ~friend;

        //NW
        final long northWest = genNorthWest(r, c);
        final long aNW = northWest & friendOrFoe | BOT_BIT;
        final int nNW = Long.numberOfLeadingZeros(aNW);
        allDir |= northWest & (TOP_BIT >> nNW) & ~friend;

        //NE
        final long northEast = genNorthEast(r, c);
        final long aNE = northEast & friendOrFoe | BOT_BIT;
        final int nNE = Long.numberOfLeadingZeros(aNE);
        allDir |= northEast & (TOP_BIT >> nNE) & ~friend;

        //SW
        final long southWest = genSouthWest(r, c);
        final long aSW = southWest & friendOrFoe | TOP_BIT;
        final int nSW = Long.numberOfTrailingZeros(aSW);
        final long maskSW = (1L << nSW);
        allDir |= southWest & ((maskSW - 1L) | maskSW) & ~friend;

        //SE
        final long southEast = genSouthEast(r, c);
        final long aSE = southEast & friendOrFoe | TOP_BIT;
        final int nSE = Long.numberOfTrailingZeros(aSE);
        final long maskSE = (1L << nSE);
        allDir |= southEast & ((maskSE - 1L) | maskSE) & ~friend;

        return allDir;
    }

}
