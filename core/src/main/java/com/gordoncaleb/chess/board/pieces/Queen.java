package com.gordoncaleb.chess.board.pieces;

import java.util.List;

import com.gordoncaleb.chess.board.Board;
import com.gordoncaleb.chess.board.Move;
import com.gordoncaleb.chess.board.Side;

import static com.gordoncaleb.chess.board.bitboard.Slide.*;
import static com.gordoncaleb.chess.board.pieces.Piece.buildValidMoves;
import static com.gordoncaleb.chess.board.pieces.Piece.buildValidMovesWithPiecesTaken;

public class Queen {

    public static List<Move> generateValidMoves(final Piece p,
                                                final Board board,
                                                final long[] nullMoveInfo,
                                                final long[] posBitBoard,
                                                final List<Move> validMoves) {

        final long friends = posBitBoard[p.getSide()];
        final long foes = posBitBoard[Side.otherSide(p.getSide())];
        final long friendOrFoe = (friends | foes);
        final long footPrint = slideQueen(p.asBitMask(), friendOrFoe) & ~friends;

        final long validFootPrint = footPrint & nullMoveInfo[1] & p.blockingVector();
        final long validFootPrintWithPiecesTaken = validFootPrint & foes;
        final long validFootPrintWoPiecesTaken = validFootPrint & ~foes;

        buildValidMovesWithPiecesTaken(validFootPrintWithPiecesTaken, p.getRow(), p.getCol(), board, validMoves);
        buildValidMoves(validFootPrintWoPiecesTaken, p.getRow(), p.getCol(), validMoves);

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
            nullMoveInfo[0] |= slideQueen(mask, friendOrFoe) & ~mask;
            queens ^= mask;
        }
    }

}
