package com.gordoncaleb.chess.unit.engine;

import com.gordoncaleb.chess.board.MoveContainerFactory;
import com.gordoncaleb.chess.engine.ABWithHashEngine;
import com.gordoncaleb.chess.engine.EngineMount;
import com.gordoncaleb.chess.engine.score.StaticScorer;
import org.junit.Test;

public class EngineMountTest {

    @Test
    public void testSearchTime(){
        ABWithHashEngine engine = new ABWithHashEngine(new StaticScorer(),
                MoveContainerFactory.buildSortableMoveContainers(30));

        EngineMount engineMount = new EngineMount(engine);

    }
}
