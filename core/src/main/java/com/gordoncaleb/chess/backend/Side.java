package com.gordoncaleb.chess.backend;

public class Side {

    public static final int BLACK = 0;
    public static final int WHITE = 1;
    public static final int NONE = 2;
    public static final int BOTH = 3;

    public static String toString(int side) {
        switch (side) {
            case WHITE:
                return "WHITE";
            case BLACK:
                return "BLACK";
            case NONE:
                return "NONE";
            case BOTH:
                return "BOTH";
            default:
                return "UNKNOWN";
        }
    }

    public static int fromString(String side) {
        switch (side) {
            case "WHITE":
                return WHITE;
            case "BLACK":
                return BLACK;
            case "NONE":
                return NONE;
            case "BOTH":
                return BOTH;
            default:
                return NONE;
        }
    }

    public static int otherSide(final int side) {
        return side ^ 1;
    }

}
