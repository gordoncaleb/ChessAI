package com.gordoncaleb;

import com.gordoncaleb.chess.board.*;
import com.gordoncaleb.chess.util.Perft;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.SampleTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class PerftBenchmark {

    @Param({"0", "1", "2", "3", "4", "5", "6"})
    private int perftNum;
    private int perftDepth = 3;

    private final Board[] perftBoards = new Board[7];
    private final Perft perft = new Perft();
    private final SimpleMoveContainer[][] moveContainers = new SimpleMoveContainer[7][];

    @Setup
    public void init() {
        perftBoards[0] = perft.standardInitialPosition();
        perftBoards[1] = perft.kiwiPetePosition();
        perftBoards[2] = perft.position3();
        perftBoards[3] = perft.position4();
        perftBoards[4] = perft.position5();
        perftBoards[5] = perft.position6();
        perftBoards[6] = perft.promotionPosition();

        for (int i = 0; i < moveContainers.length; i++) {
            moveContainers[i] = MoveContainerFactory.buildMoveContainers(perftDepth + 1);
        }
    }

    @Benchmark
    @Warmup(iterations = 5)
    @Measurement(iterations = 5)
    public void testPerft() {
        perft.perftBoardRecursiveTimed(perftBoards[perftNum], 0, perftDepth, moveContainers[perftNum]);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(PerftBenchmark.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}
