package com.gordoncaleb.chess.board;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MoveContainer {

    private static final int EMPTY = -1;
    private static final int UNPRIORITIZED = -1;

    private int head = EMPTY;
    private final long[] moves;
    private final Move transientMove = new Move();
    private final long[] markedMoves;

    private final int[] movePrioritization = new int[64 * 64];

    public MoveContainer() {
        this(219, 30);
    }

    public MoveContainer(int size) {
        this(size, 30);
    }

    public MoveContainer(int size, int markedMoveSize) {
        moves = new long[size];
        markedMoves = new long[markedMoveSize];
        Arrays.fill(movePrioritization, UNPRIORITIZED);
    }

    public void add(int fromRow,
                    int fromCol,
                    int toRow,
                    int toCol,
                    int note,
                    int pieceTakenId,
                    int pieceTakenRow,
                    int pieceTakenCol) {

        add(Move.toLong(fromRow, fromCol,
                toRow, toCol,
                note,
                pieceTakenId,
                pieceTakenRow, pieceTakenCol));
    }

    public void add(int fromRow,
                    int fromCol,
                    int toRow,
                    int toCol,
                    int note) {

        add(Move.toLong(fromRow, fromCol,
                toRow, toCol,
                note));
    }

    private void add(final long move) {
        final int priority = movePrioritization[Move.fromToAsInt(move)];

        head++;
        if (priority > UNPRIORITIZED) {
            moves[head] = moves[0];
            moves[0] = move;
        } else {
            moves[head] = move;
        }
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

    public Move get(int i) {
        return Move.fromLong(moves[i], transientMove);
    }

    public Move pop() {
        return Move.fromLong(moves[head--], transientMove);
    }

    public Move peek() {
        return Move.fromLong(moves[head], transientMove);
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

    public void prioritizeMove(long move) {
        movePrioritization[Move.fromToAsInt(move)] = 1;
    }

    public void unprioritizeMove(long move) {
        movePrioritization[Move.fromToAsInt(move)] = UNPRIORITIZED;
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

    public long getMarkedMoveRaw(int i) {
        return markedMoves[i];
    }

    public List<Move> toList() {
        List<Move> l = new ArrayList<>();
        for (int i = 0; i < size(); i++) {
            l.add(Move.fromLong(moves[i]));
        }
        return l;
    }

}
