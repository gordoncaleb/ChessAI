package com.gordoncaleb.chess.unit;

import com.gordoncaleb.chess.board.Board;
import com.gordoncaleb.chess.board.BoardFactory;
import com.gordoncaleb.chess.unit.board.BoardTestFixture;
import com.gordoncaleb.chess.board.serdes.JSONParser;
import com.gordoncaleb.chess.util.Perft;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsEqual.equalTo;

public class SerdesTest {
    private static final Logger logger = LoggerFactory.getLogger(SerdesTest.class);

    @Test
    public void testJSONRoundTrip() throws Exception {
        Board board = new Perft().position3();

        String json = JSONParser.toJSON(board);
        Board boardFromJson = JSONParser.fromJSON(json);

        logger.info(json);

        assertThat(board.getHashCode(), is(equalTo(boardFromJson.getHashCode())));
    }

    @Test
    public void testJSONRoundTripWithMoveHistory() throws Exception {
        Board board = BoardFactory.getStandardChessBoard();

        BoardTestFixture.makeRandomMoves(board, 20);

        String json = JSONParser.toJSON(board, true);

        logger.info(json);

        Board boardFromJson = JSONParser.fromJSON(json);

        assertThat(boardFromJson.getMoveHistory().toList().toArray(),
                is(arrayContaining(board.getMoveHistory().toList().toArray())));
    }
}
