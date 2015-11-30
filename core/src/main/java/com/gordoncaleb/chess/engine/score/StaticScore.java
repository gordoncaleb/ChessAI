package com.gordoncaleb.chess.engine.score;

import com.gordoncaleb.chess.board.bitboard.BitBoard;
import com.gordoncaleb.chess.board.Board;
import com.gordoncaleb.chess.board.Side;
import com.gordoncaleb.chess.board.pieces.Pawn;
import com.gordoncaleb.chess.board.pieces.Piece;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.gordoncaleb.chess.board.bitboard.Slide.northFill;
import static com.gordoncaleb.chess.board.bitboard.Slide.southFill;
import static com.gordoncaleb.chess.board.pieces.Piece.PieceID.*;

public class StaticScore {
    private static final Logger logger = LoggerFactory.getLogger(StaticScore.class);

    private long openFiles = 0;

    public int getOpeningPositionValue(final Piece piece) {

        if (piece == null) {
            return 0;
        }

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

    public int getEndGamePositionValue(final Piece piece) {

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

    public int openingPositionScore(final int side, final Board b) {
        int score = 0;

        for (Piece p : b.getPieces()[side]) {
            score += getOpeningPositionValue(p);
        }

        return score;
    }

    public int endGamePositionScore(final int side, final Board b) {
        int score = 0;

        for (Piece p : b.getPieces()[side]) {
            score += getEndGamePositionValue(p);
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

    public int pawnStructureScore(final int side, final int phase, final Board b) {

        final long pawns = b.getPosBitBoard()[PAWN][side];
        final long otherPawns = b.getPosBitBoard()[PAWN][Side.otherSide(side)];

        long files = 0x0101010101010101L;

        int occupiedCol = 0;
        for (int c = 0; c < 8; c++) {
            if ((files & pawns) != 0) {

                occupiedCol++;

                if ((files & otherPawns) == 0) {
                    openFiles |= files;
                }
            }
            files = files << 1;
        }

        final int doubledPawns = Long.bitCount(pawns) - occupiedCol;

        final long passedBB = BitBoard.getPassedPawns(pawns, otherPawns, side);

        int passedPawns = 0;
        for (int i = 0; i < 8; i++) {
            passedPawns += Long.bitCount(passedBB & BitBoard.getRowMask(i)) * Values.PASSED_PAWN_BONUS[side][i];
        }

        final int isolatedPawns = Long.bitCount(getIsolatedPawns(pawns, side));

        final int backedPawns = Long.bitCount(Pawn.getPawnAttacks(pawns, side) & pawns);

        return backedPawns * Values.BACKED_PAWN_BONUS +
                doubledPawns * Values.DOUBLED_PAWN_BONUS +
                isolatedPawns * Values.ISOLATED_PAWN_BONUS +
                ((passedPawns * phase) / 256);
    }

    public long getIsolatedPawns(long pawns, int side) {
        long pawnAttacks = Pawn.getPawnAttacks(pawns, side);
        return ~(southFill(pawnAttacks) | northFill(pawnAttacks)) & pawns;
    }

    public int calcGamePhase(final Board b) {

        long[][] bbs = b.getPosBitBoard();

        final int phase = Values.TOTALPHASE -
                (Long.bitCount(bbs[PAWN][Side.WHITE] | bbs[PAWN][Side.BLACK]) * Values.PIECE_PHASE_VAL[PAWN]) -
                (Long.bitCount(bbs[KNIGHT][Side.WHITE] | bbs[KNIGHT][Side.BLACK]) * Values.PIECE_PHASE_VAL[KNIGHT]) -
                (Long.bitCount(bbs[BISHOP][Side.WHITE] | bbs[BISHOP][Side.BLACK]) * Values.PIECE_PHASE_VAL[BISHOP]) -
                (Long.bitCount(bbs[ROOK][Side.WHITE] | bbs[ROOK][Side.BLACK]) * Values.PIECE_PHASE_VAL[ROOK]) -
                (Long.bitCount(bbs[QUEEN][Side.WHITE] | bbs[QUEEN][Side.BLACK]) * Values.PIECE_PHASE_VAL[QUEEN]);

        return (phase * 256 + (Values.TOTALPHASE / 2)) / Values.TOTALPHASE;
    }

    public int staticScore(final Board b) {
        return staticScore(b, b.getTurn());
    }

    public int staticScore(final Board b, final int side) {
        final int otherSide = Side.otherSide(side);
        final int phase = calcGamePhase(b);

        final int myPawnScore = pawnStructureScore(side, phase, b);
        final int yourPawnScore = pawnStructureScore(otherSide, phase, b);

        final int openingMyScore = openingPositionScore(side, b);
        final int openingYourScore = openingPositionScore(otherSide, b);

        final int endGameMyScore = endGamePositionScore(side, b);
        final int endGameYourScore = endGamePositionScore(otherSide, b);

        final int myScore = (openingMyScore * (256 - phase) + endGameMyScore * phase) / 256 + materialScore(side, b) + myPawnScore;
        final int yourScore = (openingYourScore * (256 - phase) + endGameYourScore * phase) / 256 + materialScore(otherSide, b) + yourPawnScore;

        return myScore - yourScore;
    }

    public String printBoardScoreBreakDown(Board b) {

        String score = "";

        int turn = b.getTurn();
        int otherSide = Side.otherSide(turn);
        int phase = calcGamePhase(b);

        score += ("int phase = " + phase) + "\n";

        score += ("int myPawnScore = " + pawnStructureScore(turn, phase, b)) + "\n";
        score += ("int yourPawnScore = " + pawnStructureScore(otherSide, phase, b)) + "\n";

        score += ("int openingMyScore = " + materialScore(turn, b) + "(openingMaterialScore)+" + "(castleScore)+" + openingPositionScore(turn, b))
                + "(openPositionScore)" + "\n";
        score += ("int openingYourScore = " + materialScore(otherSide, b) + "(openMaterialScore)+" + "(castleScore)+"
                + openingPositionScore(otherSide, b) + "(openPositionScore)")
                + "\n";

        score += ("int endGameMyScore = " + materialScore(turn, b) + "(endGameMaterial)+" + endGamePositionScore(turn, b) + "(endGamePosition)") + "\n";
        score += ("int endGameYourScore = " + materialScore(otherSide, b) + "(endGameMaterial)+" + endGamePositionScore(otherSide, b) + "(endGamePosition)")
                + "\n";

        int myopen = materialScore(turn, b) + openingPositionScore(turn, b);
        int youropen = materialScore(otherSide, b) + openingPositionScore(otherSide, b);

        int myend = materialScore(turn, b) + endGamePositionScore(turn, b);
        int yourend = materialScore(otherSide, b) + endGamePositionScore(otherSide, b);

        int myscore = (myopen * (256 - phase) + myend * phase) / 256 + pawnStructureScore(turn, phase, b);

        int yourscore = (youropen * (256 - phase) + yourend * phase) / 256 + pawnStructureScore(otherSide, phase, b);

        score += ("int myScore = " + myscore + "\n");
        score += ("int yourScore = " + yourscore + "\n");

        score += ("ptDiff = " + myscore + "-" + yourscore + "=" + (myscore - yourscore) + "\n");

        return score;
    }
}
