package com.gordoncaleb.chess.board.serdes;

import java.util.Map;

public class BoardJSON {

    public static final CastleRights NO_CASTLE_RIGHTS = new CastleRights(false, false);
    public static final CastleRights CASTLE_RIGHTS = new CastleRights(true, true);

    public static class CastleRights {
        private boolean near;
        private boolean far;

        public CastleRights() {

        }

        public CastleRights(boolean near, boolean far) {
            this.near = near;
            this.far = far;
        }

        public boolean isNear() {
            return near;
        }

        public void setNear(boolean near) {
            this.near = near;
        }

        public boolean isFar() {
            return far;
        }

        public void setFar(boolean far) {
            this.far = far;
        }
    }

    private String turn;
    private Map<String, CastleRights> castle;
    private Map<String, String> setup;

    public String getTurn() {
        return turn;
    }

    public void setTurn(String turn) {
        this.turn = turn;
    }

    public Map<String, CastleRights> getCastle() {
        return castle;
    }

    public void setCastle(Map<String, CastleRights> castle) {
        this.castle = castle;
    }

    public Map<String, String> getSetup() {
        return setup;
    }

    public void setSetup(Map<String, String> setup) {
        this.setup = setup;
    }
}
