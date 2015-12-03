package com.gordoncaleb.chess.engine;


import com.gordoncaleb.chess.board.Move;
import com.gordoncaleb.chess.board.MoveContainer;

import java.util.ArrayList;
import java.util.List;

public class MovePath {

    private MoveContainer[] moveContainers;

    public MovePath(MoveContainer[] moveContainers) {
        this.moveContainers = moveContainers;
    }

    public Move get(int i) {
        return moveContainers[i].getMarkedMove();
    }

    public List<Move> asList() {
        List<Move> moves = new ArrayList<>();
        for (MoveContainer mc : moveContainers) {
            if (mc.hasMarkedMove()) {
                moves.add(mc.getMarkedMove());
            } else {
                break;
            }
        }
        return moves;
    }

}
