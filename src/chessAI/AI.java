package chessAI;

import java.util.concurrent.atomic.AtomicBoolean;

import chessBackend.Board;
import chessBackend.BoardHashEntry;
import chessBackend.GameStatus;
import chessBackend.Player;
import chessBackend.PlayerContainer;
import chessBackend.Move;
import chessBackend.ValueBounds;
import chessIO.FileIO;
import chessIO.MoveBook;
import chessPieces.Values;

public class AI extends Thread implements Player {
	// public static String version = AISettings.version;
	public static long[] noKillerMoves = {};

	// private boolean debug = AISettings.debugOutput;

	private PlayerContainer game;
	private MoveBook moveBook;

	private DecisionNode rootNode;
	private int alpha;

	// private int minSearchDepth = AISettings.minSearchDepth;
	// private long maxSearchTime = AISettings.maxSearchTime;
	// private boolean useBook = AISettings.useBook;
	// private final boolean useExtraTime = AISettings.useExtraTime;
	// private int staleHashAge = AISettings.staleHashAge;

	private int[] childNum = new int[200];

	private boolean makeMove;
	private AtomicBoolean undoMove;
	private boolean paused;

	private Object processing;

	private boolean active;

	private int taskDone;
	private int taskSize;
	private int nextTask;
	private AIProcessor[] processorThreads;

	private long maxSearched;
	private long searchedThisGame;

	private BoardHashEntry[] hashTable;
	private int moveNum;

	public AI(PlayerContainer game, boolean debug) {
		// this.debug = debug | this.debug;
		this.game = game;
		processing = new Object();

		hashTable = new BoardHashEntry[AISettings.hashTableSize];

		undoMove = new AtomicBoolean();

		moveBook = new MoveBook();
		moveBook.loadMoveBook();

		processorThreads = new AIProcessor[AISettings.numOfThreads];

		for (int i = 0; i < processorThreads.length; i++) {
			processorThreads[i] = new AIProcessor(this, AISettings.minSearchDepth);
			processorThreads[i].start();
		}

		if (debug) {
			FileIO.log(processorThreads.length + " threads created and started.");
		}

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

				if (undoMove.get()) {
					undoMoveOnSubThreads();
					undoMove.set(false);
				}

				if (makeMove && !paused) {

					aiDecision = getAIDecision();

					if (aiDecision != null && !undoMove.get()) {
						// setRootNode(aiDecision);
						game.makeMove(aiDecision.getMove());
					}

					if (AISettings.useHashTable) {
						cleanHashTable();
					}

					makeMove = false;
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

		rootNode = new DecisionNode(0);

		clearHashTable();

		for (int i = 0; i < processorThreads.length; i++) {
			processorThreads[i].setBoard(board.getCopy());
			processorThreads[i].setRootNode(rootNode);
		}

		if (AISettings.debugOutput) {
			// printRootDebug();
			FileIO.log(getBoard().toString());
		}

		makeMove = false;
		undoMove.set(false);

		moveNum = 0;
		// hashTable.clear();

		maxSearched = 0;
		searchedThisGame = 0;

		FileIO.log("New game");

	}

	public synchronized void makeMove() {
		makeMove = true;
		notifyAll();
	}

	public synchronized void pause() {
		paused = !paused;
		notifyAll();
	}

	public long undoMove() {
		if (canUndo()) {
			undoMove.set(true);

			moveNum--;

			synchronized (this) {
				notifyAll();
			}

			return getBoard().getLastMoveMade();

		} else {

			return 0;

		}
	}

	/**
	 * Blocks until move has been made
	 */
	public synchronized boolean moveMade(long move) {

		DecisionNode decision = getMatchingDecisionNode(move);

		if (decision != null) {

			setRootNode(decision);

			FileIO.log(Move.toString(move));
			FileIO.log(getBoard().toString());

			moveNum++;

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

		long mb;
		if ((mb = moveBook.getRecommendation(getBoard().getHashCode())) == 0 || !AISettings.useBook) {

			// Split the task of looking at all possible AI moves up amongst
			// multiple threads. This takes advantage of multicore systems.
			delegateProcessors(rootNode);

			aiDecision = rootNode.getHeadChild();
		} else {

			if (AISettings.debugOutput) {
				FileIO.log("Ai moved based on recommendation");
			}

			aiDecision = getMatchingDecisionNode(mb);
		}

		FileIO.log("Ai took " + (System.currentTimeMillis() - time) + "ms to move.");

		// debug print out of decision tree stats. i.e. the tree size and
		// the values of the move options that the AI has
		if (AISettings.debugOutput) {
			// game.setDecisionTreeRoot(rootNode);
			// printRootDebug();
		}

		// The users decision's chosen child represents the best move based
		// on the user's move
		// setRootNode(aiDecision);

		if (AISettings.debugOutput) {
			// FileIO.log(getBoard().toString());
		}

		System.gc();

		return aiDecision;
	}

	private void delegateProcessors(DecisionNode root) {

		taskSize = root.getChildrenSize();
		long totalSearched = 0;

		if (taskSize < 2) {
			return;
		}

		if (AISettings.debugOutput) {
			FileIO.log("New task");
		}

		synchronized (processing) {

			long startTime = System.currentTimeMillis();
			long timeLeft = AISettings.maxSearchTime;
			int it = AISettings.minSearchDepth;
			boolean checkMateFound = false;

			while (!checkMateFound && timeLeft > 0) {

				// This clears ai processors status done flags from previous
				// tasks.
				taskDone = 0;
				nextTask = 0;
				alpha = -Values.CHECKMATE_MOVE + 1;
				// root.removeAllChildren();

				// wake all threads up
				for (int d = 0; d < processorThreads.length; d++) {
					processorThreads[d].setSearchDepth(it);
					processorThreads[d].setNewTask();
				}

				// Wait for all processors to finish their task
				try {

					if (it > AISettings.minSearchDepth) {
						processing.wait(timeLeft);

						if (nextTask != taskSize) {

							nextTask = taskSize;

							for (int d = 0; d < processorThreads.length; d++) {
								processorThreads[d].stopSearch();
							}

							processing.wait();

							rootNode.sort(taskDone);

						} else {
							rootNode.sort();
						}

					} else {
						processing.wait();
						rootNode.sort();
					}

				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				timeLeft = AISettings.maxSearchTime - (System.currentTimeMillis() - startTime);

				if ((Math.abs(root.getHeadChild().getChosenPathValue()) & Values.CHECKMATE_MASK) != 0) {
					checkMateFound = true;
				}

				for (int d = 0; d < processorThreads.length; d++) {

					maxSearched = Math.max(maxSearched, processorThreads[d].getNumSearched());

					totalSearched += processorThreads[d].getNumSearched();
					System.out.println("Searched " + totalSearched + " in " + (AISettings.maxSearchTime - timeLeft));
					System.out.println((double) totalSearched / (double) (AISettings.maxSearchTime - timeLeft) + " nodes/ms");
				}

				it++;

				if (!AISettings.useExtraTime) {
					break;
				}
			}

			searchedThisGame += totalSearched;

			FileIO.log("Max Searched " + maxSearched);
			FileIO.log("Searched " + searchedThisGame + " this game");

		}

	}

	public void taskDone(DecisionNode task) {

		synchronized (processing) {
			taskDone++;
			if (AISettings.debugOutput) {
				FileIO.log(this + " " + taskDone + "/" + taskSize + " done");
			}

			if (task.getChosenPathValue() > alpha) {
				alpha = task.getChosenPathValue();
			}

			if (alpha >= Values.CHECKMATE_MOVE - 5) {
				nextTask = taskSize;
			}

			// if (nextTask == null) {
			// processing.notifyAll();
			// }
		}
	}

	public DecisionNode getNextTask() {

		synchronized (processing) {

			DecisionNode task;

			if (nextTask < taskSize) {
				task = rootNode.getChild(nextTask);
				nextTask++;

				return task;

			} else {

				processing.notifyAll();

				return null;

			}

		}
	}

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
		rootNode = new DecisionNode(0);

		for (int i = 0; i < processorThreads.length; i++) {
			synchronized (processorThreads[i]) {
				processorThreads[i].setRootNode(rootNode);
			}
		}
	}

	private void undoMoveOnSubThreads() {
		if (canUndo()) {

			rootNode = new DecisionNode(0);

			for (int i = 0; i < processorThreads.length; i++) {
				synchronized (processorThreads[i]) {
					processorThreads[i].getBoard().undoMove();
					processorThreads[i].setRootNode(rootNode);
				}
			}

		}
	}

	private boolean canUndo() {
		return (getBoard().getMoveHistory().size() > 0);
	}

	public synchronized long makeRecommendation() {
		DecisionNode rec = getAIDecision();

		printRootDebug();

		if (rec != null) {
			return rec.getMove();
		} else {
			return 0;
		}
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
			FileIO.log("Board hash code = " + Long.toHexString(getBoard().getHashCode()));
		}

		// setGameSatus(rootNode.getStatus(), getBoard().getTurn());

		System.gc();
	}

	@Override
	public void setGame(PlayerContainer game) {
		this.game = game;
	}

	// public void setGameSatus(GameStatus status, Side playerTurn) {
	// if (status != GameStatus.IN_PLAY) {
	// FileIO.log(rootNode.getStatus().toString());
	// }
	// }

	// public GameStatus getGameStatus() {
	// return rootNode.getStatus();
	// }

	public Board getBoard() {
		return processorThreads[0].getBoard();
	}

	private DecisionNode getMatchingDecisionNode(long goodMove) {

		for (int i = 0; i < rootNode.getChildrenSize(); i++) {
			if (Move.equals(rootNode.getChild(i).getMove(), goodMove)) {
				return rootNode.getChild(i);
			}

		}

		if (AISettings.debugOutput) {
			FileIO.log("Good move (" + Move.toString(goodMove) + ") not found as possibility");
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
	 * @param branch
	 *            The branch that is about to have its children counted.
	 * @param depth
	 *            The depth from the root of the branch.
	 */
	private void countChildren(DecisionNode branch, int depth) {

		childNum[depth]++;

		if (branch.hasChildren()) {

			for (int i = 0; i < branch.getChildrenSize(); i++) {
				countChildren(branch.getChild(i), depth + 1);
			}

		}

	}

	private void printChildren(DecisionNode parent) {

		for (int i = 0; i < parent.getChildrenSize(); i++) {
			FileIO.log(parent.getChild(i).toString());
		}

	}

	private void printRootDebug() {

		for (int i = 0; i < childNum.length; i++) {
			childNum[i] = 0;
		}

		FileIO.log("Counting Children...");
		// recursive function counts tree nodes and notes their depth
		countChildren(rootNode, 0);

		int totalChildren = 0;
		for (int i = 0; i < childNum.length; i++) {
			totalChildren += childNum[i];

			FileIO.log(childNum[i] + " at level " + i);

			if (childNum[i] == 0)
				break;
		}

		FileIO.log("Total Children = " + totalChildren);

		// System.out.println(playerSide.otherSide() + " chose move worth " +
		// rootNode.getChosenPathValue(0));

	}

	public int getMoveChosenPathValue(long m) {
		return getMatchingDecisionNode(m).getChosenPathValue();
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

	public int getMoveNum() {
		return moveNum;
	}

	public DecisionNode getRootNode() {
		return rootNode;
	}

	@Override
	public GameStatus getGameStatus() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void endGame() {
		nextTask = taskSize;

		for (int d = 0; d < processorThreads.length; d++) {
			processorThreads[d].stopSearch();
		}
	}

}
