package com.gordoncaleb.chess.engine;

import com.gordoncaleb.chess.board.*;
import com.gordoncaleb.chess.engine.score.Values;
import com.gordoncaleb.chess.engine.score.StaticScorer;

public class Engine {

    private static final int START_ALPHA = -Values.CHECKMATE_MOVE + 1;
    private static final int START_BETA = -START_ALPHA;

    private final StaticScorer scorer;
    private final MoveContainer[] moveContainers;
    private final MovePath movePath;

    public Engine(StaticScorer scorer, MoveContainer[] moveContainers) {
        this.scorer = scorer;
        this.moveContainers = moveContainers;
        this.movePath = new MovePath(moveContainers);
    }

    public MovePath search(final Board board, final int depth) {
        return search(board, depth, START_ALPHA, START_BETA);
    }

    public MovePath search(final Board board, final int depth, final int startAlpha, final int startBeta) {
        final int val = searchTree(board, 0, depth, startAlpha, startBeta);

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

    public int searchTree(final Board board, final int level, final int maxLevel, int alpha, final int beta) {

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
        for (int m = 0; m < moves.size(); m++) {

            final Move move = moves.get(m);

            board.makeMove(move);

            final int childScore = -searchTree(board, level + 1, maxLevel, -beta, -alpha);

            board.undoMove();

            if (childScore > maxScore) {
                maxScore = childScore;
                movePath.markMove(level, maxLevel, m);
            }

            if (maxScore > alpha) {
                //narrowing alpha beta window
                alpha = maxScore;
            }

            if (alpha >= beta) {
                //pruned!
                break;
            }

        }

        return maxScore;
    }

}
