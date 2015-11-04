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
    private int side;
    private boolean hasMoved;
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

    public enum PositionStatus {
        NO_PIECE, ENEMY, FRIEND, OFF_BOARD
    }

    public Piece(){

    }

    public Piece(int pieceId, int side, int row, int col, boolean hasMoved) {
        this.pieceId = pieceId;
        this.hasMoved = hasMoved;
        this.side = side;
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

    public int getSide() {
        return side;
    }

    public void setSide(int side){
        this.side = side;
    }

    public boolean getHasMoved() {
        return hasMoved;
    }

    public void setHasMoved(boolean moved) {
        this.hasMoved = moved;
    }

    public int getPieceID() {
        return pieceId;
    }

    public void setPieceID(int pieceId) {
        this.pieceId = pieceId;
    }

    public long asBitMask() {
        return BitBoard.getMask(row, col);
    }

    public void move(int row, int col){
        this.row = row;
        this.col = col;
        this.hasMoved = true;
    }

    public void unmove(int row, int col, boolean hadMoved){
        this.row = row;
        this.col = col;
        this.hasMoved = hadMoved;
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

    public String toXML() {
        return XMLParser.pieceToXML(this);
    }

    public boolean checkValidMove(long position, long[] nullMoveInfo) {
        return checkValidMove(nullMoveInfo, position, position);
    }

    public boolean checkValidMove(long[] nullMoveInfo, long position, long attacks) {
        if ((attacks & nullMoveInfo[1]) != 0 && (position & blockingVector) != 0) {
            return true;
        } else {
            return false;
        }
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
                return "";
        }
    }

    public void generateValidMoves(Board board, long[] nullMoveInfo, long[] posBitBoard, List<Move> validMoves) {

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

    public Piece copy() {
        return new Piece(pieceId, side, row, col, hasMoved);
    }

}
