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

    public static List<Long> generateValidMoves2(Piece p, Board board, long[] nullMoveInfo, long[] posBitBoard, List<Long> validMoves) {
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

    public static List<Long> generateValidMoves(Piece p, Board board, long[] nullMoveInfo, long[] posBitBoard, List<Long> validMoves) {
        int currentRow = p.getRow();
        int currentCol = p.getCol();
        int nextRow;
        int nextCol;
        Piece.PositionStatus pieceStatus;
        Side player = p.getSide();
        Long moveLong;

        for (int i = 0; i < 8; i++) {
            nextRow = currentRow + KNIGHTMOVES[0][i];
            nextCol = currentCol + KNIGHTMOVES[1][i];
            pieceStatus = board.checkPiece(nextRow, nextCol, player);

            if (pieceStatus != Piece.PositionStatus.OFF_BOARD) {

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
