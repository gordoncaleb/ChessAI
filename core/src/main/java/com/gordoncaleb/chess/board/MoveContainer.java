package com.gordoncaleb.chess.board;

import java.util.List;

public interface MoveContainer {
    void add(int fromRow,
             int fromCol,
             int toRow,
             int toCol,
             int note,
             int pieceTakenId,
             int pieceTakenRow,
             int pieceTakenCol);

    void add(int fromRow,
             int fromCol,
             int toRow,
             int toCol,
             int note);

    void add(Move m);

    Move get(int i);

    boolean isEmpty();

    int size();

    void clear();

    void prioritizeMove(long move, int priority);

    void unprioritizeMove(long move);

    void sort();

    void markMove(int i);

    void promoteMarkedMove(int i);

    Move getMarkedMove(int i);

    long getMarkedMoveRaw(int i);

    List<Move> toList();
}
