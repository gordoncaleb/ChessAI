package com.gordoncaleb.chess.unit.engine;

import com.gordoncaleb.chess.board.*;
import com.gordoncaleb.chess.board.pieces.Piece;
import com.gordoncaleb.chess.board.serdes.JSONParser;
import com.gordoncaleb.chess.board.serdes.PGNParser;
import com.gordoncaleb.chess.engine.MovePath;
import com.gordoncaleb.chess.engine.NegaMaxEngine;
import com.gordoncaleb.chess.engine.score.StaticScorer;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import static com.gordoncaleb.chess.board.Move.MoveNote.*;
import static com.gordoncaleb.chess.board.pieces.Piece.PieceID.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NegaMaxEngineTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(NegaMaxEngineTest.class);


    @Test
    public void testNarrowGameSearch() {
        NegaMaxEngine engine = new NegaMaxEngine(new StaticScorer(),
                MoveContainerFactory.buildMoveContainers(5));

        Board b = JSONParser.getFromSetup(Side.WHITE, new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,p,_,",
                "_,_,_,_,_,q,_,_,",
                "_,_,_,_,P,_,_,_,",
                "_,_,_,P,_,_,_,_,",
                "_,_,_,_,_,_,_,_,"
        });

        MovePath movePath = engine.search(b, 2);
        List<Move> moves = movePath.asList();

        LOGGER.info("Move Path: {}", new PGNParser().toAlgebraicNotation(moves, b));

        assertThat(moves, contains(
                new Move(5, 4, 4, 5, NORMAL, QUEEN),
                new Move(3, 6, 4, 5, NORMAL, PAWN)
        ));

    }

    @Test
    public void testSimple() {
        NegaMaxEngine engine = new NegaMaxEngine(new StaticScorer(),
                MoveContainerFactory.buildMoveContainers(5));

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

        MovePath movePath = engine.search(b, 3);
        List<Move> moves = movePath.asList();

        LOGGER.info("Move Path: {}", new PGNParser().toAlgebraicNotation(moves, b));

        assertThat(moves, contains(
                new Move(6, 5, 7, 3, NORMAL, QUEEN),
                new Move(1, 3, 0, 2, NEW_QUEEN, BISHOP),
                new Move(0, 3, 0, 2, NORMAL, QUEEN)
        ));
    }

    @Test
    @Ignore
    public void testBinaryDeepSearch() {

        final Move a = new Move(0, 1, 2, 3, Move.MoveNote.NORMAL);
        final Move b = new Move(2, 3, 1, 2, Move.MoveNote.NORMAL);

        MockBoard board = new MockBoard();

        StaticScorer scorer = mock(StaticScorer.class);
        Integer[] scores = new Integer[]{2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16};
        when(scorer.staticScore(Mockito.any())).thenReturn(1, scores);

        NegaMaxEngine engine = new NegaMaxEngine(scorer,
                MoveContainerFactory.buildMoveContainers(5));

        MovePath movePath = engine.search(board, 3);
        List<Move> moves = movePath.asList();

        System.out.println(board.getVisitHistory().stream().collect(Collectors.joining(",")));
    }

    @Test
    public void testSimple2() {
        NegaMaxEngine engine = new NegaMaxEngine(new StaticScorer(),
                MoveContainerFactory.buildMoveContainers(5));

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

        MovePath movePath = engine.search(b, 4);
        List<Move> moves = movePath.asList();

        LOGGER.info("Move Path: {}", new PGNParser().toAlgebraicNotation(moves, b));

        assertThat(moves, contains(
                new Move(6, 5, 7, 3, NORMAL, QUEEN),
                new Move(1, 3, 0, 2, NEW_QUEEN, BISHOP),
                new Move(0, 3, 0, 2, NORMAL, QUEEN),
                new Move(7, 4, 7, 3, NORMAL, KNIGHT)
        ));
    }

    @Test
    public void testLevel1() {
        NegaMaxEngine engine = new NegaMaxEngine(new StaticScorer(),
                MoveContainerFactory.buildMoveContainers(5));

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

        List<Move> movePath = engine.search(b, 1).asList();

        LOGGER.info("Move Path: {}", new PGNParser().toAlgebraicNotation(movePath, b));

        assertThat(movePath.get(0), is(equalTo(
                new Move(6, 5, 7, 3, Move.MoveNote.NORMAL, Piece.PieceID.QUEEN, 7, 3))));
    }
}
