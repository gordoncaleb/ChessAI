package com.gordoncaleb.chess.unit.engine;

import com.gordoncaleb.chess.board.Board;
import com.gordoncaleb.chess.board.Move;
import com.gordoncaleb.chess.board.MoveContainerFactory;
import com.gordoncaleb.chess.engine.ABWithHashEngine;
import com.gordoncaleb.chess.engine.MovePath;
import com.gordoncaleb.chess.engine.score.StaticScorer;
import com.gordoncaleb.chess.util.Perft;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertTrue;

public class ABWithHashEnginePVTest {

    // A deep iterative search populates the transposition table across depths.
    // A too-aggressive TT probe used to short-circuit principal-variation nodes,
    // leaving stale moves in the PV; replaying it in verifyPV then corrupted the
    // board and threw ArrayIndexOutOfBoundsException. This drives deep searches on
    // several tactically rich positions and replays each reported PV to ensure
    // every move is actually legal in the position it is played in.
    @Test
    public void principalVariationIsLegalAfterDeepSearch() {
        List<Board> positions = new Perft().allPositions();

        for (Board board : positions) {
            ABWithHashEngine engine = new ABWithHashEngine(new StaticScorer(),
                    MoveContainerFactory.buildSortableMoveContainers(12));

            MovePath movePath = engine.iterativeSearch(board.copy(), 7);
            List<Move> pv = movePath.asList();

            Board replay = board.copy();
            for (Move pvMove : pv) {
                replay.makeNullMove();
                List<Move> legal = replay.generateValidMoves().toList();
                boolean isLegal = legal.stream().anyMatch(m ->
                        m.getFromRow() == pvMove.getFromRow() && m.getFromCol() == pvMove.getFromCol()
                                && m.getToRow() == pvMove.getToRow() && m.getToCol() == pvMove.getToCol()
                                && m.getNote() == pvMove.getNote());
                assertTrue("Illegal PV move " + pvMove + " for position\n" + board, isLegal);
                replay.makeMove(pvMove);
            }
        }
    }
}
