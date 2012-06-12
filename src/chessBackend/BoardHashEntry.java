package chessBackend;

import chessAI.AISettings;

public class BoardHashEntry {
	public static final long hashIndexMask = (long) (Math.pow(2, AISettings.hashIndexSize) - 1);
	private int score;
	private int level;
	private int moveNum;
	private long hashCode;
	private long bestMove;
	private ValueBounds bounds;
	//private String stringBoard;

	public BoardHashEntry(long hashCode, int level, int score, int moveNum, ValueBounds bounds, long bestMove){//,String stringBoard) {
		this.score = score;
		this.level = level;
		this.moveNum = moveNum;
		this.hashCode = hashCode;
		this.bounds = bounds;
		this.bestMove = bestMove;
		//this.stringBoard = stringBoard;
	}

	public void setAll(long hashCode, int level, int score, int moveNum, ValueBounds bounds, long bestMove){//, String stringBoard) {
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

	public long getBestMove() {
		return bestMove;
	}

	public void setBestMove(long bestMove) {
		this.bestMove = bestMove;
	}

	public ValueBounds getBounds() {
		return bounds;
	}

	public void setBounds(ValueBounds bounds) {
		this.bounds = bounds;
	}

//	public String getStringBoard() {
//		return stringBoard;
//	}
//
//	public void setStringBoard(String stringBoard) {
//		this.stringBoard = stringBoard;
//	}

}
