package com.gordoncaleb.chess.pieces;

import com.gordoncaleb.chess.backend.Board;
import com.gordoncaleb.chess.backend.Side;

public class Values {
	public static final int CHECKMATE_MASK = (int) Math.pow(2, 19);

	public static final int KING_VALUE = (int) Math.pow(2, 20) - 1;
	public static final int QUEEN_VALUE = 1200;
	public static final int ROOK_VALUE = 600;
	public static final int BISHOP_VALUE = 400;
	public static final int KNIGHT_VALUE = 390;
	public static final int PAWN_VALUE = 100;
	public static final int NEAR_CASTLE_VALUE = 90;
	public static final int FAR_CASTLE_VALUE = 80;
	public static final int NEAR_CASTLE_ABILITY_LOST_VALUE = 50;
	public static final int FAR_CASTLE_ABILITY_LOST_VALUE = 40;
	public static final int CASTLE_ABILITY_LOST_VALUE = FAR_CASTLE_ABILITY_LOST_VALUE + NEAR_CASTLE_ABILITY_LOST_VALUE;
	public static final int CHECKMATE_MOVE = KING_VALUE;

	public static final int BACKED_PAWN_BONUS = 2;
	public static final int DOUBLED_PAWN_BONUS = -5;
	public static final int ISOLATED_PAWN_BONUS = -10;
	public static final int QUEENING_PAWN_BONUS = 600;

	public static final int[][] PASSED_PAWN_BONUS = { { 0, 10, 20, 40, 60, 100, 600, 0 }, { 0, 600, 100, 60, 40, 20, 10, 0 } };

	public static final int PAWNPHASE = 0;
	public static final int KNIGHTPHASE = 1;
	public static final int BISHOPPHASE = 1;
	public static final int ROOKPHASE = 2;
	public static final int QUEENPHASE = 4;

	public static final int TOTALPHASE = PAWNPHASE * 16 + KNIGHTPHASE * 4 + BISHOPPHASE * 4 + ROOKPHASE * 4 + QUEENPHASE * 2;
	public static final int[] PIECE_PHASE_VAL = { ROOKPHASE, KNIGHTPHASE, BISHOPPHASE, QUEENPHASE, 0, PAWNPHASE };

	public static final int[] PIECE_VALUE = { ROOK_VALUE, KNIGHT_VALUE, BISHOP_VALUE, QUEEN_VALUE, KING_VALUE, PAWN_VALUE };

	public static int getPieceValue(Piece.PieceID id) {
		return PIECE_VALUE[id.ordinal()];
	}

	public static String printBoardScoreBreakDown(Board b) {

		String score = "";

		Side turn = b.getTurn();
		int phase = b.calcGamePhase();

		score += ("int phase = " + phase) + "\n";

		score += ("int myPawnScore = " + b.pawnStructureScore(turn, phase)) + "\n";
		score += ("int yourPawnScore = " + b.pawnStructureScore(turn.otherSide(), phase)) + "\n";

		score += ("int openingMyScore = " + b.materialScore(turn) + "(openingMaterialScore)+" + b.castleScore(turn) + "(castleScore)+" + b.openingPositionScore(turn))
				+ "(openPositionScore)" + "\n";
		score += ("int openingYourScore = " + b.materialScore(turn.otherSide()) + "(openMaterialScore)+" + b.castleScore(turn.otherSide()) + "(castleScore)+"
				+ b.openingPositionScore(turn.otherSide()) + "(openPositionScore)")
				+ "\n";

		score += ("int endGameMyScore = " + b.materialScore(turn) + "(endGameMaterial)+" + b.endGamePositionScore(turn) + "(endGamePosition)") + "\n";
		score += ("int endGameYourScore = " + b.materialScore(turn.otherSide()) + "(endGameMaterial)+" + b.endGamePositionScore(turn.otherSide()) + "(endGamePosition)")
				+ "\n";

		int myopen = b.materialScore(turn) + b.castleScore(turn) + b.openingPositionScore(turn);
		int youropen = b.materialScore(turn.otherSide()) + b.castleScore(turn.otherSide()) + b.openingPositionScore(turn.otherSide());

		int myend = b.materialScore(turn) + b.endGamePositionScore(turn);
		int yourend = b.materialScore(turn.otherSide()) + b.endGamePositionScore(turn.otherSide());

		int myscore = (myopen * (256 - phase) + myend * phase) / 256 + b.pawnStructureScore(turn, phase);

		int yourscore = (youropen * (256 - phase) + yourend * phase) / 256 + b.pawnStructureScore(turn.otherSide(), phase);

		score += ("int myScore = " + myscore + "\n");
		score += ("int yourScore = " + yourscore + "\n");

		score += ("ptDiff = " + myscore + "-" + yourscore + "=" + (myscore - yourscore) + "\n");

		return score;
	}
}
