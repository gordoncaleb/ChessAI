package com.gordoncaleb.chess.pieces;

import java.util.ArrayList;
import java.util.List;

import com.gordoncaleb.chess.backend.BitBoard;
import com.gordoncaleb.chess.backend.Board;
import com.gordoncaleb.chess.backend.Side;
import com.gordoncaleb.chess.backend.Move;

public class Bishop {
    private static final int[][] BISHOPMOVES = {{1, 1, -1, -1}, {1, -1, 1, -1}};

    public Bishop() {
    }

    public static Piece.PieceID getPieceID() {
        return Piece.PieceID.BISHOP;
    }

    public static String getName() {
        return "Bishop";
    }

    public static String getStringID() {
        return "B";
    }

    public static List<Long> generateValidMoves(Piece p, Board board, long[] nullMoveInfo, long[] posBitBoard, List<Long> validMoves) {
        int currentRow = p.getRow();
        int currentCol = p.getCol();
        int nextRow;
        int nextCol;
        Long moveLong;
        int value;
        Piece.PositionStatus pieceStatus;
        Side player = p.getSide();

        int i = 1;
        for (int d = 0; d < 4; d++) {
            nextRow = currentRow + i * BISHOPMOVES[0][d];
            nextCol = currentCol + i * BISHOPMOVES[1][d];
            pieceStatus = board.checkPiece(nextRow, nextCol, player);

            while (pieceStatus == Piece.PositionStatus.NO_PIECE) {

                if (p.isValidMove(nextRow, nextCol, nullMoveInfo)) {

                    if ((nullMoveInfo[0] & BitBoard.getMask(nextRow, nextCol)) != 0) {
                        value = -Values.BISHOP_VALUE >> 1;
                    } else {
                        value = 0;
                    }

                    moveLong = Move.moveLong(currentRow, currentCol, nextRow, nextCol, value, Move.MoveNote.NONE);

                    validMoves.add(moveLong);
                }

                i++;
                nextRow = currentRow + i * BISHOPMOVES[0][d];
                nextCol = currentCol + i * BISHOPMOVES[1][d];
                pieceStatus = board.checkPiece(nextRow, nextCol, player);

            }

            if (pieceStatus == Piece.PositionStatus.ENEMY) {
                if (p.isValidMove(nextRow, nextCol, nullMoveInfo)) {
                    value = board.getPieceValue(nextRow, nextCol);

                    if ((nullMoveInfo[0] & BitBoard.getMask(nextRow, nextCol)) != 0) {
                        value -= Values.BISHOP_VALUE >> 1;
                    }

                    moveLong = Move.moveLong(currentRow, currentCol, nextRow, nextCol, value, Move.MoveNote.NONE, board.getPiece(nextRow, nextCol));
                    validMoves.add(moveLong);
                }
            }

            i = 1;
        }

        return validMoves;

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
