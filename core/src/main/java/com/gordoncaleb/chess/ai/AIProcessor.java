package com.gordoncaleb.chess.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.gordoncaleb.chess.backend.*;
import com.gordoncaleb.chess.board.Board;
import com.gordoncaleb.chess.board.Move;
import com.gordoncaleb.chess.board.pieces.Values;

public class AIProcessor extends Thread {
    private DecisionNode rootNode;
    private Board board;

    private int searchDepth;
    private boolean twigGrowthEnabled;

    private int aspirationWindowSize;

    private boolean threadActive;

    private AI ai;

    private BoardHashEntry[] hashTable;

    private long numSearched;

    private boolean stopSearch;

    private long[][] killerMoves = new long[100][AISettings.maxKillerMoves];
    private int[] killerMoveSize = new int[100];

    private StaticScore scorer = new StaticScore();

    // private int[] hashMoveCuts = new int[10];
    // private int[] killerMoveCuts = new int[10];

    // private Move[] voi = { new Move(1, 5, 2, 7), new Move(4, 7, 2, 5), new
    // Move(6, 0, 6, 7) };

    public AIProcessor(AI ai, int maxTreeLevel) {
        this.ai = ai;
        this.searchDepth = maxTreeLevel;
        twigGrowthEnabled = false;
        aspirationWindowSize = 0;

        threadActive = true;

        hashTable = ai.getHashTable();

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

                    // logger.debug("HashTable size = " +
                    // hashTable.size());
                    // hashTable.clear();
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
        clearKillerMoves();
        this.notifyAll();
    }

    public synchronized void setBoard(Board board) {
        this.board = board;
    }

    public Board getBoard() {
        return board;
    }

    public synchronized void setRootNode(DecisionNode rootNode) {

        if (rootNode.getMove() != 0) {
            board.makeMove(rootNode.getMove());
        }

        this.rootNode = rootNode;

        // if game isnt over make sure tree root shows what next valid moves are
        if (!this.rootNode.hasChildren()) {
            board.makeNullMove();
            attachValidMoves(rootNode, 0, -1);

        }

    }

    private void executeTask() {
        DecisionNode task = null;

        numSearched = 0;

        while ((task = ai.getNewTasking(task)) != null) {

            board.makeMove(task.getMove());

            if (AISettings.useLite) {

                task.setChosenPathValue(-growDecisionTreeLite(-Values.CHECKMATE_MOVE + 1, -ai.getAlpha(), searchDepth, task.getMove(), 0));

            } else {
                // task.setAlpha(ai.getAlpha());
                // task.setBeta(Values.CHECKMATE_MOVE - 10);

                growDecisionTree(task, -Values.CHECKMATE_MOVE + 1, -ai.getAlpha(), searchDepth, 0);
            }

            board.undoMove();

            if (stopSearch) {
                task.setChosenPathValue(-10000);
            }

        }

        // for (int i = 0; i < killerMoves.length; i++) {
        // logger.debug("Killer move level " + i + " size=" +
        // killerMoveSize[i]);
        // }
        // for (int i = 0; i < killerMoveCuts.length; i++) {
        // logger.debug("Killer move cuts @level " + i + " size=" +
        // killerMoveCuts[i]);
        // }
        //
        // for (int i = 0; i < hashMoveCuts.length; i++) {
        // logger.debug("Hash move cuts @level " + i + " size=" +
        // hashMoveCuts[i]);
        // }

    }

    private boolean hashTableUpdate(BoardHashEntry present, int cLevel, int cMoveNum) {
        if (present.getMoveNum() <= cMoveNum) {
            return true;
        } else {
            if (cLevel > present.getLevel()) {
                return true;
            } else {
                return false;
            }
        }
    }

    public void clearKillerMoves() {
        for (int i = 0; i < killerMoves.length; i++) {
            killerMoveSize[i] = 0;
            for (int m = 0; m < AISettings.maxKillerMoves; m++) {
                killerMoves[i][m] = 0;
            }
        }
    }

    public void addKillerMove(int level, long move) {

        if (level >= 0) {
            if (killerMoveSize[level] < AISettings.maxKillerMoves) {

                for (int i = 0; i < killerMoveSize[level]; i++) {
                    if (killerMoves[level][i] == move) {
                        return;
                    }
                }

                killerMoves[level][killerMoveSize[level]] = move;
                killerMoveSize[level]++;
            }
        }
    }

    public void attachValidMoves(DecisionNode branch, long hashMove, int level) {

        List<Long> moves;

        if (level >= 0) {
            moves = board.generateValidMoves(hashMove, killerMoves[level]);
        } else {
            moves = board.generateValidMoves(hashMove, AI.noKillerMoves);
        }

        Collections.sort(moves, Collections.reverseOrder());

        DecisionNode[] children = new DecisionNode[moves.size()];

        for (int m = 0; m < moves.size(); m++) {
            children[m] = (new DecisionNode(moves.get(m), Move.getValue(moves.get(m))));
        }

        branch.setChildren(children);
        // branch.sort();

    }

    public boolean isKillerMove(long move, int level) {

        for (int k = 0; k < killerMoveSize[level]; k++) {
            if (move == killerMoves[level][k]) {
                return true;
            }
        }

        return false;
    }

    public void resortChildrenWithKillerMoves(DecisionNode branch, int level, long hashMove) {

        if (level >= 0) {

            boolean resort = false;
            long move;
            for (int i = 0; i < branch.getChildrenSize(); i++) {
                move = branch.getChild(i).getMove();

                if (move == hashMove) {
                    branch.getChild(i).setChosenPathValue(10321);
                    resort = true;
                    continue;
                }

                if (AISettings.useKillerMove) {
                    for (int k = 0; k < killerMoveSize[level]; k++) {
                        if (move == killerMoves[level][k]) {
                            branch.getChild(i).setChosenPathValue(10000 - k - 1);
                            resort = true;
                            break;
                        }
                    }
                }
            }

            if (resort) {
                branch.sort();
            }
        }
    }

    /**
     * @param branch Branch to grow
     * @param level  Level from the rootNode that the branch is at
     * @return
     */
    private void growDecisionTree(DecisionNode branch, int alpha, int beta, int level, int bonusLevel) {

        if (stopSearch) {
            return;
        }

        int a = alpha;

        // branch.setAlpha(alpha);
        // branch.setBeta(beta);

        numSearched++;

        if (branch.hasBeenVisited() && !branch.isGameOver()) {

            if ((Math.abs(branch.getChosenPathValue()) & Values.CHECKMATE_MASK) != 0) {
                return;
            }

//			if (branch.getHeadChild().isGameOver()) {
//				//branch.setChosenPathValue(-branch.getHeadChild().getChosenPathValue());
//				
//			}
        }

        BoardHashEntry hashOut;
        long hashMove = 0;
        int hashIndex = (int) (board.getHashCode() & AISettings.hashIndexMask);
        hashOut = hashTable[hashIndex];

        if (AISettings.useHashTable) {

            if (hashOut != null) {
                if (hashOut.getHashCode() == board.getHashCode()) {

                    // if (hashOut.getStringBoard().compareTo(board.toString())
                    // != 0) {
                    // logger.info(hashOut.getStringBoard() + "\n!=\n" +
                    // board.toString());
                    // synchronized (this) {
                    // try {
                    // this.wait();
                    // } catch (InterruptedException e) {
                    // e.printStackTrace();
                    // }
                    // }
                    // }

                    hashMove = hashOut.getBestMove();

                    if (hashOut.getLevel() >= level) {

                        if (hashOut.getBounds() == BoardHashEntry.ValueBounds.PV) {

                            if (!board.drawByThreeRule()) {
                                branch.setChosenPathValue(-hashOut.getScore());
                            } else {
                                branch.setChosenPathValue(0);
                            }

                            return;
                        } else {

                            if (hashOut.getBounds() == BoardHashEntry.ValueBounds.CUT) {
                                if (hashOut.getScore() >= beta) {

                                    if (!board.drawByThreeRule()) {
                                        branch.setChosenPathValue(-hashOut.getScore());
                                    } else {
                                        branch.setChosenPathValue(0);
                                    }
                                    return;
                                }
                            }
                        }
                    }

                }
            }
        }

        int cpv = Integer.MIN_VALUE;

        if (!branch.hasBeenVisited()) {

            board.makeNullMove();

            if (AISettings.bonusEnable) {

                if ((board.getBoardStatus() == Game.GameStatus.CHECK) && (level > -AISettings.maxInCheckFrontierLevel)) {
                    bonusLevel = Math.min(bonusLevel, level - 2);
                } else {
//					if (board.canQueen()) {
//						// logger.debug("Can Queen \n" +
//						// board.toString());
//						//bonusLevel = Math.min(bonusLevel, level - 1);
//					}
                }

                if (branch.isQueenPromotion()) {
                    bonusLevel = Math.min(bonusLevel, level - 1);
                }

                if (branch.hasPieceTaken() && (level > -AISettings.maxPieceTakenFrontierLevel)) {
                    bonusLevel = Math.min(bonusLevel, level - 1);
                }
            }

            if (level > bonusLevel) {
                // check all moves of all pieces

                attachValidMoves(branch, hashMove, level);

                if (board.isGameOver()) {

                    if (board.isInCheckMate()) {
                        branch.setChosenPathValue(Values.CHECKMATE_MOVE);
                    } else {
                        branch.setChosenPathValue(0);
                    }

                    // branch.setBound(ValueBounds.PV);

                    return;
                }

            } else {
                branch.setChosenPathValue(-scorer.staticScore(board));
                // branch.setBound(ValueBounds.PV);
                return;
            }

        }

        if (!branch.isGameOver()) {

            resortChildrenWithKillerMoves(branch, level, hashMove);

            boolean pruned = false;
            for (int i = 0; i < branch.getChildrenSize(); i++) {

                if (!pruned) {
                    board.makeMove(branch.getChild(i).getMove());

                    if (level > 0) {

                        growDecisionTree(branch.getChild(i), -beta, -alpha, level - 1, bonusLevel);

                    } else {
                        // bonus depth

                        if (AISettings.bonusEnable) {

                            if (twigGrowthEnabled) {
                                branch.getChild(i).setChosenPathValue(-growDecisionTreeLite(-beta, -alpha, level - 1, branch.getChild(i).getMove(), bonusLevel));
                                // branch.getChild(i).setBound(getNodeType(-branch.getChild(i).getChosenPathValue(),
                                // -beta, -alpha));
                            } else {
                                growDecisionTree(branch.getChild(i), -beta, -alpha, level - 1, bonusLevel);
                            }
                        }

                    }

                    board.undoMove();

                    cpv = Math.max(cpv, branch.getChild(i).getChosenPathValue());

                    // alpha beta pruning
                    if (AISettings.alphaBetaPruningEnabled) {

                        if (cpv > alpha) {
                            alpha = cpv;
                        }

                        if (alpha >= beta) {
                            pruned = true;

                        }
                    }
                } else {
                    branch.getChild(i).setChosenPathValue(-10123);
                }

            }

            branch.sort();

            if ((Math.abs(cpv) & Values.CHECKMATE_MASK) != 0) {
                if (cpv > 0) {
                    cpv--;
                } else {
                    cpv++;
                }
            }

            branch.setChosenPathValue(-cpv);
            // branch.setBound(getNodeType(cpv, a, b));

            if (getNodeType(cpv, a, beta) != BoardHashEntry.ValueBounds.ALL && level >= 0 && AISettings.useKillerMove) {
                addKillerMove(level, branch.getHeadChild().getMove());
            }

            // if (getNodeType(cpv, a, b) == ValueBounds.CUT && level >= 0) {
            // if (branch.getHeadChild().getMove() == hashMove) {
            // hashMoveCuts[level]++;
            // } else {
            // if (isKillerMove(branch.getHeadChild().getMove(), level)) {
            // killerMoveCuts[level]++;
            // }
            // }
            //
            // }

            if (AISettings.useHashTable && level >= 0 && !stopSearch) {
                if (hashOut == null) {
                    hashTable[hashIndex] = new BoardHashEntry(board.getHashCode(), level, cpv, ai.getMoveNum(), getNodeType(cpv, a, beta), branch.getHeadChild()
                            .getMove());
                } else {
                    if (hashTableUpdate(hashOut, level, ai.getMoveNum())) {
                        hashOut.setAll(board.getHashCode(), level, cpv, ai.getMoveNum(), getNodeType(cpv, a, beta), branch.getHeadChild().getMove());// ,board.toString());
                    }
                }

            }

        }

    }

    private BoardHashEntry.ValueBounds getNodeType(int s, int a, int b) {
        if (s >= b) {
            return BoardHashEntry.ValueBounds.CUT;
        }

        if (s < a) {
            return BoardHashEntry.ValueBounds.ALL;
        }

        return BoardHashEntry.ValueBounds.PV;
    }

    /**
     * This method has similar functionality as 'growDecisionTree()' but it
     * doesn't add on to the decision tree data structure. Adding on to the data
     * structure at some tree depths can cause the program to overflow the heap.
     * This method is used as a quick alternative to 'growDecisionTree()' at
     * large tree depths.
     *
     * @param level  The stack distance from the initial call on
     *               'growDecisionTreeLite()'
     * @return The value of the best move for 'player' on the 'board'
     */
    private int growDecisionTreeLite(int alpha, int beta, int level, long moveMade, int bonusLevel) {
        int suggestedPathValue;
        int bestPathValue = Integer.MIN_VALUE;

        numSearched++;

        int a = alpha;
        int b = beta;
        long bestMove = 0;

        int hashIndex = (int) (board.getHashCode() & AISettings.hashIndexMask);
        BoardHashEntry hashOut;
        long hashMove = 0;
        hashOut = hashTable[hashIndex];

        if (AISettings.useHashTable) {

            if (hashOut != null) {
                if (hashOut.getHashCode() == board.getHashCode()) {

                    // if (hashOut.getStringBoard().compareTo(board.toString())
                    // != 0) {
                    // logger.info(hashOut.getStringBoard() + "\n!=\n" +
                    // board.toString());
                    // synchronized (this) {
                    // try {
                    // this.wait();
                    // } catch (InterruptedException e) {
                    // e.printStackTrace();
                    // }
                    // }
                    // }

                    if (hashOut.getLevel() >= level) {

                        if (hashOut.getBounds() == BoardHashEntry.ValueBounds.PV) {
                            return hashOut.getScore();
                        } else {

                            if (hashOut.getBounds() == BoardHashEntry.ValueBounds.CUT) {
                                if (hashOut.getScore() >= beta) {
                                    return hashOut.getScore();
                                } else {
                                    hashMove = hashOut.getBestMove();
                                }
                            } else {
                                hashMove = hashOut.getBestMove();
                            }
                        }
                    } else {
                        hashMove = hashOut.getBestMove();
                    }

                }
            }
        }

        board.makeNullMove();

        if (AISettings.bonusEnable) {

            if ((board.getBoardStatus() == Game.GameStatus.CHECK) && (level > -AISettings.maxInCheckFrontierLevel)) {
                bonusLevel = Math.min(bonusLevel, level - 2);
            }

            if ((Move.hasPieceTaken(moveMade)) && (level > -AISettings.maxPieceTakenFrontierLevel)) {
                bonusLevel = Math.min(bonusLevel, level - 1);
            }
        }

        if (level > bonusLevel) {

            if (board.insufficientMaterial() || board.drawByThreeRule()) {
                board.setBoardStatus(Game.GameStatus.DRAW);
            } else {
                ArrayList<Long> moves = new ArrayList<>(board.generateValidMoves(hashMove, AI.noKillerMoves));

                if (moves.size() == 0) {
                    if (board.isInCheck()) {
                        board.setBoardStatus(Game.GameStatus.CHECKMATE);
                    } else {
                        board.setBoardStatus(Game.GameStatus.STALEMATE);
                    }
                } else {

                    Collections.sort(moves, Collections.reverseOrder());

                    Game.GameStatus tempBoardState;
                    for (Long move : moves) {

                        tempBoardState = board.getBoardStatus();

                        board.makeMove(move);

                        suggestedPathValue = -growDecisionTreeLite(-beta, -alpha, level - 1, move, bonusLevel);

                        board.undoMove();

                        board.setBoardStatus(tempBoardState);

                        if (suggestedPathValue > bestPathValue) {
                            bestPathValue = suggestedPathValue;
                            bestMove = move;
                        }

                        if (bestPathValue > alpha) {
                            alpha = bestPathValue;
                        }

                        if (alpha >= beta) {
                            break;
                        }

                    }

                }
            }
        } else {
            bestPathValue = scorer.staticScore(board);
        }

        if (board.isInCheckMate()) {
            bestPathValue = -(Values.CHECKMATE_MOVE - (searchDepth - level));
        } else {
            if (board.isInStaleMate() || board.isDraw()) {
                bestPathValue = 0;
            }
        }

        if (getNodeType(bestPathValue, a, b) != BoardHashEntry.ValueBounds.ALL && level >= 0 && AISettings.useKillerMove) {
            addKillerMove(level, bestMove);
        }

        if (AISettings.useHashTable && level >= 0) {
            if (hashOut == null) {
                hashTable[hashIndex] = new BoardHashEntry(board.getHashCode(), level, bestPathValue, ai.getMoveNum(), getNodeType(bestPathValue, a, b), bestMove);
            } else {
                if (hashTableUpdate(hashOut, level, ai.getMoveNum())) {
                    hashOut.setAll(board.getHashCode(), level, bestPathValue, ai.getMoveNum(), getNodeType(bestPathValue, a, b), bestMove);// ,board.toString());
                }
            }

        }

        return bestPathValue;
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
