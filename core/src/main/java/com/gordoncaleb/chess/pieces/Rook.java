package com.gordoncaleb.chess.pieces;

import java.util.List;

import com.gordoncaleb.chess.backend.Board;

import static com.gordoncaleb.chess.bitboard.Slide.*;

public class Rook {

    public static String getStringID() {
        return "R";
    }

    public static List<Long> generateValidMoves(final Piece p,
                                                final Board board,
                                                final long[] nullMoveInfo,
                                                final long[] posBitBoard,
                                                final List<Long> validMoves) {

        final long friend = posBitBoard[p.getSide()];
        final long friendOrFoe = (posBitBoard[0] | posBitBoard[1]);
        final long footPrint = slideRook(p.getBit(), friendOrFoe) & ~friend;
        return Piece.generateValidMoves(footPrint, p, board, nullMoveInfo, posBitBoard, validMoves);
    }

    public static long slideRook(final long me, final long friendOrFoe) {
        final long allExceptMe = friendOrFoe & ~me;
        return northFillAndSlide(me, allExceptMe) |
                southFillAndSlide(me, allExceptMe) |
                westFillAndSlide(me, allExceptMe) |
                eastFillAndSlide(me, allExceptMe);
    }

    public static void getRookAttacks(long rooks,
                                      final long friendOrFoe,
                                      final long[] nullMoveInfo) {
        long mask;
        while ((mask = Long.lowestOneBit(rooks)) != 0) {
            nullMoveInfo[0] |= slideRook(mask, friendOrFoe) & ~mask;
            rooks ^= mask;
        }
    }

}
