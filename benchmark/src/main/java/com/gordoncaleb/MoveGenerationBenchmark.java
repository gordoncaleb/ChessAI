package com.gordoncaleb;

import com.gordoncaleb.chess.board.Board;
import com.gordoncaleb.chess.board.MoveContainer;
import com.gordoncaleb.chess.board.MoveContainerFactory;
import com.gordoncaleb.chess.board.Side;
import com.gordoncaleb.chess.board.serdes.JSONParser;
import com.gordoncaleb.chess.util.Perft;
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


    @Param({"0", "1", "2", "3", "4", "5", "6", "7"})
    private int perftNum;
    private int perftDepth = 3;

    private final Board[] perftBoards = new Board[8];
    private final Perft perft = new Perft();
    private final MoveContainer[][] moveContainers = new MoveContainer[8][];

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

        perftBoards[7] = JSONParser.getFromSetup(Side.WHITE, setup);
    }

    @Benchmark
    @Warmup(iterations = 5, batchSize = 5000)
    @Measurement(iterations = 5, batchSize = 5000)
    public void testMoveGeneration1() {
        perftBoards[perftNum].makeNullMove();
        perftBoards[perftNum].generateValidMoves();
    }

    /*
     * ============================== HOW TO RUN THIS TEST: ====================================
     *    $ mvn clean install
     *    $ java -jar target/benchmarks.jar MoveGenerationBenchmark -wi 0 -i 3
     */

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(MoveGenerationBenchmark.class.getSimpleName())
                .warmupIterations(1)
                .measurementIterations(5)
                .build();

        new Runner(opt).run();
    }

}

