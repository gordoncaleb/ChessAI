package com.gordoncaleb.chess.board;

import com.gordoncaleb.chess.board.pieces.Piece;

import java.util.*;

public class SortableMoveContainer implements MoveContainer {

    private static final int EMPTY = -1;
    private static final int UNPRIORITIZED = -1;

    private static final int AUTO_PRIORITY_BITS = 5;

    int head = EMPTY;
    private final long[] moves;
    private final Move transientMove = new Move();
    private final long[] markedMoves;

    private final int[] movePrioritization = new int[64 * 64];

    public SortableMoveContainer() {
        this(219, 30);
    }

    public SortableMoveContainer(int size) {
        this(size, 30);
    }

    public SortableMoveContainer(int size, int markedMoveSize) {
        moves = new long[size];
        markedMoves = new long[markedMoveSize];
        Arrays.fill(movePrioritization, UNPRIORITIZED);
    }

    @Override
    public void add(int fromRow,
                    int fromCol,
                    int toRow,
                    int toCol,
                    int note,
                    int pieceTakenId,
                    int pieceTakenRow,
                    int pieceTakenCol) {

        final long move = Move.toLong(fromRow, fromCol,
                toRow, toCol,
                note,
                pieceTakenId,
                pieceTakenRow, pieceTakenCol);

        final int priority = movePrioritization[Move.fromToAsInt(move)];

        head++;
        if (priority > UNPRIORITIZED) {
            moves[head] = combinePriority(move, priority);
        } else {
            moves[head] = combinePriority(move, calcPriority(note, pieceTakenId));
        }

    }

    @Override
    public void add(int fromRow,
                    int fromCol,
                    int toRow,
                    int toCol,
                    int note) {

        final long move = Move.toLong(fromRow, fromCol,
                toRow, toCol,
                note);

        final int priority = movePrioritization[Move.fromToAsInt(move)];

        head++;
        if (priority > UNPRIORITIZED) {
            moves[head] = combinePriority(move, priority);
        } else {
            moves[head] = combinePriority(move, noteRating(note));
        }
    }

    private static long combinePriority(final long move, final long priority) {
        return move | (priority << (Move.TOP_BIT + 2));
    }

    //        NEW_QUEEN 0x410
    //        CASTLE 0x3

    //        QUEEN 0x4
    //        ROOK 0x3
    //        BISHOP 0x2
    //        KNIGHT 0x1
    //        PAWN 0x0
    //        NO_PIECE 0x6

    private static int noteRating(final int note) {
        if ((note & Move.MoveNote.PROMOTION) != 0) {
            return 4;
        } else if ((note & Move.MoveNote.CASTLE) != 0) {
            return 1;
        } else {
            return 0;
        }
    }

    private static int calcPriority(final int note, final int captureId) {

        final int captureRating = captureId != Piece.PieceID.NO_PIECE ? captureId + 1 : 0;

        return captureRating != 0 ? captureRating : noteRating(note);
    }

    @Override
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

    @Override
    public Move get(int i) {
        return Move.fromLong(moves[head - i], transientMove);
    }

    @Override
    public Move pop() {
        return Move.fromLong(moves[head--], transientMove);
    }

    @Override
    public Move peek() {
        return Move.fromLong(moves[head], transientMove);
    }

    @Override
    public boolean isEmpty() {
        return head == EMPTY;
    }

    @Override
    public int size() {
        return head + 1;
    }

    @Override
    public void clear() {
        head = EMPTY;
    }

    @Override
    public void prioritizeMove(final long move, final int priority) {
        movePrioritization[Move.fromToAsInt(move)] = priority << AUTO_PRIORITY_BITS;
    }

    @Override
    public void unprioritizeMove(long move) {
        movePrioritization[Move.fromToAsInt(move)] = UNPRIORITIZED;
    }

    @Override
    public void sort() {
        final int size = size();
        Arrays.sort(moves, 0, size);
        for (int i = 0; i < size / 2; i++) {
            final long temp = moves[i];
            moves[i] = moves[head - i];
            moves[head - i] = temp;
        }
    }

    @Override
    public void markMove(final int i) {
        markedMoves[0] = moves[head - i];
    }

    @Override
    public void promoteMarkedMove(final int i) {
        markedMoves[i + 1] = markedMoves[i];
    }

    @Override
    public Move getMarkedMove(int i) {
        return Move.fromLong(markedMoves[i], transientMove);
    }

    @Override
    public long getMarkedMoveRaw(int i) {
        return markedMoves[i];
    }

    @Override
    public List<Move> toList() {
        List<Move> l = new ArrayList<>();
        for (int i = 0; i < size(); i++) {
            l.add(Move.fromLong(moves[i]));
        }
        return l;
    }
}
