package com.gordoncaleb.chess.unit.board;

import com.gordoncaleb.chess.board.RNGTable;
import com.gordoncaleb.chess.board.Side;
import com.gordoncaleb.chess.board.pieces.Piece;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RNGTableTest {

    @Test
    public void testFileLoaded() {
        RNGTable rngTable = RNGTable.instance;
        assertEquals(7477762210045365562L, rngTable.getBlackToMoveRandom());
        assertEquals(4222872400822183009L, rngTable.getPiecePerSquareRandom(Side.WHITE, Piece.PieceID.QUEEN, 3, 3));
    }

    @Test
    public void testGenerated(){
        RNGTable rngTable = new RNGTable("");
        assertEquals(RNGTable.RANDOM_COUNT, rngTable.getLoadedRandoms().size());
    }
}
