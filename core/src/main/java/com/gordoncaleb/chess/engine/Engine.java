package com.gordoncaleb.chess.engine;

import com.gordoncaleb.chess.board.*;
import com.gordoncaleb.chess.engine.score.Values;
import com.gordoncaleb.chess.engine.score.StaticScorer;

import static com.gordoncaleb.chess.board.BoardCondition.*;

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
        searchTree(board, 0, depth, startAlpha, startBeta);
        movePath.setDepth(depth);
        return movePath;
    }

    public int searchTree(final Board board, final int level, final int maxDepth, int alpha, final int beta) {

        if (level == maxDepth) {
            return scorer.staticScore(board);
        }

        int condition = isCheckDrawOrInPlay(board);

        if (condition == DRAW) {
            return Values.DRAW;
        }

        board.makeNullMove();
        MoveContainer moves = board.generateValidMoves(moveContainers[level]);

        if (moves.isEmpty()) {
            if (condition == CHECK) {
                //checkmate
                return -(Values.CHECKMATE_MOVE - (maxDepth - level));
            } else {
                //stalemate
                return Values.STALEMATE;
            }
        }

        int maxScore = Integer.MIN_VALUE;

        //try and order moves to maximize pruning
        moves.sort();

        for (int m = 0; m < moves.size(); m++) {

            final Move move = moves.get(m);

            board.makeMove(move);

            final int childScore = -searchTree(board, level + 1, maxDepth, -beta, -alpha);

            board.undoMove();

            if (childScore > maxScore) {
                maxScore = childScore;
                movePath.markMove(level, maxDepth, m);
            }

            if (maxScore > alpha) {
                //narrowing ab window
                alpha = maxScore;
            }

            if (alpha >= beta) {
                //pruned!
                break;
            }

        }

        return maxScore;
    }

    private static int isCheckDrawOrInPlay(Board board) {
        if (board.isInCheck()) {
            return CHECK;
        } else {
            return board.isDraw() ? DRAW : IN_PLAY;
        }
    }

}
