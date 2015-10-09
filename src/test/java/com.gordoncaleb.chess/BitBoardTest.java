package com.gordoncaleb.chess;

import com.gordoncaleb.chess.backend.BitBoard;
import com.gordoncaleb.chess.backend.Move;
import com.gordoncaleb.chess.backend.Side;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.gordoncaleb.chess.backend.BitBoard.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

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

    @Test
    public void testBitBoardToMoves() {

        String[] bbString = new String[]{
                "1,_,_,_,_,_,_,1,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "1,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,1,"
        };

        List<Integer> solution = Arrays.asList(
                0, 0,
                0, 7,
                4, 0,
                7, 7
        );

        verifyBitBoardToMoves(bbString,solution);
    }

    private void verifyBitBoardToMoves(String[] bbString, List<Integer> solution) {
        List<Move> moves = new ArrayList<>();
        long bb = BitBoard.parseBitBoard(bbString);
        BitBoard.bitBoardToMoves(0, 3, bb, moves);

        assertThat(moves.size(), is(equalTo(solution.size() / 2)));

        List<Integer> tos = moves.stream()
                .map(m -> new Integer[]{m.getToRow(), m.getToCol()})
                .flatMap(Stream::of)
                .collect(Collectors.toList());

        assertThat(solution, is(equalTo(tos)));
    }

}
