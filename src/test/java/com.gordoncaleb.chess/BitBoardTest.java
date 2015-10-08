package com.gordoncaleb.chess;

import com.gordoncaleb.chess.backend.Side;
import org.junit.Ignore;
import org.junit.Test;

import static com.gordoncaleb.chess.backend.BitBoard.*;

public class BitBoardTest {

    @Ignore
    @Test
    public void test() {

        // long t1 = System.currentTimeMillis();
        // for (long i = 0; i < 0xFFFFFFF; i++) {
        // bitCountLongHardWay(i);
        // }
        //
        // System.out.println("Hardway takes " + (System.currentTimeMillis() -
        // t1) + " ms");
        //
        // t1 = System.currentTimeMillis();
        // for (long i = 0; i < 0xFFFFFFF; i++) {
        // bitCountLong(i);
        // }
        //
        // System.out.println("Easy takes " + (System.currentTimeMillis() - t1)
        // + " ms");

        long val;

        // System.out.println("white castle near");
        // System.out.println(BitBoard.printBitBoard(WHITE_CASTLE_NEAR));
        //
        // System.out.println("white check near");
        // System.out.println(BitBoard.printBitBoard(WHITE_CHECK_NEAR));
        //
        // System.out.println("white check near hardway");
        // System.out.println(BitBoard.printBitBoard(getCastleMask(4, 2,
        // Side.WHITE)));
        //
        // for (int r = 0; r < 8; r++) {
        // System.out.println("Row mask =\n" +
        // BitBoard.printBitBoard(getRowMask(r)));
        // }
        //
        // for (int c = 0; c < 8; c++) {
        // System.out.println("col mask = \n" +
        // BitBoard.printBitBoard(getColMask(c)));
        // }
        //
        // for (int r = 0; r < 8; r++) {
        // System.out.println("Bottom Row mask =\n" +
        // BitBoard.printBitBoard(getBottomRows(r)));
        // }
        //
        // for (int c = 0; c < 8; c++) {
        // System.out.println("Top Row mask = \n" +
        // BitBoard.printBitBoard(getTopRows(c)));
        // }
        //
        // for (int r = 0; r < 8; r++) {
        // for (int c = 0; c < 8; c++) {
        // System.out.println("White pawn forward mask =" + r + "," + c + "\n" +
        // BitBoard.printBitBoard(getWhitePawnForward(r, c)));
        // }
        // }
        //
        // for (int r = 0; r < 8; r++) {
        // for (int c = 0; c < 8; c++) {
        // System.out.println("Black pawn forward mask =" + r + "," + c + "\n" +
        // BitBoard.printBitBoard(getBlackPawnForward(r, c)));
        // }
        // }

        // loadKnightFootPrints();

        long t1 = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            for (int r = 0; r < 8; r++) {
                for (int c = 0; c < 8; c++) {
                    // getKingFootPrint(r, c);
                }
            }
        }

        System.out.println("hardway " + (System.currentTimeMillis() - t1));

        t1 = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            for (int r = 0; r < 8; r++) {
                for (int c = 0; c < 8; c++) {
                    // getKingAttacks(getMask(r, c));
                }
            }
        }

        System.out.println("safeway " + (System.currentTimeMillis() - t1));

        // for (int r = 0; r < 8; r++) {
        // for (int c = 0; c < 8; c++) {
        // System.out.println(r + "," + c);
        // System.out.println(BitBoard.printBitBoard(getKnightFootPrint(r, c)));
        //
        // if (getKnightFootPrint(r, c) != getKnightFootPrintMem(r, c)) {
        // System.out.println("Error");
        // }
        // }
        // }

        // System.out.println(BitBoard.printBitBoard(0x7F7F7F7F7F7F7F7FL));

        String blackPawn =
                "0,0,0,0,0,0,0,0,\n" +
                        "0,0,1,0,1,0,1,0,\n" +
                        "0,0,0,1,0,0,0,0,\n" +
                        "0,1,0,0,0,0,0,0,\n" +
                        "1,0,0,0,0,0,0,0,\n" +
                        "0,0,0,0,0,0,0,0,\n" +
                        "0,0,0,0,0,0,0,0,\n" +
                        "0,0,0,0,0,0,0,0,\n";

        String whitePawn =
                "0,0,0,0,0,0,0,0,\n" +
                        "0,0,0,0,0,0,0,0,\n" +
                        "0,1,0,0,0,0,0,0,\n" +
                        "0,0,0,0,0,0,0,0,\n" +
                        "0,0,0,0,1,0,1,0,\n" +
                        "0,0,1,1,0,0,0,0,\n" +
                        "0,0,0,0,0,0,0,0,\n" +
                        "0,0,0,0,0,0,0,0,\n";

        long bp = parseBitBoard(blackPawn);

        long wp = parseBitBoard(whitePawn);

        long wpattacks = getPawnAttacks(wp, Side.WHITE);

        System.out.println(printBitBoard(~(northFill(wpattacks) | southFill(wpattacks)) & wp));

        //System.out.println(canQueen(wp,bp,Side.WHITE));

        String rooks =
                "0,0,0,0,0,0,0,0,\n" +
                        "0,0,0,0,0,0,0,0,\n" +
                        "0,0,0,0,0,0,0,0,\n" +
                        "0,0,0,0,0,0,0,0,\n" +
                        "0,0,0,0,0,0,0,0,\n" +
                        "0,0,0,0,0,0,0,0,\n" +
                        "1,0,0,0,0,0,0,0,\n" +
                        "0,0,0,0,0,1,0,0,\n";

        String king =
                "0,0,0,0,0,0,0,0,\n" +
                        "0,0,0,0,0,0,0,0,\n" +
                        "0,0,0,0,0,0,0,0,\n" +
                        "0,0,0,0,0,0,0,0,\n" +
                        "0,0,0,0,0,0,0,0,\n" +
                        "0,0,0,0,0,0,0,0,\n" +
                        "0,0,0,0,0,0,0,0,\n" +
                        "0,0,0,0,0,0,1,0,\n";

        long rooksbb = parseBitBoard(rooks);

        long kingbb = parseBitBoard(king);

        //System.out.println(isCastled(kingbb,rooksbb,Side.WHITE));

        // for (int r = 0; r < 8; r++) {
        // for (int c = 0; c < 8; c++) {
        // if (getKnightFootPrint(r, c) != getKnightAttacks(getMask(r, c))) {
        //
        // System.out.println(r + "," + c);
        // System.out.println(BitBoard.printBitBoard(getKnightFootPrint(r, c)));
        // System.out.println("!=");
        // System.out.println(BitBoard.printBitBoard(getKnightAttacks(getMask(r,
        // c))));
        //
        // System.out.println("Error!");
        // }
        // }
        // }

        // for (int r = 0; r < 8; r++) {
        // for (int c = 0; c < 8; c++) {
        // System.out.println(r + "," + c);
        // System.out.println(printBitBoard(getPosSlope(r, c)));
        // }
        // }

        // for (int r = 0; r < 8; r++) {
        // for (int c = 0; c < 8; c++) {
        // System.out.println(r + "," + c);
        // System.out.println(printBitBoard(getNegSlope(r, c)));
        // }
        // }

    }
}