package chessAI;

import java.util.concurrent.atomic.AtomicBoolean;

import chessBackend.BitBoard;
import chessBackend.Board;
import chessBackend.BoardHashEntry;
import chessBackend.GameStatus;
import chessBackend.Player;
import chessBackend.PlayerContainer;
import chessBackend.Move;
import chessIO.FileIO;
import chessIO.MoveBook;
import chessPieces.Values;

public class AI extends Thread implements Player {
	public static String VERSION = "1.2.052412";
	private boolean debug;

	private PlayerContainer game;
	private MoveBook moveBook;
	private DecisionNode rootNode;
	private int alpha;

	private int minSearchDepth;
	private long maxSearchTime;

	private int[] childNum = new int[200];

	private boolean makeMove;
	private AtomicBoolean undoMove;
	private boolean paused;

	private Object processing;

	private boolean active;

	private int taskDone;
	private int taskSize;
	private DecisionNode nextTask;
	private AIProcessor[] processorThreads;

	// private Hashtable<Long, BoardHashEntry> hashTable;

	private BoardHashEntry[] hashTable;
	private int moveNum;

	private int maxHashSize = 0;

	private boolean useBook;

	public AI(PlayerContainer game, boolean debug) {
		this.debug = debug;
		this.game = game;
		processing = new Object();

		hashTable = new BoardHashEntry[(int) Math.pow(2, BoardHashEntry.hashIndexSize)];// new
																						// Hashtable<Long,
																						// BoardHashEntry>();
		undoMove = new AtomicBoolean();

		moveBook = new MoveBook();
		moveBook.loadMoveBook();

		// Default levels
		minSearchDepth = 3;
		maxSearchTime = 5000;

		// processorThreads = new
		// AIProcessor[Runtime.getRuntime().availableProcessors()];
		processorThreads = new AIProcessor[1];

		for (int i = 0; i < processorThreads.length; i++) {
			processorThreads[i] = new AIProcessor(this, minSearchDepth);
			processorThreads[i].start();
		}

		if (debug) {
			FileIO.log(processorThreads.length + " threads created and started.");
		}

		active = true;

		BitBoard.loadMasks();

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

		for (int i = 0; i < processorThreads.length; i++) {
			processorThreads[i].setBoard(board.getCopy());
			processorThreads[i].setRootNode(rootNode);
		}

		if (debug) {
			printRootDebug();
			FileIO.log(getBoard().toString());
		}

		makeMove = false;
		undoMove.set(false);

		moveNum = 0;
		// hashTable.clear();

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
		if ((mb = moveBook.getRecommendation(getBoard().getHashCode())) == 0 || !useBook) {

			// Split the task of looking at all possible AI moves up amongst
			// multiple threads. This takes advantage of multicore systems.
			delegateProcessors(rootNode);

			aiDecision = rootNode.getChosenChild();
		} else {

			if (debug) {
				FileIO.log("Ai moved based on recommendation");
			}

			aiDecision = getMatchingDecisionNode(mb);
		}

		FileIO.log("Ai took " + (System.currentTimeMillis() - time) + "ms to move.");

		// debug print out of decision tree stats. i.e. the tree size and
		// the values of the move options that the AI has
		if (debug) {
			// game.setDecisionTreeRoot(rootNode);
			printRootDebug();
		}

		// The users decision's chosen child represents the best move based
		// on the user's move
		// setRootNode(aiDecision);

		if (debug) {
			// FileIO.log(getBoard().toString());
		}

		System.gc();

		return aiDecision;
	}

	private void delegateProcessors(DecisionNode root) {

		taskSize = root.getChildrenSize();

		if (taskSize == 0) {
			FileIO.log(this + " didnt see end of game");
			return;
		}

		if (debug) {
			FileIO.log("New task");
		}

		synchronized (processing) {

			alpha = this.getBoard().staticScore() - 100;
			int iniAlpha = alpha;

			long startTime = System.currentTimeMillis();
			long projectedEndTime = 0;
			long[] itTime = new long[100];
			int it = 2;
			boolean checkMateFound = false;
			while ((it <= minSearchDepth || projectedEndTime < maxSearchTime) && !checkMateFound) {

				// This clears ai processors status done flags from previous
				// tasks.
				taskDone = 0;
				nextTask = root.getHeadChild();
				root.removeAllChildren();

				// wake all threads up
				for (int d = 0; d < processorThreads.length; d++) {
					processorThreads[d].setSearchDepth(it);
					processorThreads[d].setNewTask();
				}

				// Wait for all processors to finish their task
				try {
					processing.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				itTime[it] = System.currentTimeMillis() - startTime;

				if (it >= minSearchDepth) {
					projectedEndTime = itTime[it] + itTime[it] * (itTime[it] / itTime[it - 1]) * 2;
					System.out.println("Projected " + (it + 1) + " end time = " + projectedEndTime);
				}

				if ((Math.abs(root.getHeadChild().getChosenPathValue()) & Values.CHECKMATE_MASK) != 0) {
					checkMateFound = true;

				}

				it++;
			}

			if (rootNode.getHeadChild().getChosenPathValue() < iniAlpha) {
				System.out.println("Zugzwang?");
			}

			for (int i = 0; i < 20; i++) {
				System.out.println("Search depth time " + i + " = " + itTime[i]);
			}

		}

	}

	public void taskDone() {

		synchronized (processing) {
			taskDone++;
			if (debug) {
				FileIO.log(this + " " + taskDone + "/" + taskSize + " done");
			}

			if (rootNode.getHeadChild().getChosenPathValue() > alpha) {
				alpha = rootNode.getHeadChild().getChosenPathValue();
			}

			// if (nextTask == null) {
			// processing.notifyAll();
			// }
		}
	}

	public DecisionNode getNextTask() {

		synchronized (processing) {

			DecisionNode task;

			task = nextTask;

			if (nextTask != null) {
				nextTask = nextTask.getNextSibling();
			}

			if (task == null) {
				processing.notifyAll();
			}

			return task;
		}
	}

	public int getAlpha() {
		synchronized (processing) {
			return alpha;
		}
	}

	public void cleanHashTable() {

		// if(hashTable.size()>maxHashSize){
		// maxHashSize = hashTable.size();
		// }
		//
		// //hashTable.clear();
		//
		// int delFrom = moveNum - 2;
		// int removed = 0;
		//
		// if (delFrom > 0) {
		// Iterator<BoardHashEntry> it = hashTable.values().iterator();
		// BoardHashEntry entry;
		// while(it.hasNext()) {
		// entry = it.next();
		// if (entry.getMoveNum() < delFrom) {
		// it.remove();
		// removed++;
		// }
		//
		// }
		// }
		//
		// System.out.println("Removed " + removed + " entries from hashtable");
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

		if (rec != null) {
			return rec.getMove();
		} else {
			return 0;
		}
	}

	private void setRootNode(DecisionNode newRootNode) {

		rootNode = newRootNode;

		// rootNode.setParent(null);
		rootNode.setNextSibling(null);
		// rootNode.setPreviousSibling(rootNode);

		// tell threads about new root node
		for (int i = 0; i < processorThreads.length; i++) {
			processorThreads[i].setRootNode(newRootNode);
		}

		if (debug) {
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

		DecisionNode currentChild = rootNode.getHeadChild();
		while (currentChild != null) {
			if (Move.equals(currentChild.getMove(), goodMove)) {
				return currentChild;
			}

			currentChild = currentChild.getNextSibling();
		}

		if (debug) {
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

		DecisionNode currentChild = branch.getHeadChild();
		while (currentChild != null) {
			countChildren(currentChild, depth + 1);
			currentChild = currentChild.getNextSibling();
		}

	}

	private void printChildren(DecisionNode parent) {

		DecisionNode currentChild = parent.getHeadChild();
		while (currentChild != null) {
			FileIO.log(currentChild.toString());
			currentChild = currentChild.getNextSibling();
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
		this.useBook = useBook;
	}

	@Override
	public String getVersion() {
		return AI.VERSION;
	}

	public BoardHashEntry[] getHashTable() {
		return hashTable;
	}

	public int getMoveNum() {
		return moveNum;
	}

	public int getMaxHashSize() {
		return maxHashSize;
	}

	public DecisionNode getRootNode() {
		return rootNode;
	}

	@Override
	public GameStatus getGameStatus() {
		// TODO Auto-generated method stub
		return null;
	}

}
