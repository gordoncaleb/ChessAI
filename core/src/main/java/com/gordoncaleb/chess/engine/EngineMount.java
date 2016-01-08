package com.gordoncaleb.chess.engine;

import com.gordoncaleb.chess.board.Board;
import com.gordoncaleb.chess.board.Move;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class EngineMount {
    private final ABWithHashEngine engine;
    private final ExecutorService executorService;

    public EngineMount(ABWithHashEngine engine) {
        this.engine = engine;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public EngineMount(ABWithHashEngine engine, ExecutorService executorService) {
        this.engine = engine;
        this.executorService = executorService;
    }

    public Future<Move> search(final Board board, final long timeTarget) {

        final long endTime = now() + timeTarget;
        return executorService.submit(() -> {

            final List<List<Move>> pvs = new ArrayList<>();
            final List<Long> times = Arrays.asList(now());

            int depth = 1;
            while (hasEnoughTime(times, endTime)) {
                MovePath movePath = engine.iterativeSearch(board, depth);
                times.add(now());

                pvs.add(movePath.asList());
                depth++;
            }

            return searchResult(pvs);
        });
    }

    private static long now() {
        return System.currentTimeMillis();
    }

    private boolean hasEnoughTime(List<Long> times, long endTime) {
        if (times.size() > 3) {
            final long timeMinus1 = times.get(times.size() - 1);
            final long timeMinus2 = times.get(times.size() - 2);
            final long timeMinus3 = times.get(times.size() - 3);

            final double d1 = timeMinus1 - timeMinus2;
            final double d2 = timeMinus2 - timeMinus3;

            final long estimatedTime = timeMinus1 + (long) (d1 * d1 / d2);

            return estimatedTime <= endTime;
        }

        return true;
    }

    private static Move searchResult(List<List<Move>> pvs) {
        return pvs.get(pvs.size() - 1).get(0);
    }
}
