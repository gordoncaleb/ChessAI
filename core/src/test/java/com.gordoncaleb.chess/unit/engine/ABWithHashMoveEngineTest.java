package com.gordoncaleb.chess.unit.engine;

import com.gordoncaleb.chess.board.Board;
import com.gordoncaleb.chess.board.Move;
import com.gordoncaleb.chess.board.MoveContainerFactory;
import com.gordoncaleb.chess.board.Side;
import com.gordoncaleb.chess.board.serdes.JSONParser;
import com.gordoncaleb.chess.board.serdes.PGNParser;
import com.gordoncaleb.chess.engine.ABWithHashMoveEngine;
import com.gordoncaleb.chess.engine.AlphaBetaEngine;
import com.gordoncaleb.chess.engine.MovePath;
import com.gordoncaleb.chess.engine.score.StaticScorer;
import com.gordoncaleb.chess.unit.engine.mocks.MockBoard;
import com.gordoncaleb.chess.unit.engine.mocks.MockScorer;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class ABWithHashMoveEngineTest {
    public static final Logger LOGGER = LoggerFactory.getLogger(ABWithHashMoveEngineTest.class);

    @Test
    public void testPosition1() {
        final int maxDepth = 10;
        testPositionToMaxLevel(maxDepth, new MockScorer(123L, maxDepth));
    }

    public void testPositionToMaxLevel(int maxLevel, MockScorer scorer) {
        MockBoard b = new MockBoard();
        for (int i = 0; i < maxLevel; i++) {
            testEngineAgainstAlphaBeta(scorer, i + 1, b);
        }
    }

    public void testEngineAgainstAlphaBeta(MockScorer scorer, int level, MockBoard b) {

        ABWithHashMoveEngine engine = new ABWithHashMoveEngine(scorer,
                MoveContainerFactory.buildMoveContainers(level + 1));

        AlphaBetaEngine alphaBetaEngine = new AlphaBetaEngine(scorer,
                MoveContainerFactory.buildMoveContainers(level + 1));

        Board b1 = b.copy();
        Board b2 = b.copy();

        LOGGER.info("Alpha Beta level={}", level);
        MovePath abMovePath = alphaBetaEngine.search(b1, level);
        List<Move> alphaBetaMoveList = abMovePath.asList();
        LOGGER.info("AlphaBetaEngine score: {} Move Path: {}", abMovePath.getScore(), MockBoard.movesToString(alphaBetaMoveList));

        LOGGER.info("Alpha Beta With hash level={}", level);
        MovePath mp = engine.iterativeSearch(b2, level);
        List<Move> engineMoveList = mp.asList();
        LOGGER.info("Alpha Beta with Hash Move score: {} Path: {}", mp.getScore(), MockBoard.movesToString(engineMoveList));

        assertThat(alphaBetaMoveList, is(equalTo(engineMoveList)));
    }

    @Test
    public void eyeBallEngineSpeed() {
        Board b = JSONParser.getFromSetup(Side.BLACK, new String[]{
                "r,n,b,q,_,k,_,r,",
                "p,p,_,P,b,p,p,p,",
                "_,_,p,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,B,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "P,P,P,_,N,n,P,P,",
                "R,N,B,Q,K,_,_,R,"
        });

        ABWithHashMoveEngine aBWithHashMoveEngine = new ABWithHashMoveEngine(new StaticScorer(),
                MoveContainerFactory.buildMoveContainers(10));

        final long now = System.currentTimeMillis();
        MovePath movePath = aBWithHashMoveEngine.iterativeSearch(b, 6);
        final long timeTaken = System.currentTimeMillis() - now;
        List<Move> moveList = movePath.asList();

        LOGGER.info("Alpha Beta with Hash Move, Move Path: {}", PGNParser.toAlgebraicNotation(moveList, b));
        LOGGER.info("Time Taken: {}", timeTaken);

        // 1... Nxd1 2.dxc8=Q Qxc8 3.Kxd1 h5 4.g4
    }
}
