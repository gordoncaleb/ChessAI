package com.gordoncaleb.chess.piece;

import com.gordoncaleb.chess.backend.BitBoard;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static junit.framework.TestCase.assertTrue;

public class KnightTest {
    public static final Logger logger = LoggerFactory.getLogger(KnightTest.class);

    @Before
    public void before() {
        BitBoard.loadKnightFootPrints();
    }

    @Test
    public void testKnightBitBoardGeneration() {


        BitBoard.getKnightFootPrintMem(2, 2);
    }


    @Test
    public void test() {

        final int NUM = 10000000;

        long t1 = System.currentTimeMillis();
        for (int i = 0; i < NUM; i++) {
            for (int r = 0; r < 8; r++) {
                for (int c = 0; c < 8; c++) {
                    BitBoard.getKnightFootPrint(r, c);
                }
            }
        }
        logger.info("Gen way " + (System.currentTimeMillis() - t1));

        long t2 = System.currentTimeMillis();
        for (int i = 0; i < NUM; i++) {
            for (int r = 0; r < 8; r++) {
                for (int c = 0; c < 8; c++) {
                    BitBoard.getKnightFootPrintMem(r, c);
                }
            }
        }
        logger.info("Mem way " + (System.currentTimeMillis() - t2));

        for (int i = 0; i < NUM; i++) {
            for (int r = 0; r < 8; r++) {
                for (int c = 0; c < 8; c++) {
                    assertTrue(BitBoard.getKnightFootPrint(r, c) == BitBoard.getKnightFootPrintMem(r, c));
                }
            }
        }

    }
}
