package com.gordoncaleb.chess.board.pieces;

import java.util.List;

import com.gordoncaleb.chess.board.bitboard.BitBoard;
import com.gordoncaleb.chess.board.Board;
import com.gordoncaleb.chess.board.Side;
import com.gordoncaleb.chess.board.Move;
import com.gordoncaleb.chess.board.serdes.XMLParser;

import static com.gordoncaleb.chess.board.pieces.Piece.PieceID.*;

public class Piece {
    private int row;
    private int col;
    private int player;
    private boolean moved;
    private long blockingVector;
    private int id;

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

    public enum PositionStatus {
        NO_PIECE, ENEMY, FRIEND, OFF_BOARD
    }

    public Piece(int id, int player, int row, int col, boolean moved) {
        this.id = id;
        this.moved = moved;
        this.player = player;
        this.row = row;
        this.col = col;
        this.blockingVector = BitBoard.ALL_ONES;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row){
        this.row = row;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col){
        this.col = col;
    }

    public long asBitMask() {
        return BitBoard.getMask(row, col);
    }

    public void move(int row, int col){
        this.row = row;
        this.col = col;
        this.moved = true;
    }

    public void unmove(int row, int col, boolean hadMoved){
        this.row = row;
        this.col = col;
        this.moved = hadMoved;
    }

    public int getSide() {
        return player;
    }

    public boolean hasMoved() {
        return moved;
    }

    public void setMoved(boolean moved) {
        this.moved = moved;
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
            stringId = this.getStringID(this.id);
        } else {
            stringId = this.getStringID(this.id).toLowerCase();
        }

        return stringId;
    }

    public String toXML() {
        return XMLParser.pieceToXML(this);
    }

    public boolean isValidMove(long position, long[] nullMoveInfo) {
        return isValidMove(nullMoveInfo, position, position);
    }

    public boolean isValidMove(long[] nullMoveInfo, long position, long attacks) {
        if ((attacks & nullMoveInfo[1]) != 0 && (position & blockingVector) != 0) {
            return true;
        } else {
            return false;
        }
    }

    public static String getStringID(int id) {
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
                return "";
        }
    }

    public int getPieceID() {
        return id;
    }

    public void setPieceID(int id) {
        this.id = id;
    }

    public void generateValidMoves(Board board, long[] nullMoveInfo, long[] posBitBoard, List<Move> validMoves) {

        switch (id) {
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

    public static List<Move> buildValidMoves(long validFootPrint,
                                             final int fromRow,
                                             final int fromCol,
                                             final List<Move> validMoves) {

        int bitNum;
        while ((bitNum = Long.numberOfTrailingZeros(validFootPrint)) < 64) {
            final long mask = (1L << bitNum);
            final int toRow = bitNum / 8;
            final int toCol = bitNum % 8;

            validMoves.add(
                    new Move(fromRow, fromCol, toRow, toCol)
            );

            validFootPrint ^= mask;
        }

        return validMoves;
    }

    public static List<Move> buildValidMovesWithPiecesTaken(long validFootPrint,
                                                            final int fromRow,
                                                            final int fromCol,
                                                            final Board board,
                                                            final List<Move> validMoves) {

        int bitNum;
        while ((bitNum = Long.numberOfTrailingZeros(validFootPrint)) < 64) {
            final long mask = (1L << bitNum);
            final int toRow = bitNum / 8;
            final int toCol = bitNum % 8;

            validMoves.add(
                    new Move(fromRow, fromCol, toRow, toCol, 0, Move.MoveNote.NONE, board.getPiece(toRow, toCol))
            );

            validFootPrint ^= mask;
        }

        return validMoves;
    }

    public Piece getCopy() {
        return new Piece(id, player, row, col, moved);
    }

}
