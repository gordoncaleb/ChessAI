package com.gordoncaleb.chess.ui.server;

import com.gordoncaleb.chess.board.Board;
import com.gordoncaleb.chess.board.MoveContainerFactory;
import com.gordoncaleb.chess.engine.ABWithHashEngine;
import com.gordoncaleb.chess.engine.EngineMount;
import com.gordoncaleb.chess.engine.score.StaticScorer;

/**
 * Holds the mutable state of one game: the board, which side the human plays,
 * and a dedicated engine mount used to compute the opponent's replies.
 */
public class GameSession {

    /** Iterative deepening never needs more than this many plies of containers. */
    private static final int MAX_SEARCH_DEPTH = 30;

    private final String id;
    private final Board board;
    private final int humanSide;
    private final int thinkTimeMs;
    private final EngineMount engine;

    public GameSession(String id, Board board, int humanSide, int thinkTimeMs) {
        this.id = id;
        this.board = board;
        this.humanSide = humanSide;
        this.thinkTimeMs = thinkTimeMs;
        this.engine = new EngineMount(new ABWithHashEngine(
                new StaticScorer(),
                MoveContainerFactory.buildSortableMoveContainers(MAX_SEARCH_DEPTH)));
    }

    public String getId() {
        return id;
    }

    public Board getBoard() {
        return board;
    }

    public int getHumanSide() {
        return humanSide;
    }

    public int getThinkTimeMs() {
        return thinkTimeMs;
    }

    public EngineMount getEngine() {
        return engine;
    }
}
