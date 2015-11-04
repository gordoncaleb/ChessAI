package com.gordoncaleb.chess.board.pieces;

import java.util.List;

import com.gordoncaleb.chess.board.Side;
import com.gordoncaleb.chess.board.bitboard.BitBoard;
import com.gordoncaleb.chess.board.Board;
import com.gordoncaleb.chess.board.Move;
import com.gordoncaleb.chess.engine.score.Values;

import static com.gordoncaleb.chess.board.bitboard.BitBoard.*;
import static com.gordoncaleb.chess.board.bitboard.Slide.*;
import static com.gordoncaleb.chess.board.pieces.Piece.*;

public class King {

    public static String getStringID() {
        return "K";
    }

    public static List<Move> generateValidMoves(final Piece p,
                                                final Board board,
                                                final long[] nullMoveInfo,
                                                final long[] posBitBoard,
                                                final List<Move> validMoves) {

        final long foes = posBitBoard[Side.otherSide(p.getSide())];
        final long friendsOrFoes = posBitBoard[0] | posBitBoard[1];
        final long footPrint = getKingAttacks(p.getBit()) & ~posBitBoard[p.getSide()];

        final long validFootPrint = footPrint & ~(nullMoveInfo[0] | nullMoveInfo[2]);
        final long validFootPrintWithPiecesTaken = validFootPrint & foes;
        final long validFootPrintWoPiecesTaken = validFootPrint & ~foes;

        buildValidMovesWithPiecesTaken(validFootPrintWithPiecesTaken, p.getRow(), p.getCol(), board, validMoves);
        buildValidMoves(validFootPrintWoPiecesTaken, p.getRow(), p.getCol(), validMoves);

        final int currentRow = p.getRow();
        final int currentCol = p.getCol();

        if (!board.isInCheck()) {
            // add possible castle move
            if (canCastleFar(p, board, p.getSide(), nullMoveInfo, friendsOrFoes)) {
                if (isValidMove(currentRow, 2, nullMoveInfo)) {
                    if (currentCol > 3) {
                        validMoves.add(new Move(currentRow, currentCol, currentRow, 2, Values.FAR_CASTLE_VALUE, Move.MoveNote.CASTLE_FAR));
                    } else {
                        validMoves.add(new Move(currentRow, board.getRookStartingCol(p.getSide(), 0), currentRow, 3, Values.FAR_CASTLE_VALUE,
                                Move.MoveNote.CASTLE_FAR));
                    }
                }
            }

            if (canCastleNear(p, board, p.getSide(), nullMoveInfo, friendsOrFoes)) {
                if (isValidMove(currentRow, 6, nullMoveInfo)) {
                    if (currentCol < 5) {
                        validMoves.add(new Move(currentRow, currentCol, currentRow, 6, Values.NEAR_CASTLE_VALUE, Move.MoveNote.CASTLE_NEAR));
                    } else {
                        validMoves.add(new Move(currentRow, board.getRookStartingCol(p.getSide(), 1), currentRow, 5, Values.NEAR_CASTLE_VALUE,
                                Move.MoveNote.CASTLE_NEAR));
                    }
                }
            }
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
            if (westSlideBlockers == 0) {
                nullMoveInfo[1] &= westSlide;
                nullMoveInfo[2] |= king << 1;
            } else if (hasOneBitOrLess(westSlideBlockers)) {
                setBlocker(board, westSlideBlockers, westSlide);
            }
        }

        final long eastSlide = eastSlideNoEdge(eastFill(king), foeStraightSliders);
        if (eastSlide != 0) {
            final long eastSlideBlockers = eastSlide & allExceptMeAndStraightSliders;
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
        b.getPiece(r, c).setBlockingVector(blockingVector);
    }

    public static boolean isValidMove(int toRow, int toCol, long[] nullMoveInfo) {
        long mask = BitBoard.getMask(toRow, toCol);
        return ((mask & (nullMoveInfo[0] | nullMoveInfo[2])) == 0);
    }

    public static boolean canCastleFar(Piece king, Board board, int player, long[] nullMoveInfo, long allPosBitBoard) {

        if (board.kingHasMoved(player) || board.farRookHasMoved(player)) {
            return false;
        }

        long kingToCastleMask = BitBoard.getCastleMask(king.getCol(), 2, player);

        int rookCol = board.getRookStartingCol(player, 0);
        long rookToCastleMask = BitBoard.getCastleMask(rookCol, 3, player);

        allPosBitBoard ^= BitBoard.getMask(king.getRow(), rookCol) | king.getBit();

        if ((kingToCastleMask & nullMoveInfo[0]) == 0) {
            if (((kingToCastleMask | rookToCastleMask) & allPosBitBoard) == 0) {
                return true;
            }
        }

        return false;

    }

    public static boolean canCastleNear(Piece king, Board board, int player, long[] nullMoveInfo, long allPosBitBoard) {

        if (board.kingHasMoved(player) || board.nearRookHasMoved(player)) {
            return false;
        }

        long kingToCastleMask = BitBoard.getCastleMask(king.getCol(), 6, player);

        int rookCol = board.getRookStartingCol(player, 1);
        long rookToCastleMask = BitBoard.getCastleMask(rookCol, 5, player);

        allPosBitBoard ^= BitBoard.getMask(king.getRow(), rookCol) | king.getBit();

        if ((kingToCastleMask & nullMoveInfo[0]) == 0) {
            if (((kingToCastleMask | rookToCastleMask) & allPosBitBoard) == 0) {
                return true;
            }
        }

        return false;
    }


}
