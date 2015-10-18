package com.gordoncaleb;

import com.gordoncaleb.chess.backend.Board;
import com.gordoncaleb.chess.backend.Side;
import com.gordoncaleb.chess.bitboard.BitBoard;
import com.gordoncaleb.chess.bitboard.Slide;
import com.gordoncaleb.chess.persistence.BoardDAO;
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

    private Board b1;

    @Setup
    public void init() {
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

        BoardDAO boardDAO = new BoardDAO();
        b1 = boardDAO.getFromSetup(Side.WHITE, setup);
    }

    @Benchmark
    @Warmup(iterations = 5, batchSize = 5000)
    @Measurement(iterations = 5, batchSize = 5000)
    public void testMoveGeneration1() {
        b1.makeNullMove();
        b1.generateValidMoves();
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

