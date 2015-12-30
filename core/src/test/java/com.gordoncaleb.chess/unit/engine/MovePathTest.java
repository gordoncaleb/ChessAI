package com.gordoncaleb.chess.unit.engine;

import com.gordoncaleb.chess.board.Move;
import com.gordoncaleb.chess.board.MoveContainer;
import com.gordoncaleb.chess.board.MoveContainerFactory;
import com.gordoncaleb.chess.engine.MovePath;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

public class MovePathTest {

    private Move moveA = new Move(0, 1, 2, 3, Move.MoveNote.NORMAL);
    private Move moveB = new Move(3, 0, 1, 2, Move.MoveNote.NORMAL);
    private Move moveC = new Move(2, 3, 0, 1, Move.MoveNote.NORMAL);
    private Move moveD = new Move(1, 2, 3, 0, Move.MoveNote.NORMAL);

    private int indexA = 0;
    private int indexB = 1;
    private int indexC = 2;
    private int indexD = 3;

    @Test
    public void testMoveMarking() {

        MovePath movePath = movePathTestFixture();
        final int maxDepth = 1;
        movePath.markMove(0, maxDepth, indexA);
        movePath.setDepth(maxDepth);

        List<Move> expected = new ArrayList<>();
        expected.add(moveA);

        assertThat(movePath.asList(), is(equalTo(expected)));
    }

    @Test
    public void testMoveMarking2() {

        MovePath movePath = movePathTestFixture();
        final int maxDepth = 2;
        movePath.markMove(1, maxDepth, indexA);
        movePath.markMove(0, maxDepth, indexB);
        movePath.setDepth(maxDepth);

        List<Move> expected = new ArrayList<>();
        expected.add(moveB);
        expected.add(moveA);

        assertThat(movePath.asList(), is(equalTo(expected)));
    }

    @Test
    public void testMoveMarking3() {

        MovePath movePath = movePathTestFixture();
        final int maxDepth = 3;
        movePath.markMove(2, maxDepth, indexA);
        movePath.markMove(2, maxDepth, indexB);
        movePath.markMove(1, maxDepth, indexB);
        movePath.markMove(2, maxDepth, indexC);
        movePath.markMove(1, maxDepth, indexD);
        movePath.markMove(0, maxDepth, indexC);
        movePath.setDepth(maxDepth);

        assertThat(movePath.asList(), is(equalTo(Arrays.asList(
                moveC,
                moveD,
                moveC
        ))));
    }

    @Test
    public void testMoveMarkingIterativeDeepening() {

        MovePath movePath = movePathTestFixture();

        //it = 1
        movePath.markMove(0, 1, indexA); // [A]
        movePath.setDepth(1);

        assertThat(movePath.asList(), is(equalTo(Arrays.asList(
                moveA
        ))));

        //it = 2
        movePath.markMove(1, 2, indexB);
        movePath.markMove(1, 2, indexC);
        movePath.markMove(0, 2, indexA); // [A, C]
        movePath.setDepth(2);

        assertThat(movePath.asList(), is(equalTo(Arrays.asList(
                moveA,
                moveC
        ))));

        //it = 3
        movePath.markMove(2, 3, indexC);
        movePath.markMove(2, 3, indexB);
        movePath.markMove(1, 3, indexD);
        movePath.markMove(2, 3, indexB);
        movePath.markMove(1, 3, indexA);
        movePath.markMove(0, 3, indexC); //[C, A, B]
        movePath.markMove(2, 3, indexC);
        movePath.markMove(2, 3, indexB);
        movePath.markMove(1, 3, indexA);
        movePath.setDepth(3);

        assertThat(movePath.asList(), is(equalTo(Arrays.asList(
                moveC,
                moveA,
                moveB
        ))));
    }

    private MovePath movePathTestFixture() {
        MoveContainer[] moveContainers = MoveContainerFactory.buildMoveContainers(5);
        MovePath movePath = new MovePath(moveContainers);

        addMoves(moveContainers[0]);
        addMoves(moveContainers[1]);
        addMoves(moveContainers[2]);
        addMoves(moveContainers[3]);
        return movePath;
    }

    private void addMoves(MoveContainer moveContainer) {
        moveContainer.add(moveA);
        moveContainer.add(moveB);
        moveContainer.add(moveC);
        moveContainer.add(moveD);
    }

}
