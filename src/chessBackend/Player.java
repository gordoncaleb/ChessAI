package chessBackend;

public interface Player {
	
	public boolean opponentMoved(Move opponentsMove);
	public boolean undoMove();
	public Side newGame(Side playerSide, Board board);
	public Move getRecommendation();
	
	public Side getSide();
	public void setSide(Side side);

}
