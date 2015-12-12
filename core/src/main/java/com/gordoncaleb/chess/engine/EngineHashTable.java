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
        if (entry.getHashCode() == hashCode && entry.getBounds() != BoardHashEntry.ValueBounds.INVALID) {
            return entry;
        } else {
            return null;
        }
    }

    public void set(long hashCode, int level, int score, int moveNum, int bounds, long bestMove) {
        entries[(int) (hashCode & hashMask)]
                .setAll(hashCode, level, score, moveNum, bounds, bestMove);
    }
}