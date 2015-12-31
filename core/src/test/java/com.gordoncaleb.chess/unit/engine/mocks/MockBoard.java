package com.gordoncaleb.chess.unit.engine.mocks;

import com.gordoncaleb.chess.board.Board;
import com.gordoncaleb.chess.board.Move;
import com.gordoncaleb.chess.board.MoveContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MockBoard extends Board {

    private static final Logger LOGGER = LoggerFactory.getLogger(MockBoard.class);

    public static final Move LeftMove = new Move(0, 1, 2, 3, Move.MoveNote.NORMAL);
    public static final Move RightMove = new Move(2, 3, 1, 2, Move.MoveNote.NORMAL);

    private final List<Move> moveHistory = new ArrayList<>();
    private final List<String> visitHistory = new ArrayList<>();

    public MockBoard() {
        super(new ArrayList<>(), new ArrayList<>(),
                0, false, false, false, false);
    }

    @Override
    public Board copy() {
        return new MockBoard();
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

    @Override
    public boolean canUndo(){
        return !moveHistory.isEmpty();
    }

    @Override
    public void undo(final int num) {
        int i = num;
        while (canUndo() && i > 0) {
            undoMove();
            i--;
        }
    }

    @Override
    public int getMoveNumber(){
        return moveHistory.size();
    }

    @Override
    public long getHashCode() {
        return moveHistory.stream()
                .map(Move::hashCode)
                .reduce(0, (a, b) -> 63 * a + b);
    }

    @Override
    public boolean isDraw() {
        return false;
    }

    @Override
    public boolean isInCheck() {
        return false;
    }

    @Override
    public List<Move> getMoveHistory() {
        return moveHistory;
    }

    @Override
    public String toString(){
        return movesToString(moveHistory);
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

    public List<String> getVisitHistory() {
        return visitHistory;
    }

    public static String movesToString(List<Move> moves) {
        return moves.stream().map(m -> {
            if (m.equals(LeftMove)) {
                return "L";
            } else if (m.equals(RightMove)) {
                return "R";
            } else {
                return "?";
            }
        }).collect(Collectors.joining(", "));
    }
}
