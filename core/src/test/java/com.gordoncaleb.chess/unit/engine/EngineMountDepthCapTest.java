package com.gordoncaleb.chess.unit.engine;

import com.gordoncaleb.chess.board.Board;
import com.gordoncaleb.chess.board.Move;
import com.gordoncaleb.chess.board.MoveContainerFactory;
import com.gordoncaleb.chess.board.Side;
import com.gordoncaleb.chess.board.serdes.JSONParser;
import com.gordoncaleb.chess.engine.ABWithHashEngine;
import com.gordoncaleb.chess.engine.EngineMount;
import com.gordoncaleb.chess.engine.score.StaticScorer;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertNotNull;

public class EngineMountDepthCapTest {

    // A sparse position whose shallow searches finish almost instantly, so the
    // time predictor keeps deepening. With a generous time budget this used to
    // deepen past the fixed-size move-container arrays and throw AIOOBE.
    @Test
    public void doesNotOverrunContainerArrays() throws Exception {
        Board board = JSONParser.getFromSetup(Side.WHITE, new String[]{
                "_,_,_,_,k,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,_,_,_,_,",
                "_,_,_,_,Q,_,_,_,",
                "_,_,_,_,K,_,_,_,"
        });

        // Small container array (size 6) + a large time budget: without the depth
        // cap this races past depth 6 and crashes.
        ABWithHashEngine engine = new ABWithHashEngine(new StaticScorer(),
                MoveContainerFactory.buildSortableMoveContainers(6));
        EngineMount mount = new EngineMount(engine);

        List<Move> pv = mount.search(board, 3000).get();
        assertNotNull(pv);
    }
}
