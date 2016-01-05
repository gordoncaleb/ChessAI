package com.gordoncaleb.chess.unit.board;

import com.gordoncaleb.chess.board.Move;
import com.gordoncaleb.chess.board.MoveContainer;
import com.gordoncaleb.chess.board.SimpleMoveContainer;
import com.gordoncaleb.chess.board.pieces.Piece;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class MoveContainerTest {

    @Test
    public void testMoveAddition() {
        MoveContainer moveContainer = new SimpleMoveContainer();

        moveContainer.add(0, 0, 7, 7, Move.MoveNote.NEW_ROOK, Piece.PieceID.NO_PIECE, 7, 7);

        assertThat(moveContainer.size(), is(equalTo(1)));
        assertThat(moveContainer.get(0), is(equalTo(
                new Move(0, 0, 7, 7, Move.MoveNote.NEW_ROOK, Piece.PieceID.NO_PIECE, 7, 7)
        )));
    }

    @Test
    public void testMoveAddition2() {
        MoveContainer moveContainer = new SimpleMoveContainer();

        moveContainer.add(1, 2, 3, 4, Move.MoveNote.NORMAL);

        assertThat(moveContainer.size(), is(equalTo(1)));
        assertThat(moveContainer.get(0), is(equalTo(
                new Move(1, 2, 3, 4, Move.MoveNote.NORMAL)
        )));
    }

    @Test
    public void testMoveAddition3() {
        MoveContainer moveContainer = new SimpleMoveContainer();

        Move moveA = new Move(1, 2, 3, 4, Move.MoveNote.NORMAL);
        Move moveB = new Move(0, 0, 7, 7, Move.MoveNote.NEW_ROOK, Piece.PieceID.NO_PIECE, 7, 7);
        moveContainer.add(moveA);
        moveContainer.add(moveB);

        assertThat(moveContainer.size(), is(equalTo(2)));
        assertThat(moveContainer.get(1), is(equalTo(moveB)));
        assertThat(moveContainer.get(0), is(equalTo(moveA)));
    }

    @Test
    public void testMovePrioritization() {
        MoveContainer moveContainer = new SimpleMoveContainer();

        Move moveA = new Move(1, 2, 3, 4, Move.MoveNote.NORMAL);
        Move moveB = new Move(0, 0, 7, 7, Move.MoveNote.NEW_ROOK, Piece.PieceID.NO_PIECE, 7, 7);

        moveContainer.prioritizeMove(moveB.toLong(), 1);

        moveContainer.add(moveA);
        moveContainer.add(moveA);
        moveContainer.add(moveA);
        moveContainer.add(moveB);
        moveContainer.add(moveA);
        moveContainer.add(moveA);

        assertThat(moveContainer.size(), is(equalTo(6)));
        assertThat(moveContainer.get(0), is(equalTo(moveB)));
        assertThat(moveContainer.get(1), is(equalTo(moveA)));
    }
}
