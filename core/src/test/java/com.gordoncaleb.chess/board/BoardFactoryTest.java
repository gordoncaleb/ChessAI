package com.gordoncaleb.chess.board;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BoardFactoryTest {
    private static final Logger logger = LoggerFactory.getLogger(BoardFactoryTest.class);

    @Test
    public void testStandardBoardCreation() {
        logger.info(BoardFactory.getStandardChessBoard().toString());
    }

    @Test
    public void test960BoardCreation() {
        logger.info(BoardFactory.getRandomChess960Board().toString());
    }
}
