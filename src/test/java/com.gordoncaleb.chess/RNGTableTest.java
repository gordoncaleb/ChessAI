package com.gordoncaleb.chess;

import com.gordoncaleb.chess.backend.RNGTable;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RNGTableTest {

    @Test @Ignore
    public void test() {
        RNGTable rngTable = RNGTable.instance;

        assertEquals(rngTable.getBlackToMoveRandom(), -6985323133131116037L);
    }
}
