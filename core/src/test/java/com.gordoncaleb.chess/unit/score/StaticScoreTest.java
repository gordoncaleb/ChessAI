package com.gordoncaleb.chess.unit.score;

import com.gordoncaleb.chess.board.Board;
import com.gordoncaleb.chess.board.Side;
import com.gordoncaleb.chess.engine.score.StaticScore;
import com.gordoncaleb.chess.board.serdes.JSONParser;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class StaticScoreTest {

    StaticScore scorer = new StaticScore();

    @Test
    public void testMaterialValue() {
        String[] setup = {
                "r,n,b,q,k,b,n,r,",
                "p,p,p,p,p,p,_,p,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "P,P,P,P,P,P,P,P,",
                "R,N,B,Q,K,B,N,R,"
        };

        testWinning(setup, Side.WHITE);
    }

    @Test
    public void testPawnStructure() {
        String[] setup = {
                "_,_,_,_,k,_,_,_,",
                "p,p,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,P,_,",
                "_,_,_,_,_,_,_,P,",
                "_,_,_,_,K,_,_,_,"
        };

        testWinning(setup, Side.WHITE);
    }

    @Test
    public void testScore() {
        String[] setup = {
                "r,n,b,q,k,b,n,r,",
                "p,p,p,p,p,p,p,p,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "P,P,P,P,P,P,P,P,",
                "R,N,B,Q,K,B,N,R,"
        };

        testDraw(setup);
    }

    private void testDraw(String[] setup) {
        Board board = JSONParser.getFromSetup(Side.WHITE, setup);
        int delta = scorer.staticScore(board);
        assertThat("Delta", delta, equalTo(0));
    }

    private void testWinning(String[] setup, int winner) {
        Board board = JSONParser.getFromSetup(Side.WHITE, setup);
        int delta = scorer.staticScore(board, winner);
        assertThat("Delta", delta, greaterThan(0));
    }
}
