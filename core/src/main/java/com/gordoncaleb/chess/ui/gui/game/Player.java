package com.gordoncaleb.chess.ui.gui.game;

import com.gordoncaleb.chess.board.Board;
import com.gordoncaleb.chess.board.Move;

public interface Player {

    Move undoMove();

    void newGame(Board board);

    void endGame();

    void gameOver(int winlose);

    void setGame(PlayerContainer game);

    void showProgress(int progress);

    void requestRecommendation();

    void recommendationMade(Move move);

    void makeMove();

    // blocks until move on players board is made
    boolean moveMade(Move move);

    void pause();

    Game.GameStatus getGameStatus();

    String getVersion();

    Board getBoard();

}
