package chessAI;

import chessBackend.Board;
import chessBackend.Game;
import chessBackend.GameStatus;
import chessBackend.Player;
import chessBackend.Side;
import chessBackend.Move;
import chessIO.MoveBook;

public class AI extends Thread implements Player {
	private boolean debug;

	private Game game;
	private MoveBook moveBook;
	private DecisionNode rootNode;

	private int maxTwigLevel;
	private int maxDecisionTreeLevel;

	private int[] childNum = new int[20];

	private boolean opponentMoved;
	private DecisionNode opponentsDecision;

	private boolean undoMove;

	private Side playerSide;

	private int taskDone;
	private int taskLeft;
	private int taskSize;
	private DecisionNode nextTask;
	private AIProcessor[] processorThreads;

	public AI(Game game, boolean debug) {
		this.debug = debug;
		this.game = game;

		moveBook = new MoveBook();

		// Default levels
		maxDecisionTreeLevel = 2;
		maxTwigLevel = 0;

		// processorThreads = new
		// AIProcessor[Runtime.getRuntime().availableProcessors()];
		processorThreads = new AIProcessor[1];
		for (int i = 0; i < processorThreads.length; i++) {
			processorThreads[i] = new AIProcessor(this, maxDecisionTreeLevel, maxTwigLevel);
			processorThreads[i].start();
		}

		if (debug) {
			System.out.println(processorThreads.length + " threads created and started.");
		}

		this.start();
	}

	@Override
	public void run() {

		while (true) {
			synchronized (this) {
				try {
					this.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			if (opponentMoved) {
				if (moveAI()) {
					game.makeMove(rootNode.getMove());
				}
				opponentMoved = false;
			}

			if (undoMove) {
				undoToOpponentsMove();
				undoMove = false;
			}

		}

	}

	public synchronized Side newGame(Side playerSide, Board board) {

		this.playerSide = playerSide;

		rootNode = new DecisionNode(null, null);

		for (int i = 0; i < processorThreads.length; i++) {
			processorThreads[i].setBoard(board.getCopy());
			processorThreads[i].setRootNode(rootNode);
		}

		if (debug) {
			printRootDebug();
			System.out.println(getBoard().toString());
		}

		if (isMyTurn()) {
			opponentMoved = true;
			notifyAll();
		}

		return null;

	}

	/**
	 * This method notifies the AI that the user has moved and also tells gives
	 * it the move that the user made.
	 * 
	 * @param usersDecision
	 *            The users move. This has a reference to the position on the
	 *            decision tree that the user effectively selected.
	 * @return The new root node, which is the AI response to 'userMove'.
	 */
	public synchronized boolean opponentMoved(Move opponentsMove) {
		setRootNode(getMatchingDecisionNode(opponentsMove));
		opponentMoved = true;
		notifyAll();

		return true;
	}

	/**
	 * At this point the root should be the user's move. This method finds the
	 * best response for the AI.
	 */
	private boolean moveAI() {

		// Game Over?
		if (rootNode.isGameOver()) {
			return false;
		}

		long time = 0;
		DecisionNode aiDecision;

		time = System.currentTimeMillis();

		Move mb;
		if ((mb = moveBook.getRecommendation(getBoard().getHashCode())) == null) {

			// Split the task of looking at all possible AI moves up amongst
			// multiple threads. This takes advantage of multicore systems.
			delegateProcessors(rootNode);

			aiDecision = rootNode.getChosenChild();
		} else {

			if (debug) {
				System.out.println("Ai moved based on recommendation");
			}

			aiDecision = getMatchingDecisionNode(mb);
		}

		System.out.println("Ai took " + (System.currentTimeMillis() - time) + "ms to move.");

		// debug print out of decision tree stats. i.e. the tree size and
		// the values of the move options that the AI has
		if (debug) {
			game.setDecisionTreeRoot(rootNode);
			printRootDebug();
		}

		// The users decision's chosen child represents the best move based
		// on the user's move
		setRootNode(aiDecision);

		if (debug) {
			System.out.println(getBoard().toString());
		}

		return true;
	}

	private void delegateProcessors(DecisionNode root) {

		// This clears ai processors status done flags from previous
		// tasks.
		clearTaskDone();

		taskSize = root.getChildrenSize();
		taskLeft = taskSize;
		nextTask = root.getHeadChild();

		// detach all children and add them back when they have been processed.
		// This allows pruning at the root level.
		root.removeAllChildren();

		if (debug) {
			System.out.println("New task");
		}

		for (int d = 0; d < processorThreads.length; d++) {
			processorThreads[d].setNewTask();
		}

		// Wait for all processors to finish their task
		while (taskDone < taskSize) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	public synchronized void taskDone() {
		taskDone++;
		if (debug) {
			System.out.println(taskDone + "/" + taskSize + " done");
		}
	}

	public synchronized DecisionNode getNextTask() {

		DecisionNode task;

		if (taskLeft == 0) {
			task = null;
		} else {
			task = nextTask;
			taskLeft--;
			nextTask = nextTask.getNextSibling();
		}

		return task;
	}

	public void clearTaskDone() {
		taskDone = 0;
	}

	private void undoToOpponentsMove() {
		if (canUndo()) {

			rootNode = new DecisionNode(null, null);

			for (int i = 0; i < processorThreads.length; i++) {
				processorThreads[i].getBoard().undoMove();
				processorThreads[i].getBoard().undoMove();
				processorThreads[i].setRootNode(rootNode);
			}

		}
	}

	public synchronized boolean undoMove() {
		undoMove = true;
		notifyAll();
		return canUndo();
	}

	private boolean canUndo() {
		if ((isMyTurn() && (getBoard().getMoveHistory().size() > 0)) || (getBoard().getMoveHistory().size() > 1)) {
			return true;
		} else {
			return false;
		}
	}

	public Move getRecommendation() {
		return null;
	}

	private void setRootNode(DecisionNode newRootNode) {

		// tell threads about new root node
		for (int i = 0; i < processorThreads.length; i++) {
			processorThreads[i].setRootNode(newRootNode);
		}

		rootNode = newRootNode;

		rootNode.setParent(null);
		rootNode.setNextSibling(null);
		rootNode.setPreviousSibling(null);

		if (debug) {
			System.out.println("Board hash code = " + Long.toHexString(getBoard().getHashCode()));
		}

		setGameSatus(rootNode.getStatus(), getBoard().getTurn());

		System.gc();
	}

	public void setGameSatus(GameStatus status, Side playerTurn) {
		if (status != GameStatus.IN_PLAY) {
			System.out.println(rootNode.getStatus().toString());
		}
	}

	@Override
	public Side getSide() {
		return playerSide;
	}

	@Override
	public void setSide(Side side) {
		// TODO Auto-generated method stub

	}

	private Board getBoard() {
		return processorThreads[0].getBoard();
	}

	public boolean isMyTurn() {
		return (playerSide == getBoard().getTurn());
	}

	private DecisionNode getMatchingDecisionNode(Move goodMove) {
		int childrenSize = rootNode.getChildrenSize();

		DecisionNode currentChild = rootNode.getHeadChild();
		for (int i = 0; i < childrenSize; i++) {
			if (currentChild.getMove().equals(goodMove)) {
				return currentChild;
			}

			currentChild = currentChild.getNextSibling();
		}

		if (debug) {
			System.out.println("Good move (" + goodMove.toString() + ") not found as possibility");
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
		for (int i = 0; i < branch.getChildrenSize(); i++) {
			countChildren(currentChild, depth + 1);
			currentChild = currentChild.getNextSibling();
		}

	}

	private void printChildren(DecisionNode parent) {

		DecisionNode currentChild = parent.getHeadChild();
		for (int i = 0; i < parent.getChildrenSize(); i++) {
			System.out.println(currentChild.toString());
			currentChild = currentChild.getNextSibling();
		}

	}

	private void printRootDebug() {
		if (opponentsDecision != null) {
			printChildren(opponentsDecision);
		}

		for (int i = 0; i < childNum.length; i++) {
			childNum[i] = 0;
		}

		System.out.println("Counting Children...");
		// recursive function counts tree nodes and notes their depth
		countChildren(rootNode, 0);

		int totalChildren = 0;
		for (int i = 0; i < childNum.length; i++) {
			totalChildren += childNum[i];

			System.out.println(childNum[i] + " at level " + i);

			if (childNum[i] == 0)
				break;
		}

		System.out.println("Total Children = " + totalChildren);

		System.out.println(playerSide.otherSide() + " chose move worth " + rootNode.getChosenPathValue(0));

	}

	public int getMoveChosenPathValue(Move m) {
		return getMatchingDecisionNode(m).getChosenPathValue(0);
	}

}
