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