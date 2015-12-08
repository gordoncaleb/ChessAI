package com.gordoncaleb.chess.unit.engine;

import com.gordoncaleb.chess.board.Board;
import com.gordoncaleb.chess.board.Move;
import com.gordoncaleb.chess.board.MoveContainer;

import java.util.ArrayList;
import java.util.List;

public class MockBoard extends Board {
    public static final Move LeftMove = new Move(0, 1, 2, 3, Move.MoveNote.NORMAL);
    public static final Move RightMove = new Move(2, 3, 1, 2, Move.MoveNote.NORMAL);

    private List<Move> moveHistory = new ArrayList<>();
    private List<String> visitHistory = new ArrayList<>();

    public MockBoard() {
        super(new ArrayList<>(), new ArrayList<>(),
                0, false, false, false, false);
    }

    @Override
    public long[] makeNullMove() {
        return new long[0];
    }

    @Override
    public MoveContainer generateValidMoves(MoveContainer mc) {
        mc.clear();
        mc.add(LeftMove);
        mc.add(RightMove);
        return mc;
    }

    @Override
    public boolean makeMove(Move move) {
        moveHistory.add(move);
        visitHistory.add(moveHistory.size() + "-" + updateVisitHistory(moveHistory.size()));
        return false;
    }

    @Override
    public Move undoMove() {
        moveHistory.remove(moveHistory.size() - 1);
        return null;
    }

    private long updateVisitHistory(int depth) {
        double min = 0;
        double max = Math.pow(2, depth);

        for (int i = 0; i < moveHistory.size(); i++) {
            double mid = (max + min) / 2;
            if (moveHistory.get(i).equals(RightMove)) {
                min = mid;
            } else {
                max = mid;
            }
        }
        return (long) max;
    }

    public List<String> getVisitHistory(){
        return visitHistory;
    }
}
