package chessGUI;

import chessBackend.Move;
import chessBackend.Side;

public interface BoardGUI {
	public void makeMove(Move move);

	public void gameOverLose();

	public void gameOverWin();

	public void gameOverStaleMate();

	public String getPlayerName(Side side);

	public long getPlayerTime(Side side);
}
