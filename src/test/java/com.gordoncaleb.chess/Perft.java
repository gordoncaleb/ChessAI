package com.gordoncaleb.chess;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.gordoncaleb.chess.ai.AI;
import com.gordoncaleb.chess.backend.Board;
import com.gordoncaleb.chess.backend.BoardFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Perft {
    private static final Logger logger = LoggerFactory.getLogger(Perft.class);

    public int[] level = new int[20];

    public int[] sizes = {1, 20, 400, 8902, 197281, 4865609};

    // 7978ms@5 home
    // 3189ms@5 work

    public static void main(String[] args) {
        Perft p = new Perft();

        long t1 = System.currentTimeMillis();

        Board board = BoardFactory.getStandardChessBoard();

        p.grow(5, board);

        logger.debug("Took " + (System.currentTimeMillis() - t1) + "ms");

        for (int i = 0; i < 20; i++) {
            logger.debug("Level " + i + ": " + p.level[i]);
            if (p.level[i] == 0) {
                break;
            }
        }
    }

    private void grow(int depth, Board board) {
        level[depth]++;
        if (depth > 0) {
            board.makeNullMove();

            List<Long> moves = new ArrayList<>(board.generateValidMoves());
            Collections.sort(moves, Collections.reverseOrder());

            moves.stream().forEach(m -> {
                board.makeMove(m);
                grow(depth - 1, board);
                board.undoMove();
            });
        }

    }

}
