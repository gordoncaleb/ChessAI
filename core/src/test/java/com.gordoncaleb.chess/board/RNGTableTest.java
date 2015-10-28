package com.gordoncaleb.chess.board;

import com.gordoncaleb.chess.board.pieces.Piece;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RNGTableTest {

    @Test
    public void testFileLoaded() {
        RNGTable rngTable = RNGTable.instance;
        assertEquals(7477762210045365562L, rngTable.getBlackToMoveRandom());
        assertEquals(-5922024454569227819L, rngTable.getPiecePerSquareRandom(Side.WHITE, Piece.PieceID.QUEEN, 3, 3));
    }

    @Test
    public void testGenerated(){
        RNGTable rngTable = new RNGTable("");
        assertEquals(RNGTable.RANDOM_COUNT, rngTable.getLoadedRandoms().size());
    }
}
