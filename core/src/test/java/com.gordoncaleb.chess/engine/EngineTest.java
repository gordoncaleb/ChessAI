package com.gordoncaleb.chess.engine;

import com.gordoncaleb.chess.board.persistence.BoardDAO;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EngineTest {
    public static final Logger logger = LoggerFactory.getLogger(EngineTest.class);

    BoardDAO boardDAO = new BoardDAO();

    @Test
    public void testScenarios() {

        Engine engine = new Engine(null, null);


    }
}
