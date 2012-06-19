package chessBackend;

public interface Player {

	public long undoMove();

	public void newGame(Board board);

	public void endGame();

	public void gameOver(int winlose);

	public void setGame(PlayerContainer game);

	public void showProgress(int progress);

	public void requestRecommendation();

	public void recommendationMade(long move);

	public void makeMove();

	// blocks until move on players board is made
	public boolean moveMade(long move);

	public void pause();

	public GameStatus getGameStatus();

	public String getVersion();

	public Board getBoard();

}
