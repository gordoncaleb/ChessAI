package com.gordoncaleb.chess.backend;

import com.gordoncaleb.chess.board.Board;

public interface PlayerContainer {

    boolean makeMove(long move);

    GameResults newGame(Board board, boolean block);

    void endGame();

    boolean undoMove();

    void showProgress(int progress);

    void requestRecommendation();

    void recommendationMade(long move);

    void pause();

    boolean isPaused();

    String getPlayerName(int side);

    long getPlayerTime(int side);

    void switchSides();

    void setSide(int side, Player player);

}
