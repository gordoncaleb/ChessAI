package com.gordoncaleb.chess.board.pieces;

import com.gordoncaleb.chess.board.Board;
import com.gordoncaleb.chess.board.Move;
import com.gordoncaleb.chess.board.MoveContainer;
import com.gordoncaleb.chess.board.Side;

import static com.gordoncaleb.chess.board.bitboard.Slide.*;
import static com.gordoncaleb.chess.board.pieces.Piece.*;
import static com.gordoncaleb.chess.board.Move.MoveNote.*;

public class Bishop {

    public static MoveContainer generateValidMoves(final Piece p,
                                                final Board board,
                                                final long[] nullMoveInfo,
                                                final long[] posBitBoard,
                                                final MoveContainer validMoves) {

        final long friends = posBitBoard[p.getSide()];
        final long foes = posBitBoard[Side.otherSide(p.getSide())];
        final long friendOrFoe = (friends | foes);
        final long footPrint = slideBishop(p.asBitMask(), friendOrFoe) & ~friends;
        final long validFootPrint = footPrint & nullMoveInfo[1] & p.blockingVector();

        buildValidMoves(validFootPrint, p.getRow(), p.getCol(), NORMAL, board, validMoves);

        return validMoves;
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
