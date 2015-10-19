package com.gordoncaleb.chess.piece;

import com.gordoncaleb.chess.bitboard.BitBoard;
import com.gordoncaleb.chess.backend.Board;
import com.gordoncaleb.chess.backend.Side;
import com.gordoncaleb.chess.io.XMLParser;
import com.gordoncaleb.chess.persistence.BoardDAO;
import com.gordoncaleb.chess.pieces.King;
import com.gordoncaleb.chess.pieces.Piece;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RookTest {

    @Test
    @Ignore
    public void test() {
        String b = "R0,N0,B0,Q0,K0,__,N0,R0," +
                "P0,P0,P0,P0,__,P0,P0,P0," +
                "__,__,__,__,__,__,__,__," +
                "__,__,__,__,P1,__,__,__," +
                "__,B0,__,__,__,__,__,__," +
                "__,__,n0,p1,__,__,__,__," +
                "p0,p0,p0,__,p0,p0,p0,p0," +
                "r0,__,b0,q0,k0,b0,n0,r0,";

        b = "<board>\n<setup>\n" + b + "</setup>\n<turn>WHITE</turn>\n</board>";

        Board board = XMLParser.XMLToBoard(b);

        Piece piece = board.getPiece(4, 1);
        long[] nullMoveInfo = new long[3];

        Side turn = board.getTurn();
        long updown = ~(board.getAllPosBitBoard()[0] | board.getAllPosBitBoard()[1]);
        long friendly = board.getAllPosBitBoard()[turn.ordinal()];
        long kingBitBoard = board.getPosBitBoard()[Piece.PieceID.KING][turn.ordinal()];

        long left = 0xFEFEFEFEFEFEFEFEL & updown;
        long right = 0x7F7F7F7F7F7F7F7FL & updown;

        long kingCheckVectors = King.getKingCheckVectors(board.getPosBitBoard()[Piece.PieceID.KING][turn.ordinal()], updown, left, right);

        piece.getNullMoveInfo(board, nullMoveInfo, updown, left, right, kingBitBoard, kingCheckVectors, friendly);

        System.out.println("updown\n" + BitBoard.printBitBoard(updown));
        System.out.println("left\n" + BitBoard.printBitBoard(left));
        System.out.println("right\n" + BitBoard.printBitBoard(right));

        System.out.println("kingCheckVectors\n" + BitBoard.printBitBoard(kingCheckVectors));


        System.out.println(BitBoard.printBitBoard(nullMoveInfo[0]));

    }

    @Test
    public void testRookWithCheck() throws Exception {
        BoardDAO boardDAO = new BoardDAO();
        Board board = boardDAO.getByFileName("/positions/testPosition9.json");

        Piece piece = board.getPiece(6, 0);
        long[] nullMoveInfo = new long[3];

        Side turn = board.getTurn();
        long updown = ~(board.getAllPosBitBoard()[0] | board.getAllPosBitBoard()[1]);
        long friendly = board.getAllPosBitBoard()[turn.ordinal()];
        long kingBitBoard = board.getPosBitBoard()[Piece.PieceID.KING][turn.ordinal()];

        long left = 0xFEFEFEFEFEFEFEFEL & updown;
        long right = 0x7F7F7F7F7F7F7F7FL & updown;

        long kingCheckVectors = King.getKingCheckVectors(board.getPosBitBoard()[Piece.PieceID.KING][turn.ordinal()], updown, left, right);

        piece.getNullMoveInfo(board, nullMoveInfo, updown, left, right, kingBitBoard, kingCheckVectors, friendly);

        System.out.println("updown\n" + BitBoard.printBitBoard(updown));
        System.out.println("left\n" + BitBoard.printBitBoard(left));
        System.out.println("right\n" + BitBoard.printBitBoard(right));

        System.out.println("kingCheckVectors\n" + BitBoard.printBitBoard(kingCheckVectors));

        System.out.println(BitBoard.printBitBoard(nullMoveInfo[0]));
        System.out.println(BitBoard.printBitBoard(nullMoveInfo[1]));
        System.out.println(BitBoard.printBitBoard(nullMoveInfo[2]));

        System.out.println(BitBoard.printBitBoard(piece.getBlockingVector()));

        assertEquals(-1, piece.getBlockingVector());
    }
}
