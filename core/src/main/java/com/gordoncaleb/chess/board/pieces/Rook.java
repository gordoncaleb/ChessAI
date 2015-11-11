package com.gordoncaleb.chess.board.pieces;

import java.util.List;

import com.gordoncaleb.chess.board.Board;
import com.gordoncaleb.chess.board.Move;
import com.gordoncaleb.chess.board.Side;

import static com.gordoncaleb.chess.board.bitboard.Slide.*;
import static com.gordoncaleb.chess.board.pieces.Piece.buildValidMoves;

public class Rook {

    public static List<Move> generateValidMoves(final Piece p,
                                                final Board board,
                                                final long[] nullMoveInfo,
                                                final long[] posBitBoard,
                                                final List<Move> validMoves) {

        final long friends = posBitBoard[p.getSide()];
        final long foes = posBitBoard[Side.otherSide(p.getSide())];
        final long friendOrFoe = (friends | foes);
        final long footPrint = slideRook(p.asBitMask(), friendOrFoe) & ~friends;
        final long validFootPrint = footPrint & nullMoveInfo[1] & p.blockingVector();

        buildValidMoves(validFootPrint, p.getRow(), p.getCol(), Move.MoveNote.NONE, board, validMoves);

        return validMoves;
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
