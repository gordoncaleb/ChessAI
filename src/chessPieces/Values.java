package chessPieces;

import chessBackend.Board;
import chessBackend.Side;

public class Values {
	public static final int CHECKMATE_MASK = (int) Math.pow(2, 19);
	
	public static final int KING_VALUE = (int) Math.pow(2, 20) - 1;
	public static final int QUEEN_VALUE = 1200;
	public static final int ROOK_VALUE = 600;
	public static final int BISHOP_VALUE = 400;
	public static final int OPEN_KNIGHT_VALUE = 450;
	public static final int END_KNIGHT_VALUE = 300;
	public static final int PAWN_VALUE = 100;
	public static final int CASTLE_VALUE = 80;
	public static final int CASTLE_ABILITY_LOST_VALUE = 40;
	public static final int KNIGHT_ENDGAME_INC = 6;
	public static final int CHECKMATE_MOVE = KING_VALUE;
	public static final int STALEMATE_MOVE = 0;
	public static final int DRAW_DIVISOR = 10;
	public static final int CHECKMATE_DEPTH_INC = 1000;

	public static final int BACKED_PAWN_BONUS = 2;
	public static final int DOUBLED_PAWN_BONUS = -5;
	public static final int PASSED_PAWN_BONUS = 10;

	public static final int PAWNPHASE = 0;
	public static final int KNIGHTPHASE = 1;
	public static final int BISHOPPHASE = 1;
	public static final int ROOKPHASE = 2;
	public static final int QUEENPHASE = 4;

	public static final int TOTALPHASE = PAWNPHASE * 16 + KNIGHTPHASE * 4 + BISHOPPHASE * 4 + ROOKPHASE * 4 + QUEENPHASE * 2;
	public static final int[] PIECE_PHASE_VAL = { ROOKPHASE, KNIGHTPHASE, BISHOPPHASE, QUEENPHASE, 0, PAWNPHASE };

	public static final int getOpeningPieceValue(PieceID id) {
		int value;
		switch (id) {
		case KING:
			value = KING_VALUE;
			break;
		case QUEEN:
			value = QUEEN_VALUE;
			break;
		case ROOK:
			value = ROOK_VALUE;
			break;
		case BISHOP:
			value = BISHOP_VALUE;
			break;
		case KNIGHT:
			value = OPEN_KNIGHT_VALUE;
			break;
		case PAWN:
			value = PAWN_VALUE;
			break;
		default:
			value = 0;
			break;
		}
		return value;
	}

	public static final int getEndGamePieceValue(PieceID id) {
		int value;
		switch (id) {
		case KING:
			value = KING_VALUE;
			break;
		case QUEEN:
			value = QUEEN_VALUE;
			break;
		case ROOK:
			value = ROOK_VALUE;
			break;
		case BISHOP:
			value = BISHOP_VALUE;
			break;
		case KNIGHT:
			value = END_KNIGHT_VALUE;
			break;
		case PAWN:
			value = PAWN_VALUE;
			break;
		default:
			value = 0;
			break;
		}
		return value;
	}

	public static String printBoardScoreBreakDown(Board b) {

		String score = "";

		Side turn = b.getTurn();
		int phase = b.calcGamePhase();

		score += ("int phase = " + phase) + "\n";

		score += ("int myPawnScore = " + b.pawnStructureScore(turn, phase)) + "\n";
		score += ("int yourPawnScore = " + b.pawnStructureScore(turn.otherSide(), phase)) + "\n";

		score += ("int openingMyScore = " + b.openingMaterialScore(turn) + "(openingMaterialScore)+" + b.castleScore(turn) + "(castleScore)+" + b.openingPositionScore(turn))
				+ "(openPositionScore)" + "\n";
		score += ("int openingYourScore = " + b.openingMaterialScore(turn.otherSide()) + "(openMaterialScore)+" + b.castleScore(turn.otherSide()) + "(castleScore)+"
				+ b.openingPositionScore(turn.otherSide()) + "(openPositionScore)")
				+ "\n";

		score += ("int endGameMyScore = " + b.endGameMaterialScore(turn) + "(endGameMaterial)+" + b.endGamePositionScore(turn) + "(endGamePosition)") + "\n";
		score += ("int endGameYourScore = " + b.endGameMaterialScore(turn.otherSide()) + "(endGameMaterial)+" + b.endGamePositionScore(turn.otherSide()) + "(endGamePosition)")
				+ "\n";

		int myopen = b.openingMaterialScore(turn) + b.castleScore(turn) + b.openingPositionScore(turn);
		int youropen = b.openingMaterialScore(turn.otherSide()) + b.castleScore(turn.otherSide()) + b.openingPositionScore(turn.otherSide());

		int myend = b.endGameMaterialScore(turn) + b.endGamePositionScore(turn);
		int yourend = b.endGameMaterialScore(turn.otherSide()) + b.endGamePositionScore(turn.otherSide());

		int myscore = (myopen * (256 - phase) + myend * phase) / 256 + b.pawnStructureScore(turn, phase);

		int yourscore = (youropen * (256 - phase) + yourend * phase) /256 + b.pawnStructureScore(turn.otherSide(), phase);

		score += ("int myScore = " + myscore + "\n");
		score += ("int yourScore = " + yourscore + "\n");

		score += ("ptDiff = " + myscore + "-" + yourscore + "=" + (myscore - yourscore) + "\n");

		return score;
	}
}
