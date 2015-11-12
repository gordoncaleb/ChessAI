package com.gordoncaleb.chess.board;

import com.gordoncaleb.chess.board.pieces.Piece;

import java.util.ArrayList;
import java.util.List;

public class MoveContainer {

    private static final int EMPTY = -1;
    private int head = EMPTY;
    private final Move[] moves;

    public MoveContainer() {
        this(219);
    }

    public MoveContainer(int size) {
        moves = new Move[size];
        initMoves();
    }

    private void initMoves() {
        for (int i = 0; i < moves.length; i++) {
            moves[i] = new Move();
        }
    }

    public boolean isEmpty() {
        return head < 0;
    }

    public int size() {
        return head + 1;
    }

    public void clear() {
        head = EMPTY;
    }

    public void add(int fromRow, int fromCol, int toRow, int toCol, int value, Move.MoveNote note, Piece pieceTaken) {
        head++;
        moves[head].set(fromRow, fromCol, toRow, toCol, value, note, pieceTaken);
    }

    public void add(int fromRow, int fromCol, int toRow, int toCol) {
        add(fromRow, fromCol, toRow, toCol, 0, Move.MoveNote.NONE, null);
    }

    public void add(int fromRow, int fromCol, int toRow, int toCol, int value) {
        add(fromRow, fromCol, toRow, toCol, value, Move.MoveNote.NONE, null);
    }

    public void add(int fromRow, int fromCol, int toRow, int toCol, int value, Move.MoveNote note) {
        add(fromRow, fromCol, toRow, toCol, value, note, null);
    }

    public void add(Move m) {
        add(m.getFromRow(), m.getFromCol(), m.getToRow(), m.getToCol(), m.getValue(), m.getNote(), m.getPieceTaken());
    }

    public Move get(int i) {
        return moves[i];
    }

    public void set(int i, Move m) {
        moves[i] = m;
    }

    public Move pop() {
        return moves[head--];
    }

    public Move peek() {
        return moves[head];
    }

    public List<Move> toList() {
        List<Move> l = new ArrayList<>();
        for (int i = 0; i < size(); i++) {
            l.add(moves[i]);
        }
        return l;
    }

    public Move[] toArray() {
        Move[] a = new Move[size()];
        System.arraycopy(moves, 0, a, 0, size());
        return a;
    }
}
