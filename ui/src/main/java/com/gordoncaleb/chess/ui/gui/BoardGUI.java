package com.gordoncaleb.chess.ui.gui;

import com.gordoncaleb.chess.board.Move;

public interface BoardGUI {
    void makeMove(Move move);

    void gameOverLose();

    void gameOverWin();

    void gameOverDraw();

    String getPlayerName(int side);

    long getPlayerTime(int side);
}
