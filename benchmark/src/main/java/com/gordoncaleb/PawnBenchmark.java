package com.gordoncaleb;

import com.gordoncaleb.chess.board.Board;
import com.gordoncaleb.chess.board.Move;
import com.gordoncaleb.chess.board.Side;
import com.gordoncaleb.chess.board.serdes.JSONParser;
import com.gordoncaleb.chess.board.pieces.Bishop;
import com.gordoncaleb.chess.board.pieces.Piece;
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
public class PawnBenchmark {

    public Board[] board = new Board[2];
    public long[][] nullMoveInfo = new long[2][];
    public long[][] posBitBoard = new long[2][];
    public Piece[] pawn = new Piece[2];

    public List<Move> validMoves;

    @Param({"0", "1"})
    private int s;

    @Setup
    public void init() {
        validMoves = new MockList<>();

        String[] setup1 = {
                "R,_,_,_,_,_,Q,_,",
                "_,P,_,N,_,B,_,_,",
                "_,_,_,_,_,_,K,_,",
                "_,_,_,_,P,_,p,P,",
                "P,_,_,p,p,p,_,p,",
                "_,p,_,_,_,_,_,_,",
                "p,b,_,n,_,q,_,p,",
                "r,_,_,_,_,k,_,r,"
        };

        board[0] = JSONParser.getFromSetup(Side.WHITE, setup1);
        pawn[0] = board[0].getPiece(6, 0);
        nullMoveInfo[0] = board[0].makeNullMove();
        posBitBoard[0] = board[0].getAllPosBitBoard();


        String[] setup2 = {
                "_,_,_,_,K,_,_,_,",
                "_,_,_,_,_,_,_,P,",
                "_,_,_,_,_,P,_,_,",
                "_,_,_,_,_,_,p,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,k,_,_,_,"
        };

        board[1] = JSONParser.getFromSetup(Side.WHITE, setup2);
        board[1].makeMove(new Move(1, 7, 3, 7, 0, Move.MoveNote.PAWN_LEAP));
        pawn[1] = board[1].getPiece(3, 6);
        nullMoveInfo[1] = board[1].makeNullMove();
        posBitBoard[1] = board[1].getAllPosBitBoard();
    }

    @Benchmark
    @Warmup(iterations = 5, batchSize = 100000)
    @Measurement(iterations = 5, batchSize = 100000)
    public List<Move> testPawnMoveGen() {
        return Bishop.generateValidMoves(pawn[s], board[s], nullMoveInfo[s], posBitBoard[s], validMoves);
    }

    /*
     * ============================== HOW TO RUN THIS TEST: ====================================
     *    $ mvn clean install
     *    $ java -jar target/benchmarks.jar KnightBenchmark -wi 0 -i 3
     */

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(PawnBenchmark.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}

