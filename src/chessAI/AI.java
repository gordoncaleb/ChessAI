package chessAI;

import java.util.Vector;

import chessBackend.Board;
import chessBackend.Game;
import chessBackend.GameStatus;
import chessBackend.Player;
import chessBackend.Move;
import chessIO.FileIO;
import chessIO.MoveBook;
import chessIO.XMLParser;

public class AI extends Thread {
	private boolean debug;

	private Game game;
	private MoveBook moveBook;
	private DecisionNode rootNode;

	private int maxTwigLevel;
	private int maxDecisionTreeLevel;

	private int[] childNum = new int[20];

	private boolean userMoved;
	private DecisionNode userDecision;

	private boolean makeNewGame;
	private Player userSide;

	private boolean undoUserMove;

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

			if (userMoved) {
				userMoved();
				userMoved = false;
			}

			if (makeNewGame) {
				makeNewGame();
				makeNewGame = false;
			}

			if (undoUserMove) {
				undoUserMove();
				undoUserMove = false;
			}

		}

	}

	private void userMoved() {

		if (debug) {
			if (userDecision == rootNode.getChosenChild()) {
				System.out.println("AI predicted the users move!!");
			}
		}

		// Set the user's move as the root of the decision tree
		setRootNode(userDecision);

		System.out.println(rootNode.getBoard().toString());

		moveAI();
		game.aiMoved(rootNode.getMove());

	}

	/**
	 * At this point the root should be the user's move. This method finds the
	 * best response for the AI.
	 */
	private void moveAI() {

		// Game Over?
		if (rootNode.getStatus() != GameStatus.CHECKMATE && rootNode.getStatus() != GameStatus.STALEMATE) {

			long time = 0;
			DecisionNode aiDecision;

			time = System.currentTimeMillis();

			Move mb;
			if ((mb = moveBook.getRecommendation(rootNode.getBoard().getHashCode())) == null) {

				// Split the task of looking at all possible AI moves up amongst
				// multiple threads. This takes advantage of multicore systems.
				delegateProcessors(rootNode);

				aiDecision = rootNode.getChosenChild();
			} else {

				// The AI's decision was decided by the good move database but
				// the tree has to grow to reflect possible responses by the
				// user

				if (debug) {
					System.out.println("Ai moved based on recommendation");
				}

				setProcessorLevels(2, 0, true);
				growRoot();
				setDefaultProcessorLevels();

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
				System.out.println(rootNode.getBoard().toString());
			}

		}
	}

	public void makeNewGame() {

		// rootNode = new DecisionNode(null, new Move(0, 0, 0, 0), new Board(),
		// firstMove);

		if (debug) {
			rootNode = new DecisionNode(null, null, XMLParser.XMLToBoard(FileIO.readFile("testboard.xml")));

		} else {
			rootNode = new DecisionNode(null, null, XMLParser.XMLToBoard(FileIO.readFile("default.xml")));
		}


		// These two numbers represent how large the initial decision tree is.
		// In the case that the user is going first, it is small, so as to speed
		// up startup times. In the case of the AI is first it is large so that
		// "thought" is put into the AI's first Move.
		
		if (userSide != rootNode.getBoard().getPlayer()) {
			moveAI();
			game.aiMoved(rootNode.getMove());
		} else {
			setProcessorLevels(0, 0, false);
			growRoot();
			setDefaultProcessorLevels();
		}

		if (debug) {
			printRootDebug();
			System.out.println(rootNode.getBoard().toString());
		}

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
	public synchronized void setUserDecision(Move usersMove) {
		this.userDecision = getMatchingDecisionNode(usersMove);
		userMoved = true;
		this.notifyAll();
	}

	public synchronized void setMakeNewGame(Player userSide) {
		this.userSide = userSide;
		makeNewGame = true;
		this.notifyAll();
	}

	public synchronized void setUndoUserMove() {
		this.undoUserMove = true;
		this.notifyAll();
	}

	public synchronized void taskDone() {
		taskDone++;
		if (debug) {
			System.out.println(taskDone + "/" + taskSize + " done");
		}
	}

	public void clearTaskDone() {
		taskDone = 0;
	}

	private void delegateProcessors(DecisionNode root) {

		if (!root.hasChildren()) {
			setProcessorLevels(0, 0, false);
			growRoot();
			setDefaultProcessorLevels();
		}

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
			processorThreads[d].setNewTask(root);
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

	private void growRoot() {
		clearTaskDone();

		taskSize = 1;
		taskLeft = taskSize;
		nextTask = rootNode;

		processorThreads[0].setNewTask(null);

		while (taskDone < taskSize) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private boolean undoUserMove() {

		if (rootNode.getParent() != null) {
			if (rootNode.getParent().getParent() != null) {

				rootNode.getBoard().undoMove();
				rootNode.getBoard().undoMove();

				DecisionNode oldRootNode = rootNode.getParent().getParent();
				rootNode = oldRootNode;
				rootNode.removeAllChildren();

				// ai thought isnt needed
				setProcessorLevels(0, 0, false);

				// reinit tree
				growRoot();

				// set ai to think after user move
				setDefaultProcessorLevels();

			} else {
				return false;
			}
		} else {
			return false;
		}

		return true;

	}

	private void setRootNode(DecisionNode newRootNode) {

		rootNode.getBoard().makeMove(newRootNode.getMove());

		this.rootNode.removeAllChildren();
		this.rootNode.addChild(newRootNode);
		this.rootNode = newRootNode;

		if (debug) {
			System.out.println("Board hash code = " + Long.toHexString(rootNode.getBoard().getHashCode()));
		}

		if (this.rootNode.getStatus() != GameStatus.IN_PLAY) {
			System.out.println(newRootNode.getStatus().toString());
		}

		System.gc();
	}

	private void setProcessorLevels(int maxDecisionTreeLevel, int maxTwigLevel, boolean pruningEnabled) {
		for (int i = 0; i < processorThreads.length; i++) {
			processorThreads[i].setMaxTreeLevel(maxDecisionTreeLevel);
			processorThreads[i].setMaxTwigLevel(maxTwigLevel);
			processorThreads[i].setPruningEnabled(pruningEnabled);
		}
	}

	private void setDefaultProcessorLevels() {
		for (int i = 0; i < processorThreads.length; i++) {
			processorThreads[i].setMaxTreeLevel(maxDecisionTreeLevel);
			processorThreads[i].setMaxTwigLevel(maxTwigLevel);
			processorThreads[i].setPruningEnabled(true);
		}
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
		if (userDecision != null) {
			printChildren(userDecision);
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

		System.out.println("User chose move worth " + rootNode.getChosenPathValue(0));

	}

	public int getMoveChosenPathValue(Move m) {
		return getMatchingDecisionNode(m).getChosenPathValue(0);
	}

}
