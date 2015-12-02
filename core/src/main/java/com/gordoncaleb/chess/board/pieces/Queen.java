package com.gordoncaleb.chess.board.pieces;

import com.gordoncaleb.chess.board.Board;
import com.gordoncaleb.chess.board.MoveContainer;
import com.gordoncaleb.chess.board.Side;

import static com.gordoncaleb.chess.board.Board.*;
import static com.gordoncaleb.chess.board.bitboard.Slide.*;
import static com.gordoncaleb.chess.board.Move.MoveNote.*;
import static com.gordoncaleb.chess.board.pieces.Piece.buildValidMoves;

public class Queen {

    public static MoveContainer generateValidMoves(final Piece p,
                                                final Board board,
                                                final long[] nullMoveInfo,
                                                final long[] posBitBoard,
                                                final MoveContainer validMoves) {

        final long friends = posBitBoard[p.getSide()];
        final long foes = posBitBoard[Side.otherSide(p.getSide())];
        final long friendOrFoe = (friends | foes);
        final long footPrint = slideQueen(p.asBitMask(), friendOrFoe) & ~friends;
        final long validFootPrint = footPrint & nullMoveInfo[CHECK_VECTORS] & p.blockingVector();

        buildValidMoves(validFootPrint, p.getRow(), p.getCol(), NORMAL, board, validMoves);

        return validMoves;
    }

    public static long slideQueen(final long me, final long friendOrFoe) {
        final long allExceptMe = friendOrFoe & ~me;
        return northFillAndSlide(me, allExceptMe) |
                southFillAndSlide(me, allExceptMe) |
                westFillAndSlide(me, allExceptMe) |
                eastFillAndSlide(me, allExceptMe) |
                northWestFillAndSlide(me, allExceptMe) |
                northEastFillAndSlide(me, allExceptMe) |
                southWestFillAndSlide(me, allExceptMe) |
                southEastFillAndSlide(me, allExceptMe);
    }

    public static void getQueenAttacks(long queens,
                                       final long friendOrFoe,
                                       final long[] nullMoveInfo) {
        long mask;
        while ((mask = Long.lowestOneBit(queens)) != 0) {
            nullMoveInfo[FOE_ATTACKS] |= slideQueen(mask, friendOrFoe) & ~mask;
            queens ^= mask;
        }
    }

}
