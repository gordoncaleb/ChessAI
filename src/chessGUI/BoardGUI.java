package chessGUI;

import chessBackend.Move;

public interface BoardGUI {
	public void makeMove(Move move);
	public void gameOverLose();
	public void gameOverWin();
	public void gameOverStaleMate();
}
