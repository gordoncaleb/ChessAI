package com.gordoncaleb.chess.unit.engine;

import com.gordoncaleb.chess.board.Board;
import com.gordoncaleb.chess.board.Move;
import com.gordoncaleb.chess.board.MoveContainerFactory;
import com.gordoncaleb.chess.board.Side;
import com.gordoncaleb.chess.board.serdes.JSONParser;
import com.gordoncaleb.chess.board.serdes.PGNParser;
import com.gordoncaleb.chess.engine.ABWithHashEngine;
import com.gordoncaleb.chess.engine.AlphaBetaEngine;
import com.gordoncaleb.chess.engine.MovePath;
import com.gordoncaleb.chess.engine.NegaMaxEngine;
import com.gordoncaleb.chess.engine.score.StaticScorer;
import com.gordoncaleb.chess.util.Perft;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class ABWithHashEngineTest {
    public static final Logger LOGGER = LoggerFactory.getLogger(ABWithHashEngineTest.class);

    @Test
    @Ignore
    public void testPosition1() {
        testPositionVsAlphaBeta(Side.BLACK, new String[]{
                "r,n,b,q,_,k,_,r,",
                "p,p,_,P,b,p,p,p,",
                "_,_,p,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,B,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "P,P,P,_,N,n,P,P,",
                "R,N,B,Q,K,_,_,R,"
        });
    }

    @Test
    @Ignore
    public void testAllPerftPositions() {
        List<Board> testBoards = new Perft().allPositions();
        testBoards.forEach(b -> testPositionToMaxLevel(3, b));
    }

    public void testPositionVsAlphaBeta(int side, String[] setup) {
        Board b = JSONParser.getFromSetup(side, setup);
        testPositionToMaxLevel(4, b);
    }

    public void testPositionToMaxLevel(int maxLevel, Board b) {
        LOGGER.info("\n" + b.toString());
        for (int i = 0; i < maxLevel; i++) {
            ABWithHashEngine engine = new ABWithHashEngine(new StaticScorer(),
                    MoveContainerFactory.buildMoveContainers(i + 1));
            testEngineAgainstAlphaBeta(engine, i + 1, b);
        }
    }

    public void testEngineAgainstAlphaBeta(ABWithHashEngine engine, int level, Board b) {
        AlphaBetaEngine alphaBetaEngine = new AlphaBetaEngine(new StaticScorer(),
                MoveContainerFactory.buildMoveContainers(level + 1));

        Board b1 = b.copy();
        Board b2 = b.copy();

        List<Move> alphaBetaMoveList = alphaBetaEngine.search(b1, level).asList();

        MovePath mp = engine.search(b2, level);
        List<Move> engineMoveList = mp.asList();

        LOGGER.info("AlphaBetaEngine Move Path: {}", new PGNParser().toAlgebraicNotation(alphaBetaMoveList, b1));
        LOGGER.info("Alpha Beta with Hash Move Path: {}", new PGNParser().toAlgebraicNotation(engineMoveList, b2));

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

        ABWithHashEngine aBWithHashEngine = new ABWithHashEngine(new StaticScorer(),
                MoveContainerFactory.buildMoveContainers(10));

        final long now = System.currentTimeMillis();
        MovePath movePath = aBWithHashEngine.search(b, 6);
        final long timeTaken = System.currentTimeMillis() - now;
        List<Move> moveList = movePath.asList();

        LOGGER.info("Alpha Beta with Hash Move Path: {}", new PGNParser().toAlgebraicNotation(moveList, b));
        LOGGER.info("Time Taken: {}", timeTaken);

        // 1... Nxd1 2.dxc8=Q Qxc8 3.Kxd1 h5 4.g4
    }
}
