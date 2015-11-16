package com.gordoncaleb.chess.board;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gordoncaleb.chess.board.pieces.Piece;
import com.gordoncaleb.chess.util.JSON;

public class Move {

    public static final Move EMPTY_MOVE = new Move();
    private static final int DEFAULT_ROW = 0;
    private static final int DEFAULT_COL = 0;

    public enum MoveNote {
        NONE, CASTLE_NEAR, CASTLE_FAR, NEW_QUEEN, NEW_KNIGHT, ENPASSANT, PAWN_LEAP
    }

    private Move.MoveNote note;
    private int fromRow, fromCol, toRow, toCol;
    private int pieceTakenId, pieceTakenRow, pieceTakenCol;

    public Move() {

    }

    public Move(int fromRow, int fromCol, int toRow, int toCol) {
        this(fromRow, fromCol, toRow, toCol, MoveNote.NONE, Piece.PieceID.NONE, DEFAULT_ROW, DEFAULT_COL);
    }

    public Move(int fromRow, int fromCol, int toRow, int toCol, MoveNote note) {
        this(fromRow, fromCol, toRow, toCol, note, Piece.PieceID.NONE, DEFAULT_ROW, DEFAULT_COL);
    }

    public Move(int fromRow, int fromCol, int toRow, int toCol, MoveNote note, Piece pieceTaken) {
        this(fromRow, fromCol, toRow, toCol, note, pieceTaken.getPieceID(), pieceTaken.getRow(), pieceTaken.getCol());
    }

    public Move(int fromRow,
                int fromCol,
                int toRow,
                int toCol,
                MoveNote note,
                int pieceTakenId,
                int pieceTakenRow,
                int pieceTakenCol) {
        set(fromRow, fromCol, toRow, toCol, note, pieceTakenId, pieceTakenRow, pieceTakenCol);
    }

    public void set(int fromRow, int fromCol, int toRow, int toCol, MoveNote note) {
        set(fromRow, fromCol, toRow, toCol, note, Piece.PieceID.NONE, DEFAULT_ROW, DEFAULT_COL);
    }

    public void set(int fromRow,
                    int fromCol,
                    int toRow,
                    int toCol,
                    MoveNote note,
                    int pieceTakenId,
                    int pieceTakenRow,
                    int pieceTakenCol) {
        this.note = note;
        this.fromRow = fromRow;
        this.fromCol = fromCol;
        this.toRow = toRow;
        this.toCol = toCol;
        this.pieceTakenId = pieceTakenId;
        this.pieceTakenRow = pieceTakenRow;
        this.pieceTakenCol = pieceTakenCol;
    }

    public static boolean equals(Move moveA, Move moveB) {
        return (moveA.equals(moveB));
    }

    public String toJson() throws JsonProcessingException {
        return JSON.toJSON(this);
    }

    public void setNote(MoveNote note) {
        this.note = note;
    }

    public MoveNote getNote() {
        return note;
    }

    public int getFromRow() {
        return fromRow;
    }

    public void setFromRow(int fromRow) {
        this.fromRow = fromRow;
    }

    public int getFromCol() {
        return fromCol;
    }

    public void setFromCol(int fromCol) {
        this.fromCol = fromCol;
    }

    public int getToRow() {
        return toRow;
    }

    public void setToRow(int toRow) {
        this.toRow = toRow;
    }

    public int getToCol() {
        return toCol;
    }

    public void setToCol(int toCol) {
        this.toCol = toCol;
    }

    public int getPieceTakenId() {
        return pieceTakenId;
    }

    public void setPieceTakenId(int pieceTakenId) {
        this.pieceTakenId = pieceTakenId;
    }

    public int getPieceTakenRow() {
        return pieceTakenRow;
    }

    public void setPieceTakenRow(int pieceTakenRow) {
        this.pieceTakenRow = pieceTakenRow;
    }

    public int getPieceTakenCol() {
        return pieceTakenCol;
    }

    public void setPieceTakenCol(int pieceTakenCol) {
        this.pieceTakenCol = pieceTakenCol;
    }

    public boolean hasPieceTaken() {
        return (this.pieceTakenId != Piece.PieceID.NONE);
    }

    public int getValue() {
        return 0;
    }

    public void setValue(int value) {
    }

    public Move copy() {
        return new Move(fromRow, fromCol, toRow, toCol, note, pieceTakenId, pieceTakenRow, pieceTakenCol);
    }

    public Move justFromTo() {
        return new Move(fromRow, fromCol, toRow, toCol, MoveNote.NONE);
    }

    public int fromToAsInt() {
        return (int) (fromToMask & toLong());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Move move = (Move) o;

        if (fromRow != move.fromRow) return false;
        if (fromCol != move.fromCol) return false;
        if (toRow != move.toRow) return false;
        if (toCol != move.toCol) return false;
        if (pieceTakenId != move.pieceTakenId) return false;
        if (pieceTakenRow != move.pieceTakenRow) return false;
        if (pieceTakenCol != move.pieceTakenCol) return false;
        return note == move.note;

    }

    @Override
    public int hashCode() {
        int result = note != null ? note.hashCode() : 0;
        result = 31 * result + fromRow;
        result = 31 * result + fromCol;
        result = 31 * result + toRow;
        result = 31 * result + toCol;
        result = 31 * result + pieceTakenId;
        result = 31 * result + pieceTakenRow;
        result = 31 * result + pieceTakenCol;
        return result;
    }

    @Override
    public String toString() {
        return "Move{" +
                "note=" + note +
                ", fromRow=" + fromRow +
                ", fromCol=" + fromCol +
                ", toRow=" + toRow +
                ", toCol=" + toCol +
                ", pieceTakenId=" + pieceTakenId +
                ", pieceTakenRow=" + pieceTakenRow +
                ", pieceTakenCol=" + pieceTakenCol +
                '}';
    }

    /**
     * Bit-field 0-2 = toCol 3-5 = toRow 6-8 = fromCol 9-11 = fromRow 12-14 =
     * move note 15 = hadMoved 16 = has piece taken 17-19 = pieceTaken col 20-22
     * = pieceTaken row 23-25 = pieceTaken id 26 = pieceTaken has moved 32-48 =
     * moveValue
     */

    private static final int fromToMask = 0xFFF;

    public long toLong() {
        return toLong(fromRow, fromCol, toRow, toCol, 0, note, pieceTakenId, pieceTakenRow, pieceTakenCol);
    }

    public static long toLong(int fromRow,
                              int fromCol,
                              int toRow,
                              int toCol,
                              int value,
                              MoveNote note,
                              int pieceTakenId,
                              int pieceTakenRow,
                              int pieceTakenCol) {
        return (pieceTakenId << 23) |
                (pieceTakenRow << 20) |
                (pieceTakenCol << 17) |
                (note.ordinal() << 12) |
                (fromRow << 9) |
                (fromCol << 6) |
                (toRow << 3) |
                toCol;
    }

    public static Move fromLong(long moveLong) {
        int fromRow = (int) ((moveLong >> 9) & 0x7);
        int fromCol = (int) ((moveLong >> 6) & 0x7);
        int toRow = (int) ((moveLong >> 3) & 0x7);
        int toCol = (int) (moveLong & 0x7);
        int value = (int) (moveLong >> 32);
        MoveNote note = MoveNote.values()[(int) ((moveLong >> 12) & 0x7)];

        int pieceTakenId = (int) ((moveLong >> 23) & 0x7);
        int pieceTakenRow = (int) ((moveLong >> 20) & 0x7);
        int pieceTakenCol = (int) ((moveLong >> 17) & 0x7);

        return new Move(fromRow, fromCol, toRow, toCol, note, pieceTakenId, pieceTakenRow, pieceTakenCol);
    }
}
