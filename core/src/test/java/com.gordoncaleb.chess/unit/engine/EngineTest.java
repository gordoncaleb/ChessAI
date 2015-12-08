package com.gordoncaleb.chess.unit.engine;

import com.gordoncaleb.chess.board.Board;
import com.gordoncaleb.chess.board.Move;
import com.gordoncaleb.chess.board.MoveContainerFactory;
import com.gordoncaleb.chess.board.Side;
import com.gordoncaleb.chess.board.pieces.Piece;
import com.gordoncaleb.chess.board.serdes.JSONParser;
import com.gordoncaleb.chess.board.serdes.PGNParser;
import com.gordoncaleb.chess.engine.Engine;
import com.gordoncaleb.chess.engine.NegaMaxEngine;
import com.gordoncaleb.chess.engine.score.StaticScorer;
import com.gordoncaleb.chess.util.Perft;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class EngineTest {
    public static final Logger LOGGER = LoggerFactory.getLogger(EngineTest.class);

    @Test
    public void testPosition1() {
        testPositionVsNegaMax(Side.BLACK, new String[]{
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
    public void testPosition2() {
        testPositionToMaxLevel(4, new Perft().kiwiPetePosition());
    }

    public void testPositionVsNegaMax(int side, String[] setup) {
        Board b = JSONParser.getFromSetup(side, setup);
        testPositionToMaxLevel(4, b);
    }

    public void testPositionToMaxLevel(int maxLevel, Board b) {
        LOGGER.info("\n" + b.toString());
        for (int i = 0; i < maxLevel; i++) {
            Engine engine = new Engine(new StaticScorer(),
                    MoveContainerFactory.buildMoveContainers(i + 1));
            testEngineAgainstNegaMax(engine, i + 1, b);
        }
    }

    public void testEngineAgainstNegaMax(Engine engine, int level, Board b) {
        NegaMaxEngine negaMaxEngine = new NegaMaxEngine(new StaticScorer(),
                MoveContainerFactory.buildMoveContainers(level + 1));

        Board b1 = b.copy();
        Board b2 = b.copy();

        List<Move> negaMaxMoveList = negaMaxEngine.search(b1, level).asList();
        List<Move> engineMoveList = engine.search(b2, level).asList();

        LOGGER.info("NegaMax Move Path: {}", new PGNParser().toAlgebraicNotation(negaMaxMoveList, b1));
        LOGGER.info("Engine Move Path: {}", new PGNParser().toAlgebraicNotation(engineMoveList, b2));

        assertThat(negaMaxMoveList, is(equalTo(engineMoveList)));
    }
}
