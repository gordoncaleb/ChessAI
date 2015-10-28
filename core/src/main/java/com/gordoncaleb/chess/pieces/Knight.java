package com.gordoncaleb.chess.pieces;

import java.util.List;

import com.gordoncaleb.chess.board.Side;
import com.gordoncaleb.chess.board.Board;

import static com.gordoncaleb.chess.board.bitboard.BitBoard.*;
import static com.gordoncaleb.chess.pieces.Piece.buildValidMoves;
import static com.gordoncaleb.chess.pieces.Piece.buildValidMovesWithPiecesTaken;

public class Knight {

    public static String getStringID() {
        return "N";
    }

    public static List<Long> generateValidMoves(final Piece p,
                                                final Board board,
                                                final long[] nullMoveInfo,
                                                final long[] posBitBoard,
                                                final List<Long> validMoves) {
        final long foes = posBitBoard[Side.otherSide(p.getSide())];
        final long footPrint = getKnightAttacks(p.getBit()) & ~posBitBoard[p.getSide()];

        final long validFootPrint = footPrint & nullMoveInfo[1] & p.getBlockingVector();
        final long validFootPrintWithPiecesTaken = validFootPrint & foes;
        final long validFootPrintWoPiecesTaken = validFootPrint & ~foes;

        buildValidMovesWithPiecesTaken(validFootPrintWithPiecesTaken, p.getRow(), p.getCol(), board, validMoves);
        buildValidMoves(validFootPrintWoPiecesTaken, p.getRow(), p.getCol(), validMoves);

        return validMoves;
    }

    public static long getKnightAttacks(long knights) {
        return ((knights & NOT_LEFT2) << 6) | // down 1 left 2
                ((knights & NOT_LEFT2) >>> 10) | // up 1 left 2
                ((knights & NOT_LEFT1) << 15) | // down 2 left 1
                ((knights & NOT_LEFT1) >>> 17) | // up 2 left 1
                ((knights & NOT_RIGHT2) >>> 6) | // up 1 right 2
                ((knights & NOT_RIGHT2) << 10) | // down 1 right 2
                ((knights & NOT_RIGHT1) >>> 15) | // up 2 right 1
                ((knights & NOT_RIGHT1) << 17);
    }

}
