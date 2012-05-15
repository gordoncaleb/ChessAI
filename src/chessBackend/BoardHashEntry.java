package chessBackend;

public class BoardHashEntry {
	public static final int hashIndexSize = 5;
	public static final long hashIndexMask = (long)(Math.pow(2, hashIndexSize) - 1);
	private int score;
	private int level;
	private int moveNum;
	private long hashCode;

	public BoardHashEntry(long hashCode, int level, int score, int moveNum) {
		this.score = score;
		this.level = level;
		this.moveNum = moveNum;
		this.hashCode = hashCode;
	}
	
	public void setAll(long hashCode, int level, int score, int moveNum){
		this.score = score;
		this.level = level;
		this.moveNum = moveNum;
		this.hashCode = hashCode;
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

	// public String getBoardString() {
	// return boardString;
	// }
	//
	// public void setBoardString(String boardString) {
	// this.boardString = boardString;
	// }

}
