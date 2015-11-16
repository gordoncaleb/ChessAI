package com.gordoncaleb.chess.board;

import com.gordoncaleb.chess.board.pieces.Piece;

public class MoveLong {
    private static final int hasPieceTakenMask = 1 << 16;

    private long moveLong;

    public MoveLong() {

    }

    public MoveLong(int fromRow, int fromCol, int toRow, int toCol) {
        this(fromRow, fromCol, toRow, toCol, 0, Move.MoveNote.NONE, null);
    }

    public MoveLong(int fromRow, int fromCol, int toRow, int toCol, int value) {
        this(fromRow, fromCol, toRow, toCol, value, Move.MoveNote.NONE, null);
    }

    public MoveLong(int fromRow, int fromCol, int toRow, int toCol, int value, Move.MoveNote note) {
        this(fromRow, fromCol, toRow, toCol, value, note, null);
    }

    public MoveLong(int fromRow, int fromCol, int toRow, int toCol, int value, Move.MoveNote note, Piece pieceTaken) {

        long moveLong = (note.ordinal() << 12) | (fromRow << 9) | (fromCol << 6) | (toRow << 3) | toCol;

        if (pieceTaken != null) {
            moveLong |= (pieceTaken.getPieceID() << 23) | (pieceTaken.getRow() << 20) | (pieceTaken.getCol() << 17) | hasPieceTakenMask;
        } else {
            moveLong |= Piece.PieceID.NONE << 23;
        }
    }


}
