package com.gordoncaleb.chess.engine;


import com.gordoncaleb.chess.board.Move;
import com.gordoncaleb.chess.board.MoveContainer;

public class MovePath {

    private MoveContainer[] moveContainers;

    public MovePath(MoveContainer[] moveContainers) {
        this.moveContainers = moveContainers;
    }

    public Move get(int i) {
        return moveContainers[i].getMarkedMove();
    }

}
