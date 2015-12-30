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

    private Board board;
    private int searchDepth;
    private int iterativeDepth;

    public ABWithHashEngine(BoardScorer scorer, MoveContainer[] moveContainers) {
        this.scorer = scorer;
        this.moveContainers = moveContainers;
        this.movePath = new MovePath(moveContainers);
        this.hashTable = new EngineHashTable(20);
    }

    @Override
    public MovePath search(final Board board, final int depth) {
        this.board = board;
        this.searchDepth = depth;
        return search(START_ALPHA, START_BETA);
    }

    public MovePath search(final int startAlpha, final int startBeta) {

        for (iterativeDepth = 1; iterativeDepth <= searchDepth; iterativeDepth++) {

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

    public int searchTree(final int level, final int alpha, final int beta) {

        if (board.isDraw()) {
            return scorer.drawValue(level);
        }

        if (level == iterativeDepth) {
            return scorer.staticScore(board);
        }

        final MoveContainer moves = moveContainers[level];
        final BoardHashEntry hashEntry = hashTable.get(board.getHashCode());

        prioritizeHashEntry(hashEntry, moves);

        board.makeNullMove();
        board.generateValidMoves(moves);

        if (moves.isEmpty()) {
            return scorer.endOfGameValue(board.isInCheck(), level);
        }

        int a = alpha;
        for (int m = 0; m < moves.size(); m++) {

            final Move move = moves.get(m);

            board.makeMove(move);

            final int childScore = -searchTree(level + 1, -beta, -a);

            board.undoMove();

            if (childScore > a) {
                //narrowing alpha beta window
                a = childScore;

                movePath.markMove(level, iterativeDepth, m);

                if (childScore >= beta) {
                    //pruned!
                    break;
                }
            }
        }

        unprioritizeHashEntry(hashEntry, moves);

        hashTable.set(board.getHashCode(),
                level,
                a,
                movePath.getRaw(level),
                board.getMoveNumber(),
                nodeType(alpha, beta, a));

        return a;
    }

    private static int nodeType(final int a, final int b, final int score) {
        if (score <= a) {
            return BoardHashEntry.ValueBounds.ALL;
        } else if (score < b) {
            return BoardHashEntry.ValueBounds.PV;
        } else {
            return BoardHashEntry.ValueBounds.CUT;
        }
    }

    private void commitPVToHashTable(final MovePath movePath) {
        for (int i = 0; i < movePath.getDepth(); i++) {
            board.makeMove(movePath.get(i));
            hashTable.set(board.getHashCode(),
                    i,
                    0,
                    movePath.getRaw(i),
                    board.getMoveNumber(),
                    BoardHashEntry.ValueBounds.PV);
        }
        board.undo(movePath.getDepth());
    }

    private static void prioritizeHashEntry(final BoardHashEntry entry, final MoveContainer moveContainer) {
        if (entry != null) {
            moveContainer.prioritizeMove(entry.getBestMove());
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
