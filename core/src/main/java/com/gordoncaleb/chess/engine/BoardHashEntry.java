package com.gordoncaleb.chess.engine;

import com.gordoncaleb.chess.board.Move;

public class BoardHashEntry {

    private int score;
    private int level;
    private int moveNum;
    private long hashCode;
    private Move bestMove;
    private int bounds;

    public static class ValueBounds {
        public static final int PV = 1;
        public static final int CUT = 2;
        public static final int ALL = 3;
        public static final int NA = 0;
    }

    public BoardHashEntry(){

    }

    public BoardHashEntry(long hashCode, int level, int score, int moveNum, int bounds, Move bestMove) {//,String stringBoard) {
        this.score = score;
        this.level = level;
        this.moveNum = moveNum;
        this.hashCode = hashCode;
        this.bounds = bounds;
        this.bestMove = bestMove;
        //this.stringBoard = stringBoard;
    }

    public void setAll(long hashCode, int level, int score, int moveNum, int bounds, Move bestMove) {//, String stringBoard) {
        this.score = score;
        this.level = level;
        this.moveNum = moveNum;
        this.hashCode = hashCode;
        this.bounds = bounds;
        this.bestMove = bestMove;
        //this.stringBoard = stringBoard;
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

    public int getMoveNum() {
        return moveNum;
    }

    public void setMoveNum(int moveNum) {
        this.moveNum = moveNum;
    }

    public long getHashCode() {
        return hashCode;
    }

    public void setHashCode(long hashCode) {
        this.hashCode = hashCode;
    }

    public Move getBestMove() {
        return bestMove;
    }

    public void setBestMove(Move bestMove) {
        this.bestMove = bestMove;
    }

    public int getBounds() {
        return bounds;
    }

    public void setBounds(int bounds) {
        this.bounds = bounds;
    }

}
