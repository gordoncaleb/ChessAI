package com.gordoncaleb.chess.backend;

import com.gordoncaleb.chess.board.bitboard.BitBoard;
import com.gordoncaleb.chess.board.Board;
import com.gordoncaleb.chess.board.Side;
import com.gordoncaleb.chess.pieces.Pawn;
import com.gordoncaleb.chess.pieces.Piece;
import com.gordoncaleb.chess.pieces.PositionBonus;
import com.gordoncaleb.chess.pieces.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.gordoncaleb.chess.pieces.Piece.PieceID.*;

public class StaticScore {
    private static final Logger logger = LoggerFactory.getLogger(StaticScore.class);

    private long openFiles = 0;

    public int getPieceValue(final int row, final int col, final Board b) {
        return Values.getPieceValue(b.getPiece(row, col).getPieceID()) + getOpeningPositionValue(b.getPiece(row, col));
    }

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
                return ((piece.getBit() & openFiles) != 0) ? PositionBonus.QUEEN_ON_OPENFILE : 0;
            case ROOK:
                return ((piece.getBit() & openFiles) != 0) ? PositionBonus.ROOK_ON_OPENFILE : 0;
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
                return ((piece.getBit() & openFiles) != 0) ? PositionBonus.QUEEN_ON_OPENFILE + 100 : 0;
            case ROOK:
                return ((piece.getBit() & openFiles) != 0) ? PositionBonus.ROOK_ON_OPENFILE + 50 : 0;
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

    public int castleScore(final int side, final int[] castleRights) {
        int score = 0;
        final int castleRight = castleRights[side];

        switch (castleRight) {
            case 1:
                score += Values.FAR_CASTLE_VALUE;
                break;
            case 2:
                score += Values.NEAR_CASTLE_VALUE;
                break;
        }

        return score;
    }

    public int pawnStructureScore(final int side, final int phase, final Board b) {

        final long pawns = b.getPosBitBoard()[Piece.PieceID.PAWN][side];
        final long otherPawns = b.getPosBitBoard()[Piece.PieceID.PAWN][Side.otherSide(side)];

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

        final int isolatedPawns = Long.bitCount(BitBoard.getIsolatedPawns(pawns, side)) * Values.ISOLATED_PAWN_BONUS;

        return BitBoard.getBackedPawns(pawns) * Values.BACKED_PAWN_BONUS + doubledPawns * Values.DOUBLED_PAWN_BONUS + ((passedPawns * phase) / 256) + isolatedPawns;
    }

    public int calcGamePhase(final Board b) {

        int phase = Values.TOTALPHASE;

        for (int i = 0; i < 2; i++) {
            for (Piece p : b.getPieces()[i]) {
                phase -= Values.PIECE_PHASE_VAL[p.getPieceID()];
            }
        }

        phase = (phase * 256 + (Values.TOTALPHASE / 2)) / Values.TOTALPHASE;

        return phase;
    }

    public int staticScore(final Board b) {
        return staticScore(b, b.getTurn());
    }

    public int staticScore(final Board b, final int side) {
        final int otherSide = Side.otherSide(side);
        final int phase = calcGamePhase(b);

        final int myPawnScore = pawnStructureScore(side, phase, b);
        final int yourPawnScore = pawnStructureScore(otherSide, phase, b);

        final int openingMyScore = castleScore(side, b.getCastleHistory()) + openingPositionScore(side, b);
        final int openingYourScore = castleScore(otherSide, b.getCastleHistory()) + openingPositionScore(otherSide, b);

        final int endGameMyScore = endGamePositionScore(side, b);
        final int endGameYourScore = endGamePositionScore(otherSide, b);

        final int myScore = (openingMyScore * (256 - phase) + endGameMyScore * phase) / 256 + materialScore(side, b) + myPawnScore;
        final int yourScore = (openingYourScore * (256 - phase) + endGameYourScore * phase) / 256 + materialScore(otherSide, b) + yourPawnScore;

        return myScore - yourScore;
    }

    public boolean canQueen(final Board b) {

        final int turn = b.getTurn();
        final long p = b.getPosBitBoard()[Piece.PieceID.PAWN][turn];
        final long o = b.getAllPosBitBoard()[Side.otherSide(turn)];

        if (turn == Side.WHITE) {
            return (((((p >>> 8) & ~o) | (Pawn.getPawnAttacks(p, turn) & o)) & 0xFFL) != 0);
        } else {
            return (((((p << 8) & ~o) | (Pawn.getPawnAttacks(p, turn) & o)) & 0xFF00000000000000L) != 0);
        }
    }

    public String printBoardScoreBreakDown(Board b) {

        String score = "";

        int turn = b.getTurn();
        int otherSide = Side.otherSide(turn);
        int phase = calcGamePhase(b);

        score += ("int phase = " + phase) + "\n";

        score += ("int myPawnScore = " + pawnStructureScore(turn, phase, b)) + "\n";
        score += ("int yourPawnScore = " + pawnStructureScore(otherSide, phase, b)) + "\n";

        score += ("int openingMyScore = " + materialScore(turn, b) + "(openingMaterialScore)+" + castleScore(turn, b.getCastleHistory()) + "(castleScore)+" + openingPositionScore(turn, b))
                + "(openPositionScore)" + "\n";
        score += ("int openingYourScore = " + materialScore(otherSide, b) + "(openMaterialScore)+" + castleScore(otherSide, b.getCastleHistory()) + "(castleScore)+"
                + openingPositionScore(otherSide, b) + "(openPositionScore)")
                + "\n";

        score += ("int endGameMyScore = " + materialScore(turn, b) + "(endGameMaterial)+" + endGamePositionScore(turn, b) + "(endGamePosition)") + "\n";
        score += ("int endGameYourScore = " + materialScore(otherSide, b) + "(endGameMaterial)+" + endGamePositionScore(otherSide, b) + "(endGamePosition)")
                + "\n";

        int myopen = materialScore(turn, b) + castleScore(turn, b.getCastleHistory()) + openingPositionScore(turn, b);
        int youropen = materialScore(otherSide, b) + castleScore(otherSide, b.getCastleHistory()) + openingPositionScore(otherSide, b);

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
