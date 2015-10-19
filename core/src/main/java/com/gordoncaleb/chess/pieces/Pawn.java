package com.gordoncaleb.chess.pieces;

import java.util.List;

import com.gordoncaleb.chess.bitboard.BitBoard;
import com.gordoncaleb.chess.backend.Board;
import com.gordoncaleb.chess.backend.Side;
import com.gordoncaleb.chess.backend.Move;

public class Pawn {

    public static String getName() {
        return "Pawn";
    }

    public static String getStringID() {
        return "P";
    }

    public static List<Long> generateValidMoves(Piece p, Board board, long[] nullMoveInfo, long[] posBitBoard, List<Long> validMoves) {
        int currentRow = p.getRow();
        int currentCol = p.getCol();
        int player = p.getSide();
        int dir;
        int fifthRank;
        Long moveLong;

        int[] lr = {1, -1};

        if (player == Side.WHITE) {
            dir = -1;
            fifthRank = 3;
        } else {
            dir = 1;
            fifthRank = 4;
        }

        if (board.checkPiece(currentRow + dir, currentCol, player) == Piece.PositionStatus.NO_PIECE) {

            if (p.isValidMove(currentRow + dir, currentCol, nullMoveInfo)) {

                moveLong = Move.moveLong(currentRow, currentCol, currentRow + dir, currentCol, 0, Move.MoveNote.NONE);

                if ((currentRow + dir) == 0 || (currentRow + dir) == 7) {
                    moveLong = Move.setNote(moveLong, Move.MoveNote.NEW_QUEEN);
                }

                validMoves.add(moveLong);
            }

            if (!p.hasMoved() && board.checkPiece(currentRow + 2 * dir, currentCol, player) == Piece.PositionStatus.NO_PIECE) {

                if (p.isValidMove(currentRow + 2 * dir, currentCol, nullMoveInfo)) {

                    validMoves.add(Move.moveLong(currentRow, currentCol, currentRow + 2 * dir, currentCol, 0, Move.MoveNote.PAWN_LEAP));

                }
            }

        }

        // Check left and right attack angles
        for (int i : lr) {
            if (board.checkPiece(currentRow + dir, currentCol + i, player) == Piece.PositionStatus.ENEMY) {

                if (p.isValidMove(currentRow + dir, currentCol + i, nullMoveInfo)) {

                    moveLong = Move.moveLong(currentRow, currentCol, currentRow + dir, currentCol + i);

                    if ((currentRow + dir) == 0 || (currentRow + dir) == 7) {
                        moveLong = Move.setNote(moveLong, Move.MoveNote.NEW_QUEEN);
                    }

                    moveLong = Move.setPieceTaken(moveLong, board.getPiece(currentRow + dir, currentCol + i));
                    validMoves.add(moveLong);
                }

            }
        }

        // Check left and right en passant rule
        if (currentRow == fifthRank && board.getLastMoveMade() != 0) {
            for (int i : lr) {
                if (board.checkPiece(fifthRank, currentCol + i, player) == Piece.PositionStatus.ENEMY) {

                    if ((Move.getToCol(board.getLastMoveMade()) == (currentCol + i)) && Move.getNote(board.getLastMoveMade()) == Move.MoveNote.PAWN_LEAP) {

                        if (p.isValidMove(currentRow + dir, currentCol + i, nullMoveInfo, BitBoard.getMask(fifthRank, currentCol + i))) {

                            moveLong = Move.moveLong(currentRow, currentCol, currentRow + dir, currentCol + i, 0, Move.MoveNote.ENPASSANT, board.getPiece(fifthRank, currentCol + i));
                            validMoves.add(moveLong);
                        }

                    }
                }
            }
        }

        return validMoves;

    }

}
