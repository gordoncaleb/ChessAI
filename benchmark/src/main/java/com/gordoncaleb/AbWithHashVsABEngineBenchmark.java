package com.gordoncaleb;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gordoncaleb.chess.board.Board;
import com.gordoncaleb.chess.board.MoveContainerFactory;
import com.gordoncaleb.chess.engine.ABWithHashEngine;
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
import org.openjdk.jmh.util.Statistics;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class AbWithHashVsABEngineBenchmark {

    @Param({"0", "1", "2", "3", "4", "5", "6"})
    private int perftNum;

    private final Board[] perftBoards = new Board[7];
    private final Perft perft = new Perft();

    private ABWithHashEngine abWithHashEngine;
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

        abWithHashEngine = new ABWithHashEngine(new StaticScorer(), MoveContainerFactory.buildSortableMoveContainers(10));
        alphaBetaEngine = new AlphaBetaEngine(new StaticScorer(), MoveContainerFactory.buildMoveContainers(10));
    }

    @Benchmark
    @Warmup(iterations = 1)
    @Measurement(iterations = 5)
    public void testAbWithHash() {
        abWithHashEngine.search(perftBoards[perftNum], 5);
    }

    @Benchmark
    @Warmup(iterations = 1)
    @Measurement(iterations = 5)
    public void testAlphaBeta() {
        alphaBetaEngine.search(perftBoards[perftNum], 5);
    }

    public static void main(String[] args) throws RunnerException, JsonProcessingException {
        Options opt = new OptionsBuilder()
                .include(AbWithHashVsABEngineBenchmark.class.getSimpleName())
                .forks(3)
//                .resultFormat(ResultFormatType.JSON)
//                .result(AbWithHashVsABEngineBenchmark.class.getSimpleName() + ".jmh.json")
                .build();

        Collection<RunResult> results = new Runner(opt).run();

        Map<String, Map<String, List<Statistics>>> resultMap = results.stream()
                .collect(Collectors.groupingBy(r -> r.getParams().getParam("perftNum"),
                        Collectors.groupingBy(r -> r.getPrimaryResult().getLabel(),
                                Collectors.mapping(r -> r.getPrimaryResult().getStatistics()
                                        , Collectors.toList()))));

        List<Map<String, Double>> allDeltas = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            Map<String, Double> deltas = statDelta(
                    resultMap.get(i + "").get("testAlphaBeta").get(0),
                    resultMap.get(i + "").get("testAbWithHash").get(0)
            );
            allDeltas.add(deltas);
            System.out.println("Param " + i + ": " + deltas);
        }

        double allAggregateMean = allDeltas.stream().mapToDouble(d -> d.get("Mean")).average().getAsDouble();
        System.out.println("Aggregate Mean: " + allAggregateMean);
    }

    private static Map<String, Double> statDelta(Statistics a, Statistics b) {
        Map<String, Double> deltaMap = new HashMap<>();
        deltaMap.put("Mean", percentDelta(a.getMean(), b.getMean()));
        deltaMap.put("Stdev", percentDelta(a.getStandardDeviation(), b.getStandardDeviation()));
        deltaMap.put("Max", percentDelta(a.getMax(), b.getMax()));
        deltaMap.put("Min", percentDelta(a.getMin(), b.getMin()));
        return deltaMap;
    }

    private static double percentDelta(double a, double b) {
        return (a / b) * 100;
    }
}