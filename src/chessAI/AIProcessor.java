package chessAI;

import java.util.Vector;

import chessBackend.Board;
import chessBackend.GameStatus;
import chessBackend.Move;
import chessBackend.MoveNote;
import chessBackend.Player;
import chessPieces.PieceID;
import chessPieces.Values;

public class AIProcessor extends Thread {
	private DecisionNode rootNode;
	private boolean isNewTask;

	private int maxTreeLevel;
	private int maxTwigLevel;
	private boolean pruningEnabled;
	private AI ai;

	private boolean twigIsInvalid;

	public AIProcessor(AI ai, int maxTreeLevel, int maxTwigLevel) {
		this.ai = ai;
		this.maxTreeLevel = maxTreeLevel;
		this.maxTwigLevel = maxTwigLevel;
	}

	@Override
	public void run() {

		while (!isInterrupted()) {

			System.out.println("Processor " + this.getId() + " ready!");

			while (!isNewTask) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			isNewTask = false;

			executeTask();

		}
	}

	public void setNewTask(DecisionNode rootNode) {
		this.rootNode = rootNode;
		this.isNewTask = true;
	}

	private void executeTask() {
		DecisionNode task;
		int ab = Integer.MIN_VALUE;

		while ((task = ai.getNextTask()) != null) {

			if (rootNode != null) {
				if (rootNode.getHeadChild() != null) {
					ab = rootNode.getHeadChild().getChosenPathValue(0);
				}

				task.getBoard().makeMove(task.getMove(), rootNode.getPlayer());
			}

//			if (maxTreeLevel >= 1) {
//				for (int level = 1; level <= maxTreeLevel; level++) {
//					growDecisionTree(task, level, ab);
//					System.out.println("level " + level);
//				}
//			} else {
//				growDecisionTree(task, 0, ab);
//			}

			growDecisionTree(task, maxTreeLevel, ab);

			if (rootNode != null) {

				task.getBoard().undoMove(task.getMove(), rootNode.getPlayer());

				if (task.getMove().isValidated()) {
					rootNode.addChild(task);
				}
			}

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

		// This is the player making the child moves, not the player that made
		// the branch.
		Player player = branch.getPlayer();

		// This is the player that made the branch as well as the player making
		// moves on children of branch.
		Player nextPlayer = getNextPlayer(player);

		// Current state at branch
		Board board = branch.getBoard();

		boolean pruned = false;
		int newAlphaBeta = Integer.MIN_VALUE;

		if (!branch.hasChildren()) {

			// Node was at the bottom of the tree. This "branch" has to grow.

			// If board is in check, castleing isn't valid
			board.setInCheckDetails(calcInCheckDetails(nextPlayer, board));

			if (board.isInCheck()) {
				branch.setStatus(GameStatus.CHECK);
			}

			// check all moves of all pieces
			Vector<Move> moves = board.generateValidMoves(player);
			Move move;
			DecisionNode newNode;

			for (int m = 0; m < moves.size(); m++) {

				move = moves.elementAt(m);

				// Check to see if the move in question resulted in
				// the loss of your king. Such a move is invalid because
				// you can't move into check.
				if (move.getPieceTaken() != null) {
					if (move.getPieceTaken().getPieceID() == PieceID.KING) {
						branch.getMove().invalidate();
						// System.out.println("Move invalidated " +
						// branch.getMove().toString());
						return;
					}
				}

				newNode = new DecisionNode(branch, move, board, nextPlayer);

				if (!pruned && (level > 0 || branch.getStatus() == GameStatus.CHECK)) {

					if (branch.getHeadChild() != null) {
						newAlphaBeta = branch.getHeadChild().getChosenPathValue(0);
					}

					board.makeMove(move, player);
					growDecisionTree(newNode, level - 1, newAlphaBeta);
					board.undoMove(move, player);

				} else {
					newNode.setChosenPathValue(move.getValue());
				}

				// Check if the newNode move was invalidated
				if (newNode.getMove().isValidated()) {
					branch.addChild(newNode);

					// alpha beta pruning
					if (pruningEnabled) {
						if (branch.getMoveValue() - newNode.getChosenPathValue(0) < alphaBeta) {
							pruned = true;
						}
					}

				}

			}

		} else {

			if (branch.getStatus() == GameStatus.CHECK) {
				System.out.println("Branch " + branch.getMove().toString() + " is Check");
			}
			// Node has already been created and has children
			int childrenSize = branch.getChildrenSize();
			DecisionNode nextChild;
			DecisionNode currentChild = branch.getHeadChild();
			branch.removeAllChildren();
			for (int i = 0; i < childrenSize; i++) {

				// System.out.println(" next child" + i);
				nextChild = currentChild.getNextSibling();

				if (!pruned && (level > 0 || branch.getStatus() == GameStatus.CHECK)) {

					if (branch.getHeadChild() != null) {
						newAlphaBeta = branch.getHeadChild().getChosenPathValue(0);
					}

					board.makeMove(currentChild.getMove(), player);

					// explore down tree
					growDecisionTree(currentChild, level - 1, newAlphaBeta);

					board.undoMove(currentChild.getMove(), player);

				} else {
					currentChild.setChosenPathValue(currentChild.getMoveValue());
				}

				// Checks if that move was valid. This invalidated check is used
				// after every move by the user. It would remove user moves that
				// put the user in check.
				if (currentChild.getMove().isValidated()) {
					branch.addChild(currentChild);

					// alpha beta pruning
					if (pruningEnabled) {

						if (branch.getMoveValue() - currentChild.getChosenPathValue(0) < alphaBeta) {
							pruned = true;
						}

					}

				}

				currentChild = nextChild;
			}

		} // end of node already has children code

		// game is over at this point
		if (!branch.hasChildren()) {

			if (branch.getStatus() == GameStatus.CHECK) {
				branch.setStatus(GameStatus.CHECKMATE);
			} else {
				branch.setStatus(GameStatus.STALEMATE);
				branch.setChosenPathValue(Values.STALEMATE_MOVE);
			}
		}

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
	private int expandTwig(DecisionNode twig) {
		twigIsInvalid = false;
		int bestMove;

		DecisionNode twigsBestSibling = twig.getParent().getHeadChild();

		if (twigsBestSibling == null) {
			bestMove = Integer.MIN_VALUE;
		} else {
			bestMove = twigsBestSibling.getChosenPathValue(0);
		}

		int twigSuggestedPathValue = growDecisionTreeLite(twig.getBoard(), twig.getPlayer(), twig.getMoveValue(), bestMove, 0);

		// int twigSuggestedPathValue = growDecisionTreeLite(twig.getBoard(),
		// twig.getPlayer(), twig.getMoveValue(),Integer.MIN_VALUE, 0);

		if (twigIsInvalid) {
			twig.getMove().invalidate();
		}

		if (twig.getBoard().isInCheck()) {
			twig.setStatus(GameStatus.CHECK);
		}

		if (twigSuggestedPathValue == -Values.CHECKMATE_MOVE) {
			twig.setStatus(GameStatus.CHECKMATE);
		}

		if (twigSuggestedPathValue == -Values.STALEMATE_MOVE) {
			twig.setStatus(GameStatus.STALEMATE);
		}

		return twigSuggestedPathValue;
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
	private int growDecisionTreeLite(Board board, Player player, int parentMoveValue, int parentBestMove, int level) {
		// Board newBoard;
		Vector<Move> moves;
		Move move;
		boolean hasMove = false;
		int suggestedMove;
		int suggestedPathValue;
		int moveValue;

		int bestMove = Integer.MIN_VALUE;
		Player nextPlayer = getNextPlayer(player);

		// If board is in check, castleing isn't valid
		board.setInCheckDetails(calcInCheckDetails(getNextPlayer(player), board));

		moves = board.generateValidMoves(player);

		for (int m = 0; m < moves.size(); m++) {
			move = moves.elementAt(m);

			// These global variables get around the fact that this
			// recursive method id lite weight and has no reference to
			// parent and grantparent
			if (move.getPieceTaken() != null) {
				if (move.getPieceTaken().getPieceID() == PieceID.KING) {
					if (level == 0) {
						twigIsInvalid = true;
					}

					return Values.KING_VALUE;
				}
			}

			moveValue = move.getValue();

			if (level < maxTwigLevel) {
				// newBoard = board.getCopy();
				board.makeMove(move, player);

				suggestedMove = growDecisionTreeLite(board, nextPlayer, moveValue, bestMove, level + 1);

				board.undoMove(move, player);

				suggestedPathValue = moveValue - suggestedMove;

				// The king was taken on the next move, which means this
				// move is invalid. Move on to the next move.
				if (suggestedMove == Values.KING_VALUE) {
					continue;
				}

			} else {
				suggestedPathValue = moveValue;
			}

			hasMove = true;

			if (suggestedPathValue > bestMove) {
				bestMove = suggestedPathValue;
			}

			if (parentMoveValue - bestMove < parentBestMove) {
				break;
			}

		}

		if (!hasMove) {
			if (board.isInCheck()) {
				bestMove = -Values.CHECKMATE_MOVE;
			} else {
				bestMove = -Values.STALEMATE_MOVE;
			}
		}

		return bestMove;
	}

	/**
	 * 
	 * @param player
	 *            The player who is putting the other player in check. Not the
	 *            player that might be in check, but the other one.
	 * @param board
	 *            Board being checked for check situation.
	 * @return Whether or not "player" has put his/her opponent in check
	 */
	private int calcInCheckDetails(Player player, Board board) {
		int[][] kingLeftRight = { { 7, 3, 7, 5 }, { 0, 3, 0, 5 } };
		Vector<Move> moves;
		Move move;
		int inCheck = 0;

		moves = board.generateValidMoves(player);
		for (int m = 0; m < moves.size(); m++) {
			move = moves.elementAt(m);

			if (move.getPieceTaken() != null) {
				if (move.getPieceTaken().getPieceID() == PieceID.KING) {
					inCheck = inCheck | 2;
				}
			}

			if (move.getToRow() == kingLeftRight[player.ordinal()][0] && move.getToCol() == kingLeftRight[player.ordinal()][1]) {
				inCheck = inCheck | 4;
			}

			if (move.getToRow() == kingLeftRight[player.ordinal()][2] && move.getToCol() == kingLeftRight[player.ordinal()][3]) {
				inCheck = inCheck | 1;
			}
		}

		return inCheck;

	}

	private Player getNextPlayer(Player player) {
		if (player == Player.USER) {
			return Player.AI;
		} else {
			return Player.USER;
		}
	}

	public void setMaxTreeLevel(int maxTreeLevel) {
		this.maxTreeLevel = maxTreeLevel;
	}

	public void setMaxTwigLevel(int maxTwigLevel) {
		this.maxTwigLevel = maxTwigLevel;
	}

	public void setPruningEnabled(boolean pruningEnabled) {
		this.pruningEnabled = pruningEnabled;
	}

	private void printMoveStack(DecisionNode node) {

		System.out.println("MOVE STACK");

		DecisionNode ancestor = node;

		while (ancestor != rootNode) {
			System.out.println(ancestor.getMove().toString());
			ancestor = ancestor.getParent();
		}
	}

}
