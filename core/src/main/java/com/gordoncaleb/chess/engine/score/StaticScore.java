package com.gordoncaleb.chess.engine.score;

import com.gordoncaleb.chess.board.bitboard.BitBoard;
import com.gordoncaleb.chess.board.Board;
import com.gordoncaleb.chess.board.Side;
import com.gordoncaleb.chess.board.pieces.Pawn;
import com.gordoncaleb.chess.board.pieces.Piece;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static com.gordoncaleb.chess.board.bitboard.Slide.northFill;
import static com.gordoncaleb.chess.board.bitboard.Slide.southFill;
import static com.gordoncaleb.chess.board.pieces.Piece.PieceID.*;

public class StaticScore {
    private static final Logger logger = LoggerFactory.getLogger(StaticScore.class);

    public static final int ENDGAME_PHASE = 256;

    public int getOpeningPositionValue(final Piece piece, final long openFiles) {

        switch (piece.getPieceID()) {
            case KNIGHT:
                return PositionBonus.getKnightPositionBonus(piece.getRow(), piece.getCol(), piece.getSide());
            case PAWN:
                return PositionBonus.getPawnPositionBonus(piece.getRow(), piece.getCol(), piece.getSide());
            case BISHOP:
                return 0;
            case KING:
                return PositionBonus.getKingOpeningPositionBonus(piece.getRow(), piece.getCol(), piece.getSide());
            case QUEEN:
                return ((piece.asBitMask() & openFiles) != 0) ? PositionBonus.QUEEN_ON_OPENFILE : 0;
            case ROOK:
                return ((piece.asBitMask() & openFiles) != 0) ? PositionBonus.ROOK_ON_OPENFILE : 0;
            default:
                logger.debug("Error: invalid piece value request!");
                return 0;
        }

    }

    public int getEndGamePositionValue(final Piece piece, final long openFiles) {

        switch (piece.getPieceID()) {
            case KNIGHT:
                return PositionBonus.getKnightPositionBonus(piece.getRow(), piece.getCol(), piece.getSide());
            case PAWN:
                return PositionBonus.getPawnPositionBonus(piece.getRow(), piece.getCol(), piece.getSide());
            case BISHOP:
                return 50;
            case KING:
                return PositionBonus.getKingEndGamePositionBonus(piece.getRow(), piece.getCol(), piece.getSide());
            case QUEEN:
                return ((piece.asBitMask() & openFiles) != 0) ? PositionBonus.QUEEN_ON_OPENFILE + 100 : 0;
            case ROOK:
                return ((piece.asBitMask() & openFiles) != 0) ? PositionBonus.ROOK_ON_OPENFILE + 50 : 0;
            default:
                logger.debug("Error: invalid piece value request!");
                return 0;
        }

    }

    public int openingPositionScore(final int side, final Board b, final long openFiles) {
        int score = 0;

        for (Piece p : b.getPieces()[side]) {
            score += getOpeningPositionValue(p, openFiles);
        }

        return score;
    }

    public int endGamePositionScore(final int side, final Board b, final long openFiles) {
        int score = 0;

        for (Piece p : b.getPieces()[side]) {
            score += getEndGamePositionValue(p, openFiles);
        }

        return score;
    }

    public int materialScore(final int side, final Board b) {
        int score = 0;

        for (Piece p : b.getPieces()[side]) {
            score += Values.getPieceValue(p.getPieceID());
        }

        return score;
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
            return (~southFill(foePawns | Pawn.getPawnAttacks(foePawns, Side.BLACK)) & friendPawns);
        } else {
            return (~northFill(foePawns | Pawn.getPawnAttacks(foePawns, Side.WHITE)) & friendPawns);
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

        final int openingMyScore = openingPositionScore(side, b, yourOpenFiles);
        final int openingYourScore = openingPositionScore(otherSide, b, myOpenFiles);

        final int endGameMyScore = endGamePositionScore(side, b, yourOpenFiles);
        final int endGameYourScore = endGamePositionScore(otherSide, b, myOpenFiles);

        final int myMaterialScore = materialScore(side, b);
        final int yourMaterialScore = materialScore(otherSide, b);

        final int myScore = (openingMyScore * (ENDGAME_PHASE - phase) + endGameMyScore * phase) / ENDGAME_PHASE + myMaterialScore + myPawnScore;
        final int yourScore = (openingYourScore * (ENDGAME_PHASE - phase) + endGameYourScore * phase) / ENDGAME_PHASE + yourMaterialScore + yourPawnScore;

        return myScore - yourScore;
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

        final int openingMyScore = openingPositionScore(side, b, yourOpenFiles);
        final int openingYourScore = openingPositionScore(otherSide, b, myOpenFiles);

        final int endGameMyScore = endGamePositionScore(side, b, yourOpenFiles);
        final int endGameYourScore = endGamePositionScore(otherSide, b, myOpenFiles);

        final int myMaterialScore = materialScore(side, b);
        final int yourMaterialScore = materialScore(otherSide, b);

        final int myScore = (openingMyScore * (ENDGAME_PHASE - phase) + endGameMyScore * phase) / ENDGAME_PHASE + myMaterialScore + myPawnScore;
        final int yourScore = (openingYourScore * (ENDGAME_PHASE - phase) + endGameYourScore * phase) / ENDGAME_PHASE + yourMaterialScore + yourPawnScore;

        Map<String, Object> scoreMap = new HashMap<>();

        scoreMap.put("phase", phase);

        scoreMap.put("myPawnScore", myPawnScore);
        scoreMap.put("yourPawnScore", yourPawnScore);

        scoreMap.put("openingMyScore", openingMyScore);
        scoreMap.put("openingYourScore", openingYourScore);

        scoreMap.put("endGameMyScore", endGameMyScore);
        scoreMap.put("endGameYourScore", endGameYourScore);

        scoreMap.put("myMaterialScore", myMaterialScore);
        scoreMap.put("yourMaterialScore", yourMaterialScore);

        scoreMap.put("myScore", myScore);
        scoreMap.put("yourScore", yourScore);

        scoreMap.put("scoreDiff", myScore-yourScore);

        return scoreMap.toString();
    }
}
