package com.gordoncaleb.chess.persistence;

import java.util.List;

public class GameJSON {

    public static class MoveJSON{
        private int fromRow;
    }

    private BoardJSON currentState;
    private List<MoveJSON> moves;
}
