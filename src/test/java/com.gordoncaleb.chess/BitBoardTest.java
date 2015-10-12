package com.gordoncaleb.chess;

import com.gordoncaleb.chess.backend.BitBoard;
import com.gordoncaleb.chess.backend.Move;
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

        verifyBitBoardToMoves(bbString, solution);
    }

    private void bitBoardToMoves(int fromRow, int fromCol, long bb, List<Long> moves) {
        BitBoard.bitNumbers(bb).stream()
                .map(n -> Move.moveLong(fromRow, fromCol, n / 8, n % 8))
                .forEach(m -> moves.add(m));
    }

    private void verifyBitBoardToMoves(String[] bbString, List<Integer> solution) {
        List<Long> moves = new ArrayList<>();
        long bb = BitBoard.parseBitBoard(bbString);
        bitBoardToMoves(0, 3, bb, moves);

        assertThat(moves.size(), is(equalTo(solution.size() / 2)));

        List<Integer> tos = moves.stream()
                .map(m -> new Integer[]{Move.getToRow(m), Move.getToCol(m)})
                .flatMap(Stream::of)
                .collect(Collectors.toList());

        assertThat(solution, is(equalTo(tos)));
    }

    @Test
    public void testSlideSouthVector() {
        assertThat(BitBoard.slideSouth[0][3], is(equalTo(BitBoard.parseBitBoard(new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,1,_,_,_,_,",
                "_,_,_,1,_,_,_,_,",
                "_,_,_,1,_,_,_,_,",
                "_,_,_,1,_,_,_,_,",
                "_,_,_,1,_,_,_,_,",
                "_,_,_,1,_,_,_,_,",
                "_,_,_,1,_,_,_,_,"
        }))));

        assertThat(BitBoard.slideSouth[7][3], is(equalTo(BitBoard.parseBitBoard(new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,"
        }))));

        assertThat(BitBoard.slideSouth[3][0], is(equalTo(BitBoard.parseBitBoard(new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "1,_,_,_,_,_,_,_,",
                "1,_,_,_,_,_,_,_,",
                "1,_,_,_,_,_,_,_,",
                "1,_,_,_,_,_,_,_,"
        }))));
    }

    @Test
    public void testSlideSouth() {

        String[] bbStringFriend = new String[]{
                "_,_,_,_,_,_,_,1,",
                "1,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,1,_,_,_,",
                "_,_,_,_,_,_,_,1,",
                "_,_,_,_,_,_,_,_,",
                "_,_,1,_,_,_,_,_,"
        };

        String[] bbStringFoe = new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,1,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,1,_,_,_,_,_,_,",
                "_,_,_,_,1,_,_,_,",
                "_,_,_,_,_,_,_,1,"
        };

        long friend = BitBoard.parseBitBoard(bbStringFriend);
        long foe = BitBoard.parseBitBoard(bbStringFoe);
        assertThat(BitBoard.slideSouth(0, 4, friend, foe), is(equalTo(BitBoard.parseBitBoard(new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,1,_,_,_,",
                "_,_,_,_,1,_,_,_,",
                "_,_,_,_,1,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,"
        }))));
    }

    @Test
    public void testSlideNorthVector() {
        assertThat(BitBoard.slideNorth[0][3], is(equalTo(BitBoard.parseBitBoard(new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,"
        }))));

        assertThat(BitBoard.slideNorth[7][3], is(equalTo(BitBoard.parseBitBoard(new String[]{
                "_,_,_,1,_,_,_,_,",
                "_,_,_,1,_,_,_,_,",
                "_,_,_,1,_,_,_,_,",
                "_,_,_,1,_,_,_,_,",
                "_,_,_,1,_,_,_,_,",
                "_,_,_,1,_,_,_,_,",
                "_,_,_,1,_,_,_,_,",
                "_,_,_,_,_,_,_,_,"
        }))));

        assertThat(BitBoard.slideNorth[3][0], is(equalTo(BitBoard.parseBitBoard(new String[]{
                "1,_,_,_,_,_,_,_,",
                "1,_,_,_,_,_,_,_,",
                "1,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,"
        }))));
    }

    @Test
    public void testSlideNorth() {

        String[] bbStringFriend = new String[]{
                "_,_,_,_,_,_,_,1,",
                "1,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,1,_,_,_,",
                "_,_,_,_,_,_,_,1,",
                "_,_,_,_,_,_,_,_,",
                "_,_,1,_,_,_,_,_,"
        };

        String[] bbStringFoe = new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,1,_,_,_,",
                "_,1,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,1,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,1,"
        };

        long friend = BitBoard.parseBitBoard(bbStringFriend);
        long foe = BitBoard.parseBitBoard(bbStringFoe);
        logger.info("\n" + BitBoard.printBitBoard(BitBoard.slideNorth(7, 4, friend, foe)));
    }

    @Test
    public void testSlideEastVector() {
        assertThat(BitBoard.slideEast[0][3], is(equalTo(BitBoard.parseBitBoard(new String[]{
                "_,_,_,_,1,1,1,1,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,"
        }))));

        assertThat(BitBoard.slideEast[7][3], is(equalTo(BitBoard.parseBitBoard(new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,1,1,1,1,"
        }))));

        assertThat(BitBoard.slideEast[3][0], is(equalTo(BitBoard.parseBitBoard(new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,1,1,1,1,1,1,1,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,"
        }))));
    }

    @Test
    public void testSlideWestVector() {
        logger.info("\n" + BitBoard.printBitBoard(BitBoard.slideWest[0][3]));
        assertThat(BitBoard.slideWest[0][3], is(equalTo(BitBoard.parseBitBoard(new String[]{
                "1,1,1,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,"
        }))));

        logger.info("\n" + BitBoard.printBitBoard(BitBoard.slideWest[7][3]));
        assertThat(BitBoard.slideWest[7][3], is(equalTo(BitBoard.parseBitBoard(new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "1,1,1,_,_,_,_,_,"
        }))));

        logger.info("\n" + BitBoard.printBitBoard(BitBoard.slideWest[3][7]));
        assertThat(BitBoard.slideWest[3][7], is(equalTo(BitBoard.parseBitBoard(new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "1,1,1,1,1,1,1,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,"
        }))));
    }

    @Test
    public void testSlideNorthWestVector() {
        logger.info("(3,5)\n" + BitBoard.printBitBoard(BitBoard.slideNorthWest[3][5]));
        assertThat(BitBoard.slideNorthWest[3][5], is(equalTo(BitBoard.parseBitBoard(new String[]{
                "_,_,1,_,_,_,_,_,",
                "_,_,_,1,_,_,_,_,",
                "_,_,_,_,1,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,"
        }))));

        logger.info("(7,7)\n" + BitBoard.printBitBoard(BitBoard.slideNorthWest[7][7]));
        assertThat(BitBoard.slideNorthWest[7][7], is(equalTo(BitBoard.parseBitBoard(new String[]{
                "1,_,_,_,_,_,_,_,",
                "_,1,_,_,_,_,_,_,",
                "_,_,1,_,_,_,_,_,",
                "_,_,_,1,_,_,_,_,",
                "_,_,_,_,1,_,_,_,",
                "_,_,_,_,_,1,_,_,",
                "_,_,_,_,_,_,1,_,",
                "_,_,_,_,_,_,_,_,"
        }))));

        logger.info("(3,7)\n" + BitBoard.printBitBoard(BitBoard.slideNorthWest[3][7]));
        assertThat(BitBoard.slideNorthWest[3][7], is(equalTo(BitBoard.parseBitBoard(new String[]{
                "_,_,_,_,1,_,_,_,",
                "_,_,_,_,_,1,_,_,",
                "_,_,_,_,_,_,1,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,"
        }))));
    }

    @Test
    public void testSlideNorthEastVector() {
        logger.info("(3,5)\n" + BitBoard.printBitBoard(BitBoard.slideNorthEast[3][5]));
        assertThat(BitBoard.slideNorthEast[3][5], is(equalTo(BitBoard.parseBitBoard(new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,1,",
                "_,_,_,_,_,_,1,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,"
        }))));

        logger.info("(7,0)\n" + BitBoard.printBitBoard(BitBoard.slideNorthEast[7][0]));
        assertThat(BitBoard.slideNorthEast[7][0], is(equalTo(BitBoard.parseBitBoard(new String[]{
                "_,_,_,_,_,_,_,1,",
                "_,_,_,_,_,_,1,_,",
                "_,_,_,_,_,1,_,_,",
                "_,_,_,_,1,_,_,_,",
                "_,_,_,1,_,_,_,_,",
                "_,_,1,_,_,_,_,_,",
                "_,1,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,"
        }))));

        logger.info("(3,0)\n" + BitBoard.printBitBoard(BitBoard.slideNorthEast[3][0]));
        assertThat(BitBoard.slideNorthEast[3][0], is(equalTo(BitBoard.parseBitBoard(new String[]{
                "_,_,_,1,_,_,_,_,",
                "_,_,1,_,_,_,_,_,",
                "_,1,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,"
        }))));
    }

    @Test
    public void testSlideSouthWestVector() {
        logger.info("(3,5)\n" + BitBoard.printBitBoard(BitBoard.slideSouthWest[3][5]));
        assertThat(BitBoard.slideSouthWest[3][5], is(equalTo(BitBoard.parseBitBoard(new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,1,_,_,_,",
                "_,_,_,1,_,_,_,_,",
                "_,_,1,_,_,_,_,_,",
                "_,1,_,_,_,_,_,_,"
        }))));

        logger.info("(0,7)\n" + BitBoard.printBitBoard(BitBoard.slideSouthWest[0][7]));
        assertThat(BitBoard.slideSouthWest[0][7], is(equalTo(BitBoard.parseBitBoard(new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,1,_,",
                "_,_,_,_,_,1,_,_,",
                "_,_,_,_,1,_,_,_,",
                "_,_,_,1,_,_,_,_,",
                "_,_,1,_,_,_,_,_,",
                "_,1,_,_,_,_,_,_,",
                "1,_,_,_,_,_,_,_,"
        }))));

        logger.info("(3,7)\n" + BitBoard.printBitBoard(BitBoard.slideSouthWest[3][7]));
        assertThat(BitBoard.slideSouthWest[3][7], is(equalTo(BitBoard.parseBitBoard(new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,1,_,",
                "_,_,_,_,_,1,_,_,",
                "_,_,_,_,1,_,_,_,",
                "_,_,_,1,_,_,_,_,"
        }))));
    }

    @Test
    public void testSlideSouthEastVector() {
        logger.info("(3,5)\n" + BitBoard.printBitBoard(BitBoard.slideSouthEast[3][5]));
        assertThat(BitBoard.slideSouthEast[3][5], is(equalTo(BitBoard.parseBitBoard(new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,1,_,",
                "_,_,_,_,_,_,_,1,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,"
        }))));

        logger.info("(0,0)\n" + BitBoard.printBitBoard(BitBoard.slideSouthEast[0][0]));
        assertThat(BitBoard.slideSouthEast[0][0], is(equalTo(BitBoard.parseBitBoard(new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,1,_,_,_,_,_,_,",
                "_,_,1,_,_,_,_,_,",
                "_,_,_,1,_,_,_,_,",
                "_,_,_,_,1,_,_,_,",
                "_,_,_,_,_,1,_,_,",
                "_,_,_,_,_,_,1,_,",
                "_,_,_,_,_,_,_,1,"
        }))));

        logger.info("(0,3)\n" + BitBoard.printBitBoard(BitBoard.slideSouthEast[0][3]));
        assertThat(BitBoard.slideSouthEast[0][3], is(equalTo(BitBoard.parseBitBoard(new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,1,_,_,_,",
                "_,_,_,_,_,1,_,_,",
                "_,_,_,_,_,_,1,_,",
                "_,_,_,_,_,_,_,1,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,"
        }))));
    }

}
