package com.gordoncaleb.chess;

import com.gordoncaleb.chess.backend.Move;
import com.gordoncaleb.chess.io.MoveBook;
import com.gordoncaleb.chess.io.PGNParser;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;

public class MoveBookTest {

    @Test
    public void testCompiler() throws Exception {

        PGNParser parser = new PGNParser();

        Map<Long, List<Long>> moveBook = parser.moveBookFromPGNFile("book.pgn");

        Map<Long, List<Long>> loadedMoveBook = new MoveBook().loadMoveBook();

        assertEquals(moveBook.size(), loadedMoveBook.size());

        for (Long key : moveBook.keySet()) {
            assertEquals(new HashSet<>(moveBook.get(key)), new HashSet<>(loadedMoveBook.get(key)));
        }

    }
}