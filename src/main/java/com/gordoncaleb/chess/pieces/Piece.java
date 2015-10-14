package com.gordoncaleb.chess.pieces;

import java.util.List;

import com.gordoncaleb.chess.bitboard.BitBoard;
import com.gordoncaleb.chess.backend.Board;
import com.gordoncaleb.chess.backend.Side;
import com.gordoncaleb.chess.backend.Move;

public class Piece {
    private int row;
    private int col;
    private Side player;
    private boolean moved;
    private long blockingVector;
    private PieceID id;

    public enum PieceID {
        ROOK, KNIGHT, BISHOP, QUEEN, KING, PAWN
    }

    public enum PositionStatus {
        NO_PIECE, ENEMY, FRIEND, OFF_BOARD
    }

    public Piece(PieceID id, Side player, int row, int col, boolean moved) {
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

    public void reverseMove(long newMove) {
        setPos(Move.getFromRow(newMove), Move.getFromCol(newMove));
    }

    public Side getSide() {
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

        if (this.getSide() == Side.BLACK) {
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
        Side player;

        boolean hasMoved = false;

        try {
            if (Integer.parseInt(stringPiece.substring(1, 2)) % 2 != 0) {
                hasMoved = true;
            }
        } catch (Exception e) {

        }

        if (stringPiece.charAt(0) < 'a') {
            player = Side.BLACK;
        } else {
            player = Side.WHITE;
        }

        PieceID id;
        char type = stringPiece.toUpperCase().charAt(0);

        id = charIDtoPieceID(type);

        if (id != null) {
            return new Piece(id, player, row, col, hasMoved);
        } else {
            return null;
        }

    }

    public static PieceID charIDtoPieceID(char type) {

        PieceID id;

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
                id = null;
        }

        return id;
    }

    public boolean equals(Piece piece) {

        return (piece != null
                && piece.getRow() == row
                && piece.getCol() == col
                && piece.getSide() == player
                && piece.getPieceID() == this.getPieceID());
    }

    public boolean isValidMove(int toRow, int toCol, long[] nullMoveInfo) {
        return isValidMove(toRow, toCol, nullMoveInfo, 0);
    }

    public boolean isValidMove(int toRow, int toCol, long[] nullMoveInfo, long enpassantAttack) {
        long mask = BitBoard.getMask(toRow, toCol);
        if (((mask | enpassantAttack) & nullMoveInfo[1]) != 0 & (mask & blockingVector) != 0) {
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

    public PieceID getPieceID() {
        return id;
    }

    public void setPieceID(PieceID id) {
        this.id = id;
    }

    public String getName() {
        switch (id) {
            case ROOK:
                return Rook.getName();
            case KNIGHT:
                return Knight.getName();
            case BISHOP:
                return Bishop.getName();
            case QUEEN:
                return Queen.getName();
            case KING:
                return King.getName();
            case PAWN:
                return Pawn.getName();
            default:
                return "";
        }
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

    public void getNullMoveInfo(Board board, long[] nullMoveInfo, long updown, long left, long right, long kingBitBoard, long kingCheckVectors, long friendly) {
        switch (id) {
            case ROOK:
                Rook.getNullMoveInfo(this, board, nullMoveInfo, updown, left, right, kingBitBoard, kingCheckVectors, friendly);
                break;
            case KNIGHT:
                //Knight.getNullMoveInfo(this, board, nullMoveBitBoards);
                break;
            case BISHOP:
                Bishop.getNullMoveInfo(this, board, nullMoveInfo, updown, left, right, kingBitBoard, kingCheckVectors, friendly);
                break;
            case QUEEN:
                Queen.getNullMoveInfo(this, board, nullMoveInfo, updown, left, right, kingBitBoard, kingCheckVectors, friendly);
                break;
            case KING:
                //King.getNullMoveInfo(this, board, nullMoveBitBoards);
                break;
            case PAWN:
                //Pawn.getNullMoveInfo(this, board, nullMoveBitBoards);
                break;
        }
    }

    public static List<Long> generateValidMoves(long footPrint, final Piece p, final Board board, final long[] nullMoveInfo, final long[] posBitBoard, final List<Long> validMoves) {
        final long foeBb = posBitBoard[p.getSide().otherSide().ordinal()];

        int bitNum;
        while ((bitNum = Long.numberOfTrailingZeros(footPrint)) < 64) {
            final long mask = (1L << bitNum);
            final int toRow = bitNum / 8;
            final int toCol = bitNum % 8;

            if (p.isValidMove(toRow, toCol, nullMoveInfo)) {

                final long move = ((foeBb & mask) == 0) ?
                        Move.moveLong(p.getRow(), p.getCol(), toRow, toCol) :
                        Move.moveLong(p.getRow(), p.getCol(), toRow, toCol, 0, Move.MoveNote.NONE, board.getPiece(toRow, toCol));

                validMoves.add(move);
            }

            footPrint ^= mask;
        }

        return validMoves;
    }

    public Piece getCopy() {
        return new Piece(id, player, row, col, moved);
    }

}
