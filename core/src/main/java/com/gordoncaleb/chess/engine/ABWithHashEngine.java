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

    /** Sentinel best-move stored for fail-low (ALL) nodes that have no best move. */
    private static final long NO_MOVE = 0L;
    /** Sentinel returned by the TT probe when no cutoff/score is available. */
    private static final int NO_CUTOFF = Integer.MIN_VALUE;

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
    public int getMaxSearchDepth() {
        return moveContainers.length;
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
        // Capture the hash move up front: the entry aliases a mutable table slot
        // that a child search could overwrite before we unprioritize it.
        final long hashMove = (hashEntry != null) ? hashEntry.getBestMove() : NO_MOVE;

        final int levelsToBottom = iterativeDepth - levelsToTop;

        // Use the transposition table for a cutoff, but never at the root, where
        // we must actually search to fill in the principal variation move.
        if (levelsToTop > 0) {
            final int ttScore = probeHashScore(hashEntry, alpha, beta, levelsToBottom, levelsToTop);
            if (ttScore != NO_CUTOFF) {
                return ttScore;
            }
        }

        prioritizeMove(moves, hashMove);

        board.makeNullMove();
        board.generateValidMoves(moves);

        if (moves.isEmpty()) {
            unprioritizeMove(moves, hashMove);
            return scorer.endOfGameValue(board.isInCheck(), levelsToTop);
        }

        moves.sort();

        int maxScore = alpha;
        int bestIndex = -1;
        for (int m = 0; m < moves.size(); m++) {

            final Move move = moves.get(m);

            board.makeMove(move);

            final int childScore = -searchTree(levelsToTop + 1, -beta, -maxScore);

            board.undoMove();

            if (childScore > maxScore) {
                //narrowing alpha beta window
                maxScore = childScore;
                bestIndex = m;

                movePath.markMove(levelsToTop, iterativeDepth, m);

                if (maxScore >= beta) {
                    //pruned!
                    break;
                }
            }
        }

        unprioritizeMove(moves, hashMove);

        // Fail-low (ALL) nodes have no real best move; store a sentinel so we
        // don't pollute move ordering with a leftover move from a sibling.
        final long bestMove = (bestIndex >= 0) ? movePath.getRaw(levelsToTop) : NO_MOVE;

        hashTable.set(board.getHashCode(),
                adjustScoreForStorage(maxScore, levelsToTop),
                levelsToBottom,
                bestMove,
                board.getMoveNumber(),
                EngineUtil.nodeType(alpha, beta, maxScore));

        return maxScore;
    }

    /**
     * Returns a usable score from the transposition table, or {@link #NO_CUTOFF}.
     * Honors exact (PV), lower-bound (CUT) and upper-bound (ALL) entries, and
     * re-adjusts mate scores from table-relative back to node-relative ply.
     */
    private static int probeHashScore(final BoardHashEntry entry,
                                      final int alpha,
                                      final int beta,
                                      final int levelsToBottom,
                                      final int levelsToTop) {
        if (entry == null || entry.getLevel() < levelsToBottom) {
            return NO_CUTOFF;
        }

        final int score = adjustScoreForRetrieval(entry.getScore(), levelsToTop);
        final int bounds = entry.getBounds();

        // Only take a cutoff when the stored bound proves the score lies OUTSIDE
        // the (alpha, beta) window. A within-window value belongs to a PV node,
        // which must actually be searched so its move is marked and the principal
        // variation stays reconstructable (verifyPV replays it). Short-circuiting
        // a PV node here leaves a stale move in the PV and corrupts the board.
        if (score >= beta
                && (bounds == BoardHashEntry.ValueBounds.PV || bounds == BoardHashEntry.ValueBounds.CUT)) {
            return score;
        }
        if (score <= alpha
                && (bounds == BoardHashEntry.ValueBounds.PV || bounds == BoardHashEntry.ValueBounds.ALL)) {
            return score;
        }

        return NO_CUTOFF;
    }

    /**
     * Mate scores encode distance from the root. Before storing in the TT they
     * must be made relative to the current node (add ply), and converted back
     * on retrieval (subtract ply), otherwise a mate found at one ply is reported
     * with the wrong distance when the position transposes to a different ply.
     */
    private static int adjustScoreForStorage(final int score, final int ply) {
        if (score >= Values.CHECKMATE_MASK) {
            return score + ply;
        }
        if (score <= -Values.CHECKMATE_MASK) {
            return score - ply;
        }
        return score;
    }

    private static int adjustScoreForRetrieval(final int score, final int ply) {
        if (score >= Values.CHECKMATE_MASK) {
            return score - ply;
        }
        if (score <= -Values.CHECKMATE_MASK) {
            return score + ply;
        }
        return score;
    }

    private static void prioritizeMove(final MoveContainer moveContainer, final long move) {
        if (move != NO_MOVE) {
            moveContainer.prioritizeMove(move, 1);
        }
    }

    private static void unprioritizeMove(final MoveContainer moveContainer, final long move) {
        if (move != NO_MOVE) {
            moveContainer.unprioritizeMove(move);
        }
    }

}
