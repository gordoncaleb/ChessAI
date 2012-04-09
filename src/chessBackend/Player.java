package chessBackend;

public interface Player {
	
	public boolean opponentMoved(Move opponentsMove);
	public boolean undoMove();
	public Side newGame(Side playerSide, Board board);
	public void setGame(Game game);
	public Move getRecommendation();
	
	public void pause();
	
	public Side getSide();
	public void setSide(Side side);
	
	public boolean isMyTurn();

}
