package com.gordoncaleb.chess.unit.util;

import com.gordoncaleb.chess.util.PGNGameLibrary;
import org.junit.Test;

public class GameLibraryTest {

    @Test
    public void testRandomBoardStream(){
        PGNGameLibrary lib = new PGNGameLibrary("../pgnmentor");
        lib.randomBoards()
                .limit(10)
                .forEach(b -> System.out.println(b.toString()));
    }
}

