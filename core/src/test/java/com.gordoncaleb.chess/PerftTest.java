package com.gordoncaleb.chess;

import com.gordoncaleb.chess.board.Board;
import com.gordoncaleb.chess.board.BoardFactory;
import com.gordoncaleb.chess.board.serdes.JSONParser;
import com.gordoncaleb.chess.util.Perft;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class PerftTest {
    public static final Logger logger = LoggerFactory.getLogger(PerftTest.class);
    private final Perft perft = new Perft();

    public Board standardInitialPosition() {
        return BoardFactory.getStandardChessBoard();
    }

    public Board kiwiPetePosition() {
        String fen = "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -";
        return JSONParser.fromFEN(fen);
    }

    public Board position3() {
        String fen = "8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - -";
        return JSONParser.fromFEN(fen);
    }

    public Board position4() {
        String fen = "r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1";
        return JSONParser.fromFEN(fen);
    }

    public Board position5() {
        String fen = "rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8 ";
        return JSONParser.fromFEN(fen);
    }

    //Solutions columns
    //Nodes, Captures, E.p., Castles, Promotions, Checks, Checkmates

    @Test
    public void perft1() {
        Board b = standardInitialPosition();
        logger.info("\n" + b.toString());

        int[][] sol = new int[][]{
                {20, 0, 0, 0, 0},//, 0, 0},
                {400, 0, 0, 0, 0},//, 0, 0},
                {8902, 34, 0, 0, 0},//, 12, 0},
                {197281, 1576, 0, 0, 0},//, 469, 8},
                {4865609, 82719, 258, 0, 0},//, 27351, 347},
                //{119060324, 2812008, 5248, 0, 0, 809099, 10828}
        };

        perftBoard(b, sol);
    }

    @Test
    public void perft2() {
        Board b = kiwiPetePosition();
        logger.info("\n" + b.toString());

        int[][] sol = new int[][]{
                {48, 8, 0, 2, 0},//, 0, 0},
                {2039, 351, 1, 91, 0},//, 3, 0},
                {97862, 17102, 45, 3162, 0},//, 993, 1},
                //{4085603, 757163, 1929, 128013, 15172, 25523, 43},
                //{193690690, 35043416, 73365, 4993637, 8392, 3309887, 30171}
        };

        perftBoard(b, sol);
    }

    @Test
    public void perft5() {
        Board b = position5();
        logger.info("\n" + b.toString());

        int[][] sol = new int[][]{
                {44},
                //{1486},
                //{62379},
                //{2103487},
                //{89941194},
        };

        perftBoard(b, sol);
    }

    private void perftBoard(Board b, int[][] sol) {
        int stopDepth = sol.length - 1;
        int[][] metrics = perft.perftBoardFunctional(b, stopDepth);

        logMetrics(metrics);

        for (int i = 0; i < stopDepth + 1; i++) {
            for (int c = 0; c < sol[i].length; c++) {
                assertThat(metrics[i][c], is(equalTo(sol[i][c])));
            }
        }
    }

    private void logMetrics(int[][] metrics) {
        int ply = 1;
        for (int[] plyMetric : metrics) {
            logger.info(ply + ": " + Arrays.toString(plyMetric));
            ply++;
        }
    }
}
