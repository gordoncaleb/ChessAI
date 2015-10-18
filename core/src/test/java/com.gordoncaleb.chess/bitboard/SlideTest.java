package com.gordoncaleb.chess.bitboard;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import static com.gordoncaleb.chess.bitboard.BitBoard.*;
import static com.gordoncaleb.chess.bitboard.Slide.*;

public class SlideTest {
    public static final Logger logger = LoggerFactory.getLogger(SlideTest.class);

    @Test
    public void testSlideSouthVector() {
        assertThat(southFill(getMask(0, 3)), is(equalTo(BitBoard.parseBitBoard(new String[]{
                "_,_,_,1,_,_,_,_,",
                "_,_,_,1,_,_,_,_,",
                "_,_,_,1,_,_,_,_,",
                "_,_,_,1,_,_,_,_,",
                "_,_,_,1,_,_,_,_,",
                "_,_,_,1,_,_,_,_,",
                "_,_,_,1,_,_,_,_,",
                "_,_,_,1,_,_,_,_,"
        }))));

        assertThat(southFill(getMask(7, 3)), is(equalTo(BitBoard.parseBitBoard(new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,1,_,_,_,_,"
        }))));

        assertThat(southFill(getMask(3, 0)), is(equalTo(BitBoard.parseBitBoard(new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "1,_,_,_,_,_,_,_,",
                "1,_,_,_,_,_,_,_,",
                "1,_,_,_,_,_,_,_,",
                "1,_,_,_,_,_,_,_,",
                "1,_,_,_,_,_,_,_,"
        }))));
    }

    @Test
    public void testSlideSouth() {

        String[] bbStringA = new String[]{
                "_,_,_,_,_,_,_,1,",
                "1,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,1,_,_,_,",
                "_,_,_,_,_,_,_,1,",
                "_,_,_,_,_,_,_,_,",
                "_,_,1,_,_,_,_,_,"
        };

        long bbA = BitBoard.parseBitBoard(bbStringA);
        assertThat(south(getMask(0, 4), bbA), is(equalTo(BitBoard.parseBitBoard(new String[]{
                "_,_,_,_,1,_,_,_,",
                "_,_,_,_,1,_,_,_,",
                "_,_,_,_,1,_,_,_,",
                "_,_,_,_,1,_,_,_,",
                "_,_,_,_,1,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,"
        }))));

        String[] bbStringB = new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,1,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,1,_,_,_,_,_,_,",
                "_,_,_,_,1,_,_,_,",
                "_,_,_,_,_,_,_,1,"
        };
        long bbB = BitBoard.parseBitBoard(bbStringB);
        assertThat(south(getMask(0, 4), bbB), is(equalTo(BitBoard.parseBitBoard(new String[]{
                "_,_,_,_,1,_,_,_,",
                "_,_,_,_,1,_,_,_,",
                "_,_,_,_,1,_,_,_,",
                "_,_,_,_,1,_,_,_,",
                "_,_,_,_,1,_,_,_,",
                "_,_,_,_,1,_,_,_,",
                "_,_,_,_,1,_,_,_,",
                "_,_,_,_,_,_,_,_,"
        }))));
    }

    @Test
    public void testSlideNorthVector() {
        assertThat(northFill(getMask(0, 3)), is(equalTo(BitBoard.parseBitBoard(new String[]{
                "_,_,_,1,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,"
        }))));

        assertThat(northFill(getMask(7, 3)), is(equalTo(BitBoard.parseBitBoard(new String[]{
                "_,_,_,1,_,_,_,_,",
                "_,_,_,1,_,_,_,_,",
                "_,_,_,1,_,_,_,_,",
                "_,_,_,1,_,_,_,_,",
                "_,_,_,1,_,_,_,_,",
                "_,_,_,1,_,_,_,_,",
                "_,_,_,1,_,_,_,_,",
                "_,_,_,1,_,_,_,_,"
        }))));

        assertThat(northFill(getMask(3, 0)), is(equalTo(BitBoard.parseBitBoard(new String[]{
                "1,_,_,_,_,_,_,_,",
                "1,_,_,_,_,_,_,_,",
                "1,_,_,_,_,_,_,_,",
                "1,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,"
        }))));
    }

    @Test
    public void testSlideNorth() {

        String[] bbStringA = new String[]{
                "_,_,_,_,_,_,_,1,",
                "1,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,1,_,_,_,",
                "_,_,_,_,_,_,_,1,",
                "_,_,_,_,_,_,_,_,",
                "_,_,1,_,_,_,_,_,"
        };

        long bbA = BitBoard.parseBitBoard(bbStringA);
        long result = north(getMask(7, 4), bbA);
        logger.info("\n" + BitBoard.printBitBoard(result));
        assertThat(result, is(equalTo(BitBoard.parseBitBoard(new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,1,_,_,_,",
                "_,_,_,_,1,_,_,_,",
                "_,_,_,_,1,_,_,_,",
                "_,_,_,_,1,_,_,_,"
        }))));

        String[] bbStringB = new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,1,_,_,_,",
                "_,1,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,1,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,1,"
        };

        long bbB = BitBoard.parseBitBoard(bbStringB);
        result = north(getMask(7, 4), bbB);
        logger.info("\n" + BitBoard.printBitBoard(result));
        assertThat(result, is(equalTo(BitBoard.parseBitBoard(new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,1,_,_,_,",
                "_,_,_,_,1,_,_,_,",
                "_,_,_,_,1,_,_,_,",
                "_,_,_,_,1,_,_,_,",
                "_,_,_,_,1,_,_,_,",
                "_,_,_,_,1,_,_,_,"
        }))));
    }

    @Test
    public void testSlideEastVector() {
        assertThat(eastFill(getMask(0, 3)), is(equalTo(BitBoard.parseBitBoard(new String[]{
                "_,_,_,1,1,1,1,1,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,"
        }))));

        assertThat(eastFill(getMask(7, 3)), is(equalTo(BitBoard.parseBitBoard(new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,1,1,1,1,1,"
        }))));

        assertThat(eastFill(getMask(3, 0)), is(equalTo(BitBoard.parseBitBoard(new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "1,1,1,1,1,1,1,1,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,"
        }))));
    }

    @Test
    public void testSlideWestVector() {
        logger.info("\n" + BitBoard.printBitBoard(westFill(getMask(0, 3))));
        assertThat(westFill(getMask(0, 3)), is(equalTo(BitBoard.parseBitBoard(new String[]{
                "1,1,1,1,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,"
        }))));

        logger.info("\n" + BitBoard.printBitBoard(westFill(getMask(7, 3))));
        assertThat(westFill(getMask(7, 3)), is(equalTo(BitBoard.parseBitBoard(new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "1,1,1,1,_,_,_,_,"
        }))));

        logger.info("\n" + BitBoard.printBitBoard(westFill(getMask(3, 7))));
        assertThat(westFill(getMask(3, 7)), is(equalTo(BitBoard.parseBitBoard(new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "1,1,1,1,1,1,1,1,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,"
        }))));
    }

    @Test
    public void testSlideNorthWestVector() {
        logger.info("(3,5)\n" + BitBoard.printBitBoard(northWestFill(getMask(3, 5))));
        assertThat(northWestFill(getMask(3, 5)), is(equalTo(BitBoard.parseBitBoard(new String[]{
                "_,_,1,_,_,_,_,_,",
                "_,_,_,1,_,_,_,_,",
                "_,_,_,_,1,_,_,_,",
                "_,_,_,_,_,1,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,"
        }))));

        logger.info("(7,7)\n" + BitBoard.printBitBoard(northWestFill(getMask(7, 7))));
        assertThat(northWestFill(getMask(7, 7)), is(equalTo(BitBoard.parseBitBoard(new String[]{
                "1,_,_,_,_,_,_,_,",
                "_,1,_,_,_,_,_,_,",
                "_,_,1,_,_,_,_,_,",
                "_,_,_,1,_,_,_,_,",
                "_,_,_,_,1,_,_,_,",
                "_,_,_,_,_,1,_,_,",
                "_,_,_,_,_,_,1,_,",
                "_,_,_,_,_,_,_,1,"
        }))));

        logger.info("(3,7)\n" + BitBoard.printBitBoard(northWestFill(getMask(3, 7))));
        assertThat(northWestFill(getMask(3, 7)), is(equalTo(BitBoard.parseBitBoard(new String[]{
                "_,_,_,_,1,_,_,_,",
                "_,_,_,_,_,1,_,_,",
                "_,_,_,_,_,_,1,_,",
                "_,_,_,_,_,_,_,1,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,"
        }))));
    }

    @Test
    public void testSlideNorthWest() {

        String[] bbStringA = new String[]{
                "_,_,_,_,_,_,_,1,",
                "1,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,1,_,_,_,",
                "_,_,_,_,_,_,_,1,",
                "_,_,_,_,_,_,_,_,",
                "_,_,1,_,_,_,_,_,"
        };

        long bbA = BitBoard.parseBitBoard(bbStringA);
        long result = northWest(getMask(7, 4), bbA);
        logger.info("\n" + BitBoard.printBitBoard(result));
        assertThat(result, is(equalTo(BitBoard.parseBitBoard(new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "1,_,_,_,_,_,_,_,",
                "_,1,_,_,_,_,_,_,",
                "_,_,1,_,_,_,_,_,",
                "_,_,_,1,_,_,_,_,",
                "_,_,_,_,1,_,_,_,"
        }))));

        String[] bbStringB = new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,1,_,_,_,",
                "_,1,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,1,1,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,1,"
        };

        long bbB = BitBoard.parseBitBoard(bbStringB);
        result = northWest(getMask(7, 4), bbB);
        logger.info("\n" + BitBoard.printBitBoard(result));
        assertThat(result, is(equalTo(BitBoard.parseBitBoard(new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,1,_,_,_,_,_,",
                "_,_,_,1,_,_,_,_,",
                "_,_,_,_,1,_,_,_,"
        }))));
    }

    @Test
    public void testSlideNorthEastVector() {
        logger.info("(3,5)\n" + BitBoard.printBitBoard(northEastFill(getMask(3, 5))));
        assertThat(northEastFill(getMask(3, 5)), is(equalTo(BitBoard.parseBitBoard(new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,1,",
                "_,_,_,_,_,_,1,_,",
                "_,_,_,_,_,1,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,"
        }))));

        logger.info("(7,0)\n" + BitBoard.printBitBoard(northEastFill(getMask(7, 0))));
        assertThat(northEastFill(getMask(7, 0)), is(equalTo(BitBoard.parseBitBoard(new String[]{
                "_,_,_,_,_,_,_,1,",
                "_,_,_,_,_,_,1,_,",
                "_,_,_,_,_,1,_,_,",
                "_,_,_,_,1,_,_,_,",
                "_,_,_,1,_,_,_,_,",
                "_,_,1,_,_,_,_,_,",
                "_,1,_,_,_,_,_,_,",
                "1,_,_,_,_,_,_,_,"
        }))));

        logger.info("(3,0)\n" + BitBoard.printBitBoard(northEastFill(getMask(3, 0))));
        assertThat(northEastFill(getMask(3, 0)), is(equalTo(BitBoard.parseBitBoard(new String[]{
                "_,_,_,1,_,_,_,_,",
                "_,_,1,_,_,_,_,_,",
                "_,1,_,_,_,_,_,_,",
                "1,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,"
        }))));
    }

    @Test
    public void testSlideSouthWestVector() {
        logger.info("(3,5)\n" + BitBoard.printBitBoard(southWestFill(getMask(3, 5))));
        assertThat(southWestFill(getMask(3, 5)), is(equalTo(BitBoard.parseBitBoard(new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,1,_,_,",
                "_,_,_,_,1,_,_,_,",
                "_,_,_,1,_,_,_,_,",
                "_,_,1,_,_,_,_,_,",
                "_,1,_,_,_,_,_,_,"
        }))));

        logger.info("(0,7)\n" + BitBoard.printBitBoard(southWestFill(getMask(0, 7))));
        assertThat(southWestFill(getMask(0, 7)), is(equalTo(BitBoard.parseBitBoard(new String[]{
                "_,_,_,_,_,_,_,1,",
                "_,_,_,_,_,_,1,_,",
                "_,_,_,_,_,1,_,_,",
                "_,_,_,_,1,_,_,_,",
                "_,_,_,1,_,_,_,_,",
                "_,_,1,_,_,_,_,_,",
                "_,1,_,_,_,_,_,_,",
                "1,_,_,_,_,_,_,_,"
        }))));

        logger.info("(3,7)\n" + BitBoard.printBitBoard(southWestFill(getMask(3, 7))));
        assertThat(southWestFill(getMask(3, 7)), is(equalTo(BitBoard.parseBitBoard(new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,1,",
                "_,_,_,_,_,_,1,_,",
                "_,_,_,_,_,1,_,_,",
                "_,_,_,_,1,_,_,_,",
                "_,_,_,1,_,_,_,_,"
        }))));
    }

    @Test
    public void testSlideSouthEastVector() {
        logger.info("(3,5)\n" + BitBoard.printBitBoard(southEastFill(getMask(3, 5))));
        assertThat(southEastFill(getMask(3, 5)), is(equalTo(BitBoard.parseBitBoard(new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,1,_,_,",
                "_,_,_,_,_,_,1,_,",
                "_,_,_,_,_,_,_,1,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,"
        }))));

        logger.info("(0,0)\n" + BitBoard.printBitBoard(southEastFill(getMask(0, 0))));
        assertThat(southEastFill(getMask(0, 0)), is(equalTo(BitBoard.parseBitBoard(new String[]{
                "1,_,_,_,_,_,_,_,",
                "_,1,_,_,_,_,_,_,",
                "_,_,1,_,_,_,_,_,",
                "_,_,_,1,_,_,_,_,",
                "_,_,_,_,1,_,_,_,",
                "_,_,_,_,_,1,_,_,",
                "_,_,_,_,_,_,1,_,",
                "_,_,_,_,_,_,_,1,"
        }))));

        logger.info("(0,3)\n" + BitBoard.printBitBoard(southEastFill(getMask(0, 3))));
        assertThat(southEastFill(getMask(0, 3)), is(equalTo(BitBoard.parseBitBoard(new String[]{
                "_,_,_,1,_,_,_,_,",
                "_,_,_,_,1,_,_,_,",
                "_,_,_,_,_,1,_,_,",
                "_,_,_,_,_,_,1,_,",
                "_,_,_,_,_,_,_,1,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,"
        }))));
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
        long friend = BitBoard.parseBitBoard(friendString) | BitBoard.getMask(r, c);
        long foe = BitBoard.parseBitBoard(foeString);
        long friendOrFoe = friend | foe;

        long bbSolution = BitBoard.parseBitBoard(solution);

        long result = Slide.slideAllDirections(r, c, friendOrFoe, friend);
        logger.info("Friend Of Foe\n" + BitBoard.printBitBoard(friend | foe));
        logger.info("(" + r + "," + c + ")\n" + BitBoard.printBitBoard(result));
        assertThat(BitBoard.printBitBoard(result), is(equalTo(BitBoard.printBitBoard(bbSolution))));
    }

}
