package com.gordoncaleb.chess.ui.gui.game;

import com.gordoncaleb.chess.board.Board;
import com.gordoncaleb.chess.board.Move;

public interface PlayerContainer {

    boolean makeMove(Move move) throws Exception;

    GameResults newGame(Board board, boolean block) throws Exception;

    void endGame();

    boolean undoMove();

    void showProgress(int progress);

    void requestRecommendation();

    void recommendationMade(Move move);

    void pause();

    boolean isPaused();

    String getPlayerName(int side);

    long getPlayerTime(int side);

    void switchSides();

    void setSide(int side, Player player);

}
