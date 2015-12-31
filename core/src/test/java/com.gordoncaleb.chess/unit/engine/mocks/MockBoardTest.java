package com.gordoncaleb.chess.unit.engine.mocks;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

public class MockBoardTest {

    private static Logger LOGGER = LoggerFactory.getLogger(MockBoardTest.class);

    @Test
    public void testHashCode() {
        MockBoard b1 = new MockBoard();

        b1.makeMove(MockBoard.LeftMove);

        MockBoard b2 = new MockBoard();
        b2.makeMove(MockBoard.RightMove);

        assertThat(b1.getHashCode(), is(not(equalTo(b2.getHashCode()))));
    }

    @Test
    public void testHashCode2() {
        MockBoard b1 = new MockBoard();

        b1.makeMove(MockBoard.RightMove);
        b1.makeMove(MockBoard.LeftMove);

        MockBoard b2 = new MockBoard();
        b2.makeMove(MockBoard.LeftMove);
        b2.makeMove(MockBoard.RightMove);


        LOGGER.info("b1 hash: 0x{}", Long.toHexString(b1.getHashCode()));
        LOGGER.info("b2 hash: 0x{}", Long.toHexString(b2.getHashCode()));
        assertThat(b1.getHashCode(), is(not(equalTo(b2.getHashCode()))));
    }
}
