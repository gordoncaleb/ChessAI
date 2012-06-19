package chessBackend;

public interface PlayerContainer {

	public boolean makeMove(long move);

	public GameResults newGame(Board board, boolean block);

	public void endGame();

	public boolean undoMove();

	public void showProgress(int progress);

	public void requestRecommendation();

	public void recommendationMade(long move);

	public void pause();

	public boolean isPaused();

	public String getPlayerName(Side side);

	public long getPlayerTime(Side side);

	public void switchSides();

	public void setSide(Side side, Player player);

}
