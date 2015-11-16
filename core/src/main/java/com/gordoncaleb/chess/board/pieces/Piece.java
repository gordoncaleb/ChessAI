package com.gordoncaleb.chess.board.pieces;

import java.util.List;

import com.gordoncaleb.chess.board.MoveContainer;
import com.gordoncaleb.chess.board.bitboard.BitBoard;
import com.gordoncaleb.chess.board.Board;
import com.gordoncaleb.chess.board.Side;
import com.gordoncaleb.chess.board.Move;

import static com.gordoncaleb.chess.board.pieces.Piece.PieceID.*;

public class Piece {

    public static final Piece EMPTY = new Piece(NONE, Side.NONE, 0, 0);

    private int row;
    private int col;
    private int side;
    private long blockingVector;
    private int pieceId;

    public static class PieceID {
        public static final int NONE = -1;
        public static final int PAWN = 0;
        public static final int KNIGHT = 1;
        public static final int BISHOP = 2;
        public static final int ROOK = 3;
        public static final int QUEEN = 4;
        public static final int KING = 5;
        public static final int PIECES_COUNT = 6;
    }

    public Piece() {

    }

    public Piece(int pieceId, int side, int row, int col) {
        this.pieceId = pieceId;
        this.side = side;
        this.row = row;
        this.col = col;
        this.blockingVector = BitBoard.ALL_ONES;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public int getSide() {
        return side;
    }

    public void setSide(int side) {
        this.side = side;
    }

    public int getPieceID() {
        return pieceId;
    }

    public void setPieceID(int pieceId) {
        this.pieceId = pieceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Piece piece = (Piece) o;

        if (row != piece.row) return false;
        if (col != piece.col) return false;
        if (side != piece.side) return false;
        return pieceId == piece.pieceId;

    }

    @Override
    public int hashCode() {
        int result = row;
        result = 31 * result + col;
        result = 31 * result + side;
        result = 31 * result + pieceId;
        return result;
    }

    public long asBitMask() {
        return BitBoard.getMask(row, col);
    }

    public void move(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public void unmove(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public void putBlockingVector(long blockingVector) {
        this.blockingVector = blockingVector;
    }

    public void clearBlocking() {
        blockingVector = BitBoard.ALL_ONES;
    }

    public long blockingVector() {
        return blockingVector;
    }

    public String toString() {
        String stringId;

        if (this.getSide() == Side.WHITE) {
            stringId = this.stringID(this.pieceId);
        } else {
            stringId = this.stringID(this.pieceId).toLowerCase();
        }

        return stringId;
    }

    public static String stringID(int id) {
        switch (id) {
            case ROOK:
                return "R";
            case KNIGHT:
                return "N";
            case BISHOP:
                return "B";
            case QUEEN:
                return "Q";
            case KING:
                return "K";
            case PAWN:
                return "P";
            default:
                return "_";
        }
    }

    public void generateValidMoves(Board board, long[] nullMoveInfo, long[] posBitBoard, MoveContainer validMoves) {

        switch (pieceId) {
            case ROOK:
                Rook.generateValidMoves(this, board, nullMoveInfo, posBitBoard, validMoves);
                break;
            case KNIGHT:
                Knight.generateValidMoves(this, board, nullMoveInfo, posBitBoard, validMoves);
                break;
            case BISHOP:
                Bishop.generateValidMoves(this, board, nullMoveInfo, posBitBoard, validMoves);
                break;
            case QUEEN:
                Queen.generateValidMoves(this, board, nullMoveInfo, posBitBoard, validMoves);
                break;
            case KING:
                King.generateValidMoves(this, board, nullMoveInfo, posBitBoard, validMoves);
                break;
            case PAWN:
                Pawn.generateValidMoves(this, board, nullMoveInfo, posBitBoard, validMoves);
                break;
            default:
        }

    }

    public static MoveContainer buildValidMoves(long validFootPrint,
                                                final int fromRow,
                                                final int fromCol,
                                                final int note,
                                                final Board board,
                                                final MoveContainer validMoves) {

        int bitNum;
        while ((bitNum = Long.numberOfTrailingZeros(validFootPrint)) < 64) {
            final long mask = (1L << bitNum);
            final int toRow = bitNum / 8;
            final int toCol = bitNum % 8;
            final Piece pt = board.getPiece(toRow, toCol);

            validMoves.add(
                    fromRow, fromCol, toRow, toCol, note, pt.getPieceID(), pt.getRow(), pt.getCol()
            );

            validFootPrint ^= mask;
        }

        return validMoves;
    }

    public Piece copy() {
        return new Piece(pieceId, side, row, col);
    }

}
