package com.gordoncaleb.chess.unit.engine;

import com.gordoncaleb.chess.engine.BoardHashEntry;
import com.gordoncaleb.chess.engine.EngineHashTable;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class EngineHashTableTest {


    @Test
    public void testGetNull() {
        EngineHashTable hashTable = new EngineHashTable(4);

        BoardHashEntry entry = hashTable.get(4L);

        assertThat(entry, equalTo(null));
    }

    @Test
    public void testGetVal() {
        EngineHashTable hashTable = new EngineHashTable(4);

        hashTable.set(4L, 4, 4, 4, 4, 1);

        BoardHashEntry entry = hashTable.get(4L);
        assertThat(entry, equalTo(new BoardHashEntry(4, 4, 4, 4, 4, 1)));
    }

    @Test
    public void testValReplacement() {
        EngineHashTable hashTableSmall = new EngineHashTable(4);

        long hashCodeA = 0x04L;
        long hashCodeB = 0x44L;

        BoardHashEntry eA = new BoardHashEntry(hashCodeA, 4, 4, 4, 4, BoardHashEntry.ValueBounds.ALL);
        BoardHashEntry eB = new BoardHashEntry(hashCodeB, 44, 44, 44, 44, BoardHashEntry.ValueBounds.ALL);

        hashTableSmall.set(hashCodeA, eA.getScore(), eA.getLevel(), eA.getBestMove(), eA.getMoveNum(), eA.getBounds());
        hashTableSmall.set(hashCodeB, eB.getScore(), eB.getLevel(), eB.getBestMove(), eB.getMoveNum(), eB.getBounds());

        BoardHashEntry entry = hashTableSmall.get(hashCodeB);
        assertThat(entry, equalTo(eB));

        entry = hashTableSmall.get(hashCodeA);
        assertThat(entry, equalTo(null));

        EngineHashTable hashTableLarge = new EngineHashTable(16);

        hashTableLarge.set(hashCodeA, eA.getScore(), eA.getLevel(), eA.getBestMove(), eA.getMoveNum(), eA.getBounds());
        hashTableLarge.set(hashCodeB, eB.getScore(), eB.getLevel(), eB.getBestMove(), eB.getMoveNum(), eB.getBounds());

        entry = hashTableLarge.get(hashCodeA);
        assertThat(entry, equalTo(eA));

        entry = hashTableLarge.get(hashCodeB);
        assertThat(entry, equalTo(eB));

    }

}