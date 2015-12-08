package com.gordoncaleb.chess.board;

import java.util.ArrayList;
import java.util.List;

import static com.gordoncaleb.chess.board.pieces.Piece.PieceID.*;

public class MoveContainer {

    private static final int EMPTY = -1;
    private int head = EMPTY;
    private final long[] moves;
    private final Move transientMove = new Move();
    private final long[] markedMoves;

    public MoveContainer() {
        this(219, 30);
    }

    public MoveContainer(int size) {
        this(size, 30);
    }

    public MoveContainer(int size, int markedMoveSize) {
        moves = new long[size];
        markedMoves = new long[markedMoveSize];
    }

    public boolean isEmpty() {
        return head == EMPTY;
    }

    public int size() {
        return head + 1;
    }

    public void clear() {
        head = EMPTY;
    }

    public void add(int fromRow,
                    int fromCol,
                    int toRow,
                    int toCol,
                    int note,
                    int pieceTakenId,
                    int pieceTakenRow,
                    int pieceTakenCol) {
        head++;
        moves[head] = Move.toLong(fromRow, fromCol,
                toRow, toCol,
                note,
                pieceTakenId,
                pieceTakenRow, pieceTakenCol);
    }

    public void add(int fromRow,
                    int fromCol,
                    int toRow,
                    int toCol,
                    int note) {
        head++;
        moves[head] = Move.toLong(fromRow, fromCol,
                toRow, toCol,
                note,
                NO_PIECE,
                0, 0);
    }

    public void add(Move m) {
        add(m.getFromRow(),
                m.getFromCol(),
                m.getToRow(),
                m.getToCol(),
                m.getNote(),
                m.getPieceTakenId(),
                m.getPieceTakenRow(),
                m.getPieceTakenCol());
    }

    public void markMove(final int i) {
        markedMoves[0] = moves[i];
    }

    public void promoteMarkedMove(final int i) {
        markedMoves[i + 1] = markedMoves[i];
    }

    public Move getMarkedMove(int i) {
        return Move.fromLong(markedMoves[i], transientMove);
    }

    public Move get(int i) {
        return Move.fromLong(moves[i], transientMove);
    }

    public Move pop() {
        return Move.fromLong(moves[head--], transientMove);
    }

    public Move peek() {
        return Move.fromLong(moves[head], transientMove);
    }

    public List<Move> toList() {
        List<Move> l = new ArrayList<>();
        for (int i = 0; i < size(); i++) {
            l.add(Move.fromLong(moves[i]));
        }
        return l;
    }

}
