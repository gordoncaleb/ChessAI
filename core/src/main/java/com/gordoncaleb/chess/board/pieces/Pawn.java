package com.gordoncaleb.chess.board.pieces;

import java.util.List;

import com.gordoncaleb.chess.board.Board;
import com.gordoncaleb.chess.board.Side;
import com.gordoncaleb.chess.board.Move;
import com.gordoncaleb.chess.board.bitboard.BitBoard;

import static com.gordoncaleb.chess.board.bitboard.BitBoard.*;

public class Pawn {

    private static final int BLACK_PAWN_LEAP_ROW = 2;
    private static final int WHITE_PAWN_LEAP_ROW = 5;
    private static final long WHITE_PAWN_LEAP_ROW_MASK = 0x0000FF0000000000L;
    private static final long BLACK_PAWN_LEAP_ROW_MASK = 0x0000000000FF0000L;
    private static final long QUEENING_MASK = 0xFF000000000000FFL;
    private static final long NOT_QUEENING_MASK = ~QUEENING_MASK;

    public static List<Move> generateValidMoves(final Piece p, final Board board, final long[] nullMoveInfo, final long[] posBitBoard, final List<Move> validMoves) {

        final long mask = p.asBitMask();
        final int side = p.getSide();
        final int row = p.getRow();
        final int col = p.getCol();

        final Move lastMove = board.getLastMoveMade();
        final int enpassantCol = lastMove.getFromCol();
        final long enpassantColMask = lastMove.getNote() == Move.MoveNote.PAWN_LEAP ? BitBoard.getColMask(lastMove.getFromCol()) : 0L;

        final long foes = posBitBoard[Side.otherSide(side)];
        final long emptySpace = ~(posBitBoard[0] | posBitBoard[1]);
        final long valid = nullMoveInfo[1] & p.blockingVector();

        final long hops, attacks, validLeapMoves, enPassantAttack;

        if (side == Side.BLACK) {
            hops = (mask << 8) & emptySpace;
            attacks = ((mask & NOT_RIGHT1) << 9 | (mask & NOT_LEFT1) << 7);
            validLeapMoves = ((hops & BLACK_PAWN_LEAP_ROW_MASK) << 8) & emptySpace & valid;

            enPassantAttack = attacks & WHITE_PAWN_LEAP_ROW_MASK & enpassantColMask;

            if ((enPassantAttack & p.blockingVector()) != 0 && (nullMoveInfo[1] & getMask(4, enpassantCol)) != 0) {
                validMoves.add(new Move(row, col, WHITE_PAWN_LEAP_ROW, enpassantCol, 0,
                        Move.MoveNote.ENPASSANT, board.getPiece(4, enpassantCol)
                ));
            }
        } else {
            hops = (mask >>> 8) & emptySpace;
            attacks = ((mask & NOT_RIGHT1) >>> 7 | (mask & NOT_LEFT1) >>> 9);
            validLeapMoves = ((hops & WHITE_PAWN_LEAP_ROW_MASK) >>> 8) & emptySpace & valid;

            enPassantAttack = attacks & BLACK_PAWN_LEAP_ROW_MASK & enpassantColMask;

            if ((enPassantAttack & p.blockingVector()) != 0 && (nullMoveInfo[1] & getMask(3, enpassantCol)) != 0) {
                validMoves.add(new Move(row, col, BLACK_PAWN_LEAP_ROW, enpassantCol, 0,
                        Move.MoveNote.ENPASSANT, board.getPiece(3, enpassantCol)
                ));
            }
        }

        final long validHopsAndAttacks = valid & (hops | attacks & foes);

        Piece.buildValidMoves(validHopsAndAttacks & NOT_QUEENING_MASK, row, col, Move.MoveNote.NONE, board, validMoves);
        Piece.buildValidMoves(validHopsAndAttacks & QUEENING_MASK, row, col, Move.MoveNote.NEW_QUEEN, board, validMoves);
        Piece.buildValidMoves(validLeapMoves, row, col, Move.MoveNote.PAWN_LEAP, board, validMoves);

        return validMoves;
    }

    public static List<Move> generateValidMoves2(Piece p, Board board, long[] nullMoveInfo, long[] posBitBoard, List<Move> validMoves) {
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
        if (currentRow == fifthRank && board.getLastMoveMade().getNote() == Move.MoveNote.PAWN_LEAP) {
            for (int i : lr) {
                if (board.checkPiece(fifthRank, currentCol + i, player) == Piece.PositionStatus.ENEMY) {

                    if (board.getLastMoveMade().getToCol() == (currentCol + i)) {

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

    public static long getPawnAttacks(final long pawns, final int side) {
        if (side == Side.BLACK) {
            return (pawns & NOT_RIGHT1) << 9 |
                    (pawns & NOT_LEFT1) << 7;
        } else {
            return (pawns & NOT_RIGHT1) >>> 7 |
                    (pawns & NOT_LEFT1) >>> 9;
        }
    }

}
