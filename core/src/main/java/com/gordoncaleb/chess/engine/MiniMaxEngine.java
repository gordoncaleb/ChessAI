package com.gordoncaleb.chess.engine;


import com.gordoncaleb.chess.board.Board;
import com.gordoncaleb.chess.board.Move;
import com.gordoncaleb.chess.board.MoveContainer;
import com.gordoncaleb.chess.board.MoveContainerFactory;
import com.gordoncaleb.chess.engine.score.StaticScorer;

public class MiniMaxEngine {

    public static final int MAX_DEPTH = 20;

    private final StaticScorer scorer;
    private final MoveContainer[] moveContainers;
    private final MovePath movePath;

    public MiniMaxEngine(StaticScorer scorer) {
        this.scorer = scorer;
        this.moveContainers = MoveContainerFactory.buildMoveContainers(MAX_DEPTH);
        this.movePath = new MovePath(moveContainers);
    }

    public MovePath search(final Board board, final int depth) {
        resetMoveContainers();
        searchTree(board, 0, depth, Integer.MIN_VALUE);
        return movePath;
    }

    private int searchTree(final Board b, final int level, final int maxLevel, int maxScore) {

        if (level < maxLevel) {

            MoveContainer moves = moveContainers[level];
            b.makeNullMove();
            b.generateValidMoves(moves);

            for (int i = 0; i < moves.size(); i++) {
                b.makeMove(moves.get(i));
                int temp = searchTree(b, level + 1, maxLevel, -maxScore);

                if (temp > maxScore) {
                    maxScore = temp;
                    moves.markMove();
                }

                b.undoMove();
            }

            return -maxScore;
        } else {
            return scorer.staticScore(b);
        }
    }

    private void resetMoveContainers() {
        for (MoveContainer mc : moveContainers) {
            mc.resetMarkedMove();
        }
    }

}
