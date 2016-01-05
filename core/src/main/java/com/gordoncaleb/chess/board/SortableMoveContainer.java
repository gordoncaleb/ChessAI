package com.gordoncaleb.chess.board;

import com.gordoncaleb.chess.board.pieces.Piece;

import java.util.*;
import java.util.stream.Collectors;

public class SortableMoveContainer implements MoveContainer {

    private static final int EMPTY = -1;
    private static final int UNPRIORITIZED = -1;
    private static final int AUTO_PRIORITY_BITS = 5;

    private int head = EMPTY;
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

        add(Move.toLong(fromRow, fromCol,
                toRow, toCol,
                note,
                pieceTakenId,
                pieceTakenRow, pieceTakenCol),
                note, pieceTakenId);
    }

    @Override
    public void add(int fromRow,
                    int fromCol,
                    int toRow,
                    int toCol,
                    int note) {

        add(Move.toLong(fromRow, fromCol,
                toRow, toCol,
                note),
                note, Piece.PieceID.NO_PIECE);
    }

    private void add(final long move, final int note, final int pieceTakenId) {
        final int priority = movePrioritization[Move.fromToAsInt(move)];

        head++;
        if (priority > UNPRIORITIZED) {
            moves[head] = combinePriority(move, priority);
        } else {
            final int autoPriority = calcAutoPriority(note, pieceTakenId) << AUTO_PRIORITY_BITS;
            moves[head] = combinePriority(move, autoPriority);
        }
    }

    private static long combinePriority(final long move, final long priority) {
        return move | (priority << (Move.TOP_BIT));
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
            return Piece.PieceID.QUEEN;
        } else if ((note & Move.MoveNote.CASTLE) != 0) {
            return Piece.PieceID.PAWN;
        } else {
            return Piece.PieceID.PAWN + 1;
        }
    }

    private static int calcAutoPriority(final int note, final int captureId) {
        if (captureId != Piece.PieceID.NO_PIECE) {
            return captureId;
        } else {
            return noteRating(note);
        }
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
        return Move.fromLong(moves[i], transientMove);
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
        movePrioritization[Move.fromToAsInt(move)] = priority;
    }

    @Override
    public void unprioritizeMove(long move) {
        movePrioritization[Move.fromToAsInt(move)] = UNPRIORITIZED;
    }

    @Override
    public void sort() {
        final int size = size();
        Arrays.sort(moves, 0, size);
//        for (int i = 0; i < size / 2; i++) {
//            final long temp = moves[i];
//            moves[i] = moves[head - i];
//            moves[head - i] = temp;
//        }
    }

    @Override
    public void markMove(final int i) {
        markedMoves[0] = moves[i];
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
        return Arrays.stream(moves)
                .limit(size())
                .mapToObj(Move::fromLong)
                .collect(Collectors.toList());
    }
}
