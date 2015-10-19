package com.gordoncaleb.chess.pieces;

import java.util.List;

import com.gordoncaleb.chess.bitboard.BitBoard;
import com.gordoncaleb.chess.backend.Board;
import static com.gordoncaleb.chess.bitboard.Slide.*;

public class Bishop {

    public static String getName() {
        return "Bishop";
    }

    public static String getStringID() {
        return "B";
    }

    public static List<Long> generateValidMoves(final Piece p,
                                                final Board board,
                                                final long[] nullMoveInfo,
                                                final long[] posBitBoard,
                                                final List<Long> validMoves) {

        final long friend = posBitBoard[p.getSide()];
        final long friendOrFoe = (posBitBoard[0] | posBitBoard[1]);
        final long footPrint = slideBishop(p.getBit(), friendOrFoe, friend);
        return Piece.generateValidMoves(footPrint, p, board, nullMoveInfo, posBitBoard, validMoves);
    }

    public static long slideBishop(final long mask, final long friendOrFoe, final long friend) {
        final long friendOrFoeNotMe = friendOrFoe & ~mask;
        final long slide = northWest(mask, friendOrFoeNotMe) |
                northEast(mask, friendOrFoeNotMe) |
                southWest(mask, friendOrFoeNotMe) |
                southEast(mask, friendOrFoeNotMe);
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


        // going westward -----------------------------------------------------
        if ((bitPiece & 0x0101010101010101L) == 0) {

            // northwest
            while ((temp2 = (temp2 >>> 9 & left)) != 0) {
                attackVector |= temp2;
                temp = temp2;
                r--;
                c--;
            }
            temp = temp >>> 9;
            nullMoveInfo[0] |= attackVector | temp;

            // check to see if king collision is possible
            if ((BitBoard.getNegSlope(r, c) & kingBitBoard) != 0) {

                if ((temp & kingBitBoard) != 0) {
                    nullMoveInfo[1] &= attackVector | bitPiece;
                    nullMoveInfo[2] |= temp >>> 9;
                } else {
                    if ((temp & friendly) != 0 && r > 1 && c > 1) {
                        temp = temp >>> 9;
                        if ((temp & kingCheckVectors) != 0) {
                            board.getPiece(r - 1, c - 1).setBlockingVector(BitBoard.getNegSlope(r, c));
                        }
                    }
                }
            }

            // south west
            temp = bitPiece;
            temp2 = bitPiece;
            r = piece.getRow();
            c = piece.getCol();
            attackVector = 0;

            while ((temp2 = (temp2 << 7 & left)) != 0) {
                attackVector |= temp2;
                temp = temp2;
                r++;
                c--;
            }

            temp = temp << 7;
            nullMoveInfo[0] |= attackVector | temp;

            // check to see if king collision is possible
            if ((BitBoard.getPosSlope(r, c) & kingBitBoard) != 0) {

                if ((temp & kingBitBoard) != 0) {
                    nullMoveInfo[1] &= attackVector | bitPiece;
                    nullMoveInfo[2] |= temp << 7;
                } else {
                    if ((temp & friendly) != 0 && r < 6 && c > 1) {
                        temp = temp << 7;
                        if ((temp & kingCheckVectors) != 0) {
                            board.getPiece(r + 1, c - 1).setBlockingVector(BitBoard.getPosSlope(r, c));
                        }
                    }
                }
            }

        }

        // going eastward
        if ((bitPiece & 0x8080808080808080L) == 0) {

            // northeast
            temp = bitPiece;
            temp2 = bitPiece;
            c = piece.getCol();
            r = piece.getRow();
            attackVector = 0;

            while ((temp2 = (temp2 >> 7 & right)) != 0) {
                attackVector |= temp2;
                temp = temp2;
                c++;
                r--;
            }

            temp = temp >> 7;
            nullMoveInfo[0] |= attackVector | temp;

            // check to see if king collision is possible
            if ((BitBoard.getPosSlope(r, c) & kingBitBoard) != 0) {

                if ((temp & kingBitBoard) != 0) {
                    nullMoveInfo[1] &= attackVector | bitPiece;
                    nullMoveInfo[2] |= temp >> 7;
                } else {
                    if ((temp & friendly) != 0 && r > 1 && c < 6) {
                        temp = temp >> 7;
                        if ((temp & kingCheckVectors) != 0) {
                            board.getPiece(r - 1, c + 1).setBlockingVector(BitBoard.getPosSlope(r, c));
                        }
                    }
                }
            }

            // southeast
            temp = bitPiece;
            temp2 = bitPiece;
            c = piece.getCol();
            r = piece.getRow();
            attackVector = 0;

            while ((temp2 = (temp2 << 9 & right)) != 0) {
                attackVector |= temp2;
                temp = temp2;
                c++;
                r++;
            }

            temp = temp << 9;
            nullMoveInfo[0] |= attackVector | temp;

            // check to see if king collision is possible
            if ((BitBoard.getNegSlope(r, c) & kingBitBoard) != 0) {

                if ((temp & kingBitBoard) != 0) {
                    nullMoveInfo[1] &= attackVector | bitPiece;
                    nullMoveInfo[2] |= temp << 9;
                } else {
                    if ((temp & friendly) != 0 && c < 6 && r < 6) {
                        temp = temp << 9;
                        if ((temp & kingCheckVectors) != 0) {
                            board.getPiece(r + 1, c + 1).setBlockingVector(BitBoard.getNegSlope(r, c));
                        }
                    }
                }
            }

        }

    }

}
