package com.gordoncaleb.chess.bitboard;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class SlideTest {
    public static final Logger logger = LoggerFactory.getLogger(SlideTest.class);

    @Test
    public void testSlideSouthVector() {
        assertThat(Slide.slideSouth[0][3], is(equalTo(BitBoard.parseBitBoard(new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,1,_,_,_,_,",
                "_,_,_,1,_,_,_,_,",
                "_,_,_,1,_,_,_,_,",
                "_,_,_,1,_,_,_,_,",
                "_,_,_,1,_,_,_,_,",
                "_,_,_,1,_,_,_,_,",
                "_,_,_,1,_,_,_,_,"
        }))));

        assertThat(Slide.slideSouth[7][3], is(equalTo(BitBoard.parseBitBoard(new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,"
        }))));

        assertThat(Slide.slideSouth[3][0], is(equalTo(BitBoard.parseBitBoard(new String[]{
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
        assertThat(Slide.slideSouth(0, 4, friend | foe, friend), is(equalTo(BitBoard.parseBitBoard(new String[]{
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
        assertThat(Slide.slideNorth[0][3], is(equalTo(BitBoard.parseBitBoard(new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,"
        }))));

        assertThat(Slide.slideNorth[7][3], is(equalTo(BitBoard.parseBitBoard(new String[]{
                "_,_,_,1,_,_,_,_,",
                "_,_,_,1,_,_,_,_,",
                "_,_,_,1,_,_,_,_,",
                "_,_,_,1,_,_,_,_,",
                "_,_,_,1,_,_,_,_,",
                "_,_,_,1,_,_,_,_,",
                "_,_,_,1,_,_,_,_,",
                "_,_,_,_,_,_,_,_,"
        }))));

        assertThat(Slide.slideNorth[3][0], is(equalTo(BitBoard.parseBitBoard(new String[]{
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

        long friend = BitBoard.parseBitBoard(bbStringA);
        long foe = BitBoard.parseBitBoard(bbStringB);
        long result = Slide.slideNorth(7, 4, friend | foe, friend);
        logger.info("\n" + BitBoard.printBitBoard(result));
        assertThat(result, is(equalTo(BitBoard.parseBitBoard(new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,1,_,_,_,",
                "_,_,_,_,1,_,_,_,",
                "_,_,_,_,_,_,_,_,"
        }))));


        friend = BitBoard.parseBitBoard(bbStringB);
        foe = BitBoard.parseBitBoard(bbStringA);
        result = Slide.slideNorth(7, 4, friend | foe, friend);
        logger.info("\n" + BitBoard.printBitBoard(result));
        assertThat(result, is(equalTo(BitBoard.parseBitBoard(new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,1,_,_,_,",
                "_,_,_,_,1,_,_,_,",
                "_,_,_,_,1,_,_,_,",
                "_,_,_,_,_,_,_,_,"
        }))));
    }

    @Test
    public void testSlideEastVector() {
        assertThat(Slide.slideEast[0][3], is(equalTo(BitBoard.parseBitBoard(new String[]{
                "_,_,_,_,1,1,1,1,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,"
        }))));

        assertThat(Slide.slideEast[7][3], is(equalTo(BitBoard.parseBitBoard(new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,1,1,1,1,"
        }))));

        assertThat(Slide.slideEast[3][0], is(equalTo(BitBoard.parseBitBoard(new String[]{
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
        logger.info("\n" + BitBoard.printBitBoard(Slide.slideWest[0][3]));
        assertThat(Slide.slideWest[0][3], is(equalTo(BitBoard.parseBitBoard(new String[]{
                "1,1,1,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,"
        }))));

        logger.info("\n" + BitBoard.printBitBoard(Slide.slideWest[7][3]));
        assertThat(Slide.slideWest[7][3], is(equalTo(BitBoard.parseBitBoard(new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "1,1,1,_,_,_,_,_,"
        }))));

        logger.info("\n" + BitBoard.printBitBoard(Slide.slideWest[3][7]));
        assertThat(Slide.slideWest[3][7], is(equalTo(BitBoard.parseBitBoard(new String[]{
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
        logger.info("(3,5)\n" + BitBoard.printBitBoard(Slide.slideNorthWest[3][5]));
        assertThat(Slide.slideNorthWest[3][5], is(equalTo(BitBoard.parseBitBoard(new String[]{
                "_,_,1,_,_,_,_,_,",
                "_,_,_,1,_,_,_,_,",
                "_,_,_,_,1,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,"
        }))));

        logger.info("(7,7)\n" + BitBoard.printBitBoard(Slide.slideNorthWest[7][7]));
        assertThat(Slide.slideNorthWest[7][7], is(equalTo(BitBoard.parseBitBoard(new String[]{
                "1,_,_,_,_,_,_,_,",
                "_,1,_,_,_,_,_,_,",
                "_,_,1,_,_,_,_,_,",
                "_,_,_,1,_,_,_,_,",
                "_,_,_,_,1,_,_,_,",
                "_,_,_,_,_,1,_,_,",
                "_,_,_,_,_,_,1,_,",
                "_,_,_,_,_,_,_,_,"
        }))));

        logger.info("(3,7)\n" + BitBoard.printBitBoard(Slide.slideNorthWest[3][7]));
        assertThat(Slide.slideNorthWest[3][7], is(equalTo(BitBoard.parseBitBoard(new String[]{
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

        long friend = BitBoard.parseBitBoard(bbStringA);
        long foe = BitBoard.parseBitBoard(bbStringB);
        long result = Slide.slideNorthWest(7, 4, friend | foe, friend);
        logger.info("\n" + BitBoard.printBitBoard(result));
        assertThat(result, is(equalTo(BitBoard.parseBitBoard(new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,1,_,_,_,_,_,",
                "_,_,_,1,_,_,_,_,",
                "_,_,_,_,_,_,_,_,"
        }))));


        friend = BitBoard.parseBitBoard(bbStringB);
        foe = BitBoard.parseBitBoard(bbStringA);
        result = Slide.slideNorthWest(7, 4, friend | foe, friend);
        logger.info("\n" + BitBoard.printBitBoard(result));
        assertThat(result, is(equalTo(BitBoard.parseBitBoard(new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,1,_,_,_,_,",
                "_,_,_,_,_,_,_,_,"
        }))));
    }

    @Test
    public void testSlideNorthEastVector() {
        logger.info("(3,5)\n" + BitBoard.printBitBoard(Slide.slideNorthEast[3][5]));
        assertThat(Slide.slideNorthEast[3][5], is(equalTo(BitBoard.parseBitBoard(new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,1,",
                "_,_,_,_,_,_,1,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,"
        }))));

        logger.info("(7,0)\n" + BitBoard.printBitBoard(Slide.slideNorthEast[7][0]));
        assertThat(Slide.slideNorthEast[7][0], is(equalTo(BitBoard.parseBitBoard(new String[]{
                "_,_,_,_,_,_,_,1,",
                "_,_,_,_,_,_,1,_,",
                "_,_,_,_,_,1,_,_,",
                "_,_,_,_,1,_,_,_,",
                "_,_,_,1,_,_,_,_,",
                "_,_,1,_,_,_,_,_,",
                "_,1,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,"
        }))));

        logger.info("(3,0)\n" + BitBoard.printBitBoard(Slide.slideNorthEast[3][0]));
        assertThat(Slide.slideNorthEast[3][0], is(equalTo(BitBoard.parseBitBoard(new String[]{
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
        logger.info("(3,5)\n" + BitBoard.printBitBoard(Slide.slideSouthWest[3][5]));
        assertThat(Slide.slideSouthWest[3][5], is(equalTo(BitBoard.parseBitBoard(new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,1,_,_,_,",
                "_,_,_,1,_,_,_,_,",
                "_,_,1,_,_,_,_,_,",
                "_,1,_,_,_,_,_,_,"
        }))));

        logger.info("(0,7)\n" + BitBoard.printBitBoard(Slide.slideSouthWest[0][7]));
        assertThat(Slide.slideSouthWest[0][7], is(equalTo(BitBoard.parseBitBoard(new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,1,_,",
                "_,_,_,_,_,1,_,_,",
                "_,_,_,_,1,_,_,_,",
                "_,_,_,1,_,_,_,_,",
                "_,_,1,_,_,_,_,_,",
                "_,1,_,_,_,_,_,_,",
                "1,_,_,_,_,_,_,_,"
        }))));

        logger.info("(3,7)\n" + BitBoard.printBitBoard(Slide.slideSouthWest[3][7]));
        assertThat(Slide.slideSouthWest[3][7], is(equalTo(BitBoard.parseBitBoard(new String[]{
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
        logger.info("(3,5)\n" + BitBoard.printBitBoard(Slide.slideSouthEast[3][5]));
        assertThat(Slide.slideSouthEast[3][5], is(equalTo(BitBoard.parseBitBoard(new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,1,_,",
                "_,_,_,_,_,_,_,1,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,"
        }))));

        logger.info("(0,0)\n" + BitBoard.printBitBoard(Slide.slideSouthEast[0][0]));
        assertThat(Slide.slideSouthEast[0][0], is(equalTo(BitBoard.parseBitBoard(new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,1,_,_,_,_,_,_,",
                "_,_,1,_,_,_,_,_,",
                "_,_,_,1,_,_,_,_,",
                "_,_,_,_,1,_,_,_,",
                "_,_,_,_,_,1,_,_,",
                "_,_,_,_,_,_,1,_,",
                "_,_,_,_,_,_,_,1,"
        }))));

        logger.info("(0,3)\n" + BitBoard.printBitBoard(Slide.slideSouthEast[0][3]));
        assertThat(Slide.slideSouthEast[0][3], is(equalTo(BitBoard.parseBitBoard(new String[]{
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
        long result = slideAllDirections(r, c, friend, foe);
        logger.info("Friend Of Foe\n" + BitBoard.printBitBoard(friend | foe));
        logger.info("(" + r + "," + c + ")\n" + BitBoard.printBitBoard(result));
        assertThat(result, is(equalTo(BitBoard.parseBitBoard(solution))));
    }

    private long slideAllDirections(int r, int c, long friend, long foe) {
        long friendOrFoe = friend | foe;
        return Slide.slideAllDirections(r, c, friendOrFoe, friend);
    }

    private long slideAllDirectionsGen(int r, int c, long friend, long foe) {
        long friendOrFoe = friend | foe;
        return Slide.slideAllDirectionsGen(r, c, friendOrFoe, friend);
    }

    @Test
    public void testSlideGen() {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                logger.info(r + "," + c);
                assertThat(BitBoard.printBitBoard(Slide.slideNorth[r][c]), is(equalTo(BitBoard.printBitBoard(Slide.genNorth(r, c)))));
                assertThat(BitBoard.printBitBoard(Slide.slideNorthWest[r][c]), is(equalTo(BitBoard.printBitBoard(Slide.genNorthWest(r, c)))));
                assertThat(BitBoard.printBitBoard(Slide.slideNorthEast[r][c]), is(equalTo(BitBoard.printBitBoard(Slide.genNorthEast(r, c)))));
                assertThat(BitBoard.printBitBoard(Slide.slideSouth[r][c]), is(equalTo(BitBoard.printBitBoard(Slide.genSouth(r, c)))));
                assertThat(BitBoard.printBitBoard(Slide.slideSouthWest[r][c]), is(equalTo(BitBoard.printBitBoard(Slide.genSouthWest(r, c)))));
                assertThat(BitBoard.printBitBoard(Slide.slideSouthEast[r][c]), is(equalTo(BitBoard.printBitBoard(Slide.genSouthEast(r, c)))));
                assertThat(BitBoard.printBitBoard(Slide.slideWest[r][c]), is(equalTo(BitBoard.printBitBoard(Slide.genWest(r, c)))));
                assertThat(BitBoard.printBitBoard(Slide.slideEast[r][c]), is(equalTo(BitBoard.printBitBoard(Slide.genEast(r, c)))));
            }
        }
    }

}
