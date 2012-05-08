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

	private int maxTreeLevel;
	private int maxTwigLevel;
	private boolean twigGrowthEnabled;
	private final boolean iterativeDeepening = false;
	private final boolean useHashTable = true;

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

					System.out.println("HashTable size = " + hashTable.size());
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
		BoardHashEntry hashOut;
		boolean hashHit = false;

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

			hashOut = hashTable.get(board.getHashCode());

			if (hashOut != null) {
				if (hashOut.getLevel() >= maxTreeLevel + maxTwigLevel + 1) {
					task.setChosenPathValue(hashOut.getScore());
					hashHit = true;
				}
			}

			if (!hashHit) {
				task.setChosenPathValue(null);

				if (iterativeDeepening) {
					twigGrowthEnabled = false;
					for (int i = 1; i <= maxTreeLevel; i++) {
						growDecisionTree(task, i, ab);
					}

					twigGrowthEnabled = true;
					growDecisionTree(task, maxTreeLevel, ab);
				} else {
					growDecisionTree(task, maxTreeLevel, ab);
				}

				if (useHashTable) {
					hashTable.put(board.getHashCode(), new BoardHashEntry(maxTreeLevel + maxTwigLevel + 1, task.getChosenPathValue(0)));
				}
			}

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
		boolean hashHit = false;

		if (!branch.hasChildren()) {

			// check all moves of all pieces
			Vector<Move> moves = board.generateValidMoves();

			branch.setStatus(board.getBoardStatus());

			if (board.isGameOver()) {
				if (board.isInStaleMate() || board.isDraw()) {
					branch.setChosenPathValue((board.winningBy(board.getTurn()) + branch.getPieceTakenValue()) / Values.DRAW_DIVISOR);
				}

				return;
			}

			Move move;
			DecisionNode newNode;
			for (int m = 0; m < moves.size(); m++) {

				move = moves.elementAt(m);

				hashHit = false;

				newNode = new DecisionNode(branch, move);

				if (pruned) {

					newNode.setChosenPathValue(move.getValue());

				} else {

					board.makeMove(move);

					// use hashtable to see if hashcode has been seen before
					hashOut = hashTable.get(board.getHashCode());

					if (hashOut != null) {
						// check to see if depth is sufficient
						if (hashOut.getLevel() >= (level + maxTwigLevel)) {
							newNode.setChosenPathValue(hashOut.getScore());
							hashHit = true;
						}
					}

					if (!hashHit) {
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

						if (useHashTable) {
							// add or update new entry into hash table
							hashTable.put(board.getHashCode(), new BoardHashEntry(level + maxTwigLevel, newNode.getChosenPathValue(0)));
						}
					}

					board.undoMove();

				}

				branch.setChosenPathValue(null);
				branch.addChild(newNode);

				// alpha beta pruning
				if (pruningEnabled) {
					if (branch.getMoveValue() - newNode.getChosenPathValue(0) <= alphaBeta + aspirationWindowSize) {
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

				hashHit = false;

				if (pruned) {

					currentChild.setChosenPathValue(currentChild.getMoveValue());

				} else {

					if (level > 0) {

						if (branch.getHeadChild() != null) {
							newAlphaBeta = branch.getHeadChild().getChosenPathValue(0);
						}

						board.makeMove(currentChild.getMove());

						// explore down tree
						hashOut = hashTable.get(board.getHashCode());

						if (hashOut != null) {
							if (hashOut.getLevel() >= level + maxTwigLevel) {
								currentChild.setChosenPathValue(hashOut.getScore());
								hashHit = true;
							}
						}

						if (!hashHit) {
							currentChild.setChosenPathValue(null);
							growDecisionTree(currentChild, level - 1, newAlphaBeta);
							if (useHashTable) {
								hashTable.put(board.getHashCode(), new BoardHashEntry(level + maxTwigLevel, currentChild.getChosenPathValue(0)));
							}
						}

						board.undoMove();

					}
				}

				// alpha beta pruning
				if (pruningEnabled) {

					if (branch.getMoveValue() - currentChild.getChosenPathValue(0) <= alphaBeta + aspirationWindowSize) {
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
		boolean hashHit = false;

		BoardHashEntry hashOut;

		int childsBestPathValue = Integer.MIN_VALUE;

		moves = board.generateValidMoves();

		if (!board.isGameOver()) {

			for (int m = 0; m < moves.size(); m++) {
				move = moves.elementAt(m);

				hashHit = false;

				tempBoardState = board.getBoardStatus();

				board.makeMove(move);

				hashOut = hashTable.get(board.getHashCode());

				if (hashOut != null) {
					if (hashOut.getLevel() >= level) {
						// use hashed value
						hashHit = true;
					}
				}

				if (!hashHit) {
					if ((level > 0 || board.isInCheck() || move.getPieceTaken() != null) && (level > -maxFrontierLevel)) {
						suggestedPathValue = growDecisionTreeLite(board, move, childsBestPathValue, level - 1);
						// hashTable.put(board.getHashCode(), new
						// BoardHashEntry(level, suggestedPathValue));
					} else {
						suggestedPathValue = move.getValue();
						// hashTable.put(board.getHashCode(), new
						// BoardHashEntry(0, suggestedPathValue));
					}
				} else {
					suggestedPathValue = hashOut.getScore();
				}

				if (suggestedPathValue > childsBestPathValue) {
					childsBestPathValue = suggestedPathValue;
				}

				board.undoMove();

				board.setBoardStatus(tempBoardState);

				if (parentMove.getValue() - childsBestPathValue <= alphaBeta + aspirationWindowSize) {
					break;
				}

			}

		}

		int parentsBestPathValue;

		if (board.isInCheckMate()) {
			parentsBestPathValue = Values.CHECKMATE_MOVE - (maxTwigLevel - level + maxTreeLevel + 1) * Values.CHECKMATE_DEPTH_INC;
		} else {
			if (board.isInStaleMate() || board.isDraw()) {
				parentsBestPathValue = (board.winningBy(board.getTurn()) + parentMove.getValue()) / Values.DRAW_DIVISOR;
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
