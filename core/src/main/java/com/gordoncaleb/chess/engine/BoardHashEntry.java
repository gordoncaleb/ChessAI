package com.gordoncaleb.chess.engine;

public class BoardHashEntry {

    private int score;
    private int level;
    private long hashCode;
    private long bestMove;
    private int moveNum;
    private int bounds;

    public static class ValueBounds {
        public static final int PV = 1; //exact
        public static final int CUT = 2; //
        public static final int ALL = 3;
        public static final int NA = 0;
    }

    public BoardHashEntry() {
        bounds = ValueBounds.NA;
    }

    public BoardHashEntry(long hashCode, int score, int level, long bestMove, int moveNum, int bounds) {
        this.score = score;
        this.level = level;
        this.hashCode = hashCode;
        this.bestMove = bestMove;
        this.moveNum = moveNum;
        this.bounds = bounds;
    }

    public void setAll(long hashCode, int score, int level, long bestMove, int moveNum, int bounds) {
        this.score = score;
        this.level = level;
        this.hashCode = hashCode;
        this.bounds = bounds;
        this.bestMove = bestMove;
        this.moveNum = moveNum;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public long getHashCode() {
        return hashCode;
    }

    public void setHashCode(long hashCode) {
        this.hashCode = hashCode;
    }

    public long getBestMove() {
        return bestMove;
    }

    public void setBestMove(long bestMove) {
        this.bestMove = bestMove;
    }

    public int getMoveNum() {
        return moveNum;
    }

    public void setMoveNum(int moveNum) {
        this.moveNum = moveNum;
    }

    public int getBounds() {
        return bounds;
    }

    public void setBounds(int bounds) {
        this.bounds = bounds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BoardHashEntry hashEntry = (BoardHashEntry) o;

        if (score != hashEntry.score) return false;
        if (level != hashEntry.level) return false;
        if (hashCode != hashEntry.hashCode) return false;
        if (bestMove != hashEntry.bestMove) return false;
        if (moveNum != hashEntry.moveNum) return false;
        return bounds == hashEntry.bounds;

    }

    @Override
    public int hashCode() {
        int result = score;
        result = 31 * result + level;
        result = 31 * result + (int) (hashCode ^ (hashCode >>> 32));
        result = 31 * result + (int) (bestMove ^ (bestMove >>> 32));
        result = 31 * result + moveNum;
        result = 31 * result + bounds;
        return result;
    }
}
