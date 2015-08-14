package com.gordoncaleb.chess;

import com.gordoncaleb.chess.io.PGNParser;
import org.junit.Test;

public class PGNParserTest {

    @Test
    public void test(){

        PGNParser.moveBookFromPGNFile("/example.pgn");
    }

}
