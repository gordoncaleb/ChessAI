package com.gordoncaleb.chess.engine;

import com.gordoncaleb.chess.board.Move;
import com.gordoncaleb.chess.board.serdes.PGNParser;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class MoveBookTest {

    @Test
    public void testCompiler() throws Exception {

        PGNParser parser = new PGNParser();

        Map<Long, List<Long>> moveBook = parser.moveBookFromPGNFile(MoveBook.MOVEBOOK_FILE);

        Map<Long, List<Long>> loadedMoveBook = new MoveBook().loadMoveBook();

        assertEquals(moveBook.size(), loadedMoveBook.size());

        for (Long key : moveBook.keySet()) {
            List<Long> movesFromTo = moveBook.get(key).stream()
                    .map(l -> l & Move.fromToMask)
                    .collect(Collectors.toList());

            assertEquals(new TreeSet<>(movesFromTo), new TreeSet<>(loadedMoveBook.get(key)));
        }

    }
}