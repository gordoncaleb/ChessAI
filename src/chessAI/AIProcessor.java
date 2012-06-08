package chessAI;

import java.util.ArrayList;

import chessBackend.Board;
import chessBackend.GameStatus;
import chessBackend.BoardHashEntry;
import chessBackend.Move;
import chessBackend.ValueBounds;
import chessIO.FileIO;
import chessPieces.Values;

public class AIProcessor extends Thread {
	private DecisionNode rootNode;
	private Board board;

	private int searchDepth;
	private boolean twigGrowthEnabled;
	private final boolean useHashTable = AISettings.useHashTable;

	private int maxInCheckFrontierLevel = AISettings.maxInCheckFrontierLevel;
	private int maxPieceTakenFrontierLevel = AISettings.maxPieceTakenFrontierLevel;

	private final boolean pruningEnabled = AISettings.alphaBetaPruningEnabled;
	private int aspirationWindowSize;

	private boolean threadActive;

	private AI ai;

	private BoardHashEntry[] hashTable;

	private long numSearched;

	private boolean stopSearch;

	private boolean bonusEnabled = AISettings.bonusEnable;

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

					// System.out.println("HashTable size = " +
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
			attachValidMoves(rootNode, 0);

		}

	}

	private void executeTask() {
		DecisionNode task;

		numSearched = 0;

		while ((task = ai.getNextTask()) != null) {

			board.makeMove(task.getMove());

			if (AISettings.useLite) {

				task.setChosenPathValue(-growDecisionTreeLite(-Values.CHECKMATE_MOVE - 1, -ai.getAlpha(), searchDepth, task.getMove(), 0));

			} else {
				// task.setAlpha(ai.getAlpha());
				// task.setBeta(Values.CHECKMATE_MOVE - 10);

				growDecisionTree(task, -Values.CHECKMATE_MOVE - 1, -ai.getAlpha(), searchDepth, 0);
			}

			board.undoMove();

			ai.taskDone(task);

		}

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

	public void attachValidMoves(DecisionNode branch, long hashMove) {

		ArrayList<Long> moves = board.generateValidMoves(true, hashMove);

		DecisionNode[] children = new DecisionNode[moves.size()];

		for (int m = 0; m < moves.size(); m++) {
			children[m] = (new DecisionNode(moves.get(m), Move.getValue(moves.get(m))));
		}

		branch.setChildren(children);
		// branch.sort();

	}

	/**
	 * 
	 * @param branch
	 *            Branch to grow
	 * @param level
	 *            Level from the rootNode that the branch is at
	 * @return
	 */
	private void growDecisionTree(DecisionNode branch, int alpha, int beta, int level, int bonusLevel) {

		if (stopSearch) {
			return;
		}

		int a = alpha;
		int b = beta;

		branch.setAlpha(alpha);
		branch.setBeta(beta);

		numSearched++;

		int sortTo = 0;

		int hashIndex = (int) (board.getHashCode() & BoardHashEntry.hashIndexMask);
		BoardHashEntry hashOut;
		long hashMove = 0;
		hashOut = hashTable[hashIndex];

		if (useHashTable) {

			if (hashOut != null) {
				if (hashOut.getHashCode() == board.getHashCode()) {

					// if (hashOut.getStringBoard().compareTo(board.toString())
					// != 0) {
					// FileIO.log(hashOut.getStringBoard() + "\n!=\n" +
					// board.toString());
					// synchronized (this) {
					// try {
					// this.wait();
					// } catch (InterruptedException e) {
					// e.printStackTrace();
					// }
					// }
					// }

					if (hashOut.getLevel() >= (level - bonusLevel)) {

						if (hashOut.getBounds() == ValueBounds.PV) {

							branch.setChosenPathValue(-hashOut.getScore());
							branch.setBound(ValueBounds.PV);
							// branch.setChildren(null);
							// System.out.println("Found hash entry EXACT");
							return;
						} else {

							if (hashOut.getBounds() == ValueBounds.CUT) {
								if (hashOut.getScore() >= beta) {
									branch.setChosenPathValue(-hashOut.getScore());
									branch.setBound(ValueBounds.ALL);
									// branch.setChildren(null);
									// System.out.println("Found hash entry ATLEAST");
									return;
								} else {
									hashMove = hashOut.getBestMove();
								}
							} else {
								hashMove = hashOut.getBestMove();
							}
						}
					}

				}
			}
		}

		int cpv = Integer.MIN_VALUE;

		if (!branch.hasBeenVisited()) {

			board.makeNullMove();

			if (bonusEnabled) {

				if ((board.getBoardStatus() == GameStatus.CHECK) && (level > -maxInCheckFrontierLevel)) {
					bonusLevel = Math.min(bonusLevel, level - 2);
				}

				if (branch.hasPieceTaken() && (level > -maxPieceTakenFrontierLevel)) {
					bonusLevel = Math.min(bonusLevel, level - 1);
				}
			}

			if (level > bonusLevel) {
				// check all moves of all pieces
				attachValidMoves(branch, hashMove);

				if (board.isGameOver()) {

					if (board.isInCheckMate()) {
						branch.setChosenPathValue(Values.CHECKMATE_MOVE);
					} else {
						branch.setChosenPathValue(0);
					}

					branch.setBound(ValueBounds.PV);

					return;
				}

			} else {
				branch.setChosenPathValue(-board.staticScore());
				branch.setBound(ValueBounds.PV);
				return;
			}

		}

		if (!branch.isGameOver()) {

			for (int i = 0; i < branch.getChildrenSize(); i++) {

				sortTo = i + 1;

				board.makeMove(branch.getChild(i).getMove());

				if (level > 0) {

					growDecisionTree(branch.getChild(i), -beta, -alpha, level - 1, bonusLevel);

				} else {
					// bonus depth

					if (bonusEnabled) {

						if (twigGrowthEnabled) {
							branch.getChild(i).setChosenPathValue(-growDecisionTreeLite(-beta, -alpha, level, branch.getChild(i).getMove(), bonusLevel));
							branch.getChild(i).setBound(getNodeType(-branch.getChild(i).getChosenPathValue(), -beta, -alpha));
						} else {
							growDecisionTree(branch.getChild(i), -beta, -alpha, level - 1, bonusLevel);
						}
					}

				}

				board.undoMove();

				cpv = Math.max(cpv, branch.getChild(i).getChosenPathValue());

				// alpha beta pruning
				if (pruningEnabled) {

					if (cpv > alpha) {
						alpha = cpv;
					}

					if (alpha >= beta) {
						break;
					}
				}

			}

			branch.sort(sortTo);

			if ((Math.abs(cpv) & Values.CHECKMATE_MASK) != 0) {
				if (cpv > 0) {
					cpv--;
				} else {
					cpv++;
				}
			}

			branch.setChosenPathValue(-cpv);
			branch.setBound(getNodeType(cpv, a, b));

			if (useHashTable) {
				if (hashOut == null) {
					// public BoardHashEntry(long hashCode, int level, int
					// score,
					// int moveNum, ValueBounds bounds, long bestMove) {
					hashTable[hashIndex] = new BoardHashEntry(board.getHashCode(), level - bonusLevel, cpv, ai.getMoveNum(), branch.getBound(), branch.getHeadChild()
							.getMove());// , board.toString());
					// System.out.println("Adding " + hashIndex +
					// " to hashTable at level " + level);
				} else {
					if (hashTableUpdate(hashOut, level - bonusLevel, ai.getMoveNum())) {
						hashOut.setAll(board.getHashCode(), level - bonusLevel, cpv, ai.getMoveNum(), branch.getHeadChild().getBound(), branch.getMove());// ,board.toString());
					}
				}

			}

		}

	}

	private ValueBounds getNodeType(int s, int a, int b) {
		if (s >= b) {
			return ValueBounds.CUT;
		}

		if (s < a) {
			return ValueBounds.ALL;
		}

		return ValueBounds.PV;
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
	private int growDecisionTreeLite(int alpha, int beta, int level, long moveMade, int bonusLevel) {
		int suggestedPathValue;
		int bestPathValue = Integer.MIN_VALUE;

		numSearched++;

		int a = alpha;
		int b = beta;
		long bestMove = 0;

		int hashIndex = (int) (board.getHashCode() & BoardHashEntry.hashIndexMask);
		BoardHashEntry hashOut;
		long hashMove = 0;
		hashOut = hashTable[hashIndex];

		if (useHashTable) {

			if (hashOut != null) {
				if (hashOut.getHashCode() == board.getHashCode()) {

					// if (hashOut.getStringBoard().compareTo(board.toString())
					// != 0) {
					// FileIO.log(hashOut.getStringBoard() + "\n!=\n" +
					// board.toString());
					// synchronized (this) {
					// try {
					// this.wait();
					// } catch (InterruptedException e) {
					// e.printStackTrace();
					// }
					// }
					// }

					if (hashOut.getLevel() >= (level - bonusLevel)) {

						if (hashOut.getBounds() == ValueBounds.PV) {
							return hashOut.getScore();
						} else {

							if (hashOut.getBounds() == ValueBounds.CUT) {
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

		if (bonusEnabled) {

			if ((board.getBoardStatus() == GameStatus.CHECK) && (level > -maxInCheckFrontierLevel)) {
				bonusLevel = Math.min(bonusLevel, level - 2);
			}

			if ((Move.hasPieceTaken(moveMade)) && (level > -maxPieceTakenFrontierLevel)) {
				bonusLevel = Math.min(bonusLevel, level - 1);
			}
		}

		if (level > bonusLevel) {

			GameStatus tempBoardState;
			long move;

			ArrayList<Long> moves = new ArrayList<Long>(board.generateValidMoves(true, hashMove));

			if (!board.isGameOver()) {

				for (int m = 0; m < moves.size(); m++) {
					move = moves.get(m);

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
		} else {
			bestPathValue = board.staticScore();
		}

		if (board.isInCheckMate()) {
			bestPathValue = -(Values.CHECKMATE_MOVE - (searchDepth - level));
		} else {
			if (board.isInStaleMate() || board.isDraw()) {
				bestPathValue = 0;
			}
		}

		if (useHashTable) {
			if (hashOut == null) {
				// public BoardHashEntry(long hashCode, int level, int
				// score,
				// int moveNum, ValueBounds bounds, long bestMove) {
				hashTable[hashIndex] = new BoardHashEntry(board.getHashCode(), level - bonusLevel, bestPathValue, ai.getMoveNum(), getNodeType(bestPathValue, a, b), bestMove);// ,
																																												// board.toString());
				// System.out.println("Adding " + hashIndex +
				// " to hashTable at level " + level);
			} else {
				if (hashTableUpdate(hashOut, level - bonusLevel, ai.getMoveNum())) {
					hashOut.setAll(board.getHashCode(), level - bonusLevel, bestPathValue, ai.getMoveNum(), getNodeType(bestPathValue, a, b), bestMove);// ,board.toString());
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
