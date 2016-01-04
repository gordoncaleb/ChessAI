package com.gordoncaleb.chess.engine;


import com.gordoncaleb.chess.board.Board;
import com.gordoncaleb.chess.board.Move;
import com.gordoncaleb.chess.board.MoveContainer;
import com.gordoncaleb.chess.engine.score.BoardScorer;
import com.gordoncaleb.chess.engine.score.Values;


public class ABWithHashMoveEngine implements Engine {

    private static final int START_ALPHA = -Values.CHECKMATE_MOVE + 1;
    private static final int START_BETA = -START_ALPHA;

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
    public MovePath search(final Board board, final int maxLevel) {
        return search(board, maxLevel, START_ALPHA, START_BETA);
    }

    @Override
    public MovePath search(final Board board, final int maxLevel, final int startAlpha, final int startBeta) {
        this.board = board;

        for (iterativeDepth = 1; iterativeDepth <= maxLevel; iterativeDepth++) {

            final int score = searchTree(0, startAlpha, startBeta);
            movePath.setScore(score);

            final int checkMateIn = checkMateIn(score);
            if (checkMateIn < iterativeDepth) {
                movePath.setDepth(checkMateIn);
                return movePath;
            }

            movePath.setDepth(iterativeDepth);
            commitPVToHashTable(movePath);
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

        prioritizeHashEntry(hashEntry, moves);

        board.makeNullMove();
        board.generateValidMoves(moves);

        if (moves.isEmpty()) {
            return scorer.endOfGameValue(board.isInCheck(), levelsToTop);
        }

        for (int m = 0; m < moves.size(); m++) {

            final Move move = moves.get(m);

            board.makeMove(move);

            final int childScore = -searchTree(levelsToTop + 1, -beta, -alpha);

            board.undoMove();

            if (childScore > alpha) {
                //narrowing alpha beta window
                alpha = childScore;

                movePath.markMove(levelsToTop, iterativeDepth, m);

                if (alpha >= beta) {
                    //pruned!
                    break;
                }
            }
        }

        unprioritizeHashEntry(hashEntry, moves);

        hashTable.set(board.getHashCode(),
                alpha,
                0,
                movePath.getRaw(levelsToTop),
                board.getMoveNumber(),
                BoardHashEntry.ValueBounds.PV);

        return alpha;
    }

    private void commitPVToHashTable(final MovePath movePath) {
        for (int i = 0; i < movePath.getDepth(); i++) {
            board.makeMove(movePath.get(i));
            hashTable.set(board.getHashCode(),
                    0,
                    0,
                    movePath.getRaw(i),
                    board.getMoveNumber(),
                    BoardHashEntry.ValueBounds.PV);
        }
        board.undo(movePath.getDepth());
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

    private static int checkMateIn(final int score) {
        return Values.CHECKMATE_MOVE - Math.abs(score);
    }

}

