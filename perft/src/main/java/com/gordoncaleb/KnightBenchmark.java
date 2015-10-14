package com.gordoncaleb;

import com.gordoncaleb.chess.backend.Board;
import com.gordoncaleb.chess.backend.Side;
import com.gordoncaleb.chess.bitboard.Slide;
import com.gordoncaleb.chess.persistence.BoardDAO;
import com.gordoncaleb.chess.pieces.Knight;
import com.gordoncaleb.chess.pieces.Piece;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;

import com.gordoncaleb.chess.bitboard.BitBoard;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.SampleTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class KnightBenchmark {

    public Board board;
    public long[] nullMoveInfo;
    public long[] posBitBoard;
    public List<Long> validMoves;
    public Piece knight;

    @Setup
    public void init() {
        BitBoard.loadKnightFootPrints();

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
        board = boardDAO.getFromSetup(Side.WHITE, setup);
        knight = board.getPiece(6, 3);
        nullMoveInfo = board.makeNullMove();
        posBitBoard = board.getAllPosBitBoard();
        validMoves = new ArrayList<Long>();
    }

    @Benchmark
    @Warmup(iterations = 5, batchSize = 5000)
    @Measurement(iterations = 5, batchSize = 5000)
    public void testKnightMoveGenOld() {
        Knight.generateValidMoves(knight, board, nullMoveInfo, posBitBoard, validMoves);
    }

    @Benchmark
    @Warmup(iterations = 5, batchSize = 5000)
    @Measurement(iterations = 5, batchSize = 5000)
    public void testKnightMoveGen() {
        Knight.generateValidMoves2(knight, board, nullMoveInfo, posBitBoard, validMoves);
    }

    /*
     * ============================== HOW TO RUN THIS TEST: ====================================
     *    $ mvn clean install
     *    $ java -jar target/benchmarks.jar KnightBenchmark -wi 0 -i 3
     */

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(KnightBenchmark.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }

}
