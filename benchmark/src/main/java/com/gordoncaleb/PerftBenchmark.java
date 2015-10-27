package com.gordoncaleb;

import com.gordoncaleb.chess.backend.Board;
import com.gordoncaleb.chess.util.Perft;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.SampleTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class PerftBenchmark {

    @Param({"0", "1"})
    private int perftNum;
    private int perftDepth = 3;

    private final Board[] perftBoards = new Board[2];
    private final Perft perft = new Perft();

    @Setup
    public void init() {
        perftBoards[0] = perft.standardInitialPosition();
        perftBoards[1] = perft.kiwiPetePosition();
    }

    @Benchmark
    @Warmup(iterations = 5)
    @Measurement(iterations = 5)
    public void testPerft() {
        perft.perftBoardFunctional(perftBoards[perftNum], perftDepth);
    }
}
