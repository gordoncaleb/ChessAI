package com.gordoncaleb.chess.engine;

import com.gordoncaleb.chess.board.Board;
import com.gordoncaleb.chess.board.Move;
import com.gordoncaleb.chess.board.MoveContainer;
import com.gordoncaleb.chess.engine.score.BoardScorer;
import com.gordoncaleb.chess.engine.score.Values;

public class AlphaBetaEngine implements Engine {

    private static final int START_ALPHA = -Values.CHECKMATE_MOVE + 1;
    private static final int START_BETA = -START_ALPHA;

    private final BoardScorer scorer;
    private final MoveContainer[] moveContainers;
    private final MovePath movePath;

    private Board board;
    private int maxLevel;

    public AlphaBetaEngine(BoardScorer scorer, MoveContainer[] moveContainers) {
        this.scorer = scorer;
        this.moveContainers = moveContainers;
        this.movePath = new MovePath(moveContainers);
    }

    @Override
    public MovePath search(Board board, int depth) {
        return search(board, depth, START_ALPHA, START_BETA);
    }

    @Override
    public MovePath search(Board board, int depth, int startAlpha, int startBeta) {
        this.board = board;
        this.maxLevel = depth;
        final int score = searchTree(0, startAlpha, startBeta);

        movePath.setScore(score);
        movePath.setDepth(depth);

        EngineUtil.verifyPV(movePath, board);

        return movePath;
    }

    @Override
    public MovePath iterativeSearch(final Board board, final int maxLevel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public MovePath iterativeSearch(final Board board, final int maxLevel, final int startAlpha, final int startBeta) {
        throw new UnsupportedOperationException();
    }

    public int searchTree(final int level, int alpha, final int beta) {

        if (board.isDraw()) {
            return scorer.drawValue();
        }

        if (level == maxLevel) {
            return scorer.staticScore(board);
        }

        board.makeNullMove();
        final MoveContainer moves = board.generateValidMoves(moveContainers[level]);

        if (moves.isEmpty()) {
            return scorer.endOfGameValue(board.isInCheck(), level);
        }

        for (int m = 0; m < moves.size(); m++) {

            final Move move = moves.get(m);

            board.makeMove(move);

            final int childScore = -searchTree(level + 1, -beta, -alpha);

            board.undoMove();

            if (childScore > alpha) {
                movePath.markMove(level, maxLevel, m);

                if (childScore >= beta) {
                    //pruned!
                    return childScore;
                }

                //narrowing alpha beta window
                alpha = childScore;
            }

        }

        return alpha;
    }

}
