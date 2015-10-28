package com.gordoncaleb.chess.board.bitboard;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import static com.gordoncaleb.chess.board.bitboard.BitBoard.*;
import static com.gordoncaleb.chess.board.bitboard.Slide.*;

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
        assertThat(southFillAndSlide(getMask(0, 4), bbA), is(equalTo(BitBoard.parseBitBoard(new String[]{
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
        assertThat(southFillAndSlide(getMask(0, 4), bbB), is(equalTo(BitBoard.parseBitBoard(new String[]{
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
        long result = northFillAndSlide(getMask(7, 4), bbA);
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
        result = northFillAndSlide(getMask(7, 4), bbB);
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
        long result = northWestFillAndSlide(getMask(7, 4), bbA);
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
        result = northWestFillAndSlide(getMask(7, 4), bbB);
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

        logger.info("(0,3)\n" + printBitBoard(southEastFill(getMask(0, 3))));
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
    public void testNorthSlideZeroIfNoCollision() {

        long allExceptMe = parseBitBoard(new String[]{
                "_,_,_,1,_,_,_,_,",
                "_,_,_,_,1,_,_,_,",
                "_,_,_,_,_,1,_,_,",
                "_,_,_,_,_,_,1,_,",
                "_,_,_,_,_,_,_,1,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,"
        });


        long me = parseBitBoard(new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,1,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,"
        });

        testNorthSlideNoEdge(me, allExceptMe, parseBitBoard(new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,"
        }));
    }

    @Test
    public void testNorthSlideNonZeroIfCollision() {

        long allExceptMe = parseBitBoard(new String[]{
                "_,_,_,1,_,_,_,_,",
                "_,_,_,_,1,_,_,_,",
                "_,_,_,_,_,1,_,_,",
                "_,_,_,_,_,_,1,_,",
                "_,_,_,_,_,_,_,1,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,"
        });


        long me = parseBitBoard(new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,1,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,"
        });

        testNorthSlideNoEdge(me, allExceptMe, parseBitBoard(new String[]{
                "_,_,_,1,_,_,_,_,",
                "_,_,_,1,_,_,_,_,",
                "_,_,_,1,_,_,_,_,",
                "_,_,_,1,_,_,_,_,",
                "_,_,_,1,_,_,_,_,",
                "_,_,_,1,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,"
        }));
    }

    public void testNorthSlideNoEdge(long me, long allExceptMe, long solution){
        long northFill = northFill(me);
        long northSlide = northSlideNoEdge(northFill, allExceptMe);

        logger.info("NorthSlideNoEdge\n" + printBitBoard(me) +
                "\n to \n" +
                printBitBoard(allExceptMe) +
                "\n is \n" +
                printBitBoard(northSlide)
        );

        assertThat(printBitBoard(northSlide),is(equalTo(printBitBoard(solution))));
    }

    @Test
    public void testSouthSlideNonZeroIfCollision() {

        long allExceptMe = parseBitBoard(new String[]{
                "_,_,_,1,_,_,_,_,",
                "_,_,_,_,1,_,_,_,",
                "_,_,_,_,_,1,_,_,",
                "_,_,_,_,_,_,1,_,",
                "_,_,_,_,_,1,_,_,",
                "_,_,_,_,1,_,_,_,",
                "_,_,_,1,_,_,_,_,",
                "_,_,1,_,_,_,_,_,"
        });


        long me = parseBitBoard(new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,1,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,"
        });

        testSouthSlideNoEdge(me, allExceptMe, parseBitBoard(new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,1,_,_,_,_,",
                "_,_,_,1,_,_,_,_,",
                "_,_,_,1,_,_,_,_,",
                "_,_,_,1,_,_,_,_,",
                "_,_,_,_,_,_,_,_,"
        }));
    }

    @Test
    public void testSouthSlideZeroIfNoCollision() {

        long allExceptMe = parseBitBoard(new String[]{
                "_,_,_,1,_,_,_,_,",
                "_,_,_,_,1,_,_,_,",
                "_,_,_,_,_,1,_,_,",
                "_,_,_,_,_,_,1,_,",
                "_,_,_,_,_,1,_,_,",
                "_,_,_,_,1,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,1,_,_,_,_,_,"
        });


        long me = parseBitBoard(new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,1,_,_,_,_,",
                "_,_,_,_,_,_,_,_,"
        });

        testSouthSlideNoEdge(me, allExceptMe, parseBitBoard(new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,"
        }));
    }

    public void testSouthSlideNoEdge(long me, long allExceptMe, long solution){
        long southFill = southFill(me);
        long southSlide = southSlideNoEdge(southFill, allExceptMe);

        logger.info("SouthSlideNoEdge\n" + printBitBoard(me) +
                "\n to \n" +
                printBitBoard(allExceptMe) +
                "\n is \n" +
                printBitBoard(southSlide)
        );

        assertThat(printBitBoard(southSlide),is(equalTo(printBitBoard(solution))));
    }


}
