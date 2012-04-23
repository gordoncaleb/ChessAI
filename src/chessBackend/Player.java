package chessBackend;

public interface Player {
	
	public boolean moveMade(Move move);
	public Move undoMove();
	public Side newGame(Side playerSide, Board board);
	public void setGame(Game game);
	public void endGame();
	
	public Move getRecommendation();
	
	public void pause();
	
	public Side getSide();
	public void setSide(Side side);
	
	public GameStatus getGameStatus();
	
	public Board getBoard();
	
	public boolean isMyTurn();

}
