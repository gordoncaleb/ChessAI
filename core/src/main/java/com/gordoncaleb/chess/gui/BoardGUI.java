package com.gordoncaleb.chess.gui;

import com.gordoncaleb.chess.backend.Side;

public interface BoardGUI {
	public void makeMove(long move);

	public void gameOverLose();

	public void gameOverWin();

	public void gameOverDraw();

	public String getPlayerName(int side);

	public long getPlayerTime(int side);
}
