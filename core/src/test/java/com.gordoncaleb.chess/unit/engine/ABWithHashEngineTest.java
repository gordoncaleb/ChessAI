package com.gordoncaleb.chess.unit.engine;

import com.gordoncaleb.chess.board.Board;
import com.gordoncaleb.chess.board.Move;
import com.gordoncaleb.chess.board.MoveContainerFactory;
import com.gordoncaleb.chess.board.Side;
import com.gordoncaleb.chess.board.serdes.JSONParser;
import com.gordoncaleb.chess.board.serdes.PGNParser;
import com.gordoncaleb.chess.engine.ABWithHashEngine;
import com.gordoncaleb.chess.engine.ABWithHashMoveEngine;
import com.gordoncaleb.chess.engine.MovePath;
import com.gordoncaleb.chess.engine.NegaMaxEngine;
import com.gordoncaleb.chess.engine.score.StaticScorer;
import com.gordoncaleb.chess.util.Perft;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.gordoncaleb.chess.board.Move.MoveNote.NORMAL;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class ABWithHashEngineTest {
    public static final Logger LOGGER = LoggerFactory.getLogger(ABWithHashEngineTest.class);

    @Test
    public void testPosition1() {
        final int maxDepth = 6;

        Board board = JSONParser.getFromSetup(Side.BLACK, new String[]{
                "r,n,b,q,_,k,_,r,",
                "p,p,_,P,b,p,p,p,",
                "_,_,p,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,B,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "P,P,P,_,N,n,P,P,",
                "R,N,B,Q,K,_,_,R,"
        });

        testPositionToMaxLevel(maxDepth, board, new StaticScorer());
    }

    @Test
    public void testAllPerftPositions() {
        List<Board> testBoards = new Perft().allPositions();
        testBoards.forEach(b -> {
            LOGGER.info("\n{}", b.toString());
            testPositionToMaxLevel(4, b, new StaticScorer());
        });
    }

    public void testPositionToMaxLevel(int maxLevel, Board board, StaticScorer scorer) {
        for (int i = 0; i < maxLevel; i++) {
            testEngineABWithHash(scorer, i + 1, board);
        }
    }

    public void testEngineABWithHash(StaticScorer scorer, int level, Board b) {

        ABWithHashEngine abWithHashEngine = new ABWithHashEngine(scorer,
                MoveContainerFactory.buildSortableMoveContainers(level + 1));

        ABWithHashMoveEngine aBWithHashMoveEngine = new ABWithHashMoveEngine(scorer,
                MoveContainerFactory.buildMoveContainers(level + 1));

        Board b1 = b.copy();
        Board b2 = b.copy();

        LOGGER.info("AB with HashMove engine, level={}", level);
        long now1 = System.currentTimeMillis();
        MovePath abHashMoveMp = aBWithHashMoveEngine.iterativeSearch(b1, level);
        long timeTaken1 = System.currentTimeMillis() - now1;
        List<Move> alphaBetaMoveList = abHashMoveMp.asList();
        LOGGER.info("Alpha Beta with HashMove timeTaken: {}", timeTaken1);
        LOGGER.info("AB with HashMove engine, score: {} Move Path: {}", abHashMoveMp.getScore(), PGNParser.toAlgebraicNotation(alphaBetaMoveList, b1));

        LOGGER.info("Alpha Beta With Hash level={}", level);
        long now2 = System.currentTimeMillis();
        MovePath abWithHashMp = abWithHashEngine.iterativeSearch(b2, level);
        long timeTaken2 = System.currentTimeMillis() - now2;
        List<Move> engineMoveList = abWithHashMp.asList();
        LOGGER.info("Alpha Beta with Hash timeTaken: {} {}", timeTaken2, percentDelta(timeTaken1, timeTaken2));
        LOGGER.info("Alpha Beta with Hash score: {} Path: {}", abWithHashMp.getScore(), PGNParser.toAlgebraicNotation(engineMoveList, b2));

        assertThat(abHashMoveMp.getScore(), is(equalTo(abWithHashMp.getScore())));
    }

    @Test
    public void testCheckMate() {
        ABWithHashEngine engine = new ABWithHashEngine(new StaticScorer(),
                MoveContainerFactory.buildMoveContainers(5));

        Board b = JSONParser.getFromSetup(Side.BLACK, new String[]{
                "r,n,b,_,_,k,_,_,",
                "p,p,_,r,b,p,p,p,",
                "_,_,p,_,_,r,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,B,_,_,q,_,_,",
                "_,_,_,_,_,_,_,_,",
                "P,P,P,_,N,_,P,P,",
                "R,N,B,_,K,_,_,R,"
        });

        MovePath movePath = engine.search(b, 4);
        List<Move> moves = movePath.asList();

        LOGGER.info("Move Path: {}", PGNParser.toAlgebraicNotation(moves, b));

        assertThat(moves, contains(
                new Move(4, 5, 6, 5, NORMAL)
        ));
    }

    private static String percentDelta(double a, double b) {
        final double delta = ((a / b)) * 100;
        return String.format("%4.2f%%", delta);
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
                MoveContainerFactory.buildSortableMoveContainers(20));

        final long now = System.currentTimeMillis();
        MovePath movePath = aBWithHashEngine.iterativeSearch(b, 8);
        final long timeTaken = System.currentTimeMillis() - now;
        List<Move> moveList = movePath.asList();

        LOGGER.info("Alpha Beta with Hash Move Path: {}", PGNParser.toAlgebraicNotation(moveList, b));
        LOGGER.info("Time Taken: {}", timeTaken);

        // 1... Nxd1 2.dxc8=Q Qxc8 3.Kxd1 h5 4.g4
        // depth 9 ~= 6100ms
    }
}
