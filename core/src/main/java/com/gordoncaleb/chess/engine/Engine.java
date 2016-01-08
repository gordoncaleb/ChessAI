package com.gordoncaleb.chess.engine;

import com.gordoncaleb.chess.board.Board;

public interface Engine {
    MovePath search(final Board board, final int depth);
    MovePath search(final Board board, final int depth, final int startAlpha, final int startBeta);

    MovePath iterativeSearch(final Board board, final int maxLevel);
    MovePath iterativeSearch(final Board board, final int maxLevel, final int startAlpha, final int startBeta);
}
