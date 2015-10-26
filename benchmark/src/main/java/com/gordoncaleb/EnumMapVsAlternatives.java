package com.gordoncaleb;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.EnumMap;
import java.util.concurrent.TimeUnit;

import static com.gordoncaleb.EnumMapVsAlternatives.TestEnum.*;


@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class EnumMapVsAlternatives {

    public enum TestEnum {
        ONE, TWO
    }

    public static final int A = 0;
    public static final int B = 1;

    EnumMap<TestEnum, Long> sideLongEnumMap = new EnumMap<>(TestEnum.class);
    long[] sideLongArray = new long[TestEnum.values().length];

    @Param({"1", "10000"})
    long t;

    @Setup
    public void init() {
        sideLongEnumMap = new EnumMap<>(TestEnum.class);
        sideLongArray = new long[2];
    }

    @Benchmark
    @Warmup(iterations = 5, batchSize = 5000)
    @Measurement(iterations = 5, batchSize = 5000)
    public long testEnumMapAccess() {
        sideLongEnumMap.put(ONE, t);
        long r = sideLongEnumMap.get(ONE);
        sideLongEnumMap.put(TWO, t);
        return sideLongEnumMap.get(TWO);
    }

    @Benchmark
    @Warmup(iterations = 5, batchSize = 5000)
    @Measurement(iterations = 5, batchSize = 5000)
    public long testEnumArrayWithOrdinal() {
        sideLongArray[ONE.ordinal()] = t;
        long r = sideLongArray[ONE.ordinal()];
        sideLongArray[TWO.ordinal()] = t;
        return sideLongArray[TWO.ordinal()];
    }

    @Benchmark
    @Warmup(iterations = 5, batchSize = 5000)
    @Measurement(iterations = 5, batchSize = 5000)
    public long testEnumArrayWithInt() {
        sideLongArray[A] = t;
        long r = sideLongArray[A];
        sideLongArray[B] = t;
        return sideLongArray[B];
    }

    /*
     * ============================== HOW TO RUN THIS TEST: ====================================
     *    $ mvn clean install
     *    $ java -jar target/benchmarks.jar CacheVsCalc -wi 0 -i 3
     */

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(EnumMapVsAlternatives.class.getSimpleName())
                .forks(1)
                .threads(4)
                .build();

        new Runner(opt).run();
    }

}