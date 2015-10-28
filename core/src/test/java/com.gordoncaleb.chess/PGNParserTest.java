package com.gordoncaleb.chess;

import com.gordoncaleb.chess.board.Board;
import com.gordoncaleb.chess.board.serdes.PGNParser;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

public class PGNParserTest {
    private static final Logger logger = LoggerFactory.getLogger(PGNParserTest.class);

    PGNParser parser;

    @Before
    public void init() {
        parser = new PGNParser();
    }

    @Test
    public void testFileParse() throws Exception {
        List<PGNParser.PGNGame> exampleGames = parser.loadFile("/pgns/example.pgn");
        assertEquals(6, exampleGames.size());
        assertEquals(exampleGames.get(0).getMetaData().get("ECO"), "C23");
    }

    @Test
    public void testMetaParse() {
        Map<String, String> result = parser.parseMetaData("[WhiteElo \"2260\"]");
        assertEquals(result.get("WhiteElo"), "2260");
    }

    @Test
    public void testGameLineMatch() {
        String line = "1.e4 e6 2.d4 d5 3.Nd2 c5 4.exd5 Qxd5 5.Ngf3 cxd4 6.Bc4 Qd6 7.O-O Nf6 8.Nb3 Nc6";
        assertTrue(parser.isGameLine(line));
        assertTrue(parser.isStartGameLine(line));
    }

    @Test
    public void testLineProcess() throws IOException {
        List<PGNParser.PGNGame> games = parser.parseFileLines(gameLines());
        assertEquals(2, games.size());
    }

    @Test
    public void tesNotationResolution() throws Exception {
        List<PGNParser.PGNGame> games = parser.parseFileLines(gameLines());
        Board board = parser.getPGNGameAsBoard(games.get(0));
        assertEquals(19508493362867960L, board.getHashCode());
    }

    public BufferedReader gameLines() {
        List<String> lines = new ArrayList<>();

        lines.add("");
        lines.add("[WhiteElo \"2260\"]");
        lines.add("[BlackElo \"2455\"]");
        lines.add("[ECO \"C07\"]");
        lines.add("");
        lines.add("1. e4 e6 2. d4 d5 3. Nd2 c5 *");
        lines.add("");
        lines.add("[WhiteElo \"2260\"]");
        lines.add("[BlackElo \"2255\"]");
        lines.add("[ECO \"Z07\"]");
        lines.add("");
        lines.add("1.e4 e6 2.d4 d5 3.Nd2 c5 4.exd5 Qxd5 5.Ngf3 cxd4 6.Bc4 Qd6 7.O-O Nf6 8.Nb3 Nc6");
        lines.add("9.Nbxd4 Nxd4 10.Nxd4 Bd7 11.Be3 Qc7 12.Bd3 O-O-O 13.Qf3 e5 14.Nb5 e4 15.Qf4 Qxf4");

        return new BufferedReader(new StringReader(lines.stream().collect(Collectors.joining("\n"))));

    }

}
