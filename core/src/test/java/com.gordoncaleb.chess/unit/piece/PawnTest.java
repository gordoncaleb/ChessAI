package com.gordoncaleb.chess.unit.piece;

import com.gordoncaleb.chess.board.Board;
import com.gordoncaleb.chess.board.Move;
import com.gordoncaleb.chess.board.MoveContainer;
import com.gordoncaleb.chess.board.Side;
import com.gordoncaleb.chess.board.pieces.Pawn;
import com.gordoncaleb.chess.board.serdes.JSONParser;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class PawnTest {
    public static final Logger logger = LoggerFactory.getLogger(PawnTest.class);

    @Test
    public void testPawnLeap() {
        String[] setup = {
                "_,_,_,_,k,_,_,_,",
                "_,_,_,_,p,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,P,_,_,_,",
                "_,_,_,_,K,_,_,_,"
        };

        Board b = JSONParser.getFromSetup(Side.WHITE, setup);

        testMovesExactly(b, 6, 4, new Move[]{
                new Move(6, 4, 5, 4, Move.MoveNote.NORMAL),
                new Move(6, 4, 4, 4, Move.MoveNote.PAWN_LEAP)
        });
    }

    @Test
    public void testPawnLeap2() {
        String[] setup = {
                "_,_,_,_,k,_,_,_,",
                "_,_,_,_,p,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,P,_,_,_,",
                "_,_,_,_,K,_,_,_,"
        };

        Board b = JSONParser.getFromSetup(Side.BLACK, setup);

        testMovesExactly(b, 1, 4, new Move[]{
                new Move(1, 4, 2, 4, Move.MoveNote.NORMAL),
                new Move(1, 4, 3, 4, Move.MoveNote.PAWN_LEAP)
        });
    }

    @Test
    public void testPawnAttack() {
        String[] setup = {
                "_,_,_,_,k,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,p,_,p,_,_,",
                "_,_,_,_,P,_,_,_,",
                "_,_,_,_,K,_,_,_,"
        };

        Board b = JSONParser.getFromSetup(Side.WHITE, setup);

        testMovesExactly(b, 6, 4, new Move[]{
                new Move(6, 4, 5, 4, Move.MoveNote.NORMAL),
                new Move(6, 4, 4, 4, Move.MoveNote.PAWN_LEAP),
                new Move(6, 4, 5, 5, Move.MoveNote.NORMAL, b.getPiece(5, 5)),
                new Move(6, 4, 5, 3, Move.MoveNote.NORMAL, b.getPiece(5, 3))
        });
    }

    @Test
    public void testPawnObstructed() {
        String[] setup = {
                "_,_,_,_,k,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,p,N,p,_,_,",
                "_,_,_,_,P,_,_,_,",
                "_,_,_,_,K,_,_,_,"
        };

        Board b = JSONParser.getFromSetup(Side.WHITE, setup);

        testMovesExactly(b, 6, 4, new Move[]{
                new Move(6, 4, 5, 5, Move.MoveNote.NORMAL, b.getPiece(5, 5)),
                new Move(6, 4, 5, 3, Move.MoveNote.NORMAL, b.getPiece(5, 3))
        });
    }

    @Test
    public void testPawnObstructed2() {
        String[] setup = {
                "_,_,_,_,k,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,N,_,_,_,",
                "_,_,_,p,_,p,_,_,",
                "_,_,_,_,P,_,_,_,",
                "_,_,_,_,K,_,_,_,"
        };

        Board b = JSONParser.getFromSetup(Side.WHITE, setup);

        testMovesExactly(b, 6, 4, new Move[]{
                new Move(6, 4, 5, 4, Move.MoveNote.NORMAL),
                new Move(6, 4, 5, 5, Move.MoveNote.NORMAL, b.getPiece(5, 5)),
                new Move(6, 4, 5, 3, Move.MoveNote.NORMAL, b.getPiece(5, 3))
        });
    }

    @Test
    public void testPawnEnpassant() {
        String[] setup = {
                "_,_,_,_,k,_,_,_,",
                "_,_,_,_,_,_,_,p,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,P,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,K,_,_,_,"
        };
        Board b = JSONParser.getFromSetup(Side.BLACK, setup);
        b.makeNullMove();
        b.generateValidMoves();
        b.makeMove(new Move(1, 7, 3, 7, Move.MoveNote.PAWN_LEAP));

        testMovesExactly(b, 3, 6, new Move[]{
                new Move(3, 6, 2, 7, Move.MoveNote.EN_PASSANT, b.getPiece(3, 7)),
                new Move(3, 6, 2, 6, Move.MoveNote.NORMAL),
        });
    }

    @Test
    public void testPawnEnpassantInvalid() {
        String[] setup = {
                "_,_,_,_,k,_,q,_,",
                "_,_,_,_,_,_,_,p,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,P,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,K,_,"
        };
        Board b = JSONParser.getFromSetup(Side.BLACK, setup);
        b.makeNullMove();
        b.generateValidMoves();
        b.makeMove(new Move(1, 7, 3, 7, Move.MoveNote.PAWN_LEAP));

        testMovesExactly(b, 3, 6, new Move[]{
                new Move(3, 6, 2, 6, Move.MoveNote.NORMAL),
        });
    }

    @Test
    public void testPawnQueening() {
        String[] setup = {
                "_,_,_,_,k,_,_,r,",
                "_,_,_,_,_,_,P,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,K,_,_,_,"
        };
        Board b = JSONParser.getFromSetup(Side.WHITE, setup);

        testMovesExactly(b, 1, 6, new Move[]{
                new Move(1, 6, 0, 6, Move.MoveNote.NEW_QUEEN),
                new Move(1, 6, 0, 7, Move.MoveNote.NEW_QUEEN, b.getPiece(0, 7)),
        });
    }

    @Test
    public void testPawnBlocking() {
        String[] setup = {
                "_,_,_,_,k,_,_,q,",
                "_,_,_,_,_,_,P,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "K,_,_,_,_,_,_,_,"
        };
        Board b = JSONParser.getFromSetup(Side.WHITE, setup);

        testMovesExactly(b, 1, 6, new Move[]{
                new Move(1, 6, 0, 7, Move.MoveNote.NEW_QUEEN,b.getPiece(0, 7)),
        });
    }

    @Test
    public void testPawnBlocking2() {
        String[] setup = {
                "_,_,_,_,k,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,q,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,P,_,_,_,",
                "_,_,_,K,_,_,_,_,"
        };
        Board b = JSONParser.getFromSetup(Side.WHITE, setup);

        testMovesExactly(b, 6, 4, new Move[]{});
    }

    @Test
    public void testPawnBlocking3() {
        String[] setup = {
                "_,_,_,_,_,_,_,k,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,p,_,_,_,_,_,_,",
                "Q,_,_,_,K,_,_,_,"
        };
        Board b = JSONParser.getFromSetup(Side.BLACK, setup);

        testMovesExactly(b, 6, 1, new Move[]{
                new Move(6, 1, 7, 0, Move.MoveNote.NEW_QUEEN, b.getPiece(7, 0)),
        });
    }

    private void testMovesExactly(Board b, int pRow, int pCol, Move[] expectedMoves) {
        long[] nullMoveInfo = b.makeNullMove();
        List<Move> moves = Pawn.generateValidMoves(b.getPiece(pRow, pCol), b, nullMoveInfo, b.getAllPosBitBoard(), new MoveContainer(20)).toList();
        assertThat(moves, containsInAnyOrder(expectedMoves));
    }

}
