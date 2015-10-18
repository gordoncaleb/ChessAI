package com.gordoncaleb.chess.pieces;

import java.util.List;

import com.gordoncaleb.chess.bitboard.BitBoard;
import com.gordoncaleb.chess.backend.Board;

public class Knight {

    public static String getName() {
        return "Knight";
    }

    public static String getStringID() {
        return "N";
    }

    public static List<Long> generateValidMoves(final Piece p, final Board board, final long[] nullMoveInfo, final long[] posBitBoard, final List<Long> validMoves) {
        long footPrint = BitBoard.getKnightFootPrintMem(p.getRow(), p.getCol()) & ~posBitBoard[p.getSide().ordinal()];
        return Piece.generateValidMoves(footPrint, p, board, nullMoveInfo, posBitBoard, validMoves);
    }

}
