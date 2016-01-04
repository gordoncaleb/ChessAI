package com.gordoncaleb.chess.board;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gordoncaleb.chess.board.pieces.Piece;
import com.gordoncaleb.chess.util.JSON;

import static com.gordoncaleb.chess.board.pieces.Piece.PieceID.*;

public class Move {

    public static final Move EMPTY_MOVE = new Move();
    private static final int DEFAULT_ROW = 0;
    private static final int DEFAULT_COL = 0;

    public static class MoveNote {
        public static final int NORMAL = 0x0;
        public static final int CASTLE_NEAR = 0x1;
        public static final int CASTLE_FAR = 0x2;
        public static final int CASTLE = 0x3;
        public static final int EN_PASSANT = 0x4;
        public static final int PAWN_LEAP = 0x8;
        public static final int PROMOTION = 0x10;
        public static final int NEW_KNIGHT = (KNIGHT << 8) | PROMOTION;
        public static final int NEW_BISHOP = (BISHOP << 8) | PROMOTION;
        public static final int NEW_ROOK = (ROOK << 8) | PROMOTION;
        public static final int NEW_QUEEN = (QUEEN << 8) | PROMOTION;

        private MoveNote() {
        }
    }

    private int note;
    private int fromRow, fromCol, toRow, toCol;
    private int pieceTakenId, pieceTakenRow, pieceTakenCol;

    public Move() {
        //intentionally left blank
    }

    public Move(int fromRow, int fromCol, int toRow, int toCol) {
        this(fromRow, fromCol, toRow, toCol, MoveNote.NORMAL, Piece.PieceID.NO_PIECE, DEFAULT_ROW, DEFAULT_COL);
    }

    public Move(int fromRow, int fromCol, int toRow, int toCol, int note) {
        this(fromRow, fromCol, toRow, toCol, note, Piece.PieceID.NO_PIECE, DEFAULT_ROW, DEFAULT_COL);
    }

    public Move(int fromRow, int fromCol, int toRow, int toCol, int note, Piece pieceTaken) {
        this(fromRow, fromCol, toRow, toCol, note, pieceTaken.getPieceID(), pieceTaken.getRow(), pieceTaken.getCol());
    }

    public Move(int fromRow,
                int fromCol,
                int toRow,
                int toCol,
                int note,
                int pieceTakenId,
                int pieceTakenRow,
                int pieceTakenCol) {
        set(fromRow, fromCol, toRow, toCol, note, pieceTakenId, pieceTakenRow, pieceTakenCol);
    }

    public Move(int fromRow,
                int fromCol,
                int toRow,
                int toCol,
                int note,
                int pieceTakenId) {
        set(fromRow, fromCol, toRow, toCol, note, pieceTakenId, toRow, toCol);
    }

    public void set(int fromRow, int fromCol, int toRow, int toCol, int note) {
        set(fromRow, fromCol, toRow, toCol, note, Piece.PieceID.NO_PIECE, DEFAULT_ROW, DEFAULT_COL);
    }

    public void set(int fromRow,
                    int fromCol,
                    int toRow,
                    int toCol,
                    int note,
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

    public boolean hasPromotion() {
        return (note & MoveNote.PROMOTION) != 0;
    }

    public int promotionChoice() {
        return note >> 8;
    }

    public void setNote(int note) {
        this.note = note;
    }

    public int getNote() {
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
        return (this.pieceTakenId != Piece.PieceID.NO_PIECE);
    }

    public Move copy() {
        return new Move(fromRow, fromCol, toRow, toCol, note, pieceTakenId, pieceTakenRow, pieceTakenCol);
    }

    public Move justFromTo() {
        return new Move(fromRow, fromCol, toRow, toCol, MoveNote.NORMAL);
    }

    public int fromToAsInt() {
        return (int) (FromToMask & toLong());
    }

    public static int fromToAsInt(long move) {
        return (int) (FromToMask & move);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Move move = (Move) o;

        if (note != move.note) return false;
        if (fromRow != move.fromRow) return false;
        if (fromCol != move.fromCol) return false;
        if (toRow != move.toRow) return false;
        if (toCol != move.toCol) return false;
        if (pieceTakenId != move.pieceTakenId) return false;
        if (pieceTakenRow != move.pieceTakenRow) return false;
        return pieceTakenCol == move.pieceTakenCol;
    }

    @Override
    public int hashCode() {
        int result = note;
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

    public boolean destinationMatch(Move m) {
        return m.getToRow() == getToRow() &&
                m.getToCol() == getToCol();
    }

    private static final int FromToMask = 0xFFF;
    public static final int TOP_BIT = 33;

    public long toLong() {
        return toLong(fromRow, fromCol, toRow, toCol, note, pieceTakenId, pieceTakenRow, pieceTakenCol);
    }

    public static long toLong(int fromRow,
                              int fromCol,
                              int toRow,
                              int toCol,
                              int note,
                              int pieceTakenId,
                              int pieceTakenRow,
                              int pieceTakenCol) {
        return ((long) note << 21) | //12 bits
                (pieceTakenId << 18) | // 3 bits
                (pieceTakenRow << 15) | // 3 bits
                (pieceTakenCol << 12) | // 3 bits
                (fromRow << 9) | // 3 bits
                (fromCol << 6) | // 3 bits
                (toRow << 3) | //3 bits
                toCol;
    }

    public static long toLong(int fromRow,
                              int fromCol,
                              int toRow,
                              int toCol,
                              int note) {
        return ((long) note << 21) | //12 bits
                (NO_PIECE << 18) | // 3 bits
                (fromRow << 9) | // 3 bits
                (fromCol << 6) | // 3 bits
                (toRow << 3) | //3 bits
                toCol;
    }

    public static Move fromLong(long moveLong, Move move) {
        int toCol = (int) (moveLong & 0x7); //3 bits
        int toRow = (int) ((moveLong >> 3) & 0x7); //3 bits
        int fromCol = (int) ((moveLong >> 6) & 0x7); //3 bits
        int fromRow = (int) ((moveLong >> 9) & 0x7); //3 bits

        int pieceTakenCol = (int) ((moveLong >> 12) & 0x7);
        int pieceTakenRow = (int) ((moveLong >> 15) & 0x7);
        int pieceTakenId = (int) ((moveLong >> 18) & 0x7);

        int note = (int) ((moveLong >> 21) & 0xFFF); //12 bits
        move.set(fromRow, fromCol, toRow, toCol, note, pieceTakenId, pieceTakenRow, pieceTakenCol);
        return move;
    }

    public static Move fromLong(long moveLong) {
        return fromLong(moveLong, new Move());
    }
}
