package com.gordoncaleb.chess.engine.score;

import com.gordoncaleb.chess.board.Side;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PositionBonus {
    private static final Logger logger = LoggerFactory.getLogger(PositionBonus.class);

    private static int[][] knightBonus =

            {{-30, -20, -10, -10, -10, -10, -20, -30},// 1
                    {-20, 10, 10, 10, 10, 10, 10, -20},// 2
                    {-10, 10, 20, 20, 20, 20, 10, -10},// 3
                    {-10, 10, 20, 25, 25, 20, 10, -10},// 4
                    {-10, 10, 20, 25, 25, 20, 10, -10},// 5
                    {-10, 10, 20, 20, 20, 20, 10, -10},// 6
                    {-20, 10, 10, 10, 10, 10, 10, -20},// 7
                    {-30, -20, -10, -10, -10, -10, -20, -30} // 8
                    // a, b, c, d, e, f, g, h
            };

    private static int[][] kingOpeningBonus =
            {{15, 20, 10, 0, 0, 10, 20, 15},// 1
                    {10, 5, 0, 0, 0, 0, 5, 10},// 2
                    {2, 0, 0, 0, 0, 0, 0, 2},// 3
                    {0, -1, -2, -2, -2, -2, -1, 0},// 4
                    {-2, -2, -3, -3, -3, -3, -2, -2},// 5
                    {-3, -3, -4, -4, -4, -4, -3, -3},// 6
                    {-4, -4, -5, -5, -5, -5, -4, -4},// 7
                    {-5, -4, -6, -6, -6, -6, -5, -5} // 8
                    // a, b, c, d, e, f, g, h
            };

    private static int[][] kingEndGameBonus =
            {{-10, -2, 0, 0, 0, 0, -2, -10},// 1
                    {-2, 5, 5, 5, 5, 5, 5, -5},// 2
                    {0, 5, 10, 10, 10, 10, 5, 0},// 3
                    {0, 5, 10, 12, 12, 10, 5, 0},// 4
                    {0, 5, 10, 12, 12, 10, 5, 0},// 5
                    {0, 5, 10, 10, 10, 10, 5, 0},// 6
                    {-5, 5, 5, 5, 5, 5, 5, -5},// 7
                    {-10, -5, 0, 0, 0, 0, -5, -10} // 8
                    // a, b, c, d, e, f, g, h
            };

    public static int ROOK_ON_OPENFILE_OPENING = 20;
    public static int QUEEN_ON_OPENFILE_OPENING = 10;

    public static int ROOK_ON_OPENFILE_ENDGAME = 40;
    public static int QUEEN_ON_OPENFILE_ENDGAME = 20;

    public static int BISHOP_OPENING = 1;
    public static int BISHOP_ENDGAME = 50;

    private static int[][] rookBonus =
            {{0, 0, 7, 10, 10, 5, 0, 0},// 1
                    {-5, 0, 0, 0, 0, 0, 0, -5},// 2
                    {-5, 0, 0, 0, 0, 0, 0, -5},// 3
                    {-5, 0, 0, 0, 0, 0, 0, -5},// 4
                    {-5, 0, 0, 0, 0, 0, 0, -5},// 5
                    {-5, 0, 0, 0, 0, 0, 0, -5},// 6
                    {-5, 5, 10, 10, 10, 10, 5, -5},// 7
                    {0, 0, 0, 0, 0, 0, 0, 0} // 8
                    // a, b, c, d, e, f, g, h
            };

    private static int[][] pawnBonus =
            {{0, 0, 0, 0, 0, 0, 0, 0},// 1
                    {2, 2, 2, 1, 1, 2, 2, 2},// 2
                    {3, 3, 3, 2, 2, 3, 3, 3},// 3
                    {3, 3, 3, 4, 4, 3, 3, 3},// 4
                    {10, 15, 15, 20, 20, 15, 15, 10},// 5
                    {50, 50, 50, 50, 50, 50, 50, 50},// 6
                    {100, 100, 100, 100, 100, 100, 100, 100},// 7
                    {0, 0, 0, 0, 0, 0, 0, 0} // 8
                    // a, b, c, d, e, f, g, h
            };

    public static void applyScale() {
        //scalePositionBonus(pawnBonus, 0.5);
        //scalePositionBonus(rookBonus, 0.5);
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

        logger.debug(name + " Bonus{");
        for (int i = 0; i < bonus.length; i++) {
            for (int y = 0; y < bonus[i].length; y++) {
                System.out.print(String.format("%3d", bonus[i][y]) + ",");
            }
            logger.debug("\n");
        }

        logger.debug("}");
    }

    public static void scalePositionBonus(int[][] bonus, double scale) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                bonus[r][c] = (int) (bonus[r][c] * scale);
            }
        }
    }

    public static int getKnightPositionBonus(int row, int col, int player) {
        int bonus;
        if (player == Side.BLACK) {
            bonus = knightBonus[row][col];
        } else {
            bonus = knightBonus[7 - row][col];
        }
        return bonus;
    }

    public static int getKingOpeningPositionBonus(int row, int col, int player) {
        int bonus;
        if (player == Side.BLACK) {
            bonus = kingOpeningBonus[row][col];
        } else {
            bonus = kingOpeningBonus[7 - row][col];
        }
        return bonus;
    }

    public static int getKingEndGamePositionBonus(int row, int col, int player) {
        int bonus;
        if (player == Side.BLACK) {
            bonus = kingEndGameBonus[row][col];
        } else {
            bonus = kingEndGameBonus[7 - row][col];
        }
        return bonus;
    }

    public static int getPawnPositionBonus(int row, int col, int player) {
        int bonus;
        if (player == Side.BLACK) {
            bonus = pawnBonus[row][col];
        } else {
            bonus = pawnBonus[7 - row][col];
        }
        return bonus;
    }

}
