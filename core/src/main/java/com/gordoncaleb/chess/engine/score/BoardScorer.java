package com.gordoncaleb.chess.engine.score;

import com.gordoncaleb.chess.board.Board;

public interface BoardScorer {

    int staticScore(final Board b);

    int staticScore(final Board b, final int side);

    int endOfGameValue(final boolean isInCheck, final int level);

    int drawValue(final int level);

}
