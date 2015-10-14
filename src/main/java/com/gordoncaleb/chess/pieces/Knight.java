package com.gordoncaleb.chess.pieces;

import java.util.List;

import com.gordoncaleb.chess.bitboard.BitBoard;
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

    public static List<Long> generateValidMoves2(final Piece p, final Board board, final long[] nullMoveInfo, final long[] posBitBoard, final List<Long> validMoves) {
        long footPrint = BitBoard.getKnightFootPrintMem(p.getRow(), p.getCol()) & ~posBitBoard[p.getSide().ordinal()];
        final long foeBb = posBitBoard[p.getSide().otherSide().ordinal()];

        int bitNum;
        while ((bitNum = Long.numberOfTrailingZeros(footPrint)) < 64) {
            final long mask = (1L << bitNum);
            final int toRow = bitNum / 8;
            final int toCol = bitNum % 8;

            if (p.isValidMove(toRow, toCol, nullMoveInfo)) {

                final long move = ((foeBb & mask) == 0) ?
                        Move.moveLong(p.getRow(), p.getCol(), toRow, toCol) :
                        Move.moveLong(p.getRow(), p.getCol(), toRow, toCol, 0, Move.MoveNote.NONE, board.getPiece(toRow, toCol));

                validMoves.add(move);
            }

            footPrint ^= mask;
        }

        return validMoves;
    }

    public static List<Long> generateValidMoves(final Piece p, final Board board, final long[] nullMoveInfo, final long[] posBitBoard, final List<Long> validMoves) {
        final int currentRow = p.getRow();
        final int currentCol = p.getCol();
        final Side player = p.getSide();

        for (int i = 0; i < 8; i++) {
            final int nextRow = currentRow + KNIGHTMOVES[0][i];
            final int nextCol = currentCol + KNIGHTMOVES[1][i];
            final Piece.PositionStatus pieceStatus = board.checkPiece(nextRow, nextCol, player);

            if (pieceStatus != Piece.PositionStatus.OFF_BOARD) {

                long moveLong;
                if (pieceStatus == Piece.PositionStatus.NO_PIECE) {
                    if (p.isValidMove(nextRow, nextCol, nullMoveInfo)) {
                        moveLong = Move.moveLong(currentRow, currentCol, nextRow, nextCol, 0);
                        validMoves.add(moveLong);
                    }
                }

                if (pieceStatus == Piece.PositionStatus.ENEMY) {
                    if (p.isValidMove(nextRow, nextCol, nullMoveInfo)) {
                        moveLong = Move.moveLong(currentRow, currentCol, nextRow, nextCol, 0, Move.MoveNote.NONE, board.getPiece(nextRow, nextCol));
                        validMoves.add(moveLong);
                    }
                }

            }
        }

        return validMoves;
    }

}
