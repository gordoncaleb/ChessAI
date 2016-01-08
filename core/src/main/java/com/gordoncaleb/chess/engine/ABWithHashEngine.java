package com.gordoncaleb.chess.engine;

import com.gordoncaleb.chess.board.Board;
import com.gordoncaleb.chess.board.BoardCondition;
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

    private Board board;
    private int iterativeDepth;

    public ABWithHashEngine(BoardScorer scorer, MoveContainer[] moveContainers) {
        this.scorer = scorer;
        this.moveContainers = moveContainers;
        this.movePath = new MovePath(moveContainers);
        this.hashTable = new EngineHashTable(20);
    }

    @Override
    public MovePath search(final Board board, final int depth, final int startAlpha, final int startBeta) {
        this.iterativeDepth = depth;
        this.board = board;

        final int score = searchTree(0, startAlpha, startBeta);
        movePath.setScore(score);
        movePath.setDepth(iterativeDepth);

        EngineUtil.verifyPV(movePath, board, hashTable);

        return movePath;
    }

    @Override
    public MovePath search(final Board board, final int depth) {
        return search(board, depth, START_ALPHA, START_BETA);
    }

    @Override
    public MovePath iterativeSearch(final Board board, final int maxLevel) {
        return iterativeSearch(board, maxLevel, START_ALPHA, START_BETA);
    }

    @Override
    public MovePath iterativeSearch(final Board board, final int maxLevel, final int startAlpha, final int startBeta) {

        for (int i = 1; i <= maxLevel; i++) {
            search(board, i);
            if (movePath.getEndBoardCondition() == BoardCondition.CHECKMATE) {
                break;
            }
        }

        return movePath;
    }

    public int searchTree(final int levelsToTop, final int alpha, final int beta) {

        if (board.isDraw()) {
            return scorer.drawValue();
        }

        if (levelsToTop == iterativeDepth) {
            return scorer.staticScore(board);
        }

        final MoveContainer moves = moveContainers[levelsToTop];
        final BoardHashEntry hashEntry = hashTable.get(board.getHashCode());

        final int levelsToBottom = iterativeDepth - levelsToTop;

        final Integer hashScoreCutoff = checkHashScoreForBetaCutoff(hashEntry, beta, levelsToBottom);
        if (hashScoreCutoff != null) {
            return hashScoreCutoff;
        }

        prioritizeHashEntry(hashEntry, moves);

        board.makeNullMove();
        board.generateValidMoves(moves);

        if (moves.isEmpty()) {
            return scorer.endOfGameValue(board.isInCheck(), levelsToTop);
        }

        moves.sort();

        int maxScore = alpha;
        for (int m = 0; m < moves.size(); m++) {

            final Move move = moves.get(m);

            board.makeMove(move);

            final int childScore = -searchTree(levelsToTop + 1, -beta, -maxScore);

            board.undoMove();

            if (childScore > maxScore) {
                //narrowing alpha beta window
                maxScore = childScore;

                movePath.markMove(levelsToTop, iterativeDepth, m);

                if (maxScore >= beta) {
                    //pruned!
                    break;
                }
            }
        }

        unprioritizeHashEntry(hashEntry, moves);

        hashTable.set(board.getHashCode(),
                maxScore,
                levelsToBottom,
                movePath.getRaw(levelsToTop),
                board.getMoveNumber(),
                EngineUtil.nodeType(alpha, beta, maxScore));

        return maxScore;
    }

    private static Integer checkHashScoreForBetaCutoff(final BoardHashEntry entry, final int beta, final int levelsToBottom) {
        if (entry != null &&
                (entry.getBounds() == BoardHashEntry.ValueBounds.CUT
                        || entry.getBounds() == BoardHashEntry.ValueBounds.PV) &&
                entry.getLevel() >= levelsToBottom &&
                entry.getScore() >= beta) {
            return entry.getScore();
        }
        return null;
    }

    private static void prioritizeHashEntry(final BoardHashEntry entry, final MoveContainer moveContainer) {
        if (entry != null) {
            moveContainer.prioritizeMove(entry.getBestMove(), 1);
        }
    }

    private static void unprioritizeHashEntry(final BoardHashEntry entry, final MoveContainer moveContainer) {
        if (entry != null) {
            moveContainer.unprioritizeMove(entry.getBestMove());
        }
    }

}
