package com.gordoncaleb.chess.piece;

import com.gordoncaleb.chess.backend.Board;
import com.gordoncaleb.chess.backend.Side;
import com.gordoncaleb.chess.persistence.BoardDAO;
import com.gordoncaleb.chess.pieces.King;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.gordoncaleb.chess.bitboard.BitBoard.*;
import static com.gordoncaleb.chess.pieces.Piece.PieceID.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class KingTest {
    public static final Logger logger = LoggerFactory.getLogger(KingTest.class);

    @Test
    public void testKingCheckInfo1() {

        String[] setup = {
                "R,_,B,_,K,B,_,R,",
                "P,P,P,_,_,P,P,P,",
                "_,B,_,_,_,Q,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,p,_,_,_,_,",
                "_,_,_,_,_,_,p,_,",
                "p,p,R,_,p,k,_,p,",
                "r,_,_,_,_,_,_,r,"
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
        Board board = new BoardDAO().getFromSetup(Side.WHITE, setup);
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
                "R,N,B,_,K,B,N,R,",
                "P,P,P,_,_,P,P,P,",
                "_,_,_,_,_,_,_,Q,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,p,p,_,_,_,",
                "_,_,_,q,k,p,p,_,",
                "p,p,p,_,b,n,_,p,",
                "r,n,_,_,_,_,_,r,"
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
                "R,_,B,_,K,B,_,R,",
                "P,P,P,_,_,P,P,P,",
                "_,_,_,_,_,Q,q,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,N,p,p,P,N,_,",
                "_,_,_,n,k,n,p,_,",
                "p,p,p,p,_,p,_,p,",
                "r,_,_,_,_,_,_,r,"
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
