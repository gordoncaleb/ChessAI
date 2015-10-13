package com.gordoncaleb.chess.piece;

import com.gordoncaleb.chess.bitboard.BitBoard;
import com.gordoncaleb.chess.backend.Side;
import com.gordoncaleb.chess.pieces.King;
import com.gordoncaleb.chess.pieces.Piece;
import com.gordoncaleb.chess.pieces.Queen;
import org.junit.Test;


public class QueenTest {

    @Test
    public  void test() {
        long[] nullMoveInfo = new long[3];

        nullMoveInfo[1] = -1L;

        Piece queen = new Piece(Piece.PieceID.QUEEN, Side.WHITE, 6, 0, false);
        long piece = queen.getBit();
        long kingBitBoard = BitBoard.getMask(1, 0);

        long friendly = kingBitBoard | BitBoard.getMask(1, 5);// |
        // BitBoard.getMask(4,
        // 0);
        long enemy = piece | BitBoard.getMask(4, 4);
        long bb = friendly | enemy;

        long updown = ~bb;
        long left = 0xFEFEFEFEFEFEFEFEL & ~bb;
        long right = 0x7F7F7F7F7F7F7F7FL & ~bb;

        System.out.println("pos\n" + BitBoard.printBitBoard(bb));

        long kingCheckVectors = King.getKingCheckVectors(kingBitBoard, updown, left, right);

        System.out.println("king check\n" + BitBoard.printBitBoard(kingCheckVectors));

        Queen.getNullMoveInfo(queen, null, nullMoveInfo, updown, left, right, kingBitBoard, kingCheckVectors, friendly);

        System.out.println("king\n" + BitBoard.printBitBoard(kingBitBoard));

        System.out.println("[0]\n" + BitBoard.printBitBoard(nullMoveInfo[0]));
        System.out.println("[1]\n" + BitBoard.printBitBoard(nullMoveInfo[1]));
        System.out.println("[2]\n" + BitBoard.printBitBoard(nullMoveInfo[2]));
    }
}
