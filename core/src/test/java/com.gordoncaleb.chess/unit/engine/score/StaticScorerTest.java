package com.gordoncaleb.chess.unit.engine.score;

import com.gordoncaleb.chess.board.Board;
import com.gordoncaleb.chess.board.BoardFactory;
import com.gordoncaleb.chess.board.Side;
import com.gordoncaleb.chess.board.bitboard.BitBoard;
import com.gordoncaleb.chess.engine.score.StaticScorer;
import com.gordoncaleb.chess.board.serdes.JSONParser;
import com.gordoncaleb.chess.engine.score.Values;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import static com.gordoncaleb.chess.board.bitboard.BitBoard.*;

public class StaticScorerTest {

    StaticScorer scorer = new StaticScorer();

    @Test
    public void testMaterialValueForWin() {
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

    @Test
    public void testDoubledPawns() {
        long bb = BitBoard.parseBitBoard(new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,p,_,_,_,_,_,_,",
                "_,_,_,_,p,_,_,_,",
                "_,_,_,_,_,_,p,_,",
                "_,_,_,_,p,_,_,_,",
                "_,_,_,_,p,_,_,_,",
                "_,p,_,_,p,p,p,p,",
                "_,_,_,_,_,_,_,_,"
        });

        assertThat(scorer.getDoubledPawns(bb), is(equalTo(5)));
    }

    @Test
    public void testDoubledPawns2() {
        long bb = BitBoard.parseBitBoard(new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,p,_,p,_,_,",
                "_,_,p,_,p,_,p,_,",
                "_,p,_,_,_,_,_,p,",
                "p,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,"
        });

        assertThat(scorer.getDoubledPawns(bb), is(equalTo(0)));
    }

    @Test
    public void testPassedPawns() {
        long blackPawns = parseBitBoard(new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,p,",
                "_,_,_,_,_,p,_,_,",
                "p,_,_,_,_,_,p,_,",
                "_,p,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,"
        });

        long whitePawns = parseBitBoard(new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,P,_,_,_,_,",
                "_,_,_,_,_,_,_,_,"
        });

        assertThat(printBitBoard(scorer.getPassedPawns(whitePawns, blackPawns, Side.WHITE)),
                is(equalTo(printBitBoard(parseBitBoard(new String[]{
                        "_,_,_,_,_,_,_,_,",
                        "_,_,_,_,_,_,_,_,",
                        "_,_,_,_,_,_,_,_,",
                        "_,_,_,_,_,_,_,_,",
                        "_,_,_,_,_,_,_,_,",
                        "_,_,_,_,_,_,_,_,",
                        "_,_,_,P,_,_,_,_,",
                        "_,_,_,_,_,_,_,_,"
                })))));
    }

    @Test
    public void testOpenFiles() {
        long pawns = parseBitBoard(new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,p,",
                "_,_,_,_,_,p,_,_,",
                "p,_,_,_,_,_,p,_,",
                "_,p,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,"
        });

        assertThat(printBitBoard(scorer.getOpenFiles(pawns)), is(equalTo(printBitBoard(parseBitBoard(new String[]{
                "_,_,1,1,1,_,_,_,",
                "_,_,1,1,1,_,_,_,",
                "_,_,1,1,1,_,_,_,",
                "_,_,1,1,1,_,_,_,",
                "_,_,1,1,1,_,_,_,",
                "_,_,1,1,1,_,_,_,",
                "_,_,1,1,1,_,_,_,",
                "_,_,1,1,1,_,_,_,"
        })))));
    }

    @Test
    public void testIsolatedPawns() {
        long pawns = parseBitBoard(new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,p,",
                "_,_,_,_,_,p,_,_,",
                "p,_,_,p,_,_,p,_,",
                "_,p,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,"
        });

        assertThat(printBitBoard(scorer.getIsolatedPawns(pawns, Side.WHITE)), is(equalTo(printBitBoard(parseBitBoard(new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,p,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,"
        })))));
    }

    @Test
    public void testGamePhase() {
        assertThat(scorer.calcGamePhase(BoardFactory.getStandardChessBoard()), is(equalTo(0)));
    }

    @Test
    public void testGamePhase2() {
        Board b = JSONParser.getFromSetup(Side.WHITE, new String[]{
                "_,_,_,_,k,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,K,_,_,_,"
        });

        assertThat(scorer.calcGamePhase(b), is(equalTo(StaticScorer.ENDGAME_PHASE)));
    }

    @Test
    public void testMaterialValue() {
        String[] setup = {
                "r,n,b,_,k,b,n,r,",
                "p,p,p,p,p,p,_,p,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "P,P,P,P,P,P,P,P,",
                "R,N,B,Q,K,B,N,R,"
        };

        int delta = Values.PAWN_VALUE + Values.QUEEN_VALUE;

        Board board = JSONParser.getFromSetup(Side.WHITE, setup);
        assertThat("Delta", scorer.materialScoreDelta(Side.WHITE, Side.BLACK, board.getPosBitBoard()),
                equalTo(delta));

        assertThat("Delta", scorer.materialScoreDelta(Side.BLACK, Side.WHITE, board.getPosBitBoard()),
                equalTo(-delta));
    }

}
