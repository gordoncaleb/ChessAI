package com.gordoncaleb;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;

import com.gordoncaleb.chess.bitboard.BitBoard;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class KnightBenchmark {

    static {
        BitBoard.loadKnightFootPrints();
    }

    @Benchmark
    public void testKnightFootPrintFromLookup() {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                BitBoard.getKnightFootPrintMem(r, c);
            }
        }
    }

    @Benchmark
    public void testKnightFootPrintFromGen() {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                BitBoard.getKnightFootPrint(r, c);
            }
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
     *    $ java -jar target/benchmarks.jar KnightBenchmark -wi 0 -i 3
     *    (we requested no warmup, 3 measurement iterations)
     *
     * b) Via the Java API:
     *    (see the JMH homepage for possible caveats when running from IDE:
     *      http://openjdk.java.net/projects/code-tools/jmh/)
     */

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(KnightBenchmark.class.getSimpleName())
                .warmupIterations(0)
                .measurementIterations(5)
                .build();

        new Runner(opt).run();
    }

}
