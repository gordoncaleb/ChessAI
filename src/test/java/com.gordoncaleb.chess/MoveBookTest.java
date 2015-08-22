package com.gordoncaleb.chess;

import com.gordoncaleb.chess.io.MoveBook;
import com.gordoncaleb.chess.io.PGNParser;
import org.junit.Ignore;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;

public class MoveBookTest {

    @Test @Ignore
    public void testCompiler() throws Exception {

        PGNParser parser = new PGNParser();

        Map<Long, List<Long>> moveBook = parser.moveBookFromPGNFile("/example.pgn");

        Map<Long, List<Long>> loadedMoveBook = new MoveBook().loadMoveBook();

        assertEquals(moveBook.size(), loadedMoveBook.size());

        for (Long key : moveBook.keySet()) {
            assertEquals(new HashSet<>(moveBook.get(key)), new HashSet<>(loadedMoveBook.get(key)));
        }

    }
}