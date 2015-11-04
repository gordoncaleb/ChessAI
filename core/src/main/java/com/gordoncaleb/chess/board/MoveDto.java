package com.gordoncaleb.chess.board;

import com.gordoncaleb.chess.board.pieces.Piece;

public class MoveDTO {
    private Move.MoveNote note;
    private int fromRow, fromCol, toRow, toCol;
    private Piece pieceTaken;
    private boolean hadMoved;

    public MoveDTO(Move.MoveNote note, int fromRow, int fromCol, int toRow, int toCol, Piece pieceTaken, boolean hadMoved) {
        this.note = note;
        this.fromRow = fromRow;
        this.fromCol = fromCol;
        this.toRow = toRow;
        this.toCol = toCol;
        this.pieceTaken = pieceTaken;
        this.hadMoved = hadMoved;
    }

    public Move.MoveNote getNote() {
        return note;
    }

    public void setNote(Move.MoveNote note) {
        this.note = note;
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

    public Piece getPieceTaken() {
        return pieceTaken;
    }

    public void setPieceTaken(Piece pieceTaken) {
        this.pieceTaken = pieceTaken;
    }

    public boolean isHadMoved() {
        return hadMoved;
    }

    public void setHadMoved(boolean hadMoved) {
        this.hadMoved = hadMoved;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MoveDTO moveDTO = (MoveDTO) o;

        if (fromRow != moveDTO.fromRow) return false;
        if (fromCol != moveDTO.fromCol) return false;
        if (toRow != moveDTO.toRow) return false;
        if (toCol != moveDTO.toCol) return false;
        if (hadMoved != moveDTO.hadMoved) return false;
        if (note != moveDTO.note) return false;
        return !(pieceTaken != null ? !pieceTaken.equals(moveDTO.pieceTaken) : moveDTO.pieceTaken != null);

    }

    @Override
    public int hashCode() {
        int result = note != null ? note.hashCode() : 0;
        result = 31 * result + fromRow;
        result = 31 * result + fromCol;
        result = 31 * result + toRow;
        result = 31 * result + toCol;
        result = 31 * result + (pieceTaken != null ? pieceTaken.hashCode() : 0);
        result = 31 * result + (hadMoved ? 1 : 0);
        return result;
    }
}
