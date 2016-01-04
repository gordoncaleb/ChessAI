package com.gordoncaleb.chess.unit.board;

import com.gordoncaleb.chess.board.Move;
import com.gordoncaleb.chess.board.SortableMoveContainer;
import com.gordoncaleb.chess.board.pieces.Piece;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

public class SortableMoveContainerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SortableMoveContainerTest.class);

    @Test
    public void testMoveOrdering() {
        SortableMoveContainer mc = new SortableMoveContainer();

        Move moveA = new Move(0, 0, 0, 1, Move.MoveNote.CASTLE_FAR);
        Move moveB = new Move(1, 0, 0, 1, Move.MoveNote.PAWN_LEAP);
        Move moveC = new Move(2, 0, 0, 1, Move.MoveNote.NORMAL, Piece.PieceID.NO_PIECE);
        Move moveD = new Move(3, 0, 0, 1, Move.MoveNote.NORMAL, Piece.PieceID.PAWN);
        Move moveE = new Move(4, 0, 0, 1, Move.MoveNote.NORMAL, Piece.PieceID.KNIGHT);
        Move moveF = new Move(5, 0, 0, 1, Move.MoveNote.NORMAL, Piece.PieceID.BISHOP);
        Move moveG = new Move(6, 0, 0, 1, Move.MoveNote.NORMAL, Piece.PieceID.ROOK);
        Move moveH = new Move(7, 0, 0, 1, Move.MoveNote.NORMAL, Piece.PieceID.QUEEN);

        mc.prioritizeMove(moveA.toLong(), 1);
        mc.prioritizeMove(moveB.toLong(), 2);

        mc.add(moveA);
        mc.add(moveB);
        mc.add(moveC);
        mc.add(moveD);
        mc.add(moveE);
        mc.add(moveF);
        mc.add(moveG);
        mc.add(moveH);

        mc.sort();

        List<Move> result = mc.toList();

        result.forEach(m -> LOGGER.info(m.getFromRow() + ""));

        //1, 0, 7, 6, 5, 4, 3, 2
        assertThat(mc.toList(), is(equalTo(Arrays.asList(moveB, moveA, moveH, moveG, moveF, moveE, moveD, moveC))));
    }
}
