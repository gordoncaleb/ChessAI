package com.gordoncaleb.chess.util;


import com.gordoncaleb.chess.board.Board;
import com.gordoncaleb.chess.board.BoardFactory;
import com.gordoncaleb.chess.board.Move;
import com.gordoncaleb.chess.board.MoveContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
        String fen = "rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8";
        return BoardFactory.fromFEN(fen);
    }

    public Board position6() {
        String fen = "r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - - 0 10";
        return BoardFactory.fromFEN(fen);
    }

    public Board promotionPosition(){
        String fen = "n1n5/PPPk4/8/8/8/8/4Kppp/5N1N b - - 0 1";
        return BoardFactory.fromFEN(fen);
    }

    public long[][] perftBoardFunctional(Board b, int stopDepth) {
        long[][] metrics = new long[stopDepth + 1][6];
        perftBoardRecursiveFunctional(b, 0, stopDepth, metrics, getMoveContainers(stopDepth + 1));
        return metrics;
    }

    private void addOtherQueeningMoveOptions(List<Move> queenings, List<Move> moves) {
        for (Move m : queenings) {
            Move altMoveKnight = m.copy();
            altMoveKnight.setNote(Move.MoveNote.NEW_KNIGHT);
            moves.add(altMoveKnight);

            Move altMoveRook = m.copy();
            altMoveRook.setNote(Move.MoveNote.NEW_ROOK);
            moves.add(altMoveRook);

            Move altMoveBishop = m.copy();
            altMoveBishop.setNote(Move.MoveNote.NEW_BISHOP);
            moves.add(altMoveBishop);
        }
    }

    private void perftBoardRecursiveFunctional(Board b, int depth, int stopDepth, long[][] metrics, List<MoveContainer> moveContainers) {
        b.makeNullMove();
        MoveContainer moves = moveContainers.get(depth);
        b.generateValidMoves(moves);
        List<Move> moveList = moves.toList();

        List<Move> queeningMoves = moveList.stream().filter(m -> m.getNote() == Move.MoveNote.NEW_QUEEN).collect(Collectors.toList());
        addOtherQueeningMoveOptions(queeningMoves, moveList);

        metrics[depth][0] += moveList.size();
        metrics[depth][1] += moveList.stream().filter(m -> m.hasPieceTaken()).count();
        metrics[depth][2] += moveList.stream().filter(m -> m.getNote() == Move.MoveNote.EN_PASSANT).count();

        metrics[depth][3] += moveList.stream().filter(m -> m.getNote() == Move.MoveNote.CASTLE_FAR ||
                m.getNote() == Move.MoveNote.CASTLE_NEAR).count();

        metrics[depth][4] += moveList.stream().filter(m -> (m.getNote() & Move.MoveNote.NEW_QUEEN) != 0).count();

        if (b.isInCheck() && depth > 0) {
            metrics[depth - 1][5]++;
        }

        if (depth < stopDepth) {
            moveList.forEach(m -> {
                b.makeMove(m);
                perftBoardRecursiveFunctional(b, depth + 1, stopDepth, metrics, moveContainers);
                b.undoMove();
            });
        }
    }

    public void perftBoardRecursiveTimed(Board b, int depth, int stopDepth, List<MoveContainer> moveContainers) {
        b.makeNullMove();
        MoveContainer moves = moveContainers.get(depth);
        b.generateValidMoves(moves);

        if (depth < stopDepth) {
            for (int i = 0; i < moves.size(); i++) {
                b.makeMove(moves.get(i));
                perftBoardRecursiveTimed(b, depth + 1, stopDepth, moveContainers);
                b.undoMove();
            }
        }
    }

    private List<MoveContainer> getMoveContainers(int size) {
        List<MoveContainer> containers = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            containers.add(new MoveContainer());
        }
        return containers;
    }
}

