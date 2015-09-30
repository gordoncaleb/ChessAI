package com.gordoncaleb.chess.board;

import com.gordoncaleb.chess.backend.*;
import com.gordoncaleb.chess.io.PGNParser;
import com.gordoncaleb.chess.persistence.BoardDAO;
import com.gordoncaleb.chess.pieces.Piece;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
    public void testFile1() throws Exception {
        testFile("/CatalanOpen.pgn");
    }

    @Test
    public void testFile2() throws Exception {
        testFile("/EnglishSymHedgehog.pgn");
    }

    public void testFile(String fileName) throws Exception {
        PGNParser parser = new PGNParser();
        List<PGNParser.PGNGame> games = parser.loadFile(fileName);

        for (PGNParser.PGNGame game : games) {
            try {
                parser.getPGNGameAsBoard(game);
            } catch (Exception e) {
                e.printStackTrace();
                throw new Exception("Error loading game: " + game, e);
            }
        }
    }

    @Test
    public void testPawnKnighting() {
        String[] setup = {
                "_,_,_,_,_,k,_,_,",
                "R,_,_,_,_,_,p,_,",
                "_,_,_,_,_,K,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,"
        };

        Board b = boardDAO.getFromSetup(Side.WHITE, setup);
        long hashCode = b.generateHashCode();
        b.makeMove(new Move(1, 6, 0, 6, 0, Move.MoveNote.NEW_KNIGHT).getMoveLong());
        assertEquals(Piece.PieceID.KNIGHT, b.getPieceID(0, 6));
        b.undoMove();
        assertEquals(Piece.PieceID.PAWN, b.getPieceID(1, 6));
        assertEquals(hashCode, b.getHashCode());
    }

    @Test
    public void testPawnQueening() {
        String[] setup = {
                "_,_,_,_,_,k,_,_,",
                "R,_,_,_,_,_,p,_,",
                "_,_,_,_,_,K,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,"
        };

        Board b = boardDAO.getFromSetup(Side.WHITE, setup);
        long hashCode = b.generateHashCode();
        b.makeMove(new Move(1, 6, 0, 6, 0, Move.MoveNote.NEW_QUEEN).getMoveLong());
        assertEquals(Piece.PieceID.QUEEN, b.getPieceID(0, 6));
        b.undoMove();
        assertEquals(Piece.PieceID.PAWN, b.getPieceID(1, 6));
        assertEquals(hashCode, b.getHashCode());
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
    public void testDoubleCheck() throws Exception {
        String[] setup = {
                "_,_,_,_,K,_,_,_,",
                "_,_,_,_,_,_,R,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,Q,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,r,r,_,p,",
                "R,_,_,_,_,_,_,k,",
                "_,_,_,_,_,_,_,_,"
        };

        testNumberOfMoves(Side.WHITE, setup, 1);
        testContainsMove(Side.WHITE, setup, new Move(6, 7, 7, 7));
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
        List<Move> moves = getValidMoves(side, setup);
        assertEquals(numberOfMoves, moves.size());
    }

    public void testContainsMove(Side side, String[] setup, Move move) {
        List<Move> moves = getValidMoves(side, setup);
        assertTrue(moves.contains(move));
    }

    public List<Move> getValidMoves(Side side, String[] setup) {
        Board b1 = boardDAO.getFromSetup(side, setup);
        b1.makeNullMove();
        return Move.fromLongs(b1.generateValidMoves());
    }

}
