package com.gordoncaleb.chess.util;


import com.gordoncaleb.chess.backend.Board;
import com.gordoncaleb.chess.backend.BoardFactory;
import com.gordoncaleb.chess.persistence.BoardDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class Perft {
    public static final Logger logger = LoggerFactory.getLogger(Perft.class);

    private final BoardDAO boardDAO = new BoardDAO();

    public Board standardInitialPosition() {
        return BoardFactory.getStandardChessBoard();
    }

    public Board kiwiPetePosition() {
        String fen = "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -";
        return boardDAO.fromFEN(fen);
    }

    public Board position3() {
        String fen = "8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - -";
        return boardDAO.fromFEN(fen);
    }

    public Board position4() {
        String fen = "r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1";
        return boardDAO.fromFEN(fen);
    }

    public Board position5() {
        String fen = "rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8 ";
        return boardDAO.fromFEN(fen);
    }

    public int[][] perftBoard(Board b, int stopDepth) {
        int[][] metrics = new int[stopDepth + 1][7];
        perftBoardRecursive(b, 0, stopDepth, metrics);
        return metrics;
    }

    private void perftBoardRecursive(Board b, int depth, int stopDepth, int[][] metrics) {
        b.makeNullMove();
        List<Long> moves = new ArrayList<>(b.generateValidMoves());

        metrics[depth][0] += moves.size();

        if (depth < stopDepth) {
            moves.forEach(m -> {
                b.makeMove(m);
                perftBoardRecursive(b, depth + 1, stopDepth, metrics);
                b.undoMove();
            });
        }
    }
}

