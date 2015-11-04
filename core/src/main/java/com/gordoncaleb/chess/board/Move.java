package com.gordoncaleb.chess.board;

import com.gordoncaleb.chess.board.pieces.Piece;
import com.gordoncaleb.chess.board.serdes.XMLParser;

import java.util.List;
import java.util.stream.Collectors;

public class Move {

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
    private static final int hadMovedMask = 1 << 15;
    private static final int hasPieceTakenMask = 1 << 16;
    private static final int pieceTakenHasMovedMask = 1 << 26;

    private Move.MoveNote note;
    private int fromRow, fromCol, toRow, toCol;
    private Piece pieceTaken;
    private boolean hadMoved;

    public Move(int fromRow, int fromCol, int toRow, int toCol) {
        this(fromRow, fromCol, toRow, toCol, 0, MoveNote.NONE, null, false);
    }

    public Move(int fromRow, int fromCol, int toRow, int toCol, int value) {
        this(fromRow, fromCol, toRow, toCol, value, MoveNote.NONE, null, false);
    }

    public Move(int fromRow, int fromCol, int toRow, int toCol, int value, MoveNote note) {
        this(fromRow, fromCol, toRow, toCol, value, note, null, false);
    }

    public Move(int fromRow, int fromCol, int toRow, int toCol, int value, MoveNote note, Piece pieceTaken) {
        this(fromRow, fromCol, toRow, toCol, 0, note, pieceTaken, false);
    }

    public Move(int fromRow, int fromCol, int toRow, int toCol, int value, MoveNote note, Piece pieceTaken, boolean hadMoved) {
        this.note = note;
        this.fromRow = fromRow;
        this.fromCol = fromCol;
        this.toRow = toRow;
        this.toCol = toCol;
        this.pieceTaken = pieceTaken;
        this.hadMoved = hadMoved;
    }

    public static boolean equals(Move moveA, Move moveB) {
        return (moveA.equals(moveB));
    }

    public String toXML() {
        return XMLParser.moveToXML(this);
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

    public boolean hadMoved() {
        return this.hadMoved;
    }

    public void setHadMoved(boolean hadMoved) {
        this.hadMoved = hadMoved;
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
        return fromLong(toLong());
    }

    public long toLong() {
        long moveLong = (note.ordinal() << 12) | (fromRow << 9) | (fromCol << 6) | (toRow << 3) | toCol;

        if (hadMoved) {
            moveLong |= hadMovedMask;
        }

        if (pieceTaken != null) {
            moveLong |= (pieceTaken.getPieceID() << 23) | (pieceTaken.getRow() << 20) | (pieceTaken.getCol() << 17) | hasPieceTakenMask;

            if (pieceTaken.hasMoved()) {
                moveLong |= pieceTakenHasMovedMask;
            }
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
        boolean hadMoved = (moveLong & hadMovedMask) != 0;

        int pieceTakenId = (int) ((moveLong >> 23) & 0x7);
        int pieceTakenRow = (int) ((moveLong >> 20) & 0x7);
        int pieceTakenCol = (int) ((moveLong >> 17) & 0x7);
        boolean pieceTakenHasMoved = ((moveLong & pieceTakenHasMovedMask) != 0);

        Piece pieceTaken = new Piece(pieceTakenId, -1, pieceTakenRow, pieceTakenCol, pieceTakenHasMoved);

        return new Move(fromRow, fromCol, toRow, toCol, value, note, pieceTaken, hadMoved);
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
        return toCol == move.toCol;

    }

    @Override
    public int hashCode() {
        int result = fromRow;
        result = 31 * result + fromCol;
        result = 31 * result + toRow;
        result = 31 * result + toCol;
        return result;
    }

    @Override
    public String toString() {
        String moveString;
        if (hasPieceTaken()) {
            moveString = "Moving from " + getFromRow() + "," + getFromCol() + " to " + getToRow() + "," + getToCol()
                    + " Move Note: " + getNote().toString() + " Value:" + getValue() + " PieceTaken: " + pieceTaken.toString();
        } else {
            moveString = "Moving from " + getFromRow() + "," + getFromCol() + " to " + getToRow() + "," + getToCol()
                    + " Move Note: " + getNote().toString() + " Value:" + getValue();
        }
        return moveString;
    }


}
