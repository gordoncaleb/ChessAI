package com.gordoncaleb;


import com.gordoncaleb.chess.board.bitboard.BitBoard;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class CacheVsCalc {

    @Setup
    public void init() {
        BitBoard.loadKnightFootPrints();
    }

    @Benchmark
    @Warmup(iterations = 5, batchSize = 5000)
    @Measurement(iterations = 5, batchSize = 5000)
    public void testKnightFootPrintFromLookup() {
        BitBoard.getKnightFootPrintMem(4, 4);
    }

    @Benchmark
    @Warmup(iterations = 5, batchSize = 5000)
    @Measurement(iterations = 5, batchSize = 5000)
    public void testKnightFootPrintFromGen() {
        BitBoard.getKnightFootPrint(4, 4);
    }

    /*
     * ============================== HOW TO RUN THIS TEST: ====================================
     *    $ mvn clean install
     *    $ java -jar target/benchmarks.jar CacheVsCalc -wi 0 -i 3
     */

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(CacheVsCalc.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }

}

