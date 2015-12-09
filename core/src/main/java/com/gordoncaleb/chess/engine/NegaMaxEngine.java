package com.gordoncaleb.chess.engine;

import com.gordoncaleb.chess.board.Board;
import com.gordoncaleb.chess.board.MoveContainer;
import com.gordoncaleb.chess.engine.score.StaticScorer;
import com.gordoncaleb.chess.engine.score.Values;

public class NegaMaxEngine {

    private final StaticScorer scorer;
    private final MoveContainer[] moveContainers;
    private final MovePath movePath;

    public NegaMaxEngine(StaticScorer scorer, MoveContainer[] moveContainers) {
        this.scorer = scorer;
        this.moveContainers = moveContainers;
        this.movePath = new MovePath(moveContainers);
    }

    public MovePath search(final Board board, final int depth) {
        final int val = searchTree(board, 0, depth);

        final int checkMateFound = Values.CHECKMATE_MOVE - Math.abs(val);
        if (checkMateFound < depth) {
            movePath.setDepth(checkMateFound);
        } else {
            movePath.setDepth(depth);
        }

        return movePath;
    }

    private static int endOfGameValue(final Board board, final int level) {
        if (board.isInCheck()) {
            //checkmate
            return level - Values.CHECKMATE_MOVE;
        } else {
            //stalemate
            return Values.DRAW;
        }
    }

    private int searchTree(final Board board, final int level, final int maxLevel) {

        if (board.isDraw()) {
            return Values.DRAW;
        }

        if (level == maxLevel) {
            return scorer.staticScore(board);
        }

        board.makeNullMove();
        MoveContainer moves = board.generateValidMoves(moveContainers[level]);

        if (moves.isEmpty()) {
            return endOfGameValue(board, level);
        }

        int maxScore = Integer.MIN_VALUE;
        for (int i = 0; i < moves.size(); i++) {

            board.makeMove(moves.get(i));
            final int childScore = -searchTree(board, level + 1, maxLevel);

            if (childScore > maxScore) {
                maxScore = childScore;
                movePath.markMove(level, maxLevel, i);
            }

            board.undoMove();
        }

        return maxScore;
    }

}
