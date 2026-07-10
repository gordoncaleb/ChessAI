package com.gordoncaleb.chess.engine;


import com.gordoncaleb.chess.board.Board;
import com.gordoncaleb.chess.board.BoardCondition;
import com.gordoncaleb.chess.board.Move;
import com.gordoncaleb.chess.board.MoveContainer;
import com.gordoncaleb.chess.engine.score.BoardScorer;
import com.gordoncaleb.chess.engine.score.Values;


public class ABWithHashMoveEngine implements Engine {

    private static final int START_ALPHA = -Values.CHECKMATE_MOVE + 1;
    private static final int START_BETA = -START_ALPHA;

    private static final long NO_MOVE = 0L;

    private final BoardScorer scorer;
    private final MoveContainer[] moveContainers;
    private final MovePath movePath;
    private final EngineHashTable hashTable;

    private Board board;
    private int iterativeDepth;

    public ABWithHashMoveEngine(BoardScorer scorer, MoveContainer[] moveContainers) {
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
        this.board = board;

        for (int i = 1; i <= maxLevel; i++) {
            search(board, i);
            if (movePath.getEndBoardCondition() == BoardCondition.CHECKMATE) {
                break;
            }
        }
        return movePath;
    }

    public int searchTree(final int levelsToTop, int alpha, final int beta) {

        if (board.isDraw()) {
            return scorer.drawValue();
        }

        if (levelsToTop == iterativeDepth) {
            return scorer.staticScore(board);
        }

        final MoveContainer moves = moveContainers[levelsToTop];
        final BoardHashEntry hashEntry = hashTable.get(board.getHashCode());
        // Capture the hash move up front; the entry aliases a mutable table slot.
        final long hashMove = (hashEntry != null) ? hashEntry.getBestMove() : NO_MOVE;

        prioritizeMove(moves, hashMove);

        board.makeNullMove();
        board.generateValidMoves(moves);

        if (moves.isEmpty()) {
            unprioritizeMove(moves, hashMove);
            return scorer.endOfGameValue(board.isInCheck(), levelsToTop);
        }

        int bestIndex = -1;
        for (int m = 0; m < moves.size(); m++) {

            final Move move = moves.get(m);

            board.makeMove(move);

            final int childScore = -searchTree(levelsToTop + 1, -beta, -alpha);

            board.undoMove();

            if (childScore > alpha) {
                //narrowing alpha beta window
                alpha = childScore;
                bestIndex = m;

                movePath.markMove(levelsToTop, iterativeDepth, m);

                if (alpha >= beta) {
                    //pruned!
                    break;
                }
            }
        }

        unprioritizeMove(moves, hashMove);

        final long bestMove = (bestIndex >= 0) ? movePath.getRaw(levelsToTop) : NO_MOVE;

        hashTable.set(board.getHashCode(),
                alpha,
                0,
                bestMove,
                board.getMoveNumber(),
                BoardHashEntry.ValueBounds.PV);

        return alpha;
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

