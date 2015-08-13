package com.gordoncaleb.chess.backend;

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

    String getPlayerName(Side side);

    long getPlayerTime(Side side);

    void switchSides();

    void setSide(Side side, Player player);

}
