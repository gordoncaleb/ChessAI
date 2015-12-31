package com.gordoncaleb.chess.engine;

import com.gordoncaleb.chess.board.Board;
import com.gordoncaleb.chess.board.MoveContainer;
import com.gordoncaleb.chess.engine.score.BoardScorer;
import com.gordoncaleb.chess.engine.score.Values;

public class NegaMaxEngine implements Engine {

    private final BoardScorer scorer;
    private final MoveContainer[] moveContainers;
    private final MovePath movePath;

    public NegaMaxEngine(BoardScorer scorer, MoveContainer[] moveContainers) {
        this.scorer = scorer;
        this.moveContainers = moveContainers;
        this.movePath = new MovePath(moveContainers);
    }

    @Override
    public MovePath search(final Board board, final int maxLevel) {
        final int score = searchTree(board, 0, maxLevel);
        movePath.setScore(score);

        final int checkMateFound = Values.CHECKMATE_MOVE - Math.abs(score);
        if (checkMateFound < maxLevel) {
            movePath.setDepth(checkMateFound);
        } else {
            movePath.setDepth(maxLevel);
        }

        return movePath;
    }

    @Override
    public MovePath search(final Board board, final int maxLevel, final int startAlpha, final int startBeta) {
        return search(board, maxLevel);
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
            return scorer.drawValue();
        }

        if (level == maxLevel) {
            return scorer.staticScore(board);
        }

        board.makeNullMove();
        final MoveContainer moves = board.generateValidMoves(moveContainers[level]);

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
