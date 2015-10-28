package com.gordoncaleb.chess.board.pieces;

import java.util.List;

import com.gordoncaleb.chess.board.Board;
import com.gordoncaleb.chess.board.Side;

import static com.gordoncaleb.chess.board.bitboard.Slide.*;
import static com.gordoncaleb.chess.board.pieces.Piece.*;

public class Bishop {

    public static String getStringID() {
        return "B";
    }

    public static List<Long> generateValidMoves(final Piece p,
                                                final Board board,
                                                final long[] nullMoveInfo,
                                                final long[] posBitBoard,
                                                final List<Long> validMoves) {

        final long friends = posBitBoard[p.getSide()];
        final long foes = posBitBoard[Side.otherSide(p.getSide())];
        final long friendOrFoe = (friends | foes);
        final long footPrint = slideBishop(p.getBit(), friendOrFoe) & ~friends;

        final long validFootPrint = footPrint & nullMoveInfo[1] & p.getBlockingVector();
        final long validFootPrintWithPiecesTaken = validFootPrint & foes;
        final long validFootPrintWoPiecesTaken = validFootPrint & ~foes;

        buildValidMovesWithPiecesTaken(validFootPrintWithPiecesTaken, p.getRow(), p.getCol(), board, validMoves);
        buildValidMoves(validFootPrintWoPiecesTaken, p.getRow(), p.getCol(), validMoves);

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
