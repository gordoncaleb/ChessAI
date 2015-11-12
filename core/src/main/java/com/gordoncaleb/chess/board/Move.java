package com.gordoncaleb.chess.board;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gordoncaleb.chess.board.pieces.Piece;
import com.gordoncaleb.chess.util.JSON;

public class Move {

    public static final Move EMPTY_MOVE = new Move();

    public enum MoveNote {
        NONE, CASTLE_NEAR, CASTLE_FAR, NEW_QUEEN, NEW_KNIGHT, ENPASSANT, PAWN_LEAP
    }

    /**
     * Bit-field 0-2 = toCol 3-5 = toRow 6-8 = fromCol 9-11 = fromRow 12-14 =
     * move note 15 = hadMoved 16 = has piece taken 17-19 = pieceTaken col 20-22
     * = pieceTaken row 23-25 = pieceTaken id 26 = pieceTaken has moved 32-48 =
     * moveValue
     */

    private static final int fromToMask = 0xFFF;
    private static final int hasPieceTakenMask = 1 << 16;

    private Move.MoveNote note;
    private int fromRow, fromCol, toRow, toCol;
    private Piece pieceTaken;

    public Move() {

    }

    public Move(int fromRow, int fromCol, int toRow, int toCol) {
        this(fromRow, fromCol, toRow, toCol, 0, MoveNote.NONE, null);
    }

    public Move(int fromRow, int fromCol, int toRow, int toCol, int value) {
        this(fromRow, fromCol, toRow, toCol, value, MoveNote.NONE, null);
    }

    public Move(int fromRow, int fromCol, int toRow, int toCol, int value, MoveNote note) {
        this(fromRow, fromCol, toRow, toCol, value, note, null);
    }

    public Move(int fromRow, int fromCol, int toRow, int toCol, int value, MoveNote note, Piece pieceTaken) {
        this.note = note;
        this.fromRow = fromRow;
        this.fromCol = fromCol;
        this.toRow = toRow;
        this.toCol = toCol;
        this.pieceTaken = pieceTaken;
    }

    public void set(int fromRow, int fromCol, int toRow, int toCol) {
        set(fromRow, fromCol, toRow, toCol, 0, MoveNote.NONE, null);
    }

    public void set(int fromRow, int fromCol, int toRow, int toCol, int value) {
        set(fromRow, fromCol, toRow, toCol, value, MoveNote.NONE, null);
    }

    public void set(int fromRow, int fromCol, int toRow, int toCol, int value, MoveNote note) {
        set(fromRow, fromCol, toRow, toCol, value, note, null);
    }

    public void set(int fromRow, int fromCol, int toRow, int toCol, int value, MoveNote note, Piece pieceTaken){
        this.note = note;
        this.fromRow = fromRow;
        this.fromCol = fromCol;
        this.toRow = toRow;
        this.toCol = toCol;
        this.pieceTaken = pieceTaken;
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

    public int getValue() {
        return 0;
    }

    public void setValue(int value) {
    }

    public boolean hasPieceTaken() {
        return (this.pieceTaken != null);
    }

    public Piece getPieceTaken() {
        return this.pieceTaken;
    }

    public void setPieceTaken(Piece pieceTaken) {
        this.pieceTaken = pieceTaken;
    }

    public Move copy() {
        return new Move(fromRow, fromCol, toRow, toCol, 0, note, pieceTaken == null ? null : pieceTaken.copy());
    }

    public long toLong() {
        long moveLong = (note.ordinal() << 12) | (fromRow << 9) | (fromCol << 6) | (toRow << 3) | toCol;

        if (pieceTaken != null) {
            moveLong |= (pieceTaken.getPieceID() << 23) | (pieceTaken.getRow() << 20) | (pieceTaken.getCol() << 17) | hasPieceTakenMask;
        } else {
            moveLong |= Piece.PieceID.NONE << 23;
        }

        return moveLong;
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

        Piece pieceTaken = null;
        if ((hasPieceTakenMask & moveLong) > 0) {
            pieceTaken = new Piece(pieceTakenId, -1, pieceTakenRow, pieceTakenCol);
        }

        return new Move(fromRow, fromCol, toRow, toCol, value, note, pieceTaken);
    }

    public Move justFromTo() {
        return new Move(fromRow, fromCol, toRow, toCol);
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
        if (note != move.note) return false;
        return !(pieceTaken != null ? !pieceTaken.equals(move.pieceTaken) : move.pieceTaken != null);

    }

    @Override
    public int hashCode() {
        int result = note != null ? note.hashCode() : 0;
        result = 31 * result + fromRow;
        result = 31 * result + fromCol;
        result = 31 * result + toRow;
        result = 31 * result + toCol;
        result = 31 * result + (pieceTaken != null ? pieceTaken.hashCode() : 0);
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
                ", pieceTaken=" + pieceTaken +
                '}';
    }
}
