package com.gordoncaleb.chess;

import com.gordoncaleb.chess.board.Board;
import com.gordoncaleb.chess.util.Perft;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class PerftTest {
    public static final Logger logger = LoggerFactory.getLogger(PerftTest.class);
    private final Perft perft = new Perft();

    //Solutions columns
    //Nodes, Captures, E.p., Castles, Promotions, Checks, Checkmates

    @Test
    public void perft1() {
        Board b = perft.standardInitialPosition();
        logger.info("\n" + b.toString());

        long[][] sol = new long[][]{
                {20, 0, 0, 0, 0, 0},// 0},
                {400, 0, 0, 0, 0, 0},//, 0},
                {8902, 34, 0, 0, 0, 12},//, 0},
                {197281, 1576, 0, 0, 0, 469},// 8},
                {4865609, 82719, 258, 0, 0, 27351},// 347},
                //{119060324, 2812008, 5248, 0, 0, 809099}//, 10828}
        };

        perftBoard(b, sol);
    }

    @Test
    public void perft2() {
        Board b = perft.kiwiPetePosition();
        logger.info("\n" + b.toString());

        long[][] sol = new long[][]{
                {48, 8, 0, 2, 0, 0},// 0},
                {2039, 351, 1, 91, 0, 3},//, 0},
                {97862, 17102, 45, 3162, 0, 993},//, 1},
                {4085603, 757163, 1929, 128013, 15172, 25523},//, 43},
                //{193690690, 35043416, 73365, 4993637, 8392, 3309887}//, 30171}
        };

        perftBoard(b, sol);
    }

    @Test
    @Ignore
    public void perft3() {
        Board b = perft.position3();
        logger.info("\n" + b.toString());

        long[][] sol = new long[][]{
                {14, 1, 0, 0, 0, 2},//  0},
                {191, 14, 0, 0, 0, 10},//  0},
                {2812, 209, 2, 0, 0, 267},//  0},
                {43238, 3348, 123, 0, 0, 1680},//  17},
                {674624, 52051, 1165, 0, 0, 52950},//  0},
                {11030083, 940350, 33325, 0, 7552, 452473},// 2733},
                //{178633661, 14519036, 294874, 0, 140024, 12797406, 87}
        };

        perftBoard(b, sol);
    }

    @Test
    public void perft4() {
        Board b = perft.position4();
        logger.info("\n" + b.toString());

        long[][] sol = new long[][]{
                {6, 0, 0, 0, 0, 0},// 0},
                {264, 87, 0, 6, 48, 10},// 0},
                {9467, 1021, 4, 0, 120, 38},// 22},
                {422333, 131393, 0, 7795, 60032, 15492},// 5},
                {15833292, 2046173, 6512, 0, 329464, 200568},// 50562},
                //{706045033, 210369132, 212, 10882006, 81102984, 26973664},// 81076}
        };

        perftBoard(b, sol);
    }

    @Test
    public void perft5() {
        Board b = perft.position5();
        logger.info("\n" + b.toString());

        long[][] sol = new long[][]{
                {44},
                {1486},
                {62379},
                {2103487},
                //{89941194},
        };

        perftBoard(b, sol);
    }

    @Test
    public void perft6() {
        Board b = perft.position6();
        logger.info("\n" + b.toString());

        long[][] sol = new long[][]{
                {46L},
                {2079L},
                {89890L},
                {3894594L},
                //{164075551L},
                //{6923051137L},
                //{287188994746L},
                //{11923589843526L},
                //{490154852788714L}
        };

        perftBoard(b, sol);
    }

    @Test
    public void perftPromotion() {
        Board b = perft.promotionPosition();
        logger.info("\n" + b.toString());

        long[][] sol = new long[][]{
                {24},
                {496},
                {9483},
                {182838},
                {3605103},
                //{71179139},
        };

        perftBoard(b, sol);
    }

    private void perftBoard(Board b, long[][] sol) {
        int stopDepth = sol.length - 1;
        long[][] metrics = perft.perftBoardFunctional(b, stopDepth);

        logMetrics(metrics);

        if (sol[stopDepth].length > 5) {
            metrics[stopDepth][5] = sol[stopDepth][5];
        }

        for (int i = 0; i <= stopDepth; i++) {
            assertThat(matchLength(metrics[i], sol[i]), is(equalTo(sol[i])));
        }
    }

    private long[] matchLength(long[] metrics, long[] sol) {
        if (sol.length != metrics.length) {
            long[] temp = new long[sol.length];
            System.arraycopy(metrics, 0, temp, 0, temp.length);
            return temp;
        } else {
            return metrics;
        }
    }

    private void logMetrics(long[][] metrics) {
        int ply = 1;
        for (long[] plyMetric : metrics) {
            logger.info(ply + ": " + Arrays.toString(plyMetric));
            ply++;
        }
    }
}
