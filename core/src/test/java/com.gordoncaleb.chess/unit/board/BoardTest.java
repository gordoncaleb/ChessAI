package com.gordoncaleb.chess.unit.board;

import com.gordoncaleb.chess.board.Board;
import com.gordoncaleb.chess.board.BoardFactory;
import com.gordoncaleb.chess.board.Move;
import com.gordoncaleb.chess.board.Side;
import com.gordoncaleb.chess.board.bitboard.BitBoard;
import com.gordoncaleb.chess.board.serdes.PGNParser;
import com.gordoncaleb.chess.board.serdes.JSONParser;
import com.gordoncaleb.chess.board.pieces.Piece;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.gordoncaleb.chess.board.bitboard.BitBoard.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BoardTest {
    public static final Logger logger = LoggerFactory.getLogger(BoardTest.class);

    @Test
    public void testSerialization() throws IOException {
        Board b1 = JSONParser.getByFileName("/positions/standardSetup.json");
        Board b2 = BoardFactory.getStandardChessBoard();
        assertEquals(b1.getHashCode(), b2.getHashCode());
        assertFalse(b2.getHashCode() == 0L);
    }

    @Test
    public void testHashCodeUniquness() throws Exception {
        testFile("/movebook/eco.pgn", this::assertHashCodesAreUnique);
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

        Stack<Move> moveStack = new Stack<>();
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
                "_,_,_,_,_,K,_,_,",
                "r,_,_,_,_,_,P,_,",
                "_,_,_,_,_,k,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,"
        };

        Board b = JSONParser.getFromSetup(Side.WHITE, setup);
        long hashCode = b.generateHashCode();
        b.makeMove(new Move(1, 6, 0, 6, Move.MoveNote.NEW_KNIGHT));
        assertEquals(Piece.PieceID.KNIGHT, b.getPieceID(0, 6));
        b.undoMove();
        assertEquals(Piece.PieceID.PAWN, b.getPieceID(1, 6));
        assertEquals(hashCode, b.getHashCode());
    }

    @Test
    public void testPawnQueening() {
        String[] setup = {
                "_,_,_,_,_,K,_,_,",
                "r,_,_,_,_,_,P,_,",
                "_,_,_,_,_,k,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,"
        };

        Board b = JSONParser.getFromSetup(Side.WHITE, setup);
        long hashCode = b.generateHashCode();
        b.makeMove(new Move(1, 6, 0, 6, Move.MoveNote.NEW_QUEEN));
        assertEquals(Piece.PieceID.QUEEN, b.getPieceID(0, 6));
        b.undoMove();
        assertEquals(Piece.PieceID.PAWN, b.getPieceID(1, 6));
        assertEquals(hashCode, b.getHashCode());
    }

    @Test
    public void testEnPassantOutOfCheck() throws Exception {
        String[] setup = {
                "_,_,_,_,_,_,_,_,",
                "_,p,_,n,_,_,_,p,",
                "_,_,_,_,_,_,k,_,",
                "_,p,_,_,p,_,P,_,",
                "_,_,_,_,P,_,K,P,",
                "_,P,_,_,_,P,P,P,",
                "P,B,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,"
        };

        Board b = JSONParser.getFromSetup(Side.BLACK, setup);
        b.makeMove(new Move(1, 7, 3, 7, Move.MoveNote.PAWN_LEAP));

        logger.info(b.toJson(false));

        b.makeNullMove();
        List<Move> moves = b.generateValidMoves().toList();
        Piece pt = b.getPiece(3, 7);
        assertThat(moves, containsInAnyOrder(
                new Move(3, 6, 2, 7, Move.MoveNote.EN_PASSANT, b.getPiece(3, 7))
        ));
    }


    @Test
    public void testDoubleCheck() throws Exception {
        String[] setup = {
                "_,_,_,_,k,_,_,_,",
                "_,_,_,_,_,_,r,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,q,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,R,R,_,P,",
                "r,_,_,_,_,_,_,K,",
                "_,_,_,_,_,_,_,_,"
        };

        testNumberOfMoves(Side.WHITE, setup, 1);
        testContainsExactlyMoves(Side.WHITE, setup, Arrays.asList(new Move(6, 7, 7, 7, Move.MoveNote.NORMAL)));
    }

    @Test
    public void testCheckByPawn() throws Exception {
        String[] setup = {
                "r,n,b,q,k,b,n,r,",
                "p,p,p,_,_,p,p,p,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,P,P,p,_,_,",
                "_,_,_,Q,K,B,P,_,",
                "P,P,p,_,B,P,_,P,",
                "R,N,_,_,_,_,N,R,"
        };

        testContainsExactlyMoves(Side.WHITE, setup, Arrays.asList(
                new Move(5, 4, 4, 5),
                new Move(5, 6, 4, 5),
                new Move(5, 4, 6, 3)
        ));
    }

    @Test
    public void testCheckQueenUseDefBlock() throws Exception {
        String[] setup = {
                "r,n,b,_,k,b,n,r,",
                "p,p,p,_,_,p,p,p,",
                "_,_,_,_,_,_,_,q,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,P,P,_,_,_,",
                "_,_,_,Q,K,P,P,_,",
                "P,P,p,_,B,N,_,P,",
                "R,N,_,_,_,_,_,R,"
        };

        testContainsExactlyMoves(Side.WHITE, setup, Arrays.asList(
                new Move(5, 5, 4, 5)
        ));
    }

    @Test
    public void testCheckQueenMoveKing() throws Exception {
        String[] setup = {
                "r,n,b,_,k,b,n,r,",
                "p,p,p,_,_,p,p,p,",
                "_,_,_,_,_,_,_,q,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,P,P,_,_,_,",
                "_,_,_,Q,K,P,P,_,",
                "P,P,p,_,_,N,_,P,",
                "R,N,_,_,_,_,_,R,"
        };

        testContainsExactlyMoves(Side.WHITE, setup, Arrays.asList(
                new Move(5, 5, 4, 5),
                new Move(5, 4, 6, 4)
        ));
    }

    @Test
    public void testCheckKnight() throws Exception {
        String[] setup = {
                "r,n,b,q,k,b,n,r,",
                "p,p,p,_,_,p,p,p,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,P,P,_,n,_,",
                "_,_,_,Q,K,_,P,_,",
                "P,P,p,_,_,P,N,P,",
                "R,N,_,_,_,_,_,R,"
        };

        testContainsExactlyMoves(Side.WHITE, setup, Arrays.asList(
                new Move(5, 4, 4, 5),
                new Move(5, 4, 5, 5),
                new Move(5, 4, 6, 4),
                new Move(5, 4, 6, 3)
        ));
    }

    @Test
    public void testCheckKnightWithRookThreat() throws Exception {
        String[] setup = {
                "_,n,b,q,k,b,n,r,",
                "p,p,p,_,_,p,p,p,",
                "_,_,_,_,_,r,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,P,P,_,n,_,",
                "_,_,_,Q,K,_,P,_,",
                "P,P,p,_,_,P,N,P,",
                "R,N,_,_,_,_,_,R,"
        };

        testContainsExactlyMoves(Side.WHITE, setup, Arrays.asList(
                new Move(5, 4, 6, 4),
                new Move(5, 4, 6, 3)
        ));
    }

    @Test
    public void testCheckKnightTakeKnight() throws Exception {
        String[] setup = {
                "r,n,b,q,k,b,n,r,",
                "p,p,p,_,_,p,p,p,",
                "_,_,_,_,_,_,Q,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,P,P,_,n,_,",
                "_,_,_,N,K,_,P,_,",
                "P,P,p,_,_,P,N,P,",
                "R,_,_,_,_,_,_,R,"
        };

        testContainsExactlyMoves(Side.WHITE, setup, Arrays.asList(
                new Move(5, 4, 4, 5),
                new Move(5, 4, 5, 5),
                new Move(5, 4, 6, 4),
                new Move(5, 4, 6, 3),
                new Move(2, 6, 4, 6)
        ));
    }

    @Test
    public void testCheckKnightTakeKnightNoOption() throws Exception {
        String[] setup = {
                "r,n,b,q,k,b,n,r,",
                "p,p,p,_,_,p,p,p,",
                "_,_,_,_,_,_,Q,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,P,P,P,n,_,",
                "_,_,_,N,K,N,P,_,",
                "P,P,P,P,P,P,_,P,",
                "R,_,_,_,_,_,_,R,"
        };

        testContainsExactlyMoves(Side.WHITE, setup, Arrays.asList(
                new Move(2, 6, 4, 6)
        ));
    }

    @Test
    public void testCheckPawnAndKnight() throws Exception {
        String[] setup = {
                "r,_,b,_,k,b,_,r,",
                "p,p,p,_,_,p,p,p,",
                "_,_,_,_,_,q,Q,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,n,P,P,p,n,_,",
                "_,_,_,N,K,N,P,_,",
                "P,P,P,P,_,P,_,P,",
                "R,_,_,_,_,_,_,R,"
        };

        testContainsExactlyMoves(Side.WHITE, setup, Arrays.asList(
                new Move(5, 4, 6, 4)
        ));
    }

    @Test
    public void testCheckDoubleKnight() throws Exception {
        String[] setup = {
                "r,_,b,q,k,b,_,r,",
                "p,p,p,_,_,p,p,p,",
                "_,_,_,_,_,_,Q,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,n,P,P,P,n,_,",
                "_,_,_,N,K,N,P,_,",
                "P,P,P,P,_,P,_,P,",
                "R,_,_,_,_,_,_,R,"
        };

        testContainsExactlyMoves(Side.WHITE, setup, Arrays.asList(
                new Move(5, 4, 6, 4)
        ));
    }

    @Test
    public void testCheckDoubleKnightNullMove() throws Exception {
        String[] setup = {
                "r,_,b,q,k,b,_,r,",
                "p,p,p,_,_,p,p,p,",
                "_,_,_,_,_,_,Q,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,n,P,P,P,n,_,",
                "_,_,_,N,K,N,P,_,",
                "P,P,P,P,_,P,_,P,",
                "R,_,_,_,_,_,_,R,"
        };

        long[] nullMoveInfo = getNullMoveInfo(Side.WHITE, setup);

        assertThat(printBitBoard(nullMoveInfo[1]), is(equalTo(printBitBoard(parseBitBoard(new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
        })))));
    }

    @Test
    public void testCheckDoubleSliderNullMove() throws Exception {
        String[] setup = {
                "r,_,b,_,k,_,_,r,",
                "p,p,_,_,_,p,p,p,",
                "_,q,_,Q,_,_,_,q,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,P,_,_,_,",
                "_,_,_,N,K,N,P,_,",
                "P,P,P,P,_,P,_,P,",
                "R,_,_,_,_,_,_,R,"
        };

        long[] nullMoveInfo = getNullMoveInfo(Side.WHITE, setup);

        assertThat(printBitBoard(nullMoveInfo[1]), is(equalTo(printBitBoard(parseBitBoard(new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
        })))));
    }

    @Test
    public void testCheckDoubleSlider() throws Exception {
        String[] setup = {
                "r,_,b,_,k,_,_,r,",
                "p,p,_,_,_,p,p,p,",
                "_,q,_,Q,_,_,_,q,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,P,_,_,_,",
                "_,_,_,N,K,N,P,_,",
                "P,P,P,P,_,P,_,P,",
                "R,_,_,_,_,_,_,R,"
        };

        testContainsExactlyMoves(Side.WHITE, setup, Arrays.asList(
                new Move(5, 4, 6, 4)
        ));
    }

    @Test
    public void testQueenLeftEdge() throws Exception {
        String[] setup = {
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,p,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "R,_,_,_,q,k,_,K,",
                "_,_,_,_,_,_,_,_,"
        };

        testNumberOfMoves(Side.WHITE, setup, 13);
    }

    @Test
    public void testBishopKingThreat() {
        String[] setup = new String[]{
                "_,_,r,_,_,r,k,_,",
                "_,_,_,_,b,p,p,p,",
                "p,_,_,_,p,n,_,_,",
                "q,p,n,_,N,_,_,_,",
                "_,_,_,_,_,B,_,_,",
                "_,_,N,_,_,_,P,_,",
                "P,P,_,_,P,P,b,P,",
                "_,Q,R,r,_,_,K,_,"
        };

        testContainsMove(Side.WHITE, setup,
                new Move(7, 6, 6, 6)
        );
    }

    @Test
    public void testRookLeftEdge() throws Exception {
        String[] setup = {
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,p,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "R,_,_,_,r,k,_,K,",
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
                "_,_,_,p,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "K,_,k,r,_,_,_,R,",
                "_,_,_,_,_,_,_,_,"
        };

        testNumberOfMoves(Side.WHITE, setup, 13);
    }

    @Test
    public void testRookTopEdge() throws Exception {
        String[] setup = {
                "_,_,_,_,R,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,r,_,_,_,",
                "_,_,_,_,k,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,K,_,_,_,"
        };

        testNumberOfMoves(Side.WHITE, setup, 13);
    }

    @Test
    public void testRookBottomEdge() throws Exception {
        String[] setup = {
                "_,_,_,_,K,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,k,_,_,_,",
                "_,_,_,_,r,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,R,_,_,_,"
        };

        testNumberOfMoves(Side.WHITE, setup, 13);
    }

    @Test
    @Ignore
    public void testEnPassantCausesCheck() throws Exception {
        String[] setup = {
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "K,R,_,_,_,_,_,_,",
                "_,R,_,_,_,p,_,k,",
                "_,R,_,_,_,_,_,_,",
                "_,_,_,_,P,_,_,_,",
                "_,_,_,_,_,_,_,_,"
        };

//              "_,_,_,_,_,_,_,_,",
//              "_,_,_,_,_,_,_,_,",
//              "_,_,_,_,_,_,_,_,",
//              "K,R,_,_,_,_,_,_,",
//              "_,R,_,_,P,p,_,k,",
//              "_,R,_,_,_,_,_,_,",
//              "_,_,_,_,_,_,_,_,",
//              "_,_,_,_,_,_,_,_,"

        testContainsExactlyMoves(Side.WHITE, setup, new Move[]{
                new Move(6, 4, 4, 4, Move.MoveNote.PAWN_LEAP)
        }, Arrays.asList(
                new Move(4, 5, 5, 5, Move.MoveNote.NORMAL),
                new Move(4, 7, 4, 6, Move.MoveNote.NORMAL)
        ));

    }

    @Test
    public void testDrawByThreeRule() throws Exception {
        String[] setup = {
                "_,_,_,_,k,_,_,_,",
                "_,_,_,_,_,_,_,q,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,Q,",
                "_,_,_,_,K,_,_,_,"
        };

        Move[] moves = new Move[]{
                new Move(6, 7, 6, 0),
                new Move(1, 7, 1, 0),
                new Move(6, 0, 6, 7),
                new Move(1, 0, 1, 7),
        };

        Board board = JSONParser.getFromSetup(Side.WHITE, setup);

        assertFalse(board.drawByThreeRule());
        assertThat(board.getHashCodeFreq(), is(equalTo(1)));

        makeMoves(board, moves);

        assertFalse(board.drawByThreeRule());
        assertThat(board.getHashCodeFreq(), is(equalTo(2)));

        makeMoves(board, moves);

        assertThat(board.drawByThreeRule(), is(equalTo(true)));
        assertThat(board.getHashCodeFreq(), is(equalTo(3)));
    }

    @Test
    public void testCopy() {
        Board board = BoardFactory.getStandardChessBoard();
        BoardTestFixture.makeRandomMoves(board, 20);
        Board copyBoard = board.copy();

        assertThat(board.getHashCode(), is(equalTo(copyBoard.getHashCode())));
        assertThat(board.getMoveHistory().toArray(), is(arrayContaining(copyBoard.getMoveHistory().toArray())));

        assertThat(board.getPiecesTakenFor(Side.WHITE).toArray(), is(arrayContaining(copyBoard.getPiecesTakenFor(Side.WHITE).toArray())));
        assertThat(board.getPiecesTakenFor(Side.BLACK).toArray(), is(arrayContaining(copyBoard.getPiecesTakenFor(Side.BLACK).toArray())));
    }

    @Test
    public void testsCastlingRightsKingCastleNear() {
        String[] setup = {
                "r,_,b,_,k,_,_,r,",
                "p,p,_,_,_,p,p,p,",
                "_,q,_,Q,_,_,_,q,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,P,_,_,_,",
                "_,_,_,N,_,N,P,_,",
                "P,P,P,P,_,P,_,P,",
                "R,_,_,_,K,_,_,R,"
        };

        testCastleRights(
                setup, Side.WHITE,
                true, true,
                false, false,
                new Move(7, 4, 7, 6, Move.MoveNote.CASTLE_NEAR)
        );
    }

    @Test
    public void testsCastlingRightsAfterRookTaken() {
        String[] setup = {
                "r,_,b,_,k,_,_,r,",
                "p,p,_,_,_,p,p,p,",
                "_,_,q,Q,_,_,_,q,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,N,_,_,P,_,",
                "P,P,P,P,_,P,_,P,",
                "R,_,_,_,K,N,_,R,"
        };

        testCastleRights(
                setup, Side.WHITE,
                true, true,
                false, true,
                new Move(2, 2, 7, 7, Move.MoveNote.NORMAL, Piece.PieceID.ROOK, 7, 7)
        );
    }

    @Test
    public void testsCastlingRightsKingCastleFar() {
        String[] setup = {
                "r,_,b,_,k,_,_,r,",
                "p,p,_,_,_,p,p,p,",
                "_,q,_,Q,_,_,_,q,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,P,_,_,_,",
                "_,_,_,N,_,N,P,_,",
                "P,P,P,P,_,P,_,P,",
                "R,_,_,_,K,_,_,R,"
        };

        testCastleRights(
                setup, Side.WHITE,
                true, true,
                false, false,
                new Move(7, 4, 7, 1, Move.MoveNote.CASTLE_FAR)
        );
    }

    @Test
    public void testsCastlingRightsNearRookMove() {
        String[] setup = {
                "r,_,b,_,k,_,_,r,",
                "p,p,_,_,_,p,p,p,",
                "_,q,_,Q,_,_,_,q,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,P,_,_,_,",
                "_,_,_,N,_,N,P,_,",
                "P,P,P,P,_,P,_,P,",
                "R,_,_,_,K,_,_,R,"
        };

        testCastleRights(
                setup, Side.WHITE,
                true, true,
                false, true,
                new Move(7, 7, 7, 6)
        );
    }

    @Test
    public void testsCastlingRightsFarRookMove() {
        String[] setup = {
                "r,_,b,_,k,_,_,r,",
                "p,p,_,_,_,p,p,p,",
                "_,q,_,Q,_,_,_,q,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,P,_,_,_,",
                "_,_,_,N,_,N,P,_,",
                "P,P,P,P,_,P,_,P,",
                "R,_,_,_,K,_,_,R,"
        };

        testCastleRights(
                setup, Side.WHITE,
                true, true,
                true, false,
                new Move(7, 0, 7, 1)
        );
    }

    private void testCastleRights(String[] setup, int side,
                                  boolean initNear, boolean initFar,
                                  boolean near, boolean far,
                                  Move move) {
        Board b = JSONParser.getFromSetup(side, setup);

        assertTrue(b.canCastleNear(side) == initNear);
        assertTrue(b.canCastleFar(side) == initFar);

        b.makeMove(move);

        assertTrue(b.canCastleNear(side) == near);
        assertTrue(b.canCastleFar(side) == far);

        b.undoMove();

        assertTrue(b.canCastleNear(side) == initNear);
        assertTrue(b.canCastleFar(side) == initFar);
    }

    private void makeMoves(Board b, Move[] moves) {
        Stream.of(moves)
                .forEach(b::makeMove);
    }

    public void testNumberOfMoves(int side, String[] setup, int numberOfMoves) {
        testNumberOfMoves(side, setup, new Move[0], numberOfMoves);
    }

    public void testNumberOfMoves(int side, String[] setup, Move[] movesToMake, int numberOfMoves) {
        List<Move> moves = getValidMoves(side, setup, movesToMake);
        assertEquals(numberOfMoves, moves.size());
    }

    public void testContainsExactlyMoves(int side, String[] setup, List<Move> expectedMoves) {
        testContainsExactlyMoves(side, setup, new Move[0], expectedMoves);
    }

    public void testContainsExactlyMoves(int side, String[] setup, Move[] movesToMake, List<Move> expectedMoves) {
        List<Move> moves = getValidMoves(side, setup, movesToMake).stream()
                .map(Move::justFromTo)
                .collect(Collectors.toList());
        assertThat(moves, containsInAnyOrder(expectedMoves.toArray()));
    }

    public void testContainsMove(int side, String[] setup, Move expectedMove) {
        testContainsMove(side, setup, new Move[0], expectedMove);
    }

    public void testContainsMove(int side, String[] setup, Move[] movesToMake, Move expectedMove) {
        List<Move> moves = getValidMoves(side, setup, movesToMake).stream()
                .map(Move::justFromTo)
                .collect(Collectors.toList());
        assertThat(moves, hasItem(expectedMove));
    }

    public List<Move> getValidMoves(int side, String[] setup, Move[] movesToMake) {
        Board b1 = JSONParser.getFromSetup(side, setup);
        this.makeMoves(b1, movesToMake);
        b1.makeNullMove();
        return b1.generateValidMoves().toList();
    }

    public long[] getNullMoveInfo(int side, String[] setup) {
        Board b1 = JSONParser.getFromSetup(side, setup);
        return b1.makeNullMove();
    }


    public void verifyBitBoards(Board b) {

        long[][] posBitBoard = b.getPosBitBoard();
        Piece piece;

        long[][] allBitBoard = new long[Piece.PieceID.PIECES_COUNT][2];

        for (int i = 0; i < Piece.PieceID.PIECES_COUNT; i++) {
            allBitBoard[i][0] = posBitBoard[i][0];
            allBitBoard[i][1] = posBitBoard[i][1];
        }

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                piece = b.getPiece(r, c);

                if (piece.getPieceID() != Piece.PieceID.NO_PIECE) {
                    allBitBoard[piece.getPieceID()][piece.getSide()] ^= BitBoard.getMask(r, c);
                }

            }
        }

        for (int i = 0; i < Piece.PieceID.PIECES_COUNT; i++) {
            assertTrue(allBitBoard[i][0] == 0);
            assertTrue(allBitBoard[i][1] == 0);
        }

    }
}
