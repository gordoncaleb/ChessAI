package chessBackend;

public class GameResults {
	
	private Side winner;
	private int winBy;
	
	private long winnerTime;
	private long loserTime;

	public GameResults(Side winner, int winBy, long winnerTime, long loserTime) {
		this.winner = winner;
		this.winBy = winBy;
		
		this.winnerTime = winnerTime;
		this.loserTime = loserTime;
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

	public long getWinnerTime() {
		return winnerTime;
	}

	public void setWinnerTime(long winnerTime) {
		this.winnerTime = winnerTime;
	}

	public long getLoserTime() {
		return loserTime;
	}

	public void setLoserTime(long loserTime) {
		this.loserTime = loserTime;
	}
	
	
	

}
