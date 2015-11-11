package com.gordoncaleb.chess.board.pieces;

import java.util.List;

import com.gordoncaleb.chess.board.Move;
import com.gordoncaleb.chess.board.Side;
import com.gordoncaleb.chess.board.Board;

import static com.gordoncaleb.chess.board.bitboard.BitBoard.*;
import static com.gordoncaleb.chess.board.pieces.Piece.buildValidMoves;

public class Knight {

    public static List<Move> generateValidMoves(final Piece p,
                                                final Board board,
                                                final long[] nullMoveInfo,
                                                final long[] posBitBoard,
                                                final List<Move> validMoves) {

        final long footPrint = getKnightAttacks(p.asBitMask()) & ~posBitBoard[p.getSide()];
        final long validFootPrint = footPrint & nullMoveInfo[1] & p.blockingVector();

        buildValidMoves(validFootPrint, p.getRow(), p.getCol(), Move.MoveNote.NONE, board, validMoves);

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
