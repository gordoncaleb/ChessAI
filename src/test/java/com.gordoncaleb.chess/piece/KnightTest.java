package com.gordoncaleb.chess.piece;

import com.gordoncaleb.chess.bitboard.BitBoard;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class KnightTest {
    public static final Logger logger = LoggerFactory.getLogger(KnightTest.class);

    @Before
    public void before() {
        BitBoard.loadKnightFootPrints();
    }

    @Test
    public void testKnightBitBoardGeneration1() {
        String[] solution = new String[]{
                "_,1,_,1,_,_,_,_,",
                "1,_,_,_,1,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "1,_,_,_,1,_,_,_,",
                "_,1,_,1,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,"
        };
        testAttackBB(solution, 2, 2);
    }

    @Test
    public void testKnightBitBoardGeneration2() {
        String[] solution = new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,1,_,",
                "_,_,_,_,_,1,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,1,_,_,",
                "_,_,_,_,_,_,1,_,",
                "_,_,_,_,_,_,_,_,"
        };
        testAttackBB(solution, 4, 7);
    }

    @Test
    public void testKnightBitBoardGeneration3() {
        String[] solution = new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,1,_,1,_,_,",
                "_,_,1,_,_,_,1,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,1,_,_,_,1,_,"
        };
        testAttackBB(solution, 6, 4);
    }

    @Test
    public void testKnightBitBoardGeneration4() {
        String[] solution = new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,1,_,",
                "_,_,_,_,_,1,_,_,",
                "_,_,_,_,_,_,_,_,"
        };
        testAttackBB(solution, 7, 7);
    }

    @Test
    public void testKnightBitBoardGeneration5() {
        String[] solution = new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,1,_,_,_,_,_,_,",
                "_,_,1,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,1,_,_,_,_,_,"
        };
        testAttackBB(solution, 6, 0);
    }

    public void testAttackBB(String[] solution, int fromRow, int fromCol) {

        long bb = BitBoard.getKnightFootPrintMem(fromRow, fromCol);
        long bbSolution = BitBoard.parseBitBoard(solution);
        assertThat(BitBoard.printBitBoard(bb), is(equalTo(BitBoard.printBitBoard(bbSolution))));

        bb = BitBoard.getKnightFootPrint(fromRow, fromCol);
        assertThat(BitBoard.printBitBoard(bb), is(equalTo(BitBoard.printBitBoard(bbSolution))));
    }
}
