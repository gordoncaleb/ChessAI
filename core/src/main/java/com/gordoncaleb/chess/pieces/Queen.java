package com.gordoncaleb.chess.pieces;

import java.util.List;

import com.gordoncaleb.chess.bitboard.BitBoard;
import com.gordoncaleb.chess.backend.Board;
import com.gordoncaleb.chess.bitboard.Slide;

public class Queen {

    public static int[][] QUEENMOVES = {{1, 1, -1, -1, 1, -1, 0, 0}, {1, -1, 1, -1, 0, 0, 1, -1}};

    public Queen() {
    }

    public static Piece.PieceID getPieceID() {
        return Piece.PieceID.QUEEN;
    }

    public static String getName() {
        return "Queen";
    }

    public static String getStringID() {
        return "Q";
    }

    public static List<Long> generateValidMoves(final Piece p,
                                                final Board board,
                                                final long[] nullMoveInfo,
                                                final long[] posBitBoard,
                                                final List<Long> validMoves) {

        final long friend = posBitBoard[p.getSide().ordinal()];
        final long friendOrFoe = (posBitBoard[0] | posBitBoard[1]);
        final long footPrint = Slide.slideQueen(p.getRow(), p.getCol(), friendOrFoe, friend);
        return Piece.generateValidMoves(footPrint, p, board, nullMoveInfo, posBitBoard, validMoves);
    }

    public static void getNullMoveInfo(final Piece piece,
                                       final Board board,
                                       final long[] nullMoveInfo,
                                       final long updown,
                                       final long left,
                                       final long right,
                                       final long kingBitBoard,
                                       final long kingCheckVectors,
                                       final long friendly) {

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
                if ((temp & friendly) != 0) {
                    temp = temp >>> 8;
                    if ((temp & kingCheckVectors) != 0 && r > 1) {
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

                if ((temp & kingBitBoard) != 0) {
                    nullMoveInfo[1] &= attackVector | bitPiece;
                    nullMoveInfo[2] |= temp >>> 1;
                } else {
                    if ((temp & friendly) != 0 && c > 1) {
                        temp = temp >>> 1;
                        if ((temp & kingCheckVectors) != 0) {
                            board.getPiece(r, c - 1).setBlockingVector(BitBoard.getRowMask(r));
                        }
                    }
                }
            }

            // northwest
            temp2 = bitPiece;
            temp = bitPiece;
            c = piece.getCol();
            attackVector = 0;

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

            // northeast
            temp = bitPiece;
            temp2 = bitPiece;
            c = piece.getCol();
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
                    if ((temp & friendly) != 0 && r < 6 && c < 6) {
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
