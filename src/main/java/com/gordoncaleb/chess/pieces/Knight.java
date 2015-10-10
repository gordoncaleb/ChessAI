package com.gordoncaleb.chess.pieces;

import java.util.List;

import com.gordoncaleb.chess.backend.BitBoard;
import com.gordoncaleb.chess.backend.Board;
import com.gordoncaleb.chess.backend.Side;
import com.gordoncaleb.chess.backend.Move;

public class Knight {
    private static int[][] KNIGHTMOVES = {{2, 2, -2, -2, 1, -1, 1, -1}, {1, -1, 1, -1, 2, 2, -2, -2}};

    public Knight() {
    }

    public static Piece.PieceID getPieceID() {
        return Piece.PieceID.KNIGHT;
    }

    public static String getName() {
        return "Knight";
    }

    public static String getStringID() {
        return "N";
    }

    public static List<Long> generateValidMoves(Piece p, Board board, long[] nullMoveInfo, long[] posBitBoard, List<Long> validMoves) {
        long footPrint = BitBoard.getKnightFootPrint(p.getRow(), p.getCol()) & ~posBitBoard[p.getSide().ordinal()];
        long foeBb = posBitBoard[p.getSide().otherSide().ordinal()];

        BitBoard.bitNumbers(footPrint).stream()
                .map(n -> enrichMove(board, p.getRow(), p.getCol(), foeBb, n))
                .filter(m -> p.isValidMove(m.getToRow(), m.getToCol(), nullMoveInfo))
                .forEach(m -> validMoves.add(m.getMoveLong()));

        return validMoves;
    }

    private static Move enrichMove(Board b, int fromRow, int fromCol, long foeBb, int bitNum) {
        if ((foeBb & (1L << bitNum)) == 0) {
            return new Move(fromRow, fromCol, bitNum / 8, bitNum % 8);
        } else {
            int toRow = bitNum / 8;
            int toCol = bitNum % 8;
            return new Move(fromRow, fromCol, toRow, toCol, 0, Move.MoveNote.NONE, b.getPiece(toRow, toCol));
        }
    }

}
