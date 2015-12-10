package com.gordoncaleb;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.gordoncaleb.chess.board.Board;
import com.gordoncaleb.chess.board.MoveContainerFactory;
import com.gordoncaleb.chess.engine.AlphaBetaEngine;
import com.gordoncaleb.chess.engine.NegaMaxEngine;
import com.gordoncaleb.chess.engine.score.StaticScorer;
import com.gordoncaleb.chess.util.Perft;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class EngineBenchmark {

    @Param({"0", "1"})//, "2", "3", "4", "5", "6"})
    private int perftNum;

    private final Board[] perftBoards = new Board[7];
    private final Perft perft = new Perft();

    private NegaMaxEngine negaMaxEngine;
    private AlphaBetaEngine alphaBetaEngine;

    @Setup
    public void init() {
        perftBoards[0] = perft.standardInitialPosition();
        perftBoards[1] = perft.kiwiPetePosition();
        perftBoards[2] = perft.position3();
        perftBoards[3] = perft.position4();
        perftBoards[4] = perft.position5();
        perftBoards[5] = perft.position6();
        perftBoards[6] = perft.promotionPosition();

        negaMaxEngine = new NegaMaxEngine(new StaticScorer(), MoveContainerFactory.buildMoveContainers(10));
        alphaBetaEngine = new AlphaBetaEngine(new StaticScorer(), MoveContainerFactory.buildMoveContainers(10));
    }

    @Benchmark
    @Warmup(iterations = 1)
    @Measurement(iterations = 1)
    public void testNegaMax() {
        negaMaxEngine.search(perftBoards[perftNum], 3);
    }

    @Benchmark
    @Warmup(iterations = 1)
    @Measurement(iterations = 1)
    public void testAlphaBeta() {
        alphaBetaEngine.search(perftBoards[perftNum], 3);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(EngineBenchmark.class.getSimpleName())
                .warmupIterations(1)
                .measurementIterations(1)
                .forks(1)
                .resultFormat(ResultFormatType.JSON)
                .result(EngineBenchmark.class.getSimpleName() + ".jmh.json")
                .build();

        Collection<RunResult> results = new Runner(opt).run();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        results.forEach(result -> {
            System.out.println(result.getPrimaryResult().getStatistics());
            System.out.println(result.getPrimaryResult().getLabel());
            System.out.println(result.getParams().getParam("perftNum"));
            System.out.println(result.getPrimaryResult().getScore());

        });

    }

}
