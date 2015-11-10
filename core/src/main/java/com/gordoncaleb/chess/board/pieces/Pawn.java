package com.gordoncaleb.chess.board.pieces;

import java.util.List;

import com.gordoncaleb.chess.board.Board;
import com.gordoncaleb.chess.board.Side;
import com.gordoncaleb.chess.board.Move;

import static com.gordoncaleb.chess.board.bitboard.BitBoard.*;

public class Pawn {

    public static List<Move> generateValidMoves(Piece p, Board board, long[] nullMoveInfo, long[] posBitBoard, List<Move> validMoves) {
        int currentRow = p.getRow();
        int currentCol = p.getCol();
        int player = p.getSide();
        int dir;
        int fifthRank;
        Move move;

        int[] lr = {1, -1};

        if (player == Side.WHITE) {
            dir = -1;
            fifthRank = 3;
        } else {
            dir = 1;
            fifthRank = 4;
        }

        if (board.checkPiece(currentRow + dir, currentCol, player) == Piece.PositionStatus.NO_PIECE) {

            if (p.checkValidMove(getMask(currentRow + dir, currentCol), nullMoveInfo)) {

                move = new Move(currentRow, currentCol, currentRow + dir, currentCol, 0, Move.MoveNote.NONE);

                if ((currentRow + dir) == 0 || (currentRow + dir) == 7) {
                    move.setNote(Move.MoveNote.NEW_QUEEN);
                }

                validMoves.add(move);
            }

            if (currentRow == Board.PAWN_ROW[player] && board.checkPiece(currentRow + 2 * dir, currentCol, player) == Piece.PositionStatus.NO_PIECE) {

                if (p.checkValidMove(getMask(currentRow + 2 * dir, currentCol), nullMoveInfo)) {

                    validMoves.add(new Move(currentRow, currentCol, currentRow + 2 * dir, currentCol, 0, Move.MoveNote.PAWN_LEAP));

                }
            }

        }

        // Check left and right attack angles
        for (int i : lr) {
            if (board.checkPiece(currentRow + dir, currentCol + i, player) == Piece.PositionStatus.ENEMY) {

                if (p.checkValidMove(getMask(currentRow + dir, currentCol + i), nullMoveInfo)) {

                    move = new Move(currentRow, currentCol, currentRow + dir, currentCol + i);

                    if ((currentRow + dir) == 0 || (currentRow + dir) == 7) {
                        move.setNote(Move.MoveNote.NEW_QUEEN);
                    }

                    move.setPieceTaken(board.getPiece(currentRow + dir, currentCol + i));
                    validMoves.add(move);
                }

            }
        }

        // Check left and right en passant rule
        if (currentRow == fifthRank && board.getLastMoveMade() != null) {
            for (int i : lr) {
                if (board.checkPiece(fifthRank, currentCol + i, player) == Piece.PositionStatus.ENEMY) {

                    if ((board.getLastMoveMade().getToCol() == (currentCol + i)) && board.getLastMoveMade().getNote() == Move.MoveNote.PAWN_LEAP) {

                        long position = getMask(currentRow + dir, currentCol + i);
                        if (p.checkValidMove(nullMoveInfo, position, position | getMask(fifthRank, currentCol + i))) {

                            move = new Move(currentRow, currentCol, currentRow + dir, currentCol + i, 0, Move.MoveNote.ENPASSANT, board.getPiece(fifthRank, currentCol + i));
                            validMoves.add(move);
                        }

                    }
                }
            }
        }

        return validMoves;

    }

    public static long getPawnAttacks(long pawns, int side) {
        if (side == Side.BLACK) {
            return ((pawns & NOT_RIGHT1) << 9) | ((pawns & NOT_LEFT1) << 7);
        } else {
            return ((pawns & NOT_RIGHT1) >>> 7) | ((pawns & NOT_LEFT1) >>> 9);
        }
    }

}
