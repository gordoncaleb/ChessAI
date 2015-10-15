package com.gordoncaleb.chess.backend;

public class GameResults {

	private Side winner;
	private int winBy;

	private long whiteTime;
	private long blackTime;

	private long maxWhiteTime;
	private long maxBlackTime;

	private long numOfMoves;
	
	private Game.GameStatus endGameStatus;

	public GameResults(Game.GameStatus endGameStatus, Side winner, int winBy, long whiteTime, long blackTime, int numOfMoves, long maxWhiteTime, long maxBlackTime) {
		this.winner = winner;
		this.winBy = winBy;

		this.numOfMoves = numOfMoves;

		this.whiteTime = whiteTime;
		this.blackTime = blackTime;

		this.maxWhiteTime = maxWhiteTime;
		this.maxBlackTime = maxBlackTime;
		
		this.endGameStatus = endGameStatus;
	}

	public Side getWinner() {
		return winner;
	}

	public void setWinner(Side winner) {
		this.winner = winner;
	}

	public int getWinBy() {
		return winBy;
	}

	public void setWinBy(int winBy) {
		this.winBy = winBy;
	}

	public long getTime(Side side) {
		if (side == Side.WHITE) {
			return whiteTime;
		} else {
			return blackTime;
		}
	}

	public long getNumOfMoves() {
		return numOfMoves;
	}

	public void setNumOfMoves(long numOfMoves) {
		this.numOfMoves = numOfMoves;
	}

	public long getMaxTime(Side side) {
		if (side == Side.WHITE) {
			return maxWhiteTime;
		} else {
			return maxBlackTime;
		}
	}

	public Game.GameStatus getEndGameStatus() {
		return endGameStatus;
	}

}
