package com.gordoncaleb.chess.pieces;

import java.util.List;

import com.gordoncaleb.chess.bitboard.BitBoard;
import com.gordoncaleb.chess.backend.Board;

import static com.gordoncaleb.chess.bitboard.BitBoard.*;

public class Knight {

    public static String getName() {
        return "Knight";
    }

    public static String getStringID() {
        return "N";
    }

    public static List<Long> generateValidMoves(final Piece p,
                                                final Board board,
                                                final long[] nullMoveInfo,
                                                final long[] posBitBoard,
                                                final List<Long> validMoves) {
        long footPrint = BitBoard.getKnightFootPrintMem(p.getRow(), p.getCol()) & ~posBitBoard[p.getSide()];
        return Piece.generateValidMoves(footPrint, p, board, nullMoveInfo, posBitBoard, validMoves);
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
