package com.gordoncaleb.chess.piece;

import com.gordoncaleb.chess.board.Board;
import com.gordoncaleb.chess.board.Side;
import com.gordoncaleb.chess.board.serdes.JSONParser;
import com.gordoncaleb.chess.board.pieces.King;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.gordoncaleb.chess.board.bitboard.BitBoard.*;
import static com.gordoncaleb.chess.board.pieces.Piece.PieceID.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class KingTest {
    public static final Logger logger = LoggerFactory.getLogger(KingTest.class);

    @Test
    public void testKingCheckInfo1() {

        String[] setup = {
                "r,_,b,_,k,b,_,r,",
                "p,p,p,_,_,p,p,p,",
                "_,b,_,_,_,q,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,P,_,_,_,_,",
                "_,_,_,_,_,_,P,_,",
                "P,P,r,_,P,K,_,P,",
                "R,_,_,_,_,_,_,R,"
        };

        testCheckVector(setup, new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,1,_,_,",
                "_,_,_,_,_,1,_,_,",
                "_,_,_,_,_,1,_,_,",
                "_,_,_,_,_,1,_,_,",
                "_,_,_,_,_,1,_,_,",
                "_,_,_,_,_,_,_,_,"
        });

        testCheckVectorCompliment(setup, new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,1,_,_,"
        });

        testBlockingVector(setup, 6, 4, new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,1,1,1,1,_,_,",
                "_,_,_,_,_,_,_,_,"
        });

        testBlockingVector(setup, 4, 3, new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,1,_,_,_,_,_,_,",
                "_,_,1,_,_,_,_,_,",
                "_,_,_,1,_,_,_,_,",
                "_,_,_,_,1,_,_,_,",
                "_,_,_,_,_,1,_,_,",
                "_,_,_,_,_,_,_,_,"
        });

    }

    private void testCheckVector(String[] setup, String[] solution) {
        long[] nullMoveInfo = new long[]{0, ALL_ONES, 0};
        testSetupKingCheckInfo(setup, nullMoveInfo);
        assertThat(printBitBoard(nullMoveInfo[1]), is(equalTo(printBitBoard(parseBitBoard(solution)))));
    }

    private void testCheckVectorCompliment(String[] setup, String[] solution) {
        long[] nullMoveInfo = new long[]{0, ALL_ONES, 0};
        testSetupKingCheckInfo(setup, nullMoveInfo);
        logger.info("Check Compliment is:\n" + printBitBoard(nullMoveInfo[2]));
        assertThat(nullMoveInfo[2] & parseBitBoard(solution), is(greaterThan(0L)));
    }

    private void testBlockingVector(String[] setup, int r, int c, String[] solution) {
        Board board = testSetupKingCheckInfo(setup, new long[]{0, ALL_ONES, 0});
        assertThat(printBitBoard(board.getPiece(r, c).getBlockingVector()), is(equalTo(printBitBoard(parseBitBoard(solution)))));
    }

    private Board testSetupKingCheckInfo(String[] setup, long[] nullMoveInfo) {
        Board board = JSONParser.getFromSetup(Side.WHITE, setup);
        long king = board.getPosBitBoard()[KING][Side.WHITE];
        long foeQueens = board.getPosBitBoard()[QUEEN][Side.BLACK];
        long foeRooks = board.getPosBitBoard()[ROOK][Side.BLACK];
        long foeBishop = board.getPosBitBoard()[BISHOP][Side.BLACK];
        long friends = board.getAllPosBitBoard()[Side.WHITE];
        long foes = board.getAllPosBitBoard()[Side.BLACK];

        King.getKingCheckInfo(board, king, foeQueens, foeRooks, foeBishop, friends | foes, nullMoveInfo);
        return board;
    }

    @Test
    public void testKingCheckInfo2() {
        String[] setup = {
                "r,n,b,_,k,b,n,r,",
                "p,p,p,_,_,p,p,p,",
                "_,_,_,_,_,_,_,q,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,P,P,_,_,_,",
                "_,_,_,Q,K,P,P,_,",
                "P,P,P,_,B,N,_,P,",
                "R,N,_,_,_,_,_,R,"
        };

        testCheckVector(setup, new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,1,",
                "_,_,_,_,_,_,1,_,",
                "_,_,_,_,_,1,_,_,",
                "_,_,_,_,1,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,"
        });

        testCheckVectorCompliment(setup, new String[]{
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,1,_,_,_,_,",
                "_,_,_,_,_,_,_,_,"
        });

    }

    @Test
    public void testKingCheckInfo3() {
        String[] setup = {
                "r,_,b,_,k,b,_,r,",
                "p,p,p,_,_,p,p,p,",
                "_,_,_,_,_,q,Q,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,n,P,P,p,n,_,",
                "_,_,_,N,K,N,P,_,",
                "P,P,P,P,_,P,_,P,",
                "R,_,_,_,_,_,_,R,"
        };

        testCheckVector(setup, new String[]{
                "1,1,1,1,1,1,1,1,",
                "1,1,1,1,1,1,1,1,",
                "1,1,1,1,1,1,1,1,",
                "1,1,1,1,1,1,1,1,",
                "1,1,1,1,1,1,1,1,",
                "1,1,1,1,1,1,1,1,",
                "1,1,1,1,1,1,1,1,",
                "1,1,1,1,1,1,1,1,"
        });

    }
}
