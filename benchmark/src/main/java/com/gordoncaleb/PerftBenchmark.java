package com.gordoncaleb;

import com.gordoncaleb.chess.backend.Board;
import com.gordoncaleb.chess.backend.Side;
import com.gordoncaleb.chess.persistence.BoardDAO;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.Warmup;

public class PerftBenchmark {

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
}
