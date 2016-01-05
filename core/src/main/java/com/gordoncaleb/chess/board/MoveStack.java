package com.gordoncaleb.chess.board;


import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MoveStack {

    private static final int EMPTY = -1;

    private int head = EMPTY;
    private final long[] moves;
    private final Move transientMove = new Move();

    public MoveStack(int size) {
        moves = new long[size];
    }

    public void add(Move m) {
        head++;
        moves[head] = Move.toLong(m.getFromRow(),
                m.getFromCol(),
                m.getToRow(),
                m.getToCol(),
                m.getNote(),
                m.getPieceTakenId(),
                m.getPieceTakenRow(),
                m.getPieceTakenCol());
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

    public List<Move> toList() {
        return Arrays.stream(moves)
                .limit(size())
                .mapToObj(Move::fromLong)
                .collect(Collectors.toList());
    }
}
