package chessBackend;

public interface Player {
	
	public Move undoMove();
	
	public void newGame(Board board);
	public void setGame(PlayerContainer game);
	
	public void makeMove();
	public Move makeRecommendation();
	
	//blocks until move on players board is made
	public boolean moveMade(Move move);
	
	public void pause();
	
	public GameStatus getGameStatus();
	
	public String getVersion();
	
	public Board getBoard();

}
