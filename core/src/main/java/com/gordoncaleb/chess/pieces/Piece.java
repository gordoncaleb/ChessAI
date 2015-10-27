package com.gordoncaleb.chess.pieces;

import java.util.List;

import com.gordoncaleb.chess.bitboard.BitBoard;
import com.gordoncaleb.chess.backend.Board;
import com.gordoncaleb.chess.backend.Side;
import com.gordoncaleb.chess.backend.Move;

import static com.gordoncaleb.chess.pieces.Piece.PieceID.*;

public class Piece {
    private int row;
    private int col;
    private int player;
    private boolean moved;
    private long blockingVector;
    private int id;

    public static class PieceID {
        public static final int ROOK = 0;
        public static final int KNIGHT = 1;
        public static final int BISHOP = 2;
        public static final int QUEEN = 3;
        public static final int KING = 4;
        public static final int PAWN = 5;
        public static final int NONE = -1;
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

    public int getCol() {
        return col;
    }

    public long getBit() {
        return BitBoard.getMask(row, col);
    }

    public void setPos(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public void move(long newMove) {
        setPos(Move.getToRow(newMove), Move.getToCol(newMove));
        moved = true;
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

    public void setBlockingVector(long blockingVector) {
        this.blockingVector = blockingVector;
    }

    public void clearBlocking() {
        blockingVector = BitBoard.ALL_ONES;
    }

    public long getBlockingVector() {
        return blockingVector;
    }

    public String toString() {
        String id;

        if (this.getSide() == Side.WHITE) {
            id = this.getStringID();
        } else {
            id = this.getStringID().toLowerCase();
        }

        return id;
    }

    public String toXML() {
        String xmlPiece = "";

        xmlPiece += "<piece>\n";
        xmlPiece += "<id>" + toString() + "</id>\n";
        xmlPiece += "<has_moved>" + hasMoved() + "</has_moved>\n";
        xmlPiece += "<position>" + getRow() + "," + getCol() + "</position>\n";
        xmlPiece += "</piece>\n";

        return xmlPiece;
    }

    public static Piece fromString(String stringPiece, int row, int col) {
        int player;

        boolean hasMoved = false;

        try {
            if (Integer.parseInt(stringPiece.substring(1, 2)) % 2 != 0) {
                hasMoved = true;
            }
        } catch (Exception e) {
            //Ignoring with reason
        }

        if (stringPiece.charAt(0) < 'a') {
            player = Side.BLACK;
        } else {
            player = Side.WHITE;
        }

        int id;
        char type = stringPiece.toUpperCase().charAt(0);

        id = charIDtoPieceID(type);

        if (id != PieceID.NONE) {
            return new Piece(id, player, row, col, hasMoved);
        } else {
            return null;
        }

    }

    public static int charIDtoPieceID(char type) {

        int id;

        switch (type) {
            case 'R':
                id = PieceID.ROOK;
                break;
            case 'N':
                id = PieceID.KNIGHT;
                break;
            case 'B':
                id = PieceID.BISHOP;
                break;
            case 'Q':
                id = PieceID.QUEEN;
                break;
            case 'K':
                id = PieceID.KING;
                break;
            case 'P':
                id = PieceID.PAWN;
                break;
            default:
                id = PieceID.NONE;
        }

        return id;
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

    public String getStringID() {
        switch (id) {
            case ROOK:
                return Rook.getStringID();
            case KNIGHT:
                return Knight.getStringID();
            case BISHOP:
                return Bishop.getStringID();
            case QUEEN:
                return Queen.getStringID();
            case KING:
                return King.getStringID();
            case PAWN:
                return Pawn.getStringID();
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

    public void generateValidMoves(Board board, long[] nullMoveInfo, long[] posBitBoard, List<Long> validMoves) {

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

    public static List<Long> buildValidMoves(long validFootPrint,
                                             final int fromRow,
                                             final int fromCol,
                                             final List<Long> validMoves) {

        int bitNum;
        while ((bitNum = Long.numberOfTrailingZeros(validFootPrint)) < 64) {
            final long mask = (1L << bitNum);
            final int toRow = bitNum / 8;
            final int toCol = bitNum % 8;

            validMoves.add(
                    Move.moveLong(fromRow, fromCol, toRow, toCol)
            );

            validFootPrint ^= mask;
        }

        return validMoves;
    }

    public static List<Long> buildValidMovesWithPiecesTaken(long validFootPrint,
                                                            final int fromRow,
                                                            final int fromCol,
                                                            final Board board,
                                                            final List<Long> validMoves) {

        int bitNum;
        while ((bitNum = Long.numberOfTrailingZeros(validFootPrint)) < 64) {
            final long mask = (1L << bitNum);
            final int toRow = bitNum / 8;
            final int toCol = bitNum % 8;

            validMoves.add(
                    Move.moveLong(fromRow, fromCol, toRow, toCol, 0, Move.MoveNote.NONE, board.getPiece(toRow, toCol))
            );

            validFootPrint ^= mask;
        }

        return validMoves;
    }

    public Piece getCopy() {
        return new Piece(id, player, row, col, moved);
    }

}
