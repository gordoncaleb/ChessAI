package com.gordoncaleb.chess.engine.score;


import com.gordoncaleb.chess.board.pieces.Piece;

import static com.gordoncaleb.chess.board.pieces.Piece.PieceID.*;

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
	public static final int[] PIECE_PHASE_VAL = new int[PIECES_COUNT];

	static{
		PIECE_PHASE_VAL[PAWN] = PAWNPHASE;
		PIECE_PHASE_VAL[KNIGHT] = KNIGHTPHASE;
		PIECE_PHASE_VAL[BISHOP] = BISHOPPHASE;
		PIECE_PHASE_VAL[QUEEN] = QUEENPHASE;
		PIECE_PHASE_VAL[ROOK] = ROOKPHASE;
		PIECE_PHASE_VAL[KING] = 0;
	}

	public static final int[] PIECE_VALUE = { ROOK_VALUE, KNIGHT_VALUE, BISHOP_VALUE, QUEEN_VALUE, KING_VALUE, PAWN_VALUE };

	public static int getPieceValue(int id) {
		return PIECE_VALUE[id];
	}

}
