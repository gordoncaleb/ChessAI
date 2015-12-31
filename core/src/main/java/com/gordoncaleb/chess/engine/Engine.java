package com.gordoncaleb.chess.engine;

import com.gordoncaleb.chess.board.Board;

public interface Engine {
    MovePath search(final Board board, final int maxLevel);
    MovePath search(final Board board, final int maxLevel, final int startAlpha, final int startBeta);
}
