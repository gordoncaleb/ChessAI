package com.gordoncaleb.chess.util;


import com.gordoncaleb.chess.board.Board;
import com.gordoncaleb.chess.board.BoardFactory;
import com.gordoncaleb.chess.board.Move;
import com.gordoncaleb.chess.board.serdes.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class Perft {
    public static final Logger logger = LoggerFactory.getLogger(Perft.class);

    public Board standardInitialPosition() {
        return BoardFactory.getStandardChessBoard();
    }

    public Board kiwiPetePosition() {
        String fen = "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -";
        return BoardFactory.fromFEN(fen);
    }

    public Board position3() {
        String fen = "8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - -";
        return BoardFactory.fromFEN(fen);
    }

    public Board position4() {
        String fen = "r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1";
        return BoardFactory.fromFEN(fen);
    }

    public Board position5() {
        String fen = "rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8 ";
        return BoardFactory.fromFEN(fen);
    }

    public int[][] perftBoardFunctional(Board b, int stopDepth) {
        int[][] metrics = new int[stopDepth + 1][7];
        perftBoardRecursiveFunctional(b, 0, stopDepth, metrics, getMoveContainers(stopDepth + 1));
        return metrics;
    }

    private void perftBoardRecursiveFunctional(Board b, int depth, int stopDepth, int[][] metrics, List<List<Long>> moveContainers) {
        b.makeNullMove();
        List<Long> moves = moveContainers.get(depth);
        b.generateValidMoves(moves);

        long pawnQueenings = moves.stream().filter(m -> Move.getNote(m) == Move.MoveNote.NEW_QUEEN).count();

        metrics[depth][0] += moves.size() + pawnQueenings * 3;
        metrics[depth][1] += moves.stream().filter(m -> Move.hasPieceTaken(m)).count();
        metrics[depth][2] += moves.stream().filter(m -> Move.getNote(m) == Move.MoveNote.ENPASSANT).count();

        metrics[depth][3] += moves.stream().filter(m -> Move.getNote(m) == Move.MoveNote.CASTLE_FAR ||
                Move.getNote(m) == Move.MoveNote.CASTLE_NEAR).count();

        metrics[depth][4] += moves.stream().filter(m -> Move.getNote(m) == Move.MoveNote.NEW_QUEEN).count() * 4;

        if (depth < stopDepth) {
            moves.forEach(m -> {
                b.makeMove(m);
                perftBoardRecursiveFunctional(b, depth + 1, stopDepth, metrics, moveContainers);
                b.undoMove();
            });
        }
    }

    public void perftBoardRecursiveTimed(Board b, int depth, int stopDepth, List<List<Long>> moveContainers) {
        b.makeNullMove();
        List<Long> moves = moveContainers.get(depth);
        b.generateValidMoves(moves);

        if (depth < stopDepth) {
            moves.forEach(m -> {
                b.makeMove(m);
                perftBoardRecursiveTimed(b, depth + 1, stopDepth, moveContainers);
                b.undoMove();
            });
        }
    }

    private List<List<Long>> getMoveContainers(int size) {
        List<List<Long>> containers = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            containers.add(new ArrayList<>());
        }
        return containers;
    }
}

