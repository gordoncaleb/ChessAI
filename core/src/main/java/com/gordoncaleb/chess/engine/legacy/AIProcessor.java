package com.gordoncaleb.chess.engine.legacy;

import java.util.Collections;
import java.util.List;

import com.gordoncaleb.chess.board.Board;
import com.gordoncaleb.chess.board.Move;
import com.gordoncaleb.chess.board.MoveContainerFactory;
import com.gordoncaleb.chess.engine.AlphaBetaEngine;
import com.gordoncaleb.chess.engine.Engine;
import com.gordoncaleb.chess.engine.MovePath;
import com.gordoncaleb.chess.engine.score.Values;
import com.gordoncaleb.chess.engine.score.StaticScorer;

public class AIProcessor extends Thread {
    private DecisionNode rootNode;
    private Board board;

    private int searchDepth;

    private boolean threadActive;

    private AI ai;

    private long numSearched;

    private boolean stopSearch;

    private StaticScorer scorer = new StaticScorer();

    private Engine engine = new AlphaBetaEngine(scorer, MoveContainerFactory.buildMoveContainers(20));

    public AIProcessor(AI ai, int maxTreeLevel) {
        this.ai = ai;
        this.searchDepth = maxTreeLevel;

        threadActive = true;
    }

    @Override
    public void run() {

        synchronized (this) {
            while (threadActive) {

                try {
                    this.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (threadActive) {
                    executeTask();
                }

            }

        }

    }

    public synchronized void terminate() {
        threadActive = false;
        this.notifyAll();
    }

    public synchronized void setNewTask() {
        stopSearch = false;
        this.notifyAll();
    }

    public synchronized void setBoard(Board board) {
        this.board = board;
    }

    public Board getBoard() {
        return board;
    }

    public synchronized void setRootNode(DecisionNode rootNode) {

        if (rootNode.getMove() != null) {
            board.makeMove(rootNode.getMove());
        }

        this.rootNode = rootNode;

        // if game isnt over make sure tree root shows what next valid moves are
        if (!this.rootNode.hasChildren()) {
            board.makeNullMove();
            attachValidMoves(rootNode, null, -1);

        }

    }

    private void executeTask() {
        DecisionNode task = null;

        numSearched = 0;

        while ((task = ai.getNewTasking(task)) != null) {

            board.makeMove(task.getMove());

            final int startAlpha = -Values.CHECKMATE_MOVE + 1;
            final int startBeta = -ai.getAlpha();

            MovePath movePath = engine.search(board, searchDepth, startAlpha, startBeta);
            task.setHeadChild(movePath.get(0));
            task.setChosenPathValue(movePath.getScore());

            board.undoMove();

            if (stopSearch) {
                task.setChosenPathValue(-10000);
            }

        }
    }

    public void attachValidMoves(DecisionNode branch, Move hashMove, int level) {

        List<Move> moves = board.generateValidMoves().toList();

        Collections.sort(moves, Collections.reverseOrder());

        DecisionNode[] children = new DecisionNode[moves.size()];

        for (int m = 0; m < moves.size(); m++) {
            children[m] = (new DecisionNode(moves.get(m), 0));
        }

        branch.setChildren(children);
        // branch.sort();

    }

    public void setSearchDepth(int searchDepth) {
        this.searchDepth = searchDepth;
    }

    public void stopSearch() {
        stopSearch = true;
    }

    public long getNumSearched() {
        return numSearched;
    }

}
