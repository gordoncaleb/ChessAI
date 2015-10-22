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

    public static long southSlideNoEdge(final long southFill, final long allExceptMe) {
        final long a = southFill & allExceptMe;
        return southFill & maskUpToBottomBit(a);
    }

    public static long southSlide(final long southFill, final long allExceptMe) {
        final long a = southFill & allExceptMe | TOP_BIT;
        return southFill & maskUpToBottomBit(a);
    }

    public static long southFillAndSlide(final long mask, final long allExceptMe) {
        return southSlide(southFill(mask), allExceptMe);
    }

    public static long southWestSlideNoEdge(final long southWestFill, final long allExceptMe) {
        final long a = southWestFill & allExceptMe;
        return southWestFill & maskUpToBottomBit(a);
    }

    public static long southWestSlide(final long southWestFill, final long allExceptMe) {
        final long a = southWestFill & allExceptMe | TOP_BIT;
        return southWestFill & maskUpToBottomBit(a);
    }

    public static long southWestFillAndSlide(final long mask, final long allExceptMe) {
        return southWestSlide(southWestFill(mask), allExceptMe);
    }

    public static long southEastSlideNoEdge(final long southEastFill, final long allExceptMe) {
        final long a = southEastFill & allExceptMe;
        return southEastFill & maskUpToBottomBit(a);
    }

    public static long southEastSlide(final long southEastFill, final long allExceptMe) {
        final long a = southEastFill & allExceptMe | TOP_BIT;
        return southEastFill & maskUpToBottomBit(a);
    }

    public static long southEastFillAndSlide(final long mask, final long allExceptMe) {
        return southEastSlide(southEastFill(mask), allExceptMe);
    }

    public static long eastSlideNoEdge(final long eastFill, final long allExceptMe) {
        final long a = eastFill & allExceptMe;
        return eastFill & maskUpToBottomBit(a);
    }

    public static long eastSlide(final long eastFill, final long allExceptMe) {
        final long a = eastFill & allExceptMe | TOP_BIT;
        return eastFill & maskUpToBottomBit(a);
    }

    public static long eastFillAndSlide(final long mask, final long allExceptMe) {
        return eastSlide(eastFill(mask), allExceptMe);
    }

    public static long northSlideNoEdge(final long northFill, final long allExceptMe) {
        final long a = northFill & allExceptMe;
        return northFill & maskBeyondTopBit(a);
    }

    public static long northSlide(final long northFill, final long allExceptMe) {
        final long a = northFill & allExceptMe | BOT_BIT;
        return northFill & maskBeyondTopBit(a);
    }

    public static long northFillAndSlide(final long mask, final long allExceptMe) {
        return northSlide(northFill(mask), allExceptMe);
    }

    public static long northWestSlideNoEdge(final long northWestFill, final long allExceptMe) {
        final long a = northWestFill & allExceptMe;
        return northWestFill & maskBeyondTopBit(a);
    }

    public static long northWestSlide(final long northWestFill, final long allExceptMe) {
        final long a = northWestFill & allExceptMe | BOT_BIT;
        return northWestFill & maskBeyondTopBit(a);
    }

    public static long northWestFillAndSlide(final long mask, final long allExceptMe) {
        return northWestSlide(northWestFill(mask), allExceptMe);
    }

    public static long northEastSlideNoEdge(final long northEastFill, final long allExceptMe) {
        final long a = northEastFill & allExceptMe;
        return northEastFill & maskBeyondTopBit(a);
    }

    public static long northEastSlide(final long northEastFill, final long allExceptMe) {
        final long a = northEastFill & allExceptMe | BOT_BIT;
        return northEastFill & maskBeyondTopBit(a);
    }

    public static long northEastFillAndSlide(final long mask, final long allExceptMe) {
        return northEastSlide(northEastFill(mask), allExceptMe);
    }

    public static long westSlideNoEdge(final long westFill, final long allExceptMe) {
        final long a = westFill & allExceptMe;
        return westFill & maskBeyondTopBit(a);
    }

    public static long westSlide(final long westFill, final long allExceptMe) {
        final long a = westFill & allExceptMe | BOT_BIT;
        return westFill & maskBeyondTopBit(a);
    }

    public static long westFillAndSlide(final long mask, final long allExceptMe) {
        return westSlide(westFill(mask), allExceptMe);
    }

    public static long maskUpToBottomBit(long n) {
        return (n - 1) ^ n;
    }

    public static long maskBeyondTopBit(long n) {
        return (TOP_BIT >> Long.numberOfLeadingZeros(n));
    }

}
