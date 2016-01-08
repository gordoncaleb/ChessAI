package com.gordoncaleb.chess.unit.engine;

import com.gordoncaleb.chess.board.Board;
import com.gordoncaleb.chess.board.Move;
import com.gordoncaleb.chess.board.MoveContainerFactory;
import com.gordoncaleb.chess.board.serdes.PGNParser;
import com.gordoncaleb.chess.engine.ABWithHashEngine;
import com.gordoncaleb.chess.engine.EngineMount;
import com.gordoncaleb.chess.engine.score.StaticScorer;
import com.gordoncaleb.chess.util.Perft;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class EngineMountTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(EngineMountTest.class);

    @Test
    public void testSearchTime() throws ExecutionException, InterruptedException {
        ABWithHashEngine engine = new ABWithHashEngine(new StaticScorer(),
                MoveContainerFactory.buildSortableMoveContainers(30));

        EngineMount engineMount = new EngineMount(engine);

        Board board = new Perft().kiwiPetePosition();

        List<Move> moveList = engineMount.search(board, 3000).get();

        LOGGER.info(PGNParser.toAlgebraicNotation(moveList, board));
    }
}
