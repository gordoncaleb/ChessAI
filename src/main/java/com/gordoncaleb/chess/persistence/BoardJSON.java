package com.gordoncaleb.chess.persistence;

import com.gordoncaleb.chess.backend.Side;

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

    private Side turn;
    private Map<Side, CastleRights> castle;
    private Map<String, String> setup;

    public Side getTurn() {
        return turn;
    }

    public void setTurn(Side turn) {
        this.turn = turn;
    }

    public Map<Side, CastleRights> getCastle() {
        return castle;
    }

    public void setCastle(Map<Side, CastleRights> castle) {
        this.castle = castle;
    }

    public Map<String, String> getSetup() {
        return setup;
    }

    public void setSetup(Map<String, String> setup) {
        this.setup = setup;
    }
}
