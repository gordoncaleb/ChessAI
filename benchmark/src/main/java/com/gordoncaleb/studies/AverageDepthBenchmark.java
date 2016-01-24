package com.gordoncaleb.studies;

import com.gordoncaleb.chess.board.Move;
import com.gordoncaleb.chess.board.MoveContainerFactory;
import com.gordoncaleb.chess.board.serdes.PGNParser;
import com.gordoncaleb.chess.engine.ABWithHashEngine;
import com.gordoncaleb.chess.engine.EngineMount;
import com.gordoncaleb.chess.engine.score.StaticScorer;
import com.gordoncaleb.chess.util.PGNGameLibrary;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class AverageDepthBenchmark {
    private static final Logger LOGGER = LoggerFactory.getLogger(AverageDepthBenchmark.class);

    @Test
    public void getAverageDepth() {
        ABWithHashEngine engine = new ABWithHashEngine(new StaticScorer(),
                MoveContainerFactory.buildSortableMoveContainers(30));
        EngineMount engineMount = new EngineMount(engine);


        PGNGameLibrary lib = new PGNGameLibrary("../pgnmentor");
        double averageDepth = lib.randomPositions()
                .limit(10)
                .mapToInt(b -> {
                    try {
                        LOGGER.info("\n" + b.toString());
                        List<Move> moveList = engineMount.search(b, 3000).get();
                        LOGGER.info(PGNParser.toAlgebraicNotation(moveList, b));
                        return moveList.size();
                    } catch (Exception e) {
                        LOGGER.error("Problem searching board", e);
                        return 0;
                    }
                }).average().getAsDouble();

        LOGGER.info(String.format("Average depth: %2.3f", averageDepth));
    }
}
