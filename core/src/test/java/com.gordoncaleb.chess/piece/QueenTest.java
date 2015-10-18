package com.gordoncaleb.chess.piece;

import com.gordoncaleb.chess.bitboard.BitBoard;
import com.gordoncaleb.chess.backend.Side;
import com.gordoncaleb.chess.pieces.King;
import com.gordoncaleb.chess.pieces.Piece;
import com.gordoncaleb.chess.pieces.Queen;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class QueenTest {

    public static final Logger logger = LoggerFactory.getLogger(QueenTest.class);

    @Test
    public void test() {
        long[] nullMoveInfo = new long[3];

        nullMoveInfo[1] = -1L;

        Piece queen = new Piece(Piece.PieceID.QUEEN, Side.WHITE, 6, 0, false);
        long piece = queen.getBit();
        long kingBitBoard = BitBoard.getMask(1, 0);

        long friendly = kingBitBoard | BitBoard.getMask(1, 5);// |
        // BitBoard.getMask(4,0);
        long enemy = piece | BitBoard.getMask(4, 4);
        long bb = friendly | enemy;

        long updown = ~bb;
        long left = 0xFEFEFEFEFEFEFEFEL & ~bb;
        long right = 0x7F7F7F7F7F7F7F7FL & ~bb;

        logger.info("pos\n" + BitBoard.printBitBoard(bb));

        long kingCheckVectors = King.getKingCheckVectors(kingBitBoard, updown, left, right);

        logger.info("king check\n" + BitBoard.printBitBoard(kingCheckVectors));

        Queen.getNullMoveInfo(queen, null, nullMoveInfo, updown, left, right, kingBitBoard, kingCheckVectors, friendly);

        logger.info("king\n" + BitBoard.printBitBoard(kingBitBoard));

        logger.info("[0]\n" + BitBoard.printBitBoard(nullMoveInfo[0]));
        logger.info("[1]\n" + BitBoard.printBitBoard(nullMoveInfo[1]));
        logger.info("[2]\n" + BitBoard.printBitBoard(nullMoveInfo[2]));
    }

    @Test
    public void testSlideAllDirections1() {

        String[] friendString = new String[]{
                "_,_,_,_,_,_,_,1,",
                "1,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,1,",
                "_,_,_,_,_,_,_,_,",
                "_,_,1,_,_,_,_,_,"
        };

        String[] foeString = new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,1,_,_,_,",
                "_,1,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,1,1,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,1,"
        };

        testFixtureAllDirections(friendString, foeString, 4, 4, new String[]{
                "1,_,_,_,_,_,_,_,",
                "_,1,_,_,_,_,_,1,",
                "_,_,1,_,1,_,1,_,",
                "_,_,_,1,1,1,_,_,",
                "1,1,1,1,_,1,1,1,",
                "_,_,_,1,1,1,_,_,",
                "_,_,1,_,1,_,1,_,",
                "_,1,_,_,1,_,_,1,"
        });

    }

    @Test
    public void testSlideAllDirections2() {

        String[] friendString = new String[]{
                "1,_,_,_,1,_,_,1,",
                "1,_,_,_,_,_,1,1,",
                "_,1,_,1,_,1,_,_,",
                "_,_,1,_,_,_,1,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,"
        };

        String[] foeString = new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,1,1,_,_,_,_,",
                "_,_,1,_,_,_,1,_,",
                "1,1,_,_,_,1,_,1,",
                "1,_,_,_,_,1,1,_,"
        };

        testFixtureAllDirections(friendString, foeString, 3, 3, new String[]{
                "_,_,_,_,_,_,1,_,",
                "_,1,_,_,_,1,_,_,",
                "_,_,1,_,1,_,_,_,",
                "_,_,_,_,1,1,_,_,",
                "_,_,1,1,1,_,_,_,",
                "_,_,_,_,_,1,_,_,",
                "_,_,_,_,_,_,1,_,",
                "_,_,_,_,_,_,_,1,"
        });

        testFixtureAllDirections(friendString, foeString, 3, 7, new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,1,_,_,",
                "_,_,_,_,_,_,1,1,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,1,1,",
                "_,_,_,_,_,1,_,1,",
                "_,_,_,_,1,_,_,1,",
                "_,_,_,1,_,_,_,_,"
        });

        testFixtureAllDirections(friendString, foeString, 5, 3, new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,1,_,",
                "_,_,_,_,_,1,_,_,",
                "_,_,1,1,1,_,_,_,",
                "_,_,1,_,1,1,1,_,",
                "_,_,1,1,1,_,_,_,",
                "_,1,_,1,_,1,_,_,"
        });

    }

    private void testFixtureAllDirections(String[] friendString, String[] foeString, int r, int c, String[] solution) {
        long mask = BitBoard.getMask(r, c);
        long friend = BitBoard.parseBitBoard(friendString) | mask;
        long foe = BitBoard.parseBitBoard(foeString);
        long friendOrFoe = friend | foe;

        long bbSolution = BitBoard.parseBitBoard(solution);

        long result = Queen.slideQueen(mask, friendOrFoe, friend);
        logger.info("Friend Of Foe\n" + BitBoard.printBitBoard(friendOrFoe));
        logger.info("(" + r + "," + c + ")\n" + BitBoard.printBitBoard(result));
        assertThat(BitBoard.printBitBoard(result), is(equalTo(BitBoard.printBitBoard(bbSolution))));
    }
}
