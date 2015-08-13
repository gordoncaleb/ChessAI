package com.gordoncaleb.chess;

import com.gordoncaleb.chess.backend.BoardFactory;
import org.junit.Test;

public class BoardFactoryTest {

    @Test
    public void testStandardBoardCreation() {
        BoardFactory.getStandardChessBoard().toString();
    }

    @Test
    public void test960BoardCreation() {
        BoardFactory.getRandomChess960Board().toString();
    }
}
