package com.gordoncaleb.chess.board;

import com.gordoncaleb.chess.backend.*;
import com.gordoncaleb.chess.io.PGNParser;
import com.gordoncaleb.chess.persistence.BoardDAO;
import com.gordoncaleb.chess.pieces.Piece;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.function.Consumer;

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
    public void testHashCodeUniquness() throws Exception {
        testFile("/pgns/eco.pgn", this::assertHashCodesAreUnique);
    }

    @Test
    public void testFile1() throws Exception {
        testFile("/pgns/CatalanOpen.pgn", b -> {
            testHashCodeGen(b);
            verifyBitBoards(b);
        });
    }

    @Test
    public void testFile2() throws Exception {
        testFile("/pgns/EnglishSymHedgehog.pgn", this::testHashCodeGen);
    }

    public void testFile(String fileName, Consumer<Board> boardConsumer) throws Exception {
        PGNParser parser = new PGNParser();
        List<PGNParser.PGNGame> games = parser.loadFile(fileName);

        for (PGNParser.PGNGame game : games) {
            try {
                Board b = parser.getPGNGameAsBoard(game);
                boardConsumer.accept(b);
            } catch (Exception e) {
                e.printStackTrace();
                throw new Exception("Error loading game: " + game, e);
            }
        }
    }

    public void testHashCodeGen(Board b) {

        long origHash = b.getHashCode();

        Stack<Long> moveStack = new Stack<>();
        while (b.canUndo()) {
            assertEquals(b.getHashCode(), b.generateHashCode());
            moveStack.push(b.undoMove());
        }

        while (!moveStack.isEmpty()) {
            assertEquals(b.getHashCode(), b.generateHashCode());
            b.makeMove(moveStack.pop());
        }

        assertEquals(b.getHashCode(), b.generateHashCode());
        assertEquals(origHash, b.generateHashCode());
    }

    public void assertHashCodesAreUnique(Board b) {
        Set<Long> hashCodes = new HashSet<>();
        while (b.canUndo()) {
            assertFalse(hashCodes.contains(b.getHashCode()));
            hashCodes.add(b.getHashCode());
            b.undoMove();
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


    public void verifyBitBoards(Board b) {

        long[][] posBitBoard = b.getPosBitBoard();
        Piece piece;

        long[][] allBitBoard = new long[Piece.PieceID.values().length][2];

        for (int i = 0; i < Piece.PieceID.values().length; i++) {
            allBitBoard[i][0] = posBitBoard[i][0];
            allBitBoard[i][1] = posBitBoard[i][1];
        }

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                piece = b.getPiece(r, c);

                if (piece != null) {
                    allBitBoard[piece.getPieceID().ordinal()][piece.getSide().ordinal()] ^= BitBoard.getMask(r, c);
                }

            }
        }

        for (int i = 0; i < Piece.PieceID.values().length; i++) {
            assertFalse(allBitBoard[i][0] != 0);
            assertFalse(allBitBoard[i][1] != 0);
        }

    }
}
