package com.gordoncaleb;

import com.gordoncaleb.chess.board.Board;
import com.gordoncaleb.chess.util.Perft;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.SampleTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class PerftBenchmark {

    @Param({"0", "1"})
    private int perftNum;
    private int perftDepth = 2;

    private final Board[] perftBoards = new Board[2];
    private final Perft perft = new Perft();
    private final List<List<Long>> moveContainers = new ArrayList<>();

    @Setup
    public void init() {
        perftBoards[0] = perft.standardInitialPosition();
        perftBoards[1] = perft.kiwiPetePosition();

        for (int i = 0; i < 10; i++) {
            moveContainers.add(new ArrayList<>());
        }
    }

    @Benchmark
    @Warmup(iterations = 5)
    @Measurement(iterations = 5)
    public void testPerft() {
        perft.perftBoardRecursiveTimed(perftBoards[perftNum], 0, perftDepth, moveContainers);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(PerftBenchmark.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}
