package com.gordoncaleb.chess.engine;


import com.gordoncaleb.chess.board.Board;
import com.gordoncaleb.chess.board.MoveContainer;
import com.gordoncaleb.chess.engine.score.StaticScorer;

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
        searchTree(board, 0, depth);
        movePath.setDepth(depth);
        return movePath;
    }

    private int searchTree(final Board b, final int level, final int maxLevel) {

        if (level == maxLevel) {
            return scorer.staticScore(b);
        }

        b.makeNullMove();
        MoveContainer moves = b.generateValidMoves(moveContainers[level]);

        int maxScore = Integer.MIN_VALUE;
        for (int i = 0; i < moves.size(); i++) {

            b.makeMove(moves.get(i));
            final int childScore = -searchTree(b, level + 1, maxLevel);

            if (childScore > maxScore) {
                maxScore = childScore;
                movePath.markMove(level, maxLevel, i);
            }

            b.undoMove();
        }

        return maxScore;
    }

}
