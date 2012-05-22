package chessBackend;

public interface Player {
	
	public long undoMove();
	
	public void newGame(Board board);
	public void setGame(PlayerContainer game);
	
	public void makeMove();
	public long makeRecommendation();
	
	//blocks until move on players board is made
	public boolean moveMade(long move);
	
	public void pause();
	
	public GameStatus getGameStatus();
	
	public String getVersion();
	
	public Board getBoard();

}
