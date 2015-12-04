package com.gordoncaleb.chess.engine;


import com.gordoncaleb.chess.board.Board;
import com.gordoncaleb.chess.board.MoveContainer;
import com.gordoncaleb.chess.board.MoveContainerFactory;
import com.gordoncaleb.chess.engine.score.StaticScorer;

public class NegaMaxEngine {

    public static final int MAX_DEPTH = 20;

    private final StaticScorer scorer;
    private final MoveContainer[] moveContainers;
    private final MovePath movePath;

    public NegaMaxEngine(StaticScorer scorer) {
        this.scorer = scorer;
        this.moveContainers = MoveContainerFactory.buildMoveContainers(MAX_DEPTH);
        this.movePath = new MovePath(moveContainers);
    }

    public MovePath search(final Board board, final int depth) {
        searchTree(board, 0, depth);
        movePath.setSize(depth);
        return movePath;
    }

    private int searchTree(final Board b, final int level, final int maxLevel) {

        if (level == maxLevel) {
            return scorer.staticScore(b);
        }

        MoveContainer moves = moveContainers[level];
        b.makeNullMove();
        b.generateValidMoves(moves);

        int myMax = Integer.MIN_VALUE;
        for (int i = 0; i < moves.size(); i++) {

            b.makeMove(moves.get(i));
            int temp = -searchTree(b, level + 1, maxLevel);

            if (temp > myMax) {
                myMax = temp;
                moves.markMove(i);
                promoteBelow(level + 1, maxLevel);
            }

            b.undoMove();
        }

        return myMax;
    }

    private void promoteBelow(final int start, final int end) {
        for (int i = start; i < end; i++) {
            moveContainers[i].promoteMarkedMove();
        }
    }

}
