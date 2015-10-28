package com.gordoncaleb.chess.piece;

import com.gordoncaleb.chess.board.bitboard.BitBoard;
import com.gordoncaleb.chess.board.pieces.Queen;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import static com.gordoncaleb.chess.board.bitboard.BitBoard.*;

public class QueenTest {

    public static final Logger logger = LoggerFactory.getLogger(QueenTest.class);

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

        long result = Queen.slideQueen(mask, friendOrFoe) & ~friend;
        logger.info("Friend Of Foe\n" + BitBoard.printBitBoard(friendOrFoe));
        logger.info("(" + r + "," + c + ")\n" + BitBoard.printBitBoard(result));
        assertThat(BitBoard.printBitBoard(result), is(equalTo(BitBoard.printBitBoard(bbSolution))));
    }

    @Test
    public void testGetAttacks() {

        long friendOrFoe = parseBitBoard(new String[]{
                "1,_,_,_,1,_,_,1,",
                "1,_,_,_,_,_,1,1,",
                "_,1,_,1,_,1,_,_,",
                "_,_,1,_,_,_,1,_,",
                "_,_,1,1,_,_,_,_,",
                "_,_,1,_,_,_,1,_,",
                "1,1,_,_,_,1,_,1,",
                "1,_,_,_,_,1,1,_,"
        });

        long queens = parseBitBoard(new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,1,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,"
        });


        long[] nullMoveInfo = new long[]{0, ALL_ONES, 0};
        Queen.getQueenAttacks(queens, friendOrFoe, nullMoveInfo);

        assertThat(printBitBoard(nullMoveInfo[0]), is(equalTo(printBitBoard(parseBitBoard(new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,1,_,_,_,_,",
                "_,_,_,_,1,_,1,_,",
                "_,_,_,_,_,1,1,1,",
                "_,_,1,1,1,1,_,1,",
                "_,_,_,_,_,1,1,1,",
                "_,_,_,_,_,_,1,_,"
        })))));

    }


}
