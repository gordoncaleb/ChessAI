package chessAI;

import java.util.Date;
import java.util.Vector;

import chessBackend.Board;
import chessBackend.Game;
import chessBackend.GameStatus;
import chessBackend.Player;
import chessBackend.Move;
import chessPieces.Piece;

public class AI extends Thread {

	private Game game;

	private boolean debug;
	private DecisionNode rootNode;

	private int maxTwigLevel;
	private int maxDecisionTreeLevel;

	private int[] childNum = new int[10];

	private boolean userMoved;
	private DecisionNode userDecision;

	private boolean makeNewGame;
	private Player firstMove;

	private boolean growBranch;
	private DecisionNode branchToGrow;
	
	private boolean undoUserMove;

	private int processorDone;
	private AIProcessor[] processorThreads;

	public AI(Game game, boolean debug) {
		this.debug = debug;
		this.game = game;

		processorThreads = new AIProcessor[Runtime.getRuntime().availableProcessors()];
		// processorThreads = new AIProcessor[1];
		for (int i = 0; i < processorThreads.length; i++) {
			processorThreads[i] = new AIProcessor(this, maxDecisionTreeLevel, maxTwigLevel);
			processorThreads[i].start();
		}

		System.out.println(processorThreads.length + " threads created and started.");
	}

	@Override
	public void run() {

		while (!this.isInterrupted()) {

			while (!userMoved && !makeNewGame && !growBranch && !undoUserMove) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			if (userMoved) {
				move();
				userMoved = false;
			}

			if (makeNewGame) {
				newGame();
				makeNewGame = false;
			}

			if (growBranch) {
				growDecisionBranch(branchToGrow);
				growBranch = false;
			}
			
			if(undoUserMove){
				undoUserMove();
				undoUserMove = false;
			}

		}

	}

	private void move() {
		userMoved = false;

		if (debug) {
			if (userDecision == rootNode.getChosenChild()) {
				System.out.println("AI predicted the users move!!");
			}
		}

		setRootNode(userDecision);

		// Game Over?
		if (rootNode.getStatus() != GameStatus.CHECKMATE && rootNode.getStatus() != GameStatus.STALEMATE) {

			long time = 0;

			time = new Date().getTime();

			clearProcessorDone();
			delagateProcessors(rootNode);
			while (processorDone != processorThreads.length) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			printChildren(userDecision);

			setRootNode(userDecision.getChosenChild());

			long timeTaken = new Date().getTime() - time;
			System.out.println("Ai took " + timeTaken + "ms to move.");

			// expandGoodDecisions(rootNode,5);
		}

		if (debug) {
			for (int i = 0; i < 10; i++) {
				childNum[i] = 0;
			}

			countChildren(rootNode, 0);

			for (int i = 0; i < 10; i++) {
				System.out.println(childNum[i] + " at level " + i);
			}

			System.out.println("AI chose move worth " + rootNode.getChosenPathValue());

		}

		game.aiMoved(rootNode);

	}

	public void newGame() {

		rootNode = new DecisionNode(null, new Move(0, 0, 0, 0), new Board(), firstMove);

		// These two numbers represent how large the initial decision tree is.
		// In the case that the user is going first it is small, so as to speed
		// up startup times. In the case of the AI is first it is large so that
		// "thought" is put into the AI's first Move.
		if (firstMove == Player.AI) {
			game.aiMoved(rootNode);
			maxDecisionTreeLevel = 1;
			maxTwigLevel = 2;
		} else {
			maxDecisionTreeLevel = 0;
			maxTwigLevel = 0;
		}

		setProcessorLevels();

		// init tree
		clearProcessorDone();
		processorThreads[0].setTask(null, rootNode, 1);

		System.out.println("Init game task set");

		while (processorDone == 0) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		System.out.println("Init game task completed");

		// These numbers determine how many moves ahead the AI thinks for the
		// rest of the game.
		maxDecisionTreeLevel = 0;
		maxTwigLevel = 2;
		setProcessorLevels();

		if (firstMove == Player.AI) {
			setRootNode(rootNode.getChosenChild());
			game.aiMoved(rootNode);
		}

		if (debug) {
			countChildren(rootNode, 0);
			for (int i = 0; i < 10; i++) {
				System.out.println(childNum[i] + " at level " + i);
			}
		}

		game.aiMoved(rootNode);
	}

	private void printChildren(DecisionNode parent) {

		DecisionNode currentChild = parent.getHeadChild();
		for (int i = 0; i < parent.getChildrenSize(); i++) {
			System.out.println(currentChild.toString());
			currentChild = currentChild.getNextSibling();
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
	public void setUserDecision(DecisionNode userDecision) {
		userMoved = true;
		this.userDecision = userDecision;
	}

	public void setMakeNewGame(Player firstMove) {
		this.firstMove = firstMove;
		makeNewGame = true;
	}

	public void growBranch(DecisionNode branch) {
		growBranch = true;
		branchToGrow = branch;
	}

	public void processorDone() {
		processorDone++;
	}

	public void clearProcessorDone() {
		processorDone = 0;
	}
	
	public void setUndoUserMove(){
		this.undoUserMove = true;
	}

	private void delagateProcessors(DecisionNode root) {

		// root.removeAllChildren();

		// Create DecisionNode for each possible AI response
		Board board = root.getBoard();
		Board newBoard;
		Player player = root.getPlayer();
		Player nextPlayer = getNextPlayer(player);
		DecisionNode newNode;

		Vector<Move> moves;
		Move move;
		Vector<Piece> pieces = board.getPlayerPieces(player);
		Piece piece;

		DecisionNode aiMoveNode = null;
		DecisionNode aiFirstMoveNode = null;
		int numAiMoves = 0;

		for (int p = 0; p < pieces.size(); p++) {
			piece = pieces.elementAt(p);
			piece.generateValidMoves();
			moves = piece.getValidMoves();
			for (int m = 0; m < moves.size(); m++) {
				move = moves.elementAt(m);

				newBoard = board.getCopy();
				newBoard.adjustKnightValue();
				newBoard.moveChessPiece(move, player);
				newNode = new DecisionNode(root, move, newBoard, nextPlayer);
				if (aiMoveNode == null) {
					aiMoveNode = newNode;
					aiFirstMoveNode = aiMoveNode;
				} else {
					aiMoveNode.setNextSibling(newNode);
					aiMoveNode = newNode;
				}

				numAiMoves++;

			}

		}

		// Attempt to evenly distribute processing across all available threads.
		int[] threadDistribution = new int[processorThreads.length];
		int atLeast = numAiMoves / processorThreads.length;
		int extra = numAiMoves % processorThreads.length;
		for (int t = 0; t < processorThreads.length; t++) {
			if (extra > 0) {
				threadDistribution[t] = atLeast + 1;
				extra--;
			} else {
				threadDistribution[t] = atLeast;
			}
		}

		// Assign moves to different threads according to previous distribution
		// calculation
		aiMoveNode = aiFirstMoveNode;

		for (int d = 0; d < threadDistribution.length; d++) {

			processorThreads[d].setTask(root, aiMoveNode, threadDistribution[d]);

			for (int i = 0; i < threadDistribution[d]; i++) {
				aiMoveNode = aiMoveNode.getNextSibling();
			}
		}

	}

	/**
	 * This method can grow a branch in the decision tree such that 'branch'
	 * will have new children or grandchildren and after the branch is grown
	 * further, the parents of the branch will be notified of their child's new
	 * value if it changed.
	 * 
	 * @param branch
	 *            The branch to grow.
	 */
	public void growDecisionBranch(DecisionNode branch) {

		int previousSuggestedPathValue = branch.getChosenPathValue();
		delagateProcessors(branch);
		int suggestedPathValue = branch.getChosenPathValue();

		// After thinking further down this path, the path value has changed.
		// Any parents or grandparents need to be notified.
		if (previousSuggestedPathValue != suggestedPathValue) {
			System.out.println("updating parents");
			updateParents(branch);
		}
	}

	/**
	 * This function assumes that a child of some node has changed it's
	 * 'chosenPathValue' and therefore needs to have it's parent resort so that
	 * the child takes it's new place amongst its siblings. All children are
	 * sorted based on their 'chosenPathValue'.
	 * 
	 * @param child
	 *            The node that has had it's 'chosenPathValue' change.
	 */
	private void updateParents(DecisionNode child) {
		DecisionNode parent = child.getParent();
		if (parent != null) {
			parent.removeChild(child);
			parent.addChild(child);
			updateParents(parent);
		}
	}

	/**
	 * This function builds 'numOfBesstDecisions' of all children of 'rootNode'.
	 * It's used to dig deeper on the 'numOfBestDecisions' of the possible AI
	 * response to all possible user moves.
	 * 
	 * @param rootNode
	 *            The game root node. It's children are all possible user moves,
	 *            and grandchildren are possible AI responses.
	 * @param numOfBestDecisions
	 *            The number of AI responses to dig deeper on.
	 */
	private void expandGoodDecisions(DecisionNode rootNode, int numOfBestDecisions) {
		int childrenSize = rootNode.getChildrenSize();
		int grandChildrenSize;

		DecisionNode nextChild;
		DecisionNode currentChild = rootNode.getHeadChild();
		for (int i = 0; i < childrenSize; i++) {

			System.out.println("expanding " + i);
			nextChild = currentChild.getNextSibling();
			growDecisionBranch(currentChild);

			currentChild = nextChild;
		}
	}

	private void setRootNode(DecisionNode newRootNode) {

		this.rootNode.removeAllChildren();
		this.rootNode.addChild(newRootNode);
		this.rootNode = newRootNode;

		if (this.rootNode.getStatus() != GameStatus.IN_PLAY) {
			System.out.println(newRootNode.getStatus().toString());
		}

		System.gc();
	}

	private void undoUserMove() {
		DecisionNode oldRootNode = rootNode.getParent().getParent();
		rootNode = oldRootNode;
		rootNode.removeAllChildren();

		// ai thought isnt needed
		maxDecisionTreeLevel = 0;
		maxTwigLevel = 0;
		setProcessorLevels();

		// reinit tree
		clearProcessorDone();
		processorThreads[0].setTask(null, rootNode, 1);

		while (processorDone == 0) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		//set ai to think after user move
		maxDecisionTreeLevel = 0;
		maxTwigLevel = 2;
		setProcessorLevels();
		
		game.aiMoved(rootNode);

	}

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

	private Player getNextPlayer(Player player) {
		if (player == Player.USER) {
			return Player.AI;
		} else {
			return Player.USER;
		}
	}

	private void setProcessorLevels() {
		for (int i = 0; i < processorThreads.length; i++) {
			processorThreads[i].setMaxTreeLevel(maxDecisionTreeLevel);
			processorThreads[i].setMaxTwigLevel(maxTwigLevel);
		}
	}

}
