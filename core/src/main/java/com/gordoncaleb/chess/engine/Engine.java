package com.gordoncaleb.chess.engine;

import com.gordoncaleb.chess.board.Board;

public interface Engine {
    MovePath search(final Board board, final int depth);
    MovePath search(final Board board, final int depth, final int startAlpha, final int startBeta);

    MovePath iterativeSearch(final Board board, final int maxLevel);
    MovePath iterativeSearch(final Board board, final int maxLevel, final int startAlpha, final int startBeta);

    /**
     * The deepest ply this engine can search, bounded by the size of its
     * pre-allocated move-container / principal-variation arrays. Callers that
     * drive iterative deepening (e.g. EngineMount) must not exceed this.
     */
    int getMaxSearchDepth();
}
