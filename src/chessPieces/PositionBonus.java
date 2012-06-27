package chessPieces;

import chessBackend.Side;

public class PositionBonus {
	private static int[][] knightBonus = 
		
		{ { -30, -20, -10, -10, -10, -10, -20, -30 },// 1
			{ -20, 10, 10, 10, 10, 10, 10, -20 },// 2
			{ -10, 10, 20, 20, 20, 20, 10, -10 },// 3
			{ -10, 10, 20, 25, 25, 20, 10, -10 },// 4
			{ -10, 10, 20, 25, 25, 20, 10, -10 },// 5
			{ -10, 10, 20, 20, 20, 20, 10, -10 },// 6
			{ -20, 10, 10, 10, 10, 10, 10, -20 },// 7
			{ -30, -20, -10, -10, -10, -10, -20, -30 } // 8
	// a, b, c, d, e, f, g, h
	};

	private static int[][] kingOpeningBonus = 
		{ { 25, 25, 35, 0, 10, 0, 40, 35 },// 1
			{ 10, 10, 5, 0, 0, 5, 10, 15 },// 2
			{ 5, 0, -10, -10, -10, -10, 0, 5 },// 3
			{ 0, -15, -20, -25, -25, -20, -15, 0 },// 4
			{ -20, -25, -30, -35, -35, -30, -25, -20 },// 5
			{ -30, -35, -40, -45, -45, -40, -35, -30 },// 6
			{ -40, -40, -50, -55, -55, -50, -45, -40 },// 7
			{ -50, -45, -60, -65, -65, -60, -55, -50 } // 8
	// a, b, c, d, e, f, g, h
	};

	private static int[][] kingEndGameBonus = 
		{ { -10, -5, 0, 0, 0, 0, -5, -10 },// 1
			{ -5, 20, 20, 20, 20, 20, 20, -5 },// 2
			{ 0, 20, 40, 40, 40, 40, 20, 0 },// 3
			{ 0, 20, 40, 45, 45, 40, 20, 0 },// 4
			{ 0, 20, 40, 45, 45, 40, 20, 0 },// 5
			{ 0, 20, 40, 40, 40, 40, 20, 0 },// 6
			{ -5, 20, 20, 20, 20, 20, 20, -5 },// 7
			{ -10, -5, 0, 0, 0, 0, -5, -10 } // 8
	// a, b, c, d, e, f, g, h
	};

	private static int[][] rookBonus = { { 0, 0, 7, 10, 10, 5, 0, 0 },// 1
			{ -5, 0, 0, 0, 0, 0, 0, -5 },// 2
			{ -5, 0, 0, 0, 0, 0, 0, -5 },// 3
			{ -5, 0, 0, 0, 0, 0, 0, -5 },// 4
			{ -5, 0, 0, 0, 0, 0, 0, -5 },// 5
			{ -5, 0, 0, 0, 0, 0, 0, -5 },// 6
			{ -5, 5, 10, 10, 10, 10, 5, -5 },// 7
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

	private static int[][] pawnBonus = { { 0, 0, 0, 0, 0, 0, 0, 0 },// 1
			{ 5, 2, 2, -10, -10, 2, 2, 5 },// 2
			{ 5, 0, 0, 0, 0, 0, 0, 5 },// 3
			{ 0, 10, 10, 15, 15, 10, 10, 0 },// 4
			{ 10, 15, 15, 20, 20, 15, 15, 10 },// 5
			{ 20, 20, 20, 20, 20, 20, 20, 20 },// 6
			{ 40, 40, 40, 40, 40, 40, 40, 40 },// 7
			{ 0, 0, 0, 0, 0, 0, 0, 0 } // 8
	// a, b, c, d, e, f, g, h
	};

	public static void applyScale() {
		scalePositionBonus(pawnBonus, 0.5);
		scalePositionBonus(rookBonus, 0.5);
		scalePositionBonus(kingEndGameBonus, 0.5);
		scalePositionBonus(kingOpeningBonus, 0.5);
		scalePositionBonus(knightBonus, 0.5);

		printBonus(pawnBonus, "pawn");
		printBonus(rookBonus, "rook");
		printBonus(kingEndGameBonus, "king endgame");
		printBonus(kingOpeningBonus, "king opening");
		printBonus(knightBonus, "knight");
	}

	private static void printBonus(int[][] bonus, String name) {

		System.out.println(name + " Bonus{");
		for (int i = 0; i < bonus.length; i++) {
			for (int y = 0; y < bonus[i].length; y++) {
				System.out.print(String.format("%3d", bonus[i][y]) + ",");
			}
			System.out.println("\n");
		}

		System.out.println("}");
	}

	public static void scalePositionBonus(int[][] bonus, double scale) {
		for (int r = 0; r < 8; r++) {
			for (int c = 0; c < 8; c++) {
				bonus[r][c] = (int) (bonus[r][c] * scale);
			}
		}
	}

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

	public static final int getRookBonus(int row, int col) {
		return rookBonus[row][col];
	}

	public static final int getPawnMoveBonus(int fromRow, int fromCol, int toRow, int toCol, Side player) {
		int bonus;
		if (player == Side.BLACK) {
			bonus = pawnBonus[toRow][toCol] - pawnBonus[fromRow][fromCol];
		} else {
			bonus = pawnBonus[7 - toRow][toCol] - pawnBonus[7 - fromRow][fromCol];
		}
		return bonus;
	}

	public static final int getPawnPositionBonus(int row, int col, Side player) {
		int bonus;
		if (player == Side.BLACK) {
			bonus = pawnBonus[row][col];
		} else {
			bonus = pawnBonus[7 - row][col];
		}
		return bonus;
	}

}
