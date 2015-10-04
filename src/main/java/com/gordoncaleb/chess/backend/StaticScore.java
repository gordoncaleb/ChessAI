package com.gordoncaleb.chess.backend;

import com.gordoncaleb.chess.pieces.Piece;
import com.gordoncaleb.chess.pieces.PositionBonus;
import com.gordoncaleb.chess.pieces.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StaticScore {
    private static final Logger logger = LoggerFactory.getLogger(StaticScore.class);

    private long openFiles = 0;

    public int getPieceValue(int row, int col, Board b) {
        return Values.getPieceValue(b.getPiece(row, col).getPieceID()) + getOpeningPositionValue(b.getPiece(row, col));
    }

    public int getOpeningPositionValue(Piece piece) {

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

    public int getEndGamePositionValue(Piece piece) {

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

    public int openingPositionScore(Side side, Board b) {
        int score = 0;

        for (Piece p : b.getPieces()[side.ordinal()]) {
            score += getOpeningPositionValue(p);
        }

        return score;
    }

    public int endGamePositionScore(Side side, Board b) {
        int score = 0;

        for (Piece p : b.getPieces()[side.ordinal()]) {
            score += getEndGamePositionValue(p);
        }

        return score;
    }

    public int materialScore(Side side, Board b) {
        int score = 0;

        for (Piece p : b.getPieces()[side.ordinal()]) {
            score += Values.getPieceValue(p.getPieceID());
        }

        return score;
    }

    public int castleScore(Side side, int[] castleRights) {
        int score = 0;
        int castleRight = castleRights[side.ordinal()];

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

    public int pawnStructureScore(Side side, int phase, Board b) {

        long pawns = b.getPosBitBoard()[Piece.PieceID.PAWN.ordinal()][side.ordinal()];
        long otherPawns = b.getPosBitBoard()[Piece.PieceID.PAWN.ordinal()][side.otherSide().ordinal()];

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

        int doubledPawns = Long.bitCount(pawns) - occupiedCol;

        long passedBB = BitBoard.getPassedPawns(pawns, otherPawns, side);

        int passedPawns = 0;

        for (int i = 0; i < 8; i++) {
            passedPawns += Long.bitCount(passedBB & BitBoard.getRowMask(i)) * Values.PASSED_PAWN_BONUS[side.ordinal()][i];
        }

        int isolatedPawns = Long.bitCount(BitBoard.getIsolatedPawns(pawns, side)) * Values.ISOLATED_PAWN_BONUS;

        return BitBoard.getBackedPawns(pawns) * Values.BACKED_PAWN_BONUS + doubledPawns * Values.DOUBLED_PAWN_BONUS + ((passedPawns * phase) / 256) + isolatedPawns;
    }

    public int calcGamePhase(Board b) {

        int phase = Values.TOTALPHASE;

        for (int i = 0; i < 2; i++) {
            for (Piece p : b.getPieces()[i]) {
                phase -= Values.PIECE_PHASE_VAL[p.getPieceID().ordinal()];
            }
        }

        phase = (phase * 256 + (Values.TOTALPHASE / 2)) / Values.TOTALPHASE;

        return phase;
    }

    public int staticScore(Board b) {
        Side turn = b.getTurn();
        int phase = calcGamePhase(b);

        int myPawnScore = pawnStructureScore(turn, phase, b);
        int yourPawnScore = pawnStructureScore(turn.otherSide(), phase, b);

        int openingMyScore = castleScore(turn, b.getCastleHistory()) + openingPositionScore(turn, b);
        int openingYourScore = castleScore(turn.otherSide(), b.getCastleHistory()) + openingPositionScore(turn.otherSide(), b);

        int endGameMyScore = endGamePositionScore(turn, b);
        int endGameYourScore = endGamePositionScore(turn.otherSide(), b);

        int myScore = (openingMyScore * (256 - phase) + endGameMyScore * phase) / 256 + materialScore(turn, b) + myPawnScore;
        int yourScore = (openingYourScore * (256 - phase) + endGameYourScore * phase) / 256 + materialScore(turn.otherSide(), b) + yourPawnScore;

        return myScore - yourScore;
    }

    public boolean canQueen(Board b) {

        Side turn = b.getTurn();
        long p = b.getPosBitBoard()[Piece.PieceID.PAWN.ordinal()][turn.ordinal()];
        long o = b.getAllPosBitBoard()[turn.otherSide().ordinal()];

        if (turn == Side.WHITE) {
            return (((((p >>> 8) & ~o) | (BitBoard.getPawnAttacks(p, turn) & o)) & 0xFFL) != 0);
        } else {
            return (((((p << 8) & ~o) | (BitBoard.getPawnAttacks(p, turn) & o)) & 0xFF00000000000000L) != 0);
        }
    }

    public String printBoardScoreBreakDown(Board b) {

        String score = "";

        Side turn = b.getTurn();
        int phase = calcGamePhase(b);

        score += ("int phase = " + phase) + "\n";

        score += ("int myPawnScore = " + pawnStructureScore(turn, phase, b)) + "\n";
        score += ("int yourPawnScore = " + pawnStructureScore(turn.otherSide(), phase, b)) + "\n";

        score += ("int openingMyScore = " + materialScore(turn, b) + "(openingMaterialScore)+" + castleScore(turn, b.getCastleHistory()) + "(castleScore)+" + openingPositionScore(turn, b))
                + "(openPositionScore)" + "\n";
        score += ("int openingYourScore = " + materialScore(turn.otherSide(), b) + "(openMaterialScore)+" + castleScore(turn.otherSide(), b.getCastleHistory()) + "(castleScore)+"
                + openingPositionScore(turn.otherSide(), b) + "(openPositionScore)")
                + "\n";

        score += ("int endGameMyScore = " + materialScore(turn, b) + "(endGameMaterial)+" + endGamePositionScore(turn, b) + "(endGamePosition)") + "\n";
        score += ("int endGameYourScore = " + materialScore(turn.otherSide(), b) + "(endGameMaterial)+" + endGamePositionScore(turn.otherSide(), b) + "(endGamePosition)")
                + "\n";

        int myopen = materialScore(turn, b) + castleScore(turn, b.getCastleHistory()) + openingPositionScore(turn, b);
        int youropen = materialScore(turn.otherSide(), b) + castleScore(turn.otherSide(), b.getCastleHistory()) + openingPositionScore(turn.otherSide(), b);

        int myend = materialScore(turn, b) + endGamePositionScore(turn, b);
        int yourend = materialScore(turn.otherSide(), b) + endGamePositionScore(turn.otherSide(), b);

        int myscore = (myopen * (256 - phase) + myend * phase) / 256 + pawnStructureScore(turn, phase, b);

        int yourscore = (youropen * (256 - phase) + yourend * phase) / 256 + pawnStructureScore(turn.otherSide(), phase, b);

        score += ("int myScore = " + myscore + "\n");
        score += ("int yourScore = " + yourscore + "\n");

        score += ("ptDiff = " + myscore + "-" + yourscore + "=" + (myscore - yourscore) + "\n");

        return score;
    }
}
