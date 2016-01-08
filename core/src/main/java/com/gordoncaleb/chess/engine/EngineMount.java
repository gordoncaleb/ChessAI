package com.gordoncaleb.chess.engine;

import com.gordoncaleb.chess.board.Board;
import com.gordoncaleb.chess.board.Move;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class EngineMount {
    private static final Logger LOGGER = LoggerFactory.getLogger(EngineMount.class);

    private final Engine engine;
    private final ExecutorService executorService;

    public EngineMount(Engine engine) {
        this.engine = engine;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public EngineMount(Engine engine, ExecutorService executorService) {
        this.engine = engine;
        this.executorService = executorService;
    }

    public Future<List<Move>> search(final Board board, final long timeTarget) {

        final long endTime = now() + timeTarget;
        return executorService.submit(() -> {

            final List<List<Move>> pvs = new ArrayList<>();
            final List<Long> times = new ArrayList<>();
            times.add(now());

            int depth = 1;
            while (hasEnoughTime(times, endTime)) {
                MovePath movePath = engine.search(board, depth);
                times.add(now());

                pvs.add(movePath.asList());
                depth++;
            }

            LOGGER.info("times: {}", times);

            return searchResult(pvs);
        });
    }

    private static long now() {
        return System.currentTimeMillis();
    }

    private static boolean hasEnoughTime(List<Long> times, long endTime) {
        if (times.size() > 3) {
            final long timeMinus1 = times.get(times.size() - 1);
            final long timeMinus2 = times.get(times.size() - 2);
            final long timeMinus3 = times.get(times.size() - 3);

            final double d1 = timeMinus1 - timeMinus2;
            final double d2 = timeMinus2 - timeMinus3;

            final double d3 = (d1 * d1 / d2);

            final long estimatedTime = timeMinus1 + (long) (d3);

            LOGGER.info("Delta 1 = {}, Delta 2 = {}, Predicted Next = {}", d1, d2, d3);

            return estimatedTime <= endTime;
        }

        return true;
    }

    private static List<Move> searchResult(List<List<Move>> pvs) {
        return pvs.get(pvs.size() - 1);
    }
}
