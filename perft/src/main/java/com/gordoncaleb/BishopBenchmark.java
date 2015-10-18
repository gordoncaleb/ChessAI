package com.gordoncaleb;

import com.gordoncaleb.chess.backend.Board;
import com.gordoncaleb.chess.backend.Side;
import com.gordoncaleb.chess.persistence.BoardDAO;
import com.gordoncaleb.chess.pieces.Bishop;
import com.gordoncaleb.chess.pieces.Piece;
import com.gordoncaleb.util.MockList;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.List;
import java.util.concurrent.TimeUnit;


@State(Scope.Thread)
@BenchmarkMode(Mode.SampleTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class BishopBenchmark {

    public Board[] board = new Board[2];
    public long[][] nullMoveInfo = new long[2][];
    public long[][] posBitBoard = new long[2][];
    public Piece[] bishop = new Piece[2];

    public List<Long> validMoves;

    @Param({"0", "1"})
    private int s;

    @Setup
    public void init() {
        validMoves = new MockList<>();
        BoardDAO boardDAO = new BoardDAO();

        String[] setup1 = {
                "R,_,_,_,_,_,Q,_,",
                "_,P,_,N,_,B,_,_,",
                "_,_,_,_,_,_,K,_,",
                "_,P,_,_,P,_,p,P,",
                "_,_,_,p,p,p,_,p,",
                "_,p,_,_,_,_,_,_,",
                "p,b,_,n,_,q,_,p,",
                "r,_,_,_,_,k,_,r,"
        };

        board[0] = boardDAO.getFromSetup(Side.WHITE, setup1);
        bishop[0] = board[0].getPiece(6, 1);
        nullMoveInfo[0] = board[0].makeNullMove();
        posBitBoard[0] = board[0].getAllPosBitBoard();


        String[] setup2 = {
                "R,_,_,_,_,_,Q,_,",
                "_,P,_,N,_,B,_,_,",
                "_,_,_,_,_,_,K,_,",
                "_,P,_,b,_,_,_,P,",
                "_,_,_,_,_,_,k,p,",
                "_,_,_,_,r,_,_,_,",
                "p,b,_,_,_,_,_,_,",
                "r,_,_,_,_,_,_,r,"
        };

        board[1] = boardDAO.getFromSetup(Side.WHITE, setup2);
        bishop[1] = board[1].getPiece(3, 3);
        nullMoveInfo[1] = board[1].makeNullMove();
        posBitBoard[1] = board[1].getAllPosBitBoard();
    }

    @Benchmark
    @Warmup(iterations = 5, batchSize = 100000)
    @Measurement(iterations = 5, batchSize = 100000)
    public List<Long> testBishopMoveGen() {
        return Bishop.generateValidMoves(bishop[s], board[s], nullMoveInfo[s], posBitBoard[s], validMoves);
    }

    @Benchmark
    @Warmup(iterations = 5, batchSize = 100000)
    @Measurement(iterations = 5, batchSize = 100000)
    public List<Long> testBishopMoveGenOld() {
        return Bishop.generateValidMoves2(bishop[s], board[s], nullMoveInfo[s], posBitBoard[s], validMoves);
    }

    /*
     * ============================== HOW TO RUN THIS TEST: ====================================
     *    $ mvn clean install
     *    $ java -jar target/benchmarks.jar KnightBenchmark -wi 0 -i 3
     */

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(BishopBenchmark.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}
