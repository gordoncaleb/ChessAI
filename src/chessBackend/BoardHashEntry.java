package chessBackend;

public class BoardHashEntry {
	private int score;
	private int level;
	//private String boardString;
	
	public BoardHashEntry(int score, int level){//, String boardString){
		this.score = score;
		this.level = level;
		//this.boardString = boardString;
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

//	public String getBoardString() {
//		return boardString;
//	}
//
//	public void setBoardString(String boardString) {
//		this.boardString = boardString;
//	}
	
	

}
