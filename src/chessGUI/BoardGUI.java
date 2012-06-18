package chessGUI;

import chessBackend.Side;

public interface BoardGUI {
	public void makeMove(long move);

	public void gameOverLose();

	public void gameOverWin();

	public void gameOverDraw();

	public String getPlayerName(Side side);

	public long getPlayerTime(Side side);
}
