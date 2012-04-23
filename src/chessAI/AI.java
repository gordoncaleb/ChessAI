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

	private boolean makeMove;
	private DecisionNode opponentsDecision;

	private boolean undoMove;

	private boolean paused;
	private Object processing;

	private boolean active;

	private Side playerSide;

	private int taskDone;
	private int taskLeft;
	private int taskSize;
	private DecisionNode nextTask;
	private AIProcessor[] processorThreads;

	public AI(Game game, boolean debug) {
		this.debug = debug;
		this.game = game;
		paused = false;
		processing = new Object();

		moveBook = new MoveBook();

		// Default levels
		maxDecisionTreeLevel = 2;
		maxTwigLevel = 0;

		// processorThreads = new
		// AIProcessor[Runtime.getRuntime().availableProcessors()];
		processorThreads = new AIProcessor[1];
		for (int i = 0; i < processorThreads.length; i++) {
			processorThreads[i] = new AIProcessor(this, maxDecisionTreeLevel,
					maxTwigLevel);
			processorThreads[i].start();
		}

		if (debug) {
			System.out.println(processorThreads.length
					+ " threads created and started.");
		}

		active = true;

		this.start();
	}

	@Override
	public void run() {
		DecisionNode aiDecision;

		while (active) {
			synchronized (this) {
				try {

					this.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				if (undoMove) {

					undoMoveOnSubThreads();
					undoMove = false;

				}

				if (makeMove && !paused) {

					aiDecision = getAIDecision();

					if (aiDecision != null && !undoMove) {
						setRootNode(aiDecision);
						game.makeMove(aiDecision.getMove(), this);
					}

					makeMove = false;
				}

			}

		}
		
		for (int i = 0; i < processorThreads.length; i++) {
			processorThreads[i].killThread();
		}

	}

	public void endGame() {
		active = false;

		synchronized (this) {
			notifyAll();
		}
	}

	public synchronized Side newGame(Side playerSide, Board board) {

		makeMove = false;
		undoMove = false;

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
			makeMove = true;
			notifyAll();
		}

		return null;

	}

	public synchronized boolean moveMade(Move move) {

		DecisionNode decision = getMatchingDecisionNode(move);

		if (decision != null) {

			setRootNode(decision);
			
			System.out.println(this + " Game status =" + rootNode.getStatus());

			if (isMyTurn()) {
				makeMove = true;
				notifyAll();
			} else {
				makeMove = false;
			}

			return true;

		} else {
			return false;
		}

	}

	public synchronized void pause() {
		paused = !paused;
		notifyAll();
	}

	public Move undoMove() {
		if (canUndo()) {
			undoMove = true;

			synchronized (this) {
				notifyAll();
			}

			return getBoard().getLastMoveMade();

		} else {

			return null;

		}
	}

	@Override
	public void setSide(Side side) {
		// TODO Auto-generated method stub

	}

	/**
	 * At this point the root should be the user's move. This method finds the
	 * best response for the AI.
	 */
	private DecisionNode getAIDecision() {

		// Game Over?
		if (rootNode.isGameOver()) {
			return null;
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

		System.out.println("Ai took " + (System.currentTimeMillis() - time)
				+ "ms to move.");

		// debug print out of decision tree stats. i.e. the tree size and
		// the values of the move options that the AI has
		if (debug) {
			game.setDecisionTreeRoot(rootNode);
			printRootDebug();
		}

		// The users decision's chosen child represents the best move based
		// on the user's move
		// setRootNode(aiDecision);

		if (debug) {
			System.out.println(getBoard().toString());
		}

		return aiDecision;
	}

	private void delegateProcessors(DecisionNode root) {

		// This clears ai processors status done flags from previous
		// tasks.
		clearTaskDone();

		taskSize = root.getChildrenSize();
		taskLeft = taskSize;
		
		if(taskLeft == 0){
			
			System.out.println(this + " didnt see end of game");
			return;
		}
		
		nextTask = root.getHeadChild();

		// detach all children and add them back when they have been processed.
		// This allows pruning at the root level.
		root.removeAllChildren();

		if (debug) {
			System.out.println("New task");
		}

		synchronized (processing) {

			// wake all threads up
			for (int d = 0; d < processorThreads.length; d++) {
				processorThreads[d].setNewTask();
			}

			// Wait for all processors to finish their task
			try {
				processing.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	public void taskDone() {

		synchronized (processing) {
			taskDone++;
			if (debug) {
				System.out.println(this + " " + taskDone + "/" + taskSize + " done");
			}

			if (taskDone >= taskSize) {
				processing.notifyAll();
			}
		}
	}

	public DecisionNode getNextTask() {

		synchronized (processing) {

			DecisionNode task;

			if (taskLeft == 0) {
				task = null;
			} else {
				task = nextTask;
				taskLeft--;
				nextTask = nextTask.getNextSibling();
			}
			
			System.out.println(this + " " + taskLeft);

			if (task == null && taskLeft != 0) {
				System.out.println("Next task was null but taskleft wasnt 0");
			}

			return task;
		}
	}

	public void clearTaskDone() {

		synchronized (processing) {
			taskDone = 0;
		}

	}

	private void undoMoveOnSubThreads() {
		if (canUndo()) {

			rootNode = new DecisionNode(null, null);

			for (int i = 0; i < processorThreads.length; i++) {
				processorThreads[i].getBoard().undoMove();
				processorThreads[i].setRootNode(rootNode);
			}

			if (isMyTurn()) {
				makeMove = true;
			} else {
				makeMove = false;
			}

		}
	}

	private boolean canUndo() {
		return (getBoard().getMoveHistory().size() > 0);
	}

	public Move getRecommendation() {
		return null;
	}

	private void setRootNode(DecisionNode newRootNode) {

		rootNode = newRootNode;

		rootNode.setParent(null);
		rootNode.setNextSibling(rootNode);
		rootNode.setPreviousSibling(rootNode);

		// tell threads about new root node
		for (int i = 0; i < processorThreads.length; i++) {
			processorThreads[i].setRootNode(newRootNode);
		}

		if (debug) {
			System.out.println("Board hash code = "
					+ Long.toHexString(getBoard().getHashCode()));
		}

		setGameSatus(rootNode.getStatus(), getBoard().getTurn());

		System.gc();
	}

	@Override
	public void setGame(Game game) {
		this.game = game;
	}

	public void setGameSatus(GameStatus status, Side playerTurn) {
		if (status != GameStatus.IN_PLAY) {
			System.out.println(rootNode.getStatus().toString());
		}
	}

	public GameStatus getGameStatus() {
		return rootNode.getStatus();
	}

	@Override
	public Side getSide() {
		return playerSide;
	}

	public Board getBoard() {
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
			System.out.println("Good move (" + goodMove.toString()
					+ ") not found as possibility");
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

		System.out.println(playerSide.otherSide() + " chose move worth "
				+ rootNode.getChosenPathValue(0));

	}

	public int getMoveChosenPathValue(Move m) {
		return getMatchingDecisionNode(m).getChosenPathValue(0);
	}

}
