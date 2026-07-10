package com.gordoncaleb.chess.board;

import java.util.Arrays;

/**
 * A minimal growable LIFO stack of primitive {@code long}s. Used for the
 * board's per-move hash-code and castle-rights history so that push/pop in the
 * search hot path does not box every value into a {@link Long} (as the previous
 * {@code ArrayDeque<Long>} did).
 */
public final class LongStack {

    private long[] values;
    private int size;

    public LongStack(int initialCapacity) {
        this.values = new long[Math.max(1, initialCapacity)];
        this.size = 0;
    }

    public void push(long value) {
        if (size == values.length) {
            values = Arrays.copyOf(values, values.length * 2);
        }
        values[size++] = value;
    }

    public long pop() {
        return values[--size];
    }

    public long peek() {
        return values[size - 1];
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public void clear() {
        size = 0;
    }
}
