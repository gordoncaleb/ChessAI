package com.gordoncaleb;

import com.gordoncaleb.chess.backend.Board;
import com.gordoncaleb.chess.backend.Side;
import com.gordoncaleb.chess.bitboard.BitBoard;
import com.gordoncaleb.chess.persistence.BoardDAO;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class MoveGenerationBenchmark {

    private static final int N = 100000;

    private Board b1;

    @Setup
    public void init() {
        String[] setup = {
                "R,_,_,_,_,_,Q,_,",
                "_,P,_,N,_,B,_,_,",
                "_,_,_,_,_,_,K,_,",
                "_,P,_,_,P,_,p,P,",
                "_,_,_,_,p,_,k,p,",
                "_,p,_,_,_,_,_,_,",
                "p,b,_,n,_,q,_,_,",
                "r,_,_,_,_,_,_,r,"
        };

        BoardDAO boardDAO = new BoardDAO();
        b1 = boardDAO.getFromSetup(Side.WHITE, setup);
    }

    @Benchmark
    public void testMoveGeneration1() {
        for (int i = 0; i < N; i++) {
            b1.makeNullMove();
            b1.generateValidMoves();
        }
    }

    /*
     * ============================== HOW TO RUN THIS TEST: ====================================
     *
     * Note the baseline is random within [0..1000] msec; and both forked runs
     * are estimating the average 500 msec with some confidence.
     *
     * You can run this test:
     *
     * a) Via the command line:
     *    $ mvn clean install
     *    $ java -jar target/benchmarks.jar MoveGenerationBenchmark -wi 0 -i 3
     *    (we requested no warmup, 3 measurement iterations)
     *
     * b) Via the Java API:
     *    (see the JMH homepage for possible caveats when running from IDE:
     *      http://openjdk.java.net/projects/code-tools/jmh/)
     */

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(MoveGenerationBenchmark.class.getSimpleName())
                .warmupIterations(0)
                .measurementIterations(5)
                .build();

        new Runner(opt).run();
    }

}

