package com.gordoncaleb.chess.engine.score;

import com.gordoncaleb.chess.board.bitboard.BitBoard;
import com.gordoncaleb.chess.board.Board;
import com.gordoncaleb.chess.board.Side;
import com.gordoncaleb.chess.board.pieces.Pawn;
import com.gordoncaleb.chess.board.pieces.Piece;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.gordoncaleb.chess.board.bitboard.Slide.northFill;
import static com.gordoncaleb.chess.board.bitboard.Slide.southFill;
import static com.gordoncaleb.chess.board.pieces.Piece.PieceID.*;
import static com.gordoncaleb.chess.engine.score.Values.*;

public class StaticScorer {
    private static final Logger LOGGER = LoggerFactory.getLogger(StaticScorer.class);

    public static final int ENDGAME_PHASE = 256;

    public int getOpeningPositionValue(final int pieceId,
                                       final int row,
                                       final int col,
                                       final int side,
                                       final long openFiles) {

        switch (pieceId) {
            case KNIGHT:
                return PositionBonus.getKnightPositionBonus(row, col, side);
            case PAWN:
                return PositionBonus.getPawnPositionBonus(row, col, side);
            case BISHOP:
                return PositionBonus.BISHOP_OPENING;
            case KING:
                return PositionBonus.getKingOpeningPositionBonus(row, col, side);
            case QUEEN:
                return ((BitBoard.getMask(row, col) & openFiles) != 0) ? PositionBonus.QUEEN_ON_OPENFILE_OPENING : 0;
            case ROOK:
                return ((BitBoard.getMask(row, col) & openFiles) != 0) ? PositionBonus.ROOK_ON_OPENFILE_OPENING : 0;
            default:
                LOGGER.debug("Error: invalid piece value request!");
                return 0;
        }

    }

    public int getEndGamePositionValue(final int pieceId,
                                       final int row,
                                       final int col,
                                       final int side,
                                       final long openFiles) {

        switch (pieceId) {
            case KNIGHT:
                return PositionBonus.getKnightPositionBonus(row, col, side);
            case PAWN:
                return PositionBonus.getPawnPositionBonus(row, col, side);
            case BISHOP:
                return PositionBonus.BISHOP_ENDGAME;
            case KING:
                return PositionBonus.getKingEndGamePositionBonus(row, col, side);
            case QUEEN:
                return ((BitBoard.getMask(row, col) & openFiles) != 0) ? PositionBonus.QUEEN_ON_OPENFILE_ENDGAME : 0;
            case ROOK:
                return ((BitBoard.getMask(row, col) & openFiles) != 0) ? PositionBonus.ROOK_ON_OPENFILE_ENDGAME : 0;
            default:
                LOGGER.debug("Error: invalid piece value request!");
                return 0;
        }

    }

    public int openingPositionScore(final List<Piece> pieces, final long openFiles) {
        int score = 0;

        for (Piece p : pieces) {
            score += getOpeningPositionValue(p.getPieceID(), p.getRow(), p.getCol(), p.getSide(), openFiles);
        }

        return score;
    }

    public int endGamePositionScore(final List<Piece> pieces, final long openFiles) {
        int score = 0;

        for (Piece p : pieces) {
            score += getEndGamePositionValue(p.getPieceID(), p.getRow(), p.getCol(), p.getSide(), openFiles);
        }

        return score;
    }

    public int materialScoreDelta(final int friendSide, final int foeSide, final long[][] bitBoards) {
        return (Long.bitCount(bitBoards[QUEEN][friendSide]) - Long.bitCount(bitBoards[QUEEN][foeSide])) * QUEEN_VALUE +
                (Long.bitCount(bitBoards[ROOK][friendSide]) - Long.bitCount(bitBoards[ROOK][foeSide])) * ROOK_VALUE +
                (Long.bitCount(bitBoards[BISHOP][friendSide]) - Long.bitCount(bitBoards[BISHOP][foeSide])) * BISHOP_VALUE +
                (Long.bitCount(bitBoards[KNIGHT][friendSide]) - Long.bitCount(bitBoards[KNIGHT][foeSide])) * KNIGHT_VALUE +
                (Long.bitCount(bitBoards[PAWN][friendSide]) - Long.bitCount(bitBoards[PAWN][foeSide])) * PAWN_VALUE;
    }

    public int pawnStructureScore(final int side, final int phase, final long friendPawns, final long foePawns) {

        final int doubledPawns = getDoubledPawns(friendPawns);

        final long passedBB = getPassedPawns(friendPawns, foePawns, side);

        int passedPawns = 0;
        for (int i = 0; i < 8; i++) {
            passedPawns += Long.bitCount(passedBB & BitBoard.getRowMask(i)) * Values.PASSED_PAWN_BONUS[side][i];
        }

        final int isolatedPawns = Long.bitCount(getIsolatedPawns(friendPawns, side));

        final int backedPawns = Long.bitCount(Pawn.getPawnAttacks(friendPawns, side) & friendPawns);

        return backedPawns * Values.BACKED_PAWN_BONUS +
                doubledPawns * Values.DOUBLED_PAWN_BONUS +
                isolatedPawns * Values.ISOLATED_PAWN_BONUS +
                ((passedPawns * phase) / ENDGAME_PHASE);
    }

    public long getPassedPawns(final long friendPawns, final long foePawns, final int friendSide) {
        if (friendSide == Side.WHITE) {
            return ~southFill(foePawns | Pawn.getPawnAttacks(foePawns, Side.BLACK)) & friendPawns;
        } else {
            return ~northFill(foePawns | Pawn.getPawnAttacks(foePawns, Side.WHITE)) & friendPawns;
        }
    }

    public long getOpenFiles(final long pawns) {
        return ~(northFill(pawns) | southFill(pawns));
    }

    public int getDoubledPawns(long pawns) {
        int occupiedCol = 0;
        for (int c = 0; c < 8; c++) {
            if ((BitBoard.COL1 << c & pawns) != 0) {
                occupiedCol++;
            }
        }
        return Long.bitCount(pawns) - occupiedCol;
    }

    public long getIsolatedPawns(final long pawns, final int side) {
        final long pawnAttacks = Pawn.getPawnAttacks(pawns, side);
        return ~(southFill(pawnAttacks) | northFill(pawnAttacks)) & pawns;
    }

    public int calcGamePhase(final Board b) {

        final long[][] bbs = b.getPosBitBoard();

        final int phase = Values.TOTALPHASE -
                (Long.bitCount(bbs[KNIGHT][Side.WHITE] | bbs[KNIGHT][Side.BLACK]) * Values.PIECE_PHASE_VAL[KNIGHT]) -
                (Long.bitCount(bbs[BISHOP][Side.WHITE] | bbs[BISHOP][Side.BLACK]) * Values.PIECE_PHASE_VAL[BISHOP]) -
                (Long.bitCount(bbs[ROOK][Side.WHITE] | bbs[ROOK][Side.BLACK]) * Values.PIECE_PHASE_VAL[ROOK]) -
                (Long.bitCount(bbs[QUEEN][Side.WHITE] | bbs[QUEEN][Side.BLACK]) * Values.PIECE_PHASE_VAL[QUEEN]);

        return (phase * ENDGAME_PHASE + (Values.TOTALPHASE / 2)) / Values.TOTALPHASE;
    }

    public int staticScore(final Board b) {
        return staticScore(b, b.getTurn());
    }

    public int staticScore(final Board b, final int side) {
        final int otherSide = Side.otherSide(side);
        final int phase = calcGamePhase(b);

        final long yourPawns = b.getPosBitBoard()[PAWN][side];
        final long myPawns = b.getPosBitBoard()[PAWN][Side.otherSide(side)];

        final int myPawnScore = pawnStructureScore(side, phase, myPawns, yourPawns);
        final int yourPawnScore = pawnStructureScore(otherSide, phase, yourPawns, myPawns);

        final long myOpenFiles = getOpenFiles(yourPawns);
        final long yourOpenFiles = getOpenFiles(myPawns);

        final int openingMyScore = openingPositionScore(b.getPiecesList()[side], yourOpenFiles);
        final int openingYourScore = openingPositionScore(b.getPiecesList()[otherSide], myOpenFiles);

        final int endGameMyScore = endGamePositionScore(b.getPiecesList()[side], yourOpenFiles);
        final int endGameYourScore = endGamePositionScore(b.getPiecesList()[otherSide], myOpenFiles);

        final int materialScoreDelta = materialScoreDelta(side, otherSide, b.getPosBitBoard());

        final int myScore = (openingMyScore * (ENDGAME_PHASE - phase) + endGameMyScore * phase) / ENDGAME_PHASE + myPawnScore;
        final int yourScore = (openingYourScore * (ENDGAME_PHASE - phase) + endGameYourScore * phase) / ENDGAME_PHASE + yourPawnScore;

        return myScore - yourScore + materialScoreDelta;
    }

    public String printBoardScoreBreakDown(Board b) {

        final int side = b.getTurn();

        final int otherSide = Side.otherSide(side);
        final int phase = calcGamePhase(b);

        final long yourPawns = b.getPosBitBoard()[PAWN][side];
        final long myPawns = b.getPosBitBoard()[PAWN][Side.otherSide(side)];

        final int myPawnScore = pawnStructureScore(side, phase, myPawns, yourPawns);
        final int yourPawnScore = pawnStructureScore(otherSide, phase, yourPawns, myPawns);

        final long myOpenFiles = getOpenFiles(yourPawns);
        final long yourOpenFiles = getOpenFiles(myPawns);

        final int openingMyScore = openingPositionScore(b.getPiecesList()[side], yourOpenFiles);
        final int openingYourScore = openingPositionScore(b.getPiecesList()[otherSide], myOpenFiles);

        final int endGameMyScore = endGamePositionScore(b.getPiecesList()[side], yourOpenFiles);
        final int endGameYourScore = endGamePositionScore(b.getPiecesList()[otherSide], myOpenFiles);

        final int materialScoreDelta = materialScoreDelta(side, otherSide, b.getPosBitBoard());

        final int myScore = (openingMyScore * (ENDGAME_PHASE - phase) + endGameMyScore * phase) / ENDGAME_PHASE + myPawnScore;
        final int yourScore = (openingYourScore * (ENDGAME_PHASE - phase) + endGameYourScore * phase) / ENDGAME_PHASE + yourPawnScore;

        Map<String, Object> scoreMap = new HashMap<>();

        scoreMap.put("phase", phase);

        scoreMap.put("myPawnScore", myPawnScore);
        scoreMap.put("yourPawnScore", yourPawnScore);

        scoreMap.put("openingMyScore", openingMyScore);
        scoreMap.put("openingYourScore", openingYourScore);

        scoreMap.put("endGameMyScore", endGameMyScore);
        scoreMap.put("endGameYourScore", endGameYourScore);

        scoreMap.put("materialScoreDelta", materialScoreDelta);

        scoreMap.put("myScore", myScore);
        scoreMap.put("yourScore", yourScore);

        scoreMap.put("scoreDiff", myScore - yourScore + materialScoreDelta);

        return scoreMap.toString();
    }
}
