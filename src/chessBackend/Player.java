package chessBackend;

public interface Player {
	
	public Move undoMove();
	
	public void newGame(Board board);
	public void setGame(Game game);
	
	public void makeMove();
	public void makeRecommendation();
	
	//blocks until move on players board is made
	public boolean moveMade(Move move, boolean gameOver);
	
	public void pause();
	
	public GameStatus getGameStatus();
	
	public Board getBoard();

}
