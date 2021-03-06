package com.gordoncaleb.chess.board.pieces;

import com.gordoncaleb.chess.board.MoveContainer;
import com.gordoncaleb.chess.board.Board;

import static com.gordoncaleb.chess.board.Board.CHECK_VECTORS;
import static com.gordoncaleb.chess.board.bitboard.BitBoard.*;
import static com.gordoncaleb.chess.board.Move.MoveNote.*;
import static com.gordoncaleb.chess.board.pieces.Piece.buildValidMoves;

public class Knight {

    public static MoveContainer generateValidMoves(final Piece p,
                                                   final Board board,
                                                   final long[] nullMoveInfo,
                                                   final long[] posBitBoard,
                                                   final MoveContainer validMoves) {

        final long footPrint = getKnightAttacks(p.asBitMask()) & ~posBitBoard[p.getSide()];
        final long validFootPrint = footPrint & nullMoveInfo[CHECK_VECTORS] & p.blockingVector();

        buildValidMoves(validFootPrint, p.getRow(), p.getCol(), NORMAL, board, validMoves);

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
