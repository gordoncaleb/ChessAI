package chessPieces;

import chessBackend.Side;

public class PositionBonus {
	private static int[][] knightBonus = 
		  { { 1, 2, 4, 4, 4, 4, 2, 1 },// 1
			{ 1, 3, 4, 5, 5, 4, 3, 1 },// 2
			{ 3, 4, 5, 6, 6, 5, 4, 3 },// 3
			{ 4, 5, 6, 8, 8, 6, 5, 4 },// 4
			{ 5, 6, 7, 8, 8, 7, 6, 5 },// 5
			{ 4, 5, 6, 7, 7, 6, 5, 4 },// 6
			{ 3, 4, 5, 6, 6, 5, 4, 3 },// 7
			{ 1, 3, 4, 5, 5, 4, 3, 1 } // 8
	// a, b, c, d, e, f, g, h
	};

	private static int[][] kingOpeningBonus = 
		  { { 0, 0, 0, 0, 0, 0, 0, 0 },// 1
			{ 0, 0, 0, 0, 0, 0, 0, 0 },// 2
			{ 0, 0, 0, 0, 0, 0, 0, 0 },// 3
			{ 0, 0, 0, 0, 0, 0, 0, 0 },// 4
			{ 0, 0, 0, 0, 0, 0, 0, 0 },// 5
			{ 0, 0, 0, 0, 0, 0, 0, 0 },// 6
			{ 5, 5, 2, 2, 2, 2, 5, 5 },// 7
			{ 15, 17, 20, 10, 10, 10, 20, 15 } // 8
	// a, b, c, d, e, f, g, h
	};

	private static int[][] kingEndGameBonus = 
		  { { 0, 0, 0, 0, 0, 0, 0, 0 },// 1
			{ 0, 1, 1, 1, 1, 1, 1, 0 },// 2
			{ 0, 1, 2, 2, 2, 2, 1, 0 },// 3
			{ 0, 1, 2, 3, 3, 2, 1, 0 },// 4
			{ 0, 1, 2, 3, 3, 2, 1, 0 },// 5
			{ 0, 1, 2, 2, 2, 2, 1, 0 },// 6
			{ 0, 1, 1, 1, 1, 1, 1, 0 },// 7
			{ 0, 0, 0, 0, 0, 0, 0, 0 } // 8
	// a, b, c, d, e, f, g, h
	};

	// private static int[][] knightBonus = {
	// {0,1,1,1,1,1,1,0},//1
	// {1,2,2,2,2,2,2,1},//2
	// {1,2,4,4,4,4,2,1},//3
	// {1,2,4,6,6,4,2,1},//4
	// {1,2,4,6,6,4,2,1},//5
	// {1,2,4,4,4,4,2,1},//6
	// {1,2,2,2,2,2,2,1},//7
	// {0,1,1,1,1,1,1,0} //8
	// //a,b,c,d,e,f,g,h
	// };

	// private static int[][] pawnBonus = { { 0, 0, 0, 0, 0, 0, 0, 0 },// 1
	// { 0, 0, 0, 0, 0, 0, 0, 0 },// 2
	// { 1, 2, 3, 5, 5, 3, 2, 1 },// 3
	// { 2, 3, 4, 6, 6, 4, 3, 2 },// 4
	// { 3, 4, 5, 7, 7, 5, 4, 3 },// 5
	// { 4, 5, 6, 8, 8, 6, 5, 4 },// 6
	// { 5, 6, 7, 9, 9, 7, 6, 5 },// 7
	// { 6, 7, 8, 10, 10, 8, 7, 6 } // 8
	// // a,b,c,d,e,f,g,h
	// };

	private static int[][] pawnOpeningBonus = 
		  { { 0,  0,  0,  0,  0,  0,  0, 0 },// 1
			{ 0,  0,  0,  0,  0,  0,  0, 0 },// 2
			{ 1,  2,  3,  5,  5,  3,  2, 1 },// 3
			{ 2,  4,  6, 10, 10,  6,  4, 2 },// 4
			{ 3,  6,  9, 15, 15,  9,  6, 3 },// 5
			{ 3,  6,  9, 15, 15,  9,  6, 3 },// 6
			{ 3,  6,  9, 15, 15,  9,  6, 3 },// 7
			{ 0,  0,  0,  0,  0,  0,  0, 0 } // 8
	// a, b, c, d, e, f, g, h
	};
	
	private static int[][] pawnEndGameBonus = 
		  { { 0,  0,  0,  0,  0,  0,  0, 0 },// 1
			{ 0,  0,  0,  0,  0,  0,  0, 0 },// 2
			{ 1,  2,  3,  5,  5,  3,  2, 1 },// 3
			{ 2,  4,  6, 10, 10,  6,  4, 2 },// 4
			{ 3,  6,  9, 15, 15,  9,  6, 3 },// 5
			{ 4,  8, 12, 20, 20, 12,  8, 4 },// 6
			{ 5, 10, 15, 25, 25, 15, 10, 5 },// 7
			{ 0,  0,  0,  0,  0,  0,  0, 0 } // 8
	// a, b, c, d, e, f, g, h
	};

	public static final int getKnightMoveBonus(int fromRow, int fromCol, int toRow, int toCol, Side player) {
		int bonus;
		if (player == Side.BLACK) {
			bonus = knightBonus[toRow][toCol] - knightBonus[fromRow][fromCol];
		} else {
			bonus = knightBonus[7 - toRow][toCol] - knightBonus[7 - fromRow][fromCol];
		}
		return bonus;
	}

	public static final int getKnightPositionBonus(int row, int col, Side player) {
		int bonus;
		if (player == Side.BLACK) {
			bonus = knightBonus[row][col];
		} else {
			bonus = knightBonus[7 - row][col];
		}
		return bonus;
	}

	public static final int getKingOpeningPositionBonus(int row, int col, Side player) {
		int bonus;
		if (player == Side.BLACK) {
			bonus = kingOpeningBonus[row][col];
		} else {
			bonus = kingOpeningBonus[7 - row][col];
		}
		return bonus;
	}
	
	public static final int getKingEndGamePositionBonus(int row, int col, Side player) {
		int bonus;
		if (player == Side.BLACK) {
			bonus = kingEndGameBonus[row][col];
		} else {
			bonus = kingEndGameBonus[7 - row][col];
		}
		return bonus;
	}

	public static final int getPawnMoveBonus(int fromRow, int fromCol, int toRow, int toCol, Side player) {
		int bonus;
		if (player == Side.BLACK) {
			bonus = pawnOpeningBonus[toRow][toCol] - pawnOpeningBonus[fromRow][fromCol];
		} else {
			bonus = pawnOpeningBonus[7 - toRow][toCol] - pawnOpeningBonus[7 - fromRow][fromCol];
		}
		return bonus;
	}

	public static final int getOpeningPawnPositionBonus(int row, int col, Side player) {
		int bonus;
		if (player == Side.BLACK) {
			bonus = pawnOpeningBonus[row][col];
		} else {
			bonus = pawnOpeningBonus[7 - row][col];
		}
		return bonus;
	}
	
	public static final int getEndGamePawnPositionBonus(int row, int col, Side player) {
		int bonus;
		if (player == Side.BLACK) {
			bonus = pawnEndGameBonus[row][col];
		} else {
			bonus = pawnEndGameBonus[7 - row][col];
		}
		return bonus;
	}
}
