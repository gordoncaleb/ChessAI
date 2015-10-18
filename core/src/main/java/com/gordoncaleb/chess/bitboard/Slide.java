package com.gordoncaleb.chess.bitboard;

import static com.gordoncaleb.chess.bitboard.BitBoard.*;

public class Slide {

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

    public static long south(final long mask, final long friendOrFoeNotMe) {
        final long south = southFill(mask);
        final long a = south & friendOrFoeNotMe | TOP_BIT;
        return south & maskUpToBottomBit(a);
    }

    public static long southWest(final long mask, final long friendOrFoeNotMe) {
        final long southWest = southWestFill(mask);
        final long a = southWest & friendOrFoeNotMe | TOP_BIT;
        return southWest & maskUpToBottomBit(a);
    }

    public static long southEast(final long mask, final long friendOrFoeNotMe) {
        final long southEast = southEastFill(mask);
        final long a = southEast & friendOrFoeNotMe | TOP_BIT;
        return southEast & maskUpToBottomBit(a);
    }

    public static long east(final long mask, final long friendOrFoeNotMe) {
        final long east = eastFill(mask);
        final long a = east & friendOrFoeNotMe | TOP_BIT;
        return east & maskUpToBottomBit(a);
    }

    public static long north(final long mask, final long friendOrFoeNotMe) {
        final long north = northFill(mask);
        final long a = north & friendOrFoeNotMe | BOT_BIT;
        return north & maskBeyondTopBit(a);
    }

    public static long northWest(final long mask, final long friendOrFoeNotMe) {
        final long northWest = northWestFill(mask);
        final long a = northWest & friendOrFoeNotMe | BOT_BIT;
        return northWest & maskBeyondTopBit(a);
    }

    public static long northEast(final long mask, final long friendOrFoeNotMe) {
        final long northEast = northEastFill(mask);
        final long a = northEast & friendOrFoeNotMe | BOT_BIT;
        return northEast & maskBeyondTopBit(a);
    }

    public static long west(final long mask, final long friendOrFoeNotMe) {
        final long west = westFill(mask);
        final long a = west & friendOrFoeNotMe | BOT_BIT;
        return west & maskBeyondTopBit(a);
    }

    public static long maskUpToBottomBit(long n) {
        return (n - 1) ^ n;
    }

    public static long maskBeyondTopBit(long n) {
        return (TOP_BIT >> Long.numberOfLeadingZeros(n));
    }

    public static long slideBishop(final int r, final int c, final long friendOrFoe, final long friend) {
        final long mask = getMask(r, c);
        final long friendOrFoeNotMe = friendOrFoe & ~mask;
        final long slide = northWest(mask, friendOrFoeNotMe) |
                northEast(mask, friendOrFoeNotMe) |
                southWest(mask, friendOrFoeNotMe) |
                southEast(mask, friendOrFoeNotMe);
        return slide & ~friend;
    }

}
