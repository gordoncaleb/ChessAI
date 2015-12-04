package com.gordoncaleb.chess.engine;


import com.gordoncaleb.chess.board.Move;
import com.gordoncaleb.chess.board.MoveContainer;

import java.util.ArrayList;
import java.util.List;

public class MovePath {

    private MoveContainer[] moveContainers;
    private int size;

    public MovePath(MoveContainer[] moveContainers) {
        this.moveContainers = moveContainers;
        size = 0;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public Move get(int i) {
        return moveContainers[i].getMarkedMove(i);
    }

    public List<Move> asList() {
        List<Move> moves = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            moves.add(get(i));
        }
        return moves;
    }

}
