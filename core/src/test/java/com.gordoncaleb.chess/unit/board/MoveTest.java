package com.gordoncaleb.chess.unit.board;

import com.gordoncaleb.chess.board.Move;
import com.gordoncaleb.chess.board.pieces.Piece;
import org.junit.Test;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

public class MoveTest {

    @Test
    public void testToLongFromLong() {
        Move m = new Move(1, 2, 3, 4, Move.MoveNote.EN_PASSANT, Piece.PieceID.KNIGHT, 6, 7);

        long mLong = m.toLong();

        Move m2 = Move.fromLong(mLong);

        assertEquals(m, m2);
    }

    @Test
    public void testToLongFromLong2() {
        Move m = new Move(7, 7, 0, 0, Move.MoveNote.NEW_ROOK, Piece.PieceID.PAWN, 0, 5);

        long mLong = m.toLong();

        Move m2 = Move.fromLong(mLong);

        assertEquals(m, m2);
    }

    @Test
    public void testGetNewPieceId() {
        Move m = new Move(7, 7, 0, 0, Move.MoveNote.NEW_ROOK, Piece.PieceID.PAWN, 0, 5);
        assertTrue(m.hasPromotion());
        assertEquals(m.promotionChoice(), Piece.PieceID.ROOK);
    }
}
