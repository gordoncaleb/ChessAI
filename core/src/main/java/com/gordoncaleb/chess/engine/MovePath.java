package com.gordoncaleb.chess.engine;


import com.gordoncaleb.chess.board.BoardCondition;
import com.gordoncaleb.chess.board.Move;
import com.gordoncaleb.chess.board.MoveContainer;

import java.util.ArrayList;
import java.util.List;

public class MovePath {

    private MoveContainer[] moveContainers;
    private int depth;
    private int score;
    private int endBoardCondition;

    public MovePath(MoveContainer[] moveContainers) {
        this.moveContainers = moveContainers;
        this.depth = 0;
        this.endBoardCondition = BoardCondition.IN_PLAY;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getEndBoardCondition() {
        return endBoardCondition;
    }

    public void setEndBoardCondition(int endBoardCondition) {
        this.endBoardCondition = endBoardCondition;
    }

    public Move get(int i) {
        return moveContainers[i].getMarkedMove(i);
    }

    public long getRaw(int i) {
        return moveContainers[i].getMarkedMoveRaw(i);
    }

    public void markMove(final int level, final int maxDepth, final int index) {
        moveContainers[level].markMove(index);
        promoteBelow(level + 1, maxDepth);
    }

    private void promoteBelow(final int start, final int end) {
        for (int i = start; i < end; i++) {
            moveContainers[i].promoteMarkedMove(i - start);
        }
    }

    public List<Move> asList() {
        List<Move> moves = new ArrayList<>();
        for (int i = 0; i < depth; i++) {
            moves.add(get(i));
        }
        return moves;
    }
}
