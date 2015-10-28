package com.gordoncaleb.chess.backend;

import com.gordoncaleb.chess.board.Board;

public interface Player {

    long undoMove();

    void newGame(Board board);

    void endGame();

    void gameOver(int winlose);

    void setGame(PlayerContainer game);

    void showProgress(int progress);

    void requestRecommendation();

    void recommendationMade(long move);

    void makeMove();

    // blocks until move on players board is made
    boolean moveMade(long move);

    void pause();

    Game.GameStatus getGameStatus();

    String getVersion();

    Board getBoard();

}
