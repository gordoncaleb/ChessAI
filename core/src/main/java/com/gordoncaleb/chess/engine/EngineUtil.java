package com.gordoncaleb.chess.engine;

import com.gordoncaleb.chess.board.Board;
import com.gordoncaleb.chess.board.BoardCondition;

public class EngineUtil {

    public static int nodeType(final int a, final int b, final int score) {
        if (score <= a) {
            //score is upper bound
            return BoardHashEntry.ValueBounds.ALL;
        } else if (score < b) {
            //score is exact
            return BoardHashEntry.ValueBounds.PV;
        } else {
            //score is lower bound
            return BoardHashEntry.ValueBounds.CUT;
        }
    }

    public static void verifyPV(final MovePath movePath,
                                final Board board,
                                final EngineHashTable hashTable) {

        final int movePathDepth = movePath.getDepth();

        int score = movePath.getScore();
        movePath.setEndBoardCondition(BoardCondition.IN_PLAY);

        for (int i = 0; i < movePathDepth; i++) {
            board.makeMove(movePath.get(i));
            board.makeNullMove();

            hashTable.set(board.getHashCode(),
                    score,
                    movePathDepth - (i + 1),
                    movePath.getRaw(i),
                    board.getMoveNumber(),
                    BoardHashEntry.ValueBounds.PV);

            score = -score;

            if (board.isDraw()) {
                movePath.setDepth(i + 1);
                movePath.setEndBoardCondition(BoardCondition.DRAW);
                break;
            }

            if (board.isCheckMate()) {
                movePath.setDepth(i + 1);
                movePath.setEndBoardCondition(BoardCondition.CHECKMATE);
                break;
            }
        }

        board.undo(movePath.getDepth());
    }

    public static void verifyPV(final MovePath movePath,
                                final Board board) {

        final int movePathDepth = movePath.getDepth();

        movePath.setEndBoardCondition(BoardCondition.IN_PLAY);

        for (int i = 0; i < movePathDepth; i++) {
            board.makeMove(movePath.get(i));
            board.makeNullMove();

            if (board.isDraw()) {
                movePath.setDepth(i + 1);
                movePath.setEndBoardCondition(BoardCondition.DRAW);
                break;
            }

            if (board.isCheckMate()) {
                movePath.setDepth(i + 1);
                movePath.setEndBoardCondition(BoardCondition.CHECKMATE);
                break;
            }
        }

        board.undo(movePath.getDepth());
    }
}
