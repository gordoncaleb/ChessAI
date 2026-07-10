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
        final BoardHashEntry entry = entries[(int) (hashCode & hashMask)];

        // Depth-preferred replacement: keep a deeper result rather than letting a
        // shallower search from a different position evict it. Always overwrite an
        // empty slot or a re-visit of the same position.
        if (entry.getBounds() == BoardHashEntry.ValueBounds.NA
                || entry.getHashCode() == hashCode
                || level >= entry.getLevel()) {
            entry.setAll(hashCode, score, level, bestMove, moveNum, bounds);
        }
    }
}