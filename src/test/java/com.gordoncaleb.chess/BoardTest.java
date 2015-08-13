package com.gordoncaleb.chess;

import com.gordoncaleb.chess.ai.AI;
import com.gordoncaleb.chess.backend.Board;
import com.gordoncaleb.chess.backend.BoardFactory;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class BoardTest {
    public static final Logger logger = LoggerFactory.getLogger(BoardTest.class);

    @Test
    public void generateAllValidMoves(){
        //Test standard board creation

    }

    public static void main(String[] args) {
        Board board = BoardFactory.getStandardChessBoard();

        ArrayList<Long> moves = board.generateValidMoves(true, 0, AI.noKillerMoves);

        long m = moves.get(0);

        int its = 1000000;

        long t1 = System.currentTimeMillis();
        for (int i = 0; i < its; i++) {
            board.makeMove(m);
            board.makeNullMove();
            moves = board.generateValidMoves(true, 0, AI.noKillerMoves);
            board.undoMove();
        }

        long A = System.currentTimeMillis() - t1;

        t1 = System.currentTimeMillis();
        for (int i = 0; i < its; i++) {
            board.makeMove(m);
            board.undoMove();
        }

        long B = System.currentTimeMillis() - t1;

        logger.debug("A= " + A + ", B= " + B + " A/B=" + (double) A / (double) B);
    }
}
