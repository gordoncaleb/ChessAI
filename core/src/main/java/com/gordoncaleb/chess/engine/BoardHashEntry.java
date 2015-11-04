package com.gordoncaleb.chess.engine;

import com.gordoncaleb.chess.board.Move;

public class BoardHashEntry {
	
	private int score;
	private int level;
	private int moveNum;
	private long hashCode;
	private Move bestMove;
	private ValueBounds bounds;

	public enum ValueBounds {
		PV, CUT, ALL, NA
	}

	public BoardHashEntry(long hashCode, int level, int score, int moveNum, ValueBounds bounds, Move bestMove){//,String stringBoard) {
		this.score = score;
		this.level = level;
		this.moveNum = moveNum;
		this.hashCode = hashCode;
		this.bounds = bounds;
		this.bestMove = bestMove;
		//this.stringBoard = stringBoard;
	}

	public void setAll(long hashCode, int level, int score, int moveNum, ValueBounds bounds, Move bestMove){//, String stringBoard) {
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

	public ValueBounds getBounds() {
		return bounds;
	}

	public void setBounds(ValueBounds bounds) {
		this.bounds = bounds;
	}

}
