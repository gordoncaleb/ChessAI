package com.gordoncaleb.chess.board;

import com.gordoncaleb.chess.backend.*;
import com.gordoncaleb.chess.io.PGNParser;
import com.gordoncaleb.chess.persistence.BoardDAO;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class BoardTest {
    public static final Logger logger = LoggerFactory.getLogger(BoardTest.class);

    BoardDAO boardDAO = new BoardDAO();

    @Test
    public void testSerialization() throws IOException {
        Board b1 = boardDAO.getByFileName("/positions/standardSetup.json");
        Board b2 = BoardFactory.getStandardChessBoard();
        assertEquals(b1.getHashCode(), b2.getHashCode());
        assertFalse(b2.getHashCode() == 0L);
    }


    @Test
    public void testValidMoves() throws Exception {
        PGNParser parser = new PGNParser();
        List<PGNParser.PGNGame> games = parser.loadFile("/CatalanOpen.pgn");

        for (PGNParser.PGNGame game : games) {
            parser.getPGNGameAsBoard(game);
        }
    }

    @Test
    public void testEnPassantOutOfCheck() {
        String[] setup = {
                "_,_,_,_,_,_,_,_,",
                "_,P,_,N,_,_,_,_,",
                "_,_,_,_,_,_,K,_,",
                "_,P,_,_,P,_,p,P,",
                "_,_,_,_,p,_,k,p,",
                "_,p,_,_,_,_,_,_,",
                "p,b,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,"
        };

        Board b1 = boardDAO.getFromSetup(Side.WHITE, setup);
        b1.getMoveHistory().add(new Move(1, 7, 3, 7, 0, Move.MoveNote.PAWN_LEAP));

        logger.info(b1.toXML(true));

        b1.makeNullMove();
        List<Move> moves = Move.fromLongs(b1.generateValidMoves());
        assertEquals(4, moves.size());

    }

    @Test
    public void testQueenLeftEdge() throws Exception {
        String[] setup = {
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,P,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "r,_,_,_,Q,K,_,k,",
                "_,_,_,_,_,_,_,_,"
        };

        testNumberOfMoves(Side.WHITE, setup, 13);
    }

    @Test
    public void testRookLeftEdge() throws Exception {
        String[] setup = {
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,P,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "r,_,_,_,R,K,_,k,",
                "_,_,_,_,_,_,_,_,"
        };

        testNumberOfMoves(Side.WHITE, setup, 13);
    }

    @Test
    public void testRookRightEdge() throws Exception {
        String[] setup = {
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,P,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "k,_,K,R,_,_,_,r,",
                "_,_,_,_,_,_,_,_,"
        };

        testNumberOfMoves(Side.WHITE, setup, 13);
    }

    @Test
    public void testRookTopEdge() throws Exception {
        String[] setup = {
                "_,_,_,_,r,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,R,_,_,_,",
                "_,_,_,_,K,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,k,_,_,_,"
        };

        testNumberOfMoves(Side.WHITE, setup, 13);
    }

    @Test
    public void testRookBottomEdge() throws Exception {
        String[] setup = {
                "_,_,_,_,k,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,K,_,_,_,",
                "_,_,_,_,R,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,r,_,_,_,"
        };

        testNumberOfMoves(Side.WHITE, setup, 13);
    }

    public void testNumberOfMoves(Side side, String[] setup, int numberOfMoves) {
        Board b1 = boardDAO.getFromSetup(side, setup);
        b1.makeNullMove();
        List<Move> moves = Move.fromLongs(b1.generateValidMoves());
        assertEquals(numberOfMoves, moves.size());
    }

}
