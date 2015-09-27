package com.gordoncaleb.chess;

import com.gordoncaleb.chess.ai.Engine;
import com.gordoncaleb.chess.backend.Board;
import com.gordoncaleb.chess.persistence.BoardDAO;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EngineTest {
    public static final Logger logger = LoggerFactory.getLogger(EngineTest.class);

    BoardDAO boardDAO = new BoardDAO();

    @Test
    public void testScenarios(){

        Engine engine = new Engine();

        Board board = boardDAO.getByXMLFileName("/positions/test_position_2.xml");

        logger.info("\n" + board.toString());

    }
}
