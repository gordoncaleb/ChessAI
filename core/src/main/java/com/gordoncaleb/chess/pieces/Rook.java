package com.gordoncaleb.chess.pieces;

import java.util.List;

import com.gordoncaleb.chess.bitboard.BitBoard;
import com.gordoncaleb.chess.backend.Board;
import static com.gordoncaleb.chess.bitboard.Slide.*;

import static com.gordoncaleb.chess.bitboard.BitBoard.getMask;

public class Rook {

    public static String getName() {
        return "Rook";
    }

    public static String getStringID() {
        return "R";
    }

    public static List<Long> generateValidMoves(final Piece p,
                                                final Board board,
                                                final long[] nullMoveInfo,
                                                final long[] posBitBoard,
                                                final List<Long> validMoves) {

        final long friend = posBitBoard[p.getSide()];
        final long friendOrFoe = (posBitBoard[0] | posBitBoard[1]);
        final long footPrint = slideRook(p.getBit(), friendOrFoe, friend);
        return Piece.generateValidMoves(footPrint, p, board, nullMoveInfo, posBitBoard, validMoves);
    }

    public static long slideRook(final long mask, final long friendOrFoe, final long friend) {
        final long friendOrFoeNotMe = friendOrFoe & ~mask;
        final long slide = north(mask, friendOrFoeNotMe) |
                south(mask, friendOrFoeNotMe) |
                west(mask, friendOrFoeNotMe) |
                east(mask, friendOrFoeNotMe);
        return slide & ~friend;
    }

    public static void getNullMoveInfo(Piece piece, Board board, long[] nullMoveInfo, long updown, long left, long right, long kingBitBoard, long kingCheckVectors,
                                       long friendly) {

        long bitPiece = piece.getBit();

        // up ------------------------------------------------------------
        long temp = bitPiece;
        long temp2 = bitPiece;
        int r = piece.getRow();
        int c = piece.getCol();
        long attackVector = 0;

        while ((temp2 = (temp2 >>> 8 & updown)) != 0) {
            attackVector |= temp2;
            temp = temp2;
            r--;
        }

        temp = temp >>> 8;
        nullMoveInfo[0] |= attackVector | temp;

        // check to see if king collision is possible
        if ((BitBoard.getColMask(c) & kingBitBoard) != 0) {

            if ((temp & kingBitBoard) != 0) {
                nullMoveInfo[1] &= attackVector | bitPiece;
                nullMoveInfo[2] |= temp >>> 8;
            } else {
                if ((temp & friendly) != 0 && r > 1) {
                    temp = temp >>> 8;
                    if ((temp & kingCheckVectors) != 0) {
                        board.getPiece(r - 1, c).setBlockingVector(BitBoard.getColMask(c));
                    }
                }
            }
        }

        // down-----------------------------------------------------------
        temp = bitPiece;
        temp2 = bitPiece;
        r = piece.getRow();
        attackVector = 0;

        while ((temp2 = (temp2 << 8 & updown)) != 0) {
            attackVector |= temp2;
            temp = temp2;
            r++;
        }

        temp = temp << 8;
        nullMoveInfo[0] |= attackVector | temp;

        // check to see if king collision is possible
        if ((BitBoard.getColMask(c) & kingBitBoard) != 0) {

            if ((temp & kingBitBoard) != 0) {
                nullMoveInfo[1] &= attackVector | bitPiece;
                nullMoveInfo[2] |= temp << 8;
            } else {
                if ((temp & friendly) != 0 && r < 6) {
                    temp = temp << 8;
                    if ((temp & kingCheckVectors) != 0) {
                        board.getPiece(r + 1, c).setBlockingVector(BitBoard.getColMask(c));
                    }
                }
            }
        }

        // going westward -----------------------------------------------------
        if ((bitPiece & 0x0101010101010101L) == 0) {

            // west
            temp = bitPiece;
            temp2 = bitPiece;
            r = piece.getRow();
            attackVector = 0;

            while ((temp2 = (temp2 >>> 1 & left)) != 0) {
                attackVector |= temp2;
                temp = temp2;
                c--;
            }

            temp = temp >>> 1;
            nullMoveInfo[0] |= attackVector | temp;

            // check to see if king collision is possible
            if ((BitBoard.getRowMask(r) & kingBitBoard) != 0) {

                //check if rook hits king
                if ((temp & kingBitBoard) != 0) {
                    nullMoveInfo[1] &= attackVector | bitPiece;
                    nullMoveInfo[2] |= temp >>> 1;
                } else {
                    //check if rook his opponent and opponent is not on board edge
                    if ((temp & friendly) != 0 && c > 1) {
                        //check if other side of opponent has vector to king
                        temp = temp >>> 1;
                        if ((temp & kingCheckVectors) != 0) {
                            board.getPiece(r, c - 1).setBlockingVector(BitBoard.getRowMask(r));
                        }
                    }
                }
            }

        }

        // going eastward
        if ((bitPiece & 0x8080808080808080L) == 0) {

            // east
            temp = bitPiece;
            temp2 = bitPiece;
            r = piece.getRow();
            c = piece.getCol();
            attackVector = 0;

            while ((temp2 = (temp2 << 1 & right)) != 0) {
                attackVector |= temp2;
                temp = temp2;
                c++;
            }

            temp = temp << 1;
            nullMoveInfo[0] |= attackVector | temp;

            // check to see if king collision is possible
            if ((BitBoard.getRowMask(r) & kingBitBoard) != 0) {

                if ((temp & kingBitBoard) != 0) {
                    nullMoveInfo[1] &= attackVector | bitPiece;
                    nullMoveInfo[2] |= temp << 1;
                } else {
                    if ((temp & friendly) != 0 && c < 6) {
                        temp = temp << 1;
                        if ((temp & kingCheckVectors) != 0) {
                            board.getPiece(r, c + 1).setBlockingVector(BitBoard.getRowMask(r));
                        }
                    }
                }
            }

        }

    }

}
