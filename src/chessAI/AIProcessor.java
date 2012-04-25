package chessAI;

import java.util.Hashtable;
import java.util.Vector;

import chessBackend.Board;
import chessBackend.GameStatus;
import chessBackend.BoardHashEntry;
import chessBackend.Move;
import chessPieces.Values;

public class AIProcessor extends Thread {
	private DecisionNode rootNode;
	private Board board;
	private boolean isNewTask;

	private int maxTreeLevel;
	private int maxTwigLevel;
	private boolean twigGrowthEnabled;

	private int maxFrontierLevel = 2;

	private boolean pruningEnabled;
	private int aspirationWindowSize;

	private boolean threadActive;

	private AI ai;

	private Hashtable<Long, BoardHashEntry> hashTable;

	public AIProcessor(AI ai, int maxTreeLevel, int maxTwigLevel) {
		this.ai = ai;
		this.maxTreeLevel = maxTreeLevel;
		this.maxTwigLevel = maxTwigLevel;
		pruningEnabled = true;
		twigGrowthEnabled = true;
		aspirationWindowSize = 10;

		threadActive = true;

		hashTable = new Hashtable<Long, BoardHashEntry>();
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

					System.out.println("HashTable size = " + hashTable.size());
					hashTable.clear();
				}

			}

		}

	}

	public synchronized void terminate() {
		threadActive = false;
		this.notifyAll();
	}

	public synchronized void setNewTask() {
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
		if (!this.rootNode.hasChildren() && !this.rootNode.isGameOver()) {
			Vector<Move> moves = board.generateValidMoves();
			for (int m = 0; m < moves.size(); m++) {
				this.rootNode.addChild(new DecisionNode(this.rootNode, moves.elementAt(m)));
			}
		}

	}

	private void executeTask() {
		DecisionNode task;
		int ab = Integer.MIN_VALUE;

		while ((task = ai.getNextTask()) != null) {

			if (rootNode.getHeadChild() != null) {
				ab = rootNode.getHeadChild().getChosenPathValue(0);
			}

			board.makeMove(task.getMove());

			// if (maxTreeLevel >= 1) {
			// for (int level = 1; level <= maxTreeLevel; level++) {
			// growDecisionTree(task, level, ab);
			// System.out.println("level " + level);
			// }
			// } else {
			// growDecisionTree(task, 0, ab);
			// }

			growDecisionTree(task, maxTreeLevel, ab);

			board.undoMove();

			rootNode.addChild(task);

			ai.taskDone();

		}

	}

	/**
	 * 
	 * @param branch
	 *            Branch to grow
	 * @param level
	 *            Level from the rootNode that the branch is at
	 * @return
	 */
	private void growDecisionTree(DecisionNode branch, int level, int alphaBeta) {

		boolean pruned = false;
		int newAlphaBeta = Integer.MIN_VALUE;
		BoardHashEntry hashOut;
		boolean skip;

		if (!branch.hasChildren()) {

			// check all moves of all pieces
			Vector<Move> moves = board.generateValidMoves();

			branch.setStatus(board.getBoardStatus());

			if (branch.isGameOver()) {
				return;
			}

			Move move;
			DecisionNode newNode;
			for (int m = 0; m < moves.size(); m++) {

				move = moves.elementAt(m);

				skip = false;

				newNode = new DecisionNode(branch, move);

				if (pruned) {

					newNode.setChosenPathValue(move.getValue());

				} else {

					board.makeMove(move);

					// use hashtable to see if hashcode has been seen before
					hashOut = hashTable.get(board.getHashCode());

					if (hashOut != null) {

						// verify boards actually match
						// if
						// (board.toString().compareTo(hashOut.getBoardString())
						// != 0) {
						// System.out.println("Hash collision!\n" +
						// board.toString() + "\n!=\n" +
						// hashOut.getBoardString());
						// }

						// check to see if depth is sufficient
						if (hashOut.getLevel() > (level + maxTwigLevel)) {
							skip = true;
						}
					} else {
						// put temp holder in for any children returning to
						// parents state
						// hashTable.put(board.getHashCode(), new
						// HashTableEntry(level + maxTwigLevel, 0));
					}

					if (!skip) {
						if (branch.getHeadChild() != null) {
							newAlphaBeta = branch.getHeadChild().getChosenPathValue(0);
						}

						if (level > 0) {

							growDecisionTree(newNode, level - 1, newAlphaBeta);

						} else {

							if (twigGrowthEnabled) {
								growFrontierTwig(newNode, newAlphaBeta);
							} else {
								newNode.setChosenPathValue(move.getValue());
							}

						}

						// add new entry into hash table
						hashTable.put(board.getHashCode(), new BoardHashEntry(level + maxTwigLevel, newNode.getChosenPathValue(0)));// ,
																																	// board.toString()));
					} else {
						newNode.setChosenPathValue(hashOut.getScore());
					}

					board.undoMove();

				}

				branch.addChild(newNode);

				// alpha beta pruning
				if (pruningEnabled) {
					if (branch.getMoveValue() - newNode.getChosenPathValue(0) < alphaBeta + aspirationWindowSize) {
						pruned = true;
					}
				}

			}

		} else {

			// Node has already been created and has children
			int childrenSize = branch.getChildrenSize();
			DecisionNode nextChild;
			DecisionNode currentChild = branch.getHeadChild();
			branch.removeAllChildren();
			for (int i = 0; i < childrenSize; i++) {

				// System.out.println(" next child" + i);
				nextChild = currentChild.getNextSibling();

				if (pruned) {

					currentChild.setChosenPathValue(currentChild.getMoveValue());

				} else {

					if (level > 0) {

						if (branch.getHeadChild() != null) {
							newAlphaBeta = branch.getHeadChild().getChosenPathValue(0);
						}

						board.makeMove(currentChild.getMove());

						// explore down tree
						growDecisionTree(currentChild, level - 1, newAlphaBeta);

						// add new entry into hash table
						hashTable.put(board.getHashCode(), new BoardHashEntry(level + maxTwigLevel, currentChild.getChosenPathValue(0)));// ,
																																			// board.toString()));

						board.undoMove();

					}
				}

				// alpha beta pruning
				if (pruningEnabled) {

					if (branch.getMoveValue() - currentChild.getChosenPathValue(0) < alphaBeta + aspirationWindowSize) {
						pruned = true;
					}

				}

				// Checks if that move was valid. This invalidated check is used
				// after every move by the user. It would remove user moves that
				// put the user in check.
				branch.addChild(currentChild);

				currentChild = nextChild;
			}

		} // end of node already has children code

	}

	/**
	 * This function uses the lite version of growDecisionTree() to get a quick
	 * idea of the move value of a twig. A twig is the bottom of the tree, which
	 * is a node that has no children. These twigs are investigated to make sure
	 * there isn't some significant event just beyond the knowledge of the
	 * decision tree. The growDecisionLite() method is used instead of the
	 * growDecisionTree() method because the massive number of twigs can be
	 * computationally problematic.
	 * 
	 * @param twig
	 *            A node at the bottom of the tree, which has no children.
	 * @return
	 */
	private void growFrontierTwig(DecisionNode twig, int alphaBeta) {

		int twigsBestPathValue = growDecisionTreeLite(board, twig.getMove(), alphaBeta, maxTwigLevel);

		twig.setStatus(board.getBoardStatus());

		twig.setChosenPathValue(twigsBestPathValue);
	}

	/**
	 * This method has similar functionality as 'growDecisionTree()' but it
	 * doesn't add on to the decision tree data structure. Adding on to the data
	 * structure at some tree depths can cause the program to overflow the heap.
	 * This method is used as a quick alternative to 'growDecisionTree()' at
	 * large tree depths.
	 * 
	 * @param board
	 *            The board that needs all possible moves evaluated.
	 * @param player
	 *            The player whose turn it is.
	 * @param level
	 *            The stack distance from the initial call on
	 *            'growDecisionTreeLite()'
	 * @return The value of the best move for 'player' on the 'board'
	 */
	private int growDecisionTreeLite(Board board, Move parentMove, int alphaBeta, int level) {
		Vector<Move> moves;
		Move move;
		int suggestedPathValue;
		GameStatus tempBoardState;

		BoardHashEntry hashOut;
		boolean skip;

		int childsBestPathValue = Integer.MIN_VALUE;

		moves = board.generateValidMoves();

		for (int m = 0; m < moves.size(); m++) {
			move = moves.elementAt(m);

			skip = false;

			if ((level > 0 || board.isInCheck() || move.getPieceTaken() != null) && (level > -maxFrontierLevel)) {

				tempBoardState = board.getBoardStatus();

				board.makeMove(move);

				hashOut = hashTable.get(board.getHashCode());

				if (hashOut != null) {

					if (hashOut.getLevel() > level) {
						skip = true;
					}
				} else {
					// hashTable.put(board.getHashCode(), new
					// HashTableEntry(level, 0));
				}

				if (!skip) {
					suggestedPathValue = growDecisionTreeLite(board, move, childsBestPathValue, level - 1);

					hashTable.put(board.getHashCode(), new BoardHashEntry(level, suggestedPathValue));// ,
																										// board.toString()));
				} else {
					suggestedPathValue = hashOut.getScore();
				}

				board.undoMove();

				board.setBoardStatus(tempBoardState);

			} else {
				suggestedPathValue = move.getValue();
			}

			if (suggestedPathValue > childsBestPathValue) {
				childsBestPathValue = suggestedPathValue;
			}

			if (parentMove.getValue() - childsBestPathValue < alphaBeta + aspirationWindowSize) {
				break;
			}

		}

		int parentsBestPathValue;

		if (board.isInCheckMate()) {
			parentsBestPathValue = Values.CHECKMATE_MOVE - (maxTwigLevel - level + maxTreeLevel) * Values.CHECKMATE_DEPTH_INC;
		} else {
			if (board.isInStaleMate()) {
				parentsBestPathValue = Values.STALEMATE_MOVE;
			} else {
				parentsBestPathValue = parentMove.getValue() - childsBestPathValue;
			}
		}

		return parentsBestPathValue;
	}

	public synchronized void setMaxTreeLevel(int maxTreeLevel) {
		this.maxTreeLevel = maxTreeLevel;
	}

	public synchronized void setMaxTwigLevel(int maxTwigLevel) {
		this.maxTwigLevel = maxTwigLevel;
	}

	public synchronized void setPruningEnabled(boolean pruningEnabled) {
		this.pruningEnabled = pruningEnabled;
	}

}
