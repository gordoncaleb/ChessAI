package com.gordoncaleb.chess.pieces;

import java.util.ArrayList;

import com.gordoncaleb.chess.backend.BitBoard;
import com.gordoncaleb.chess.backend.Board;
import com.gordoncaleb.chess.backend.Side;
import com.gordoncaleb.chess.backend.Move;

public class Pawn {

    public Pawn() {
    }

    public static Piece.PieceID getPieceID() {
        return Piece.PieceID.PAWN;
    }

    public static String getName() {
        return "Pawn";
    }

    public static String getStringID() {
        return "P";
    }

    public static ArrayList<Long> generateValidMoves(Piece p, Board board, long[] nullMoveInfo, long[] posBitBoard, ArrayList<Long> validMoves) {
        int currentRow = p.getRow();
        int currentCol = p.getCol();
        Side player = p.getSide();
        int dir;
        int fifthRank;
        int value;
        int myValue = board.getPieceValue(p.getRow(), p.getCol());
        Long moveLong;


//		logger.debug("pawn " + p.getCol() + " null move info");
//		logger.debug(BitBoard.printBitBoard(nullMoveInfo[0]));
//		logger.debug(BitBoard.printBitBoard(nullMoveInfo[1]));
//		logger.debug(BitBoard.printBitBoard(nullMoveInfo[2]));

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

                value = PositionBonus.getPawnMoveBonus(currentRow, currentCol, currentRow + dir, currentCol, p.getSide());

                if ((currentRow + dir) == 0 || (currentRow + dir) == 7) {
                    moveLong = Move.setNote(moveLong, Move.MoveNote.NEW_QUEEN);
                    value = Values.QUEEN_VALUE;
                }

                if ((nullMoveInfo[0] & BitBoard.getMask(currentRow + dir, currentCol)) != 0) {
                    value = -myValue >> 1;
                }

                moveLong = Move.setValue(moveLong, value);

                validMoves.add(moveLong);

            }

            if (!p.hasMoved() && board.checkPiece(currentRow + 2 * dir, currentCol, player) == Piece.PositionStatus.NO_PIECE) {

                if (p.isValidMove(currentRow + 2 * dir, currentCol, nullMoveInfo)) {

                    value = PositionBonus.getPawnMoveBonus(currentRow, currentCol, currentRow + 2 * dir, currentCol, p.getSide());

                    if ((nullMoveInfo[0] & BitBoard.getMask(currentRow + 2 * dir, currentCol)) != 0) {
                        value = -myValue >> 1;
                    }

                    validMoves.add(Move.moveLong(currentRow, currentCol, currentRow + 2 * dir, currentCol, value, Move.MoveNote.PAWN_LEAP));

                }
            }

        }

        // Check left and right attack angles
        for (int i : lr) {
            if (board.checkPiece(currentRow + dir, currentCol + lr[i], player) == Piece.PositionStatus.ENEMY) {

                if (p.isValidMove(currentRow + dir, currentCol + lr[i], nullMoveInfo)) {

                    moveLong = Move.moveLong(currentRow, currentCol, currentRow + dir, currentCol + lr[i]);

                    value = PositionBonus.getPawnMoveBonus(currentRow, currentCol, currentRow + dir, currentCol, p.getSide());

                    if ((currentRow + dir) == 0 || (currentRow + dir) == 7) {
                        moveLong = Move.setNote(moveLong, Move.MoveNote.NEW_QUEEN);
                        value = Values.QUEEN_VALUE;
                    }

                    if ((nullMoveInfo[0] & BitBoard.getMask(currentRow + dir, currentCol + lr[i])) != 0) {
                        value = board.getPieceValue(currentRow + dir, currentCol + lr[i]) - myValue >> 1;
                    } else {
                        value += board.getPieceValue(currentRow + dir, currentCol + lr[i]);
                    }

                    moveLong = Move.setValue(moveLong, value);

                    moveLong = Move.setPieceTaken(moveLong, board.getPiece(currentRow + dir, currentCol + lr[i]));
                    validMoves.add(moveLong);
                }

            }
        }

        // Check left and right en passant rule
        if (currentRow == fifthRank && board.getLastMoveMade() != 0) {
            for (int i : lr) {
                if (board.checkPiece(fifthRank, currentCol + lr[i], player) == Piece.PositionStatus.ENEMY) {

                    if ((Move.getToCol(board.getLastMoveMade()) == (currentCol + lr[i])) && Move.getNote(board.getLastMoveMade()) == Move.MoveNote.PAWN_LEAP) {

                        if (p.isValidMove(currentRow + dir, currentCol + lr[i], nullMoveInfo)) {

                            value = board.getPieceValue(fifthRank, currentCol + lr[i]);

                            if ((nullMoveInfo[0] & BitBoard.getMask(currentRow + dir, currentCol + lr[i])) != 0) {
                                value -= myValue >> 1;
                            }

                            moveLong = Move.moveLong(currentRow, currentCol, currentRow + dir, currentCol + lr[i], value, Move.MoveNote.ENPASSANT, board.getPiece(fifthRank, currentCol + lr[i]));
                            validMoves.add(moveLong);
                        }

                    }
                }
            }
        }

        return validMoves;

    }

    public static void getNullMoveInfo(Piece p, Board board, long[] nullMoveInfo) {

        int currentRow = p.getRow();
        int currentCol = p.getCol();
        int dir;
        Side player = p.getSide();
        Piece.PositionStatus pieceStatus;

        if (player == Side.WHITE) {
            dir = -1;
        } else {
            dir = 1;
        }

        pieceStatus = board.checkPiece(currentRow + dir, currentCol - 1, player);

        if (pieceStatus != Piece.PositionStatus.OFF_BOARD) {

            if (board.getPieceID(currentRow + dir, currentCol - 1) == Piece.PieceID.KING && pieceStatus == Piece.PositionStatus.ENEMY) {
                nullMoveInfo[1] &= p.getBit();
            }

            nullMoveInfo[0] |= BitBoard.getMask(currentRow + dir, currentCol - 1);
        }

        pieceStatus = board.checkPiece(currentRow + dir, currentCol + 1, player);

        if (pieceStatus != Piece.PositionStatus.OFF_BOARD) {

            if (board.getPieceID(currentRow + dir, currentCol + 1) == Piece.PieceID.KING && pieceStatus == Piece.PositionStatus.ENEMY) {
                nullMoveInfo[1] &= p.getBit();
            }

            nullMoveInfo[0] |= BitBoard.getMask(currentRow + dir, currentCol + 1);
        }

    }

}
