package com.gordoncaleb;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class BitFunctionsBenchmark {

    @Param({"1234", "12312410982"})
    long num;

    @Benchmark
    @Warmup(iterations = 2, batchSize = 500000)
    @Measurement(iterations = 5, batchSize = 500000)
    public int testLeadingZeros() {
        return Long.numberOfLeadingZeros(num);
    }

    @Benchmark
    @Warmup(iterations = 2, batchSize = 500000)
    @Measurement(iterations = 5, batchSize = 500000)
    public int testTrailingZeros() {
        return Long.numberOfTrailingZeros(num);
    }

    @Benchmark
    @Warmup(iterations = 2, batchSize = 500000)
    @Measurement(iterations = 5, batchSize = 500000)
    public long testHighestBit() {
        return Long.highestOneBit(num);
    }

    @Benchmark
    @Warmup(iterations = 2, batchSize = 500000)
    @Measurement(iterations = 5, batchSize = 500000)
    public long testLowestBit() {
        return Long.lowestOneBit(num);
    }

    @Benchmark
    @Warmup(iterations = 2, batchSize = 500000)
    @Measurement(iterations = 5, batchSize = 500000)
    public long testLowestBitImpl() {
        return (num - 1) ^ num;
    }

    @Benchmark
    @Warmup(iterations = 2, batchSize = 500000)
    @Measurement(iterations = 5, batchSize = 500000)
    public long testImpl() {
        return (num+1);
    }


    /*
     * ============================== HOW TO RUN THIS TEST: ====================================
     *    $ mvn clean install
     *    $ java -jar target/benchmarks.jar BitFunctionsBenchmark -wi 0 -i 3
     */

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(BitFunctionsBenchmark.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }

}

