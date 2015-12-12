package com.gordoncaleb.chess.engine;

import com.gordoncaleb.chess.board.Board;
import com.gordoncaleb.chess.board.Move;
import com.gordoncaleb.chess.board.MoveContainer;
import com.gordoncaleb.chess.engine.score.BoardScorer;
import com.gordoncaleb.chess.engine.score.Values;

public class ABWithHashEngine implements Engine {

    private static final int START_ALPHA = -Values.CHECKMATE_MOVE + 1;
    private static final int START_BETA = -START_ALPHA;

    private final BoardScorer scorer;
    private final MoveContainer[] moveContainers;
    private final MovePath movePath;
    private final EngineHashTable hashTable;

    public ABWithHashEngine(BoardScorer scorer, MoveContainer[] moveContainers) {
        this.scorer = scorer;
        this.moveContainers = moveContainers;
        this.movePath = new MovePath(moveContainers);
        this.hashTable = new EngineHashTable(15);
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

    public int searchTree(final Board board, final int level, final int maxLevel, final int alpha, final int beta) {

        if (board.isDraw()) {
            return scorer.drawValue(level);
        }

        if (level == maxLevel) {
            return scorer.staticScore(board);
        }

        board.makeNullMove();
        MoveContainer moves = board.generateValidMoves(moveContainers[level]);

        if (moves.isEmpty()) {
            return scorer.endOfGameValue(board.isInCheck(), level);
        }

        int maxScore = Integer.MIN_VALUE;
        int a = alpha;
        for (int m = 0; m < moves.size(); m++) {

            final Move move = moves.get(m);

            board.makeMove(move);

            final int childScore = -searchTree(board, level + 1, maxLevel, -beta, -a);

            board.undoMove();

            if (childScore > maxScore) {
                maxScore = childScore;
                movePath.markMove(level, maxLevel, m);
            }

            if (maxScore > alpha) {
                //narrowing alpha beta window
                a = maxScore;
            }

            if (alpha >= beta) {
                //pruned!
                break;
            }

        }

        return maxScore;
    }

}
