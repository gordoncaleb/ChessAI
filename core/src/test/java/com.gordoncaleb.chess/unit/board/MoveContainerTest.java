package com.gordoncaleb.chess.unit.board;

import com.gordoncaleb.chess.board.Move;
import com.gordoncaleb.chess.board.MoveContainer;
import com.gordoncaleb.chess.board.pieces.Piece;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class MoveContainerTest {

    @Test
    public void testMoveAddition() {
        MoveContainer moveContainer = new MoveContainer();

        moveContainer.add(0, 0, 7, 7, Move.MoveNote.NEW_ROOK, Piece.PieceID.NO_PIECE, 7, 7);

        assertThat(moveContainer.size(), is(equalTo(1)));
        assertThat(moveContainer.pop(), is(equalTo(
                new Move(0, 0, 7, 7, Move.MoveNote.NEW_ROOK, Piece.PieceID.NO_PIECE, 7, 7)
        )));
    }

    @Test
    public void testMoveAddition2() {
        MoveContainer moveContainer = new MoveContainer();

        moveContainer.add(1, 2, 3, 4, Move.MoveNote.NORMAL);

        assertThat(moveContainer.size(), is(equalTo(1)));
        assertThat(moveContainer.pop(), is(equalTo(
                new Move(1, 2, 3, 4, Move.MoveNote.NORMAL)
        )));
    }

    @Test
    public void testMoveAddition3() {
        MoveContainer moveContainer = new MoveContainer();

        Move moveA = new Move(1, 2, 3, 4, Move.MoveNote.NORMAL);
        Move moveB = new Move(0, 0, 7, 7, Move.MoveNote.NEW_ROOK, Piece.PieceID.NO_PIECE, 7, 7);
        moveContainer.add(moveA);
        moveContainer.add(moveB);

        assertThat(moveContainer.size(), is(equalTo(2)));
        assertThat(moveContainer.pop(), is(equalTo(moveB)));
        assertThat(moveContainer.pop(), is(equalTo(moveA)));
    }

    @Test
    public void testMoveAddition4() {
        MoveContainer moveContainer = new MoveContainer();

        Move moveA = new Move(1, 2, 3, 4, Move.MoveNote.NORMAL);
        Move moveB = new Move(0, 0, 7, 7, Move.MoveNote.NEW_ROOK, Piece.PieceID.NO_PIECE, 7, 7);
        moveContainer.add(moveA);
        moveContainer.add(moveB);

        assertThat(moveContainer.size(), is(equalTo(2)));
        assertThat(moveContainer.get(0), is(equalTo(moveA)));
        assertThat(moveContainer.get(1), is(equalTo(moveB)));
    }
}
