package com.gordoncaleb.chess.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collector;

public class JavaLacks {

    public static <T> Collector<T, Set<T>, List<T>> toUniqueList() {
        return Collector.of(HashSet<T>::new,
                Set<T>::add,
                (left, right) -> {
                    left.addAll(right);
                    return left;
                }, ArrayList<T>::new);
    }
}
