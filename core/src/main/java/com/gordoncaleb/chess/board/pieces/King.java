package com.gordoncaleb.chess.board.pieces;

import com.gordoncaleb.chess.board.MoveContainer;
import com.gordoncaleb.chess.board.Board;
import com.gordoncaleb.chess.board.Move;

import static com.gordoncaleb.chess.board.bitboard.BitBoard.*;
import static com.gordoncaleb.chess.board.bitboard.Slide.*;
import static com.gordoncaleb.chess.board.pieces.Piece.*;
import static com.gordoncaleb.chess.board.Move.MoveNote.*;

public class King {

    public static MoveContainer generateValidMoves(final Piece p,
                                                   final Board board,
                                                   final long[] nullMoveInfo,
                                                   final long[] posBitBoard,
                                                   final MoveContainer validMoves) {
        final int currentRow = p.getRow();
        final int currentCol = p.getCol();
        final int side = p.getSide();

        final long kingMask = p.asBitMask();
        final long friendsOrFoes = posBitBoard[0] | posBitBoard[1];
        final long footPrint = getKingAttacks(kingMask) & ~posBitBoard[side];
        final long validFootPrint = footPrint & ~(nullMoveInfo[0] | nullMoveInfo[2]);

        buildValidMoves(validFootPrint, currentRow, currentCol, NORMAL, board, validMoves);

        final long kingNoGo = nullMoveInfo[0] | (friendsOrFoes & ~kingMask);

        if (castleFar(board.canCastleFar(side),
                board.kingToCastleMasks[side][Board.FAR],
                board.rookToCastleMasks[side][Board.FAR],
                kingNoGo,
                friendsOrFoes)) {
            validMoves.add(currentRow, currentCol, currentRow, 2, CASTLE_FAR);
        }

        if (castleNear(board.canCastleNear(side),
                board.kingToCastleMasks[side][Board.NEAR],
                board.rookToCastleMasks[side][Board.NEAR],
                kingNoGo,
                friendsOrFoes)) {
            validMoves.add(currentRow, currentCol, currentRow, 6, CASTLE_NEAR);
        }

        return validMoves;
    }

    public static long getKingAttacks(long king) {
        return (king << 8) | // down 1
                (king >>> 8) | // up 1
                ((king & NOT_LEFT1) >>> 1) | // left 1
                ((king & NOT_RIGHT1) << 1) | // right 1
                ((king & NOT_RIGHT1) >>> 7) | // up 1 right 1
                ((king & NOT_RIGHT1) << 9) | // down 1 right 1
                ((king & NOT_LEFT1) >>> 9) | // up 1 left 1
                ((king & NOT_LEFT1) << 7); // down 1 left 1
    }

    public static void getKingCheckInfo(final Board board,
                                        final long king,
                                        final long foeQueens,
                                        final long foeRooks,
                                        final long foeBishops,
                                        final long friendsOrFoes,
                                        final long[] nullMoveInfo) {

        final long allExceptMe = friendsOrFoes & ~king;
        final long foeDiagonalSliders = foeBishops | foeQueens;
        final long foeStraightSliders = foeRooks | foeQueens;
        final long allExceptMeAndStraightSliders = allExceptMe & ~foeStraightSliders;
        final long allExceptMeAndDiagonalSliders = allExceptMe & ~foeDiagonalSliders;

        nullMoveInfo[3] = 0;
        nullMoveInfo[4] = 0;

        final long northSlide = northSlideNoEdge(northFill(king), foeStraightSliders);
        if (northSlide != 0) {
            final long northSlideBlockers = northSlide & allExceptMeAndStraightSliders;
            if (northSlideBlockers == 0) {
                nullMoveInfo[1] &= northSlide;
                nullMoveInfo[2] |= king << 8;
            } else if (hasOneBitOrLess(northSlideBlockers)) {
                setBlocker(board, northSlideBlockers, northSlide);
            }
        }

        final long southSlide = southSlideNoEdge(southFill(king), foeStraightSliders);
        if (southSlide != 0) {
            final long southSlideBlockers = southSlide & allExceptMeAndStraightSliders;
            if (southSlideBlockers == 0) {
                nullMoveInfo[1] &= southSlide;
                nullMoveInfo[2] |= king >>> 8;
            } else if (hasOneBitOrLess(southSlideBlockers)) {
                setBlocker(board, southSlideBlockers, southSlide);
            }
        }

        final long westSlide = westSlideNoEdge(westFill(king), foeStraightSliders);

        if (westSlide != 0) {
            final long westSlideBlockers = westSlide & allExceptMeAndStraightSliders;
            nullMoveInfo[3] = westSlideBlockers;
            if (westSlideBlockers == 0) {
                nullMoveInfo[1] &= westSlide;
                nullMoveInfo[2] |= king << 1;
            } else if (hasOneBitOrLess(westSlideBlockers)) { //has exactly 1 bit
                setBlocker(board, westSlideBlockers, westSlide);
            }
        }

        final long eastSlide = eastSlideNoEdge(eastFill(king), foeStraightSliders);
        if (eastSlide != 0) {
            final long eastSlideBlockers = eastSlide & allExceptMeAndStraightSliders;
            nullMoveInfo[4] = eastSlideBlockers;
            if (eastSlideBlockers == 0) {
                nullMoveInfo[1] &= eastSlide;
                nullMoveInfo[2] |= king >>> 1;
            } else if (hasOneBitOrLess(eastSlideBlockers)) {
                setBlocker(board, eastSlideBlockers, eastSlide);
            }
        }

        final long northWestSlide = northWestSlideNoEdge(northWestFill(king), foeDiagonalSliders);
        if (northWestSlide != 0) {
            final long northWestSlideBlockers = northWestSlide & allExceptMeAndDiagonalSliders;
            if (northWestSlideBlockers == 0) {
                nullMoveInfo[1] &= northWestSlide;
                nullMoveInfo[2] |= king << 9;
            } else if (hasOneBitOrLess(northWestSlideBlockers)) {
                setBlocker(board, northWestSlideBlockers, northWestSlide);
            }
        }

        final long northEastSlide = northEastSlideNoEdge(northEastFill(king), foeDiagonalSliders);
        if (northEastSlide != 0) {
            final long northEastSlideBlockers = northEastSlide & allExceptMeAndDiagonalSliders;
            if (northEastSlideBlockers == 0) {
                nullMoveInfo[1] &= northEastSlide;
                nullMoveInfo[2] |= king << 7;
            } else if (hasOneBitOrLess(northEastSlideBlockers)) {
                setBlocker(board, northEastSlideBlockers, northEastSlide);
            }
        }

        final long southWestSlide = southWestSlideNoEdge(southWestFill(king), foeDiagonalSliders);
        if (southWestSlide != 0) {
            final long southWestSlideBlockers = southWestSlide & allExceptMeAndDiagonalSliders;
            if (southWestSlideBlockers == 0) {
                nullMoveInfo[1] &= southWestSlide;
                nullMoveInfo[2] |= king >>> 7;
            } else if (hasOneBitOrLess(southWestSlideBlockers)) {
                setBlocker(board, southWestSlideBlockers, southWestSlide);
            }
        }

        final long southEastSlide = southEastSlideNoEdge(southEastFill(king), foeDiagonalSliders);
        if (southEastSlide != 0) {
            final long southEastSlideBlockers = southEastSlide & allExceptMeAndDiagonalSliders;
            if (southEastSlideBlockers == 0) {
                nullMoveInfo[1] &= southEastSlide;
                nullMoveInfo[2] |= king >> 9;
            } else if (hasOneBitOrLess(southEastSlideBlockers)) {
                setBlocker(board, southEastSlideBlockers, southEastSlide);
            }
        }

    }

    private static void setBlocker(Board b, long mask, long blockingVector) {
        int bitNum = Long.numberOfTrailingZeros(mask);
        int r = bitNum / 8;
        int c = bitNum % 8;
        b.getPiece(r, c).putBlockingVector(blockingVector);
    }

    public static boolean castleFar(boolean canCastleFar,
                                    long kingToFarCastleMask,
                                    long rookToFarCastleMask,
                                    long kingNoGo,
                                    long rookNoGo) {
        return (canCastleFar &&
                (kingNoGo & kingToFarCastleMask) == 0 &&
                (rookNoGo & rookToFarCastleMask) == 0);
    }

    public static boolean castleNear(boolean canCastleNear,
                                     long kingToNearCastleMask,
                                     long rookToNearCastleMask,
                                     long kingNoGo,
                                     long rookNoGo) {
        return (canCastleNear &&
                (kingNoGo & kingToNearCastleMask) == 0 &&
                (rookNoGo & rookToNearCastleMask) == 0);
    }

}
