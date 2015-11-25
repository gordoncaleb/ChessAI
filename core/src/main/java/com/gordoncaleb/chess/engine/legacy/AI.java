package com.gordoncaleb.chess.engine.legacy;

import java.util.ArrayList;

import com.gordoncaleb.chess.board.Board;
import com.gordoncaleb.chess.board.Move;
import com.gordoncaleb.chess.engine.BoardHashEntry;
import com.gordoncaleb.chess.engine.MoveBook;
import com.gordoncaleb.chess.engine.score.Values;
import com.gordoncaleb.chess.ui.gui.game.Game;
import com.gordoncaleb.chess.ui.gui.game.Player;
import com.gordoncaleb.chess.ui.gui.game.PlayerContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AI extends Thread implements Player {
    public static final Logger logger = LoggerFactory.getLogger(AI.class);

    public static final Move[] noKillerMoves = {};

    private PlayerContainer game;
    private MoveBook moveBook;

    private DecisionNode rootNode;
    private int alpha;

    private int[] childNum = new int[200];

    private boolean makeMove;
    private boolean undoMove;
    private boolean recommend;
    private boolean paused;

    private Object processing;

    private boolean active;

    private int nextTaskNum;
    private AIProcessor[] processorThreads;

    private long maxSearched;
    private long searchedThisGame;

    private BoardHashEntry[] hashTable;
    private int moveNum;

    private int depthInMemory = 0;

    public AI(PlayerContainer game) {

        this.game = game;
        processing = new Object();

        hashTable = new BoardHashEntry[AISettings.hashTableSize];

        moveBook = new MoveBook();
        moveBook.loadMoveBook();

        processorThreads = new AIProcessor[AISettings.numOfThreads];

        for (int i = 0; i < processorThreads.length; i++) {
            processorThreads[i] = new AIProcessor(this, AISettings.minSearchDepth);
            processorThreads[i].start();
        }

        logger.info(processorThreads.length + " threads created and started.");

        active = true;

        this.start();
    }

    @Override
    public void run() {
        DecisionNode aiDecision;

        synchronized (this) {
            while (active) {

                try {
                    this.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                try {
                    if (undoMove) {
                        undoMoveOnSubThreads();
                        undoMove = false;
                    }

                    if (makeMove && !paused) {

                        aiDecision = getAIDecision();

                        if (aiDecision != null && !undoMove) {
                            game.makeMove(aiDecision.getMove());
                        }

                        if (AISettings.useHashTable) {
                            cleanHashTable();
                        }

                        makeMove = false;
                    }

                    if (recommend) {
                        aiDecision = getAIDecision();

                        if (aiDecision != null) {
                            printRootDebug();
                            game.recommendationMade(aiDecision.getMove());
                        }

                        recommend = false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        }

        for (int i = 0; i < processorThreads.length; i++) {
            processorThreads[i].terminate();
        }

    }

    public synchronized void terminate() {
        active = false;
        notifyAll();

    }

    public synchronized void newGame(Board board) {

        rootNode = new DecisionNode(null);

        clearHashTable();
        depthInMemory = 0;

        for (int i = 0; i < processorThreads.length; i++) {
            processorThreads[i].setBoard(board.copy());
            processorThreads[i].setRootNode(rootNode);
        }

        if (AISettings.debugOutput) {
            // printRootDebug();
            logger.info(getBoard().toString());
        }

        makeMove = false;
        undoMove = false;
        recommend = false;

        moveNum = board.getMoveHistory().size();
        // hashTable.clear();

        maxSearched = 0;
        searchedThisGame = 0;

        logger.info("New game");

    }

    public synchronized void makeMove() {
        makeMove = true;
        notifyAll();
    }

    @Override
    public synchronized void requestRecommendation() {
        recommend = true;
        notifyAll();
    }

    public synchronized void pause() {
        paused = !paused;
        notifyAll();
    }

    public Move undoMove() {
        if (canUndo()) {
            undoMove = true;

            moveNum--;

            synchronized (this) {
                notifyAll();
            }

            return getBoard().getLastMoveMade();

        } else {

            return null;

        }
    }

    /**
     * Blocks until move has been made
     */
    public synchronized boolean moveMade(Move move) {

        DecisionNode decision = getMatchingDecisionNode(move);

        if (decision != null) {

            setRootNode(decision);

            logger.info(move.toString());
            logger.info(getBoard().toString());

            moveNum++;

            depthInMemory = Math.max(0, depthInMemory - 1);

            return true;

        } else {
            return false;
        }

    }

    /**
     * At this point the root should be the user's move. This method finds the
     * best response for the AI.
     */
    private DecisionNode getAIDecision() {

        // Game Over?
        if (!rootNode.hasChildren()) {
            return null;
        }

        // cleanHashTable();

        long time = 0;
        DecisionNode aiDecision;

        time = System.currentTimeMillis();

        logger.debug("maxMoveBookMove=" + AISettings.maxMoveBookMove + " moveNum=" + moveNum);
        Move mb;
        if ((mb = moveBook.getRecommendation(getBoard().getHashCode())) == null || !AISettings.useBook || (moveNum > AISettings.maxMoveBookMove)) {

            // Split the task of looking at all possible AI moves up amongst
            // multiple threads. This takes advantage of multicore systems.
            delegateProcessors(rootNode);

            aiDecision = rootNode.getHeadChild();
        } else {

            if (AISettings.debugOutput) {
                logger.info("Ai decision based on movebook");
            }

            aiDecision = getMatchingDecisionNode(mb);
        }

        // debug print out of decision tree stats. i.e. the tree size and
        // the values of the move options that the AI has
        if (AISettings.debugOutput) {
            logger.info("Ai took " + (System.currentTimeMillis() - time) + "ms to move.");
            // game.setDecisionTreeRoot(rootNode);
            // printRootDebug();
        }

        // The users decision's chosen child represents the best move based
        // on the user's move
        // setRootNode(aiDecision);

        if (AISettings.debugOutput) {
            // logger.info(getBoard().toString());
        }

        System.gc();

        return aiDecision;
    }

    private int getProgress(long timeLeft, int iteration) {

        int interationProgress = (int) (100.0 * ((double) (nextTaskNum - 1) / (double) rootNode.getChildrenSize()));
        int timeProgress = (int) (100.0 * (((double) AISettings.maxSearchTime - (double) timeLeft) / (double) AISettings.maxSearchTime));

        // logger.info((nextTaskNum - 1) + "/" + rootNode.getChildrenSize());

        if (AISettings.useExtraTime) {
            if (iteration > AISettings.minSearchDepth) {
                return timeProgress;
            } else {
                return Math.min(timeProgress, interationProgress);
            }
        } else {
            return interationProgress;
        }
    }

    private void delegateProcessors(DecisionNode root) {

        long totalSearched = 0;

        if (root.getChildrenSize() < 2) {
            return;
        }

        if (AISettings.debugOutput) {
            logger.info("New task");
        }

        synchronized (processing) {

            long startTime = System.currentTimeMillis();
            long timeLeft = AISettings.maxSearchTime;
            boolean checkMateFound = false;

            while (!checkMateFound && timeLeft > 0 && depthInMemory < 50) {

                // This clears ai processors status done flags from previous
                // tasks.
                // taskDone = 0;
                nextTaskNum = 0;
                alpha = -Values.CHECKMATE_MOVE + 1;
                // root.removeAllChildren();

                // wake all threads up
                for (int d = 0; d < processorThreads.length; d++) {
                    processorThreads[d].setSearchDepth(depthInMemory);
                    processorThreads[d].setNewTask();
                }

                // Wait for all processors to finish their task
                try {

                    while (nextTaskNum <= rootNode.getChildrenSize()) {

                        processing.wait(Math.max(0, timeLeft));

                        timeLeft = AISettings.maxSearchTime - (System.currentTimeMillis() - startTime);

                        game.showProgress(getProgress(timeLeft, depthInMemory));

                        if (timeLeft <= 0 && nextTaskNum <= rootNode.getChildrenSize() && depthInMemory > AISettings.minSearchDepth) {
                            break;
                        }
                    }

                    rootNode.sort(nextTaskNum - 1);

                    if (nextTaskNum <= rootNode.getChildrenSize()) {

                        nextTaskNum = rootNode.getChildrenSize();

                        for (int d = 0; d < processorThreads.length; d++) {
                            processorThreads[d].stopSearch();
                        }

                        processing.wait();

                    } else {
                        depthInMemory++;
                    }

                    // if (it > AISettings.minSearchDepth) {
                    // processing.wait(timeLeft);
                    //
                    // if (nextTask != taskSize) {
                    //
                    // nextTask = taskSize;
                    //
                    // for (int d = 0; d < processorThreads.length; d++) {
                    // processorThreads[d].stopSearch();
                    // }
                    //
                    // processing.wait();
                    //
                    // rootNode.sort(taskDone);
                    //
                    // } else {
                    // rootNode.sort();
                    // }
                    //
                    // } else {
                    // processing.wait();
                    // rootNode.sort();
                    // }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if ((Math.abs(root.getHeadChild().getChosenPathValue()) & Values.CHECKMATE_MASK) != 0) {
                    checkMateFound = true;
                }

                for (int d = 0; d < processorThreads.length; d++) {

                    maxSearched = Math.max(maxSearched, processorThreads[d].getNumSearched());

                    totalSearched += processorThreads[d].getNumSearched();
                    logger.info("Searched " + totalSearched + " in " + (AISettings.maxSearchTime - timeLeft));
                    logger.info((double) totalSearched / (double) (AISettings.maxSearchTime - timeLeft) + " nodes/ms");
                    logger.info("Depth in memory = " + depthInMemory);
                }

                if (!AISettings.useExtraTime) {
                    break;
                }
            }

            searchedThisGame += totalSearched;

            logger.info("Max Searched " + maxSearched);
            logger.info("Searched " + searchedThisGame + " this game");

            game.showProgress(0);

        }

    }

    public DecisionNode getNewTasking(DecisionNode previousTask) {

        DecisionNode nextTask;

        synchronized (processing) {

            if (nextTaskNum < rootNode.getChildrenSize()) {
                nextTask = rootNode.getChild(nextTaskNum);
            } else {
                nextTask = null;
            }

            nextTaskNum++;

            // logger.info("Tasknum " + nextTaskNum);

            if (previousTask != null) {
                if (previousTask.getChosenPathValue() > alpha && AISettings.alphaBetaPrunTopPly) {
                    alpha = previousTask.getChosenPathValue();
                }

                if (alpha >= Values.CHECKMATE_MOVE - 5) {
                    nextTask = null;
                    nextTaskNum = rootNode.getChildrenSize() + 1;
                }
            }

            processing.notifyAll();

        }

        return nextTask;
    }

    // public DecisionNode getNextTask() {
    //
    // synchronized (processing) {
    //
    // DecisionNode task;
    //
    // if (nextTask < taskSize) {
    // task = rootNode.getChild(nextTask);
    // nextTask++;
    //
    // return task;
    //
    // } else {
    //
    // processing.notifyAll();
    //
    // return null;
    //
    // }
    //
    // }
    // }

    public int getAlpha() {
        synchronized (processing) {
            return alpha;
        }
    }

    public void cleanHashTable() {

        for (int i = 0; i < hashTable.length; i++) {
            if (hashTable[i] != null) {
                if ((moveNum - hashTable[i].getMoveNum()) > AISettings.staleHashAge) {
                    hashTable[i] = null;
                }
            }
        }

        System.gc();
    }

    public void clearHashTable() {
        for (int i = 0; i < hashTable.length; i++) {
            hashTable[i] = null;
        }

        System.gc();
    }

    public void resetGameTree() {
        rootNode = new DecisionNode(null);

        for (int i = 0; i < processorThreads.length; i++) {
            synchronized (processorThreads[i]) {
                processorThreads[i].setRootNode(rootNode);
            }
        }
    }

    private void undoMoveOnSubThreads() {
        if (canUndo()) {

            rootNode = new DecisionNode(null);
            depthInMemory = 0;

            for (int i = 0; i < processorThreads.length; i++) {
                synchronized (processorThreads[i]) {
                    processorThreads[i].getBoard().undoMove();
                    processorThreads[i].setRootNode(rootNode);
                }
            }

        }
    }

    private boolean canUndo() {
        return getBoard().canUndo();
    }

    private void setRootNode(DecisionNode newRootNode) {

        rootNode = newRootNode;

        // rootNode.setParent(null);
        // rootNode.setNextSibling(null);
        // rootNode.setPreviousSibling(rootNode);

        // tell threads about new root node
        for (int i = 0; i < processorThreads.length; i++) {
            processorThreads[i].setRootNode(newRootNode);
        }

        if (AISettings.debugOutput) {
            logger.info("Board hash code = " + Long.toHexString(getBoard().getHashCode()));
        }

        // setGameSatus(rootNode.getStatus(), getBoard().getTurn());

        // System.gc();
    }

    @Override
    public void setGame(PlayerContainer game) {
        this.game = game;
    }

    // public void setGameSatus(GameStatus status, Side playerTurn) {
    // if (status != GameStatus.IN_PLAY) {
    // logger.info(rootNode.getStatus().toString());
    // }
    // }

    // public GameStatus getGameStatus() {
    // return rootNode.getStatus();
    // }

    public Board getBoard() {
        return processorThreads[0].getBoard();
    }

    private DecisionNode getMatchingDecisionNode(Move goodMove) {

        for (int i = 0; i < rootNode.getChildrenSize(); i++) {
            if (Move.equals(rootNode.getChild(i).getMove(), goodMove)) {
                return rootNode.getChild(i);
            }

        }

        if (AISettings.debugOutput) {
            logger.info("Good move (" + goodMove.toString() + ") not found as possibility");
        }

        return null;

    }

    public MoveBook getMoveBook() {
        return moveBook;
    }

    // Debug methods

    /**
     * A recursive method used for debug that keeps track of how many nodes are
     * in the decision tree and at what depth each of them are at.
     *
     * @param branch The branch that is about to have its children counted.
     * @param depth  The depth from the root of the branch.
     */
    private void countChildren(DecisionNode branch, int depth) {

        if (depth < childNum.length) {
            childNum[depth]++;

            if (branch.hasChildren()) {

                for (int i = 0; i < branch.getChildrenSize(); i++) {
                    countChildren(branch.getChild(i), depth + 1);
                }

            }
        }

    }

    private void printChildren(DecisionNode parent) {

        for (int i = 0; i < parent.getChildrenSize(); i++) {
            logger.info(parent.getChild(i).toString());
        }

    }

    private void printRootDebug() {

        for (int i = 0; i < childNum.length; i++) {
            childNum[i] = 0;
        }

        logger.info("Counting Children...");
        // recursive function counts tree nodes and notes their depth
        countChildren(rootNode, 0);

        int totalChildren = 0;
        for (int i = 0; i < childNum.length; i++) {
            totalChildren += childNum[i];

            logger.info(childNum[i] + " at level " + i);

            if (childNum[i] == 0)
                break;
        }

        logger.info("Total Children = " + totalChildren);

        // logger.debug(playerSide.otherSide() + " chose move worth " +
        // rootNode.getChosenPathValue(0));

    }

    public void setUseBook(boolean useBook) {
        AISettings.useBook = useBook;
    }

    @Override
    public String getVersion() {
        return AISettings.version;
    }

    public BoardHashEntry[] getHashTable() {
        return hashTable;
    }

    public void setHashTable(BoardHashEntry[] newHashTable) {
        this.hashTable = newHashTable;
    }

    public int getMoveNum() {
        return moveNum;
    }

    public DecisionNode getRootNode() {
        return rootNode;
    }

    public void getMovePV(DecisionNode root, ArrayList<Move> moves) {
        moves.add(root.getMove().copy());

        if (root.hasChildren()) {
            getMovePV(root.getHeadChild(), moves);
        }

    }

    @Override
    public Game.GameStatus getGameStatus() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void endGame() {
        nextTaskNum = rootNode.getChildrenSize();

        for (int d = 0; d < processorThreads.length; d++) {
            processorThreads[d].stopSearch();
        }
    }

    public void gameOver(int winlose) {

    }

    @Override
    public void showProgress(int progress) {
    }

    @Override
    public void recommendationMade(Move move) {

    }

}
