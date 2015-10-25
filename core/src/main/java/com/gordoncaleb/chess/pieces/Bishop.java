package com.gordoncaleb.chess.pieces;

import java.util.List;

import com.gordoncaleb.chess.backend.Board;

import static com.gordoncaleb.chess.bitboard.Slide.*;

public class Bishop {

    public static String getStringID() {
        return "B";
    }

    public static List<Long> generateValidMoves(final Piece p,
                                                final Board board,
                                                final long[] nullMoveInfo,
                                                final long[] posBitBoard,
                                                final List<Long> validMoves) {

        final long friend = posBitBoard[p.getSide()];
        final long friendOrFoe = (posBitBoard[0] | posBitBoard[1]);
        final long footPrint = slideBishop(p.getBit(), friendOrFoe) & ~friend;
        return Piece.generateValidMoves(footPrint, p, board, nullMoveInfo, posBitBoard, validMoves);
    }

    public static long slideBishop(final long me, final long friendOrFoe) {
        final long allExceptMe = friendOrFoe & ~me;
        return northWestFillAndSlide(me, allExceptMe) |
                northEastFillAndSlide(me, allExceptMe) |
                southWestFillAndSlide(me, allExceptMe) |
                southEastFillAndSlide(me, allExceptMe);
    }

    public static void getBishopAttacks(long bishops,
                                        final long friendOrFoe,
                                        final long[] nullMoveInfo) {
        long mask;
        while ((mask = Long.lowestOneBit(bishops)) != 0) {
            nullMoveInfo[0] |= slideBishop(mask, friendOrFoe) & ~mask;
            bishops ^= mask;
        }
    }

}
