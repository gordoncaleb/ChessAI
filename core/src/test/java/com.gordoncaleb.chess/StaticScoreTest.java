package com.gordoncaleb.chess;

import com.gordoncaleb.chess.backend.Board;
import com.gordoncaleb.chess.backend.Side;
import com.gordoncaleb.chess.backend.StaticScore;
import com.gordoncaleb.chess.persistence.BoardDAO;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class StaticScoreTest {

    StaticScore scorer = new StaticScore();
    BoardDAO boardDAO = new BoardDAO();

    @Test
    public void testMaterialValue() {
        String[] setup = {
                "R,N,B,Q,K,B,N,R,",
                "P,P,P,P,P,P,_,P,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "p,p,p,p,p,p,p,p,",
                "r,n,b,q,k,b,n,r,"
        };

        testWinning(setup, Side.WHITE);
    }

    @Test
    public void testPawnStructure() {
        String[] setup = {
                "_,_,_,_,K,_,_,_,",
                "P,P,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,p,_,",
                "_,_,_,_,_,_,_,p,",
                "_,_,_,_,k,_,_,_,"
        };

        testWinning(setup, Side.WHITE);
    }

    private void testWinning(String[] setup, Side winner) {
        Board board = boardDAO.getFromSetup(Side.WHITE, setup);
        int delta = scorer.staticScore(board, winner);
        assertThat("Delta", delta, greaterThan(0));
    }
}
