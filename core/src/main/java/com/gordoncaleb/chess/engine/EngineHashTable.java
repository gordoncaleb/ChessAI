package com.gordoncaleb.chess.engine;

public class EngineHashTable {

    private final long hashMask;
    private final BoardHashEntry[] entries;

    public EngineHashTable(final int numBits) {
        final int size = (int) Math.pow(2, numBits);
        entries = new BoardHashEntry[size];
        hashMask = (1L << numBits) - 1L;
        initEntries();
    }

    private void initEntries() {
        for (int i = 0; i < entries.length; i++) {
            entries[i] = new BoardHashEntry();
        }
    }

    public BoardHashEntry get(final long hashCode) {
        BoardHashEntry entry = entries[(int) (hashCode & hashMask)];
        if (entry.getHashCode() == hashCode && entry.getBounds() != BoardHashEntry.ValueBounds.NA) {
            return entry;
        } else {
            return null;
        }
    }

    public void set(final long hashCode, final int score, final int level, final long bestMove, final int moveNum, final int bounds) {
        entries[(int) (hashCode & hashMask)]
                .setAll(hashCode, score, level, bestMove, moveNum, bounds);
    }
}