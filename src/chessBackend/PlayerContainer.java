package chessBackend;

public interface PlayerContainer {
	
	public boolean makeMove(Move move);
	
	public GameResults newGame(Board board, boolean block);
	
	public boolean undoMove();
	
	public void pause();
	
	public boolean isPaused();

}
