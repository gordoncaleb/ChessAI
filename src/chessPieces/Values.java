package chessPieces;

public class Values {
	public static final int KING_VALUE = 1000000;
	public static final int QUEEN_VALUE = 800;
	public static final int ROOK_VALUE = 500;
	public static final int BISHOP_VALUE = 400;
	public static final int KNIGHT_VALUE = 300;
	public static final int PAWN_VALUE = 100;
	public static final int CASTLE_VALUE = 75;
	public static final int CASTLE_ABILITY_LOST_VALUE = -25;
	public static final int RISK_QUEEN = -10;
	
	public static final int getPieceValue(PieceID id){
		int value;
			switch(id){
			case KING: value = KING_VALUE; break;
			case QUEEN: value = QUEEN_VALUE; break;
			case ROOK: value = ROOK_VALUE; break;
			case BISHOP: value = BISHOP_VALUE; break;
			case KNIGHT: value = KNIGHT_VALUE; break;
			case PAWN: value = PAWN_VALUE; break;
			default: value = 0; break;
			}
		return value;
	}
}
