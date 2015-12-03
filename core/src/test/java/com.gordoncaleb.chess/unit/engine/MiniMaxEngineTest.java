package com.gordoncaleb.chess.unit.engine;

import com.gordoncaleb.chess.board.Board;
import com.gordoncaleb.chess.board.BoardFactory;
import com.gordoncaleb.chess.board.Move;
import com.gordoncaleb.chess.engine.MiniMaxEngine;
import com.gordoncaleb.chess.engine.MovePath;
import com.gordoncaleb.chess.engine.score.StaticScorer;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MiniMaxEngineTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(MiniMaxEngineTest.class);

    @Test
    public void testSimple() {
        MiniMaxEngine engine = new MiniMaxEngine(new StaticScorer());

        String fen = "rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R b KQ - 1 8";
        Board b = BoardFactory.fromFEN(fen);

        LOGGER.info(b.toString());

        MovePath movePath = engine.search(b, 4);


    }
}
