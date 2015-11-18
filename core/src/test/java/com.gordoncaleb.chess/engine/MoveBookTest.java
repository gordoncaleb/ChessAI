package com.gordoncaleb.chess.engine;

import com.gordoncaleb.chess.board.Move;
import com.gordoncaleb.chess.board.serdes.PGNParser;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MoveBookTest {

    @Test
    public void testCompiler() throws Exception {

        PGNParser parser = new PGNParser();

        Map<Long, List<Move>> moveBook = parser.moveBookFromPGNFile(MoveBook.MOVEBOOK_FILE);

        Map<Long, List<Move>> loadedMoveBook = new MoveBook().loadMoveBook();

        assertEquals(moveBook.size(), loadedMoveBook.size());

        for (Long key : moveBook.keySet()) {

            List<Move> movesFromTo = moveBook.get(key).stream()
                    .map(Move::justFromTo)
                    .collect(Collectors.toList());

            assertTrue("Binary loaded move book is missing key: " + key, loadedMoveBook.containsKey(key));
            List<Move> loadedMoveBookMoves = loadedMoveBook.get(key).stream()
                    .map(Move::justFromTo)
                    .collect(Collectors.toList());

            assertThat(movesFromTo, containsInAnyOrder(loadedMoveBookMoves.toArray()));
        }

    }
}