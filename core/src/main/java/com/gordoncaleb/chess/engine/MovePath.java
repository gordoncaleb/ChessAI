package com.gordoncaleb.chess.engine;


import com.gordoncaleb.chess.board.Move;
import com.gordoncaleb.chess.board.MoveContainer;

import java.util.ArrayList;
import java.util.List;

public class MovePath {

    private MoveContainer[] moveContainers;
    private int depth;

    public MovePath(MoveContainer[] moveContainers) {
        this.moveContainers = moveContainers;
        depth = 0;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
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
