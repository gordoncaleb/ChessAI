package chessAI;

import java.util.Date;
import java.util.Vector;

import chessBackend.Board;
import chessBackend.GameStatus;
import chessBackend.MoveNote;
import chessBackend.Player;
import chessBackend.Move;
import chessPieces.Piece;
import chessPieces.PieceID;
import chessPieces.Values;

public class AI {
	private boolean debug;
	private DecisionNode rootNode;
	private int maxTwigLevel;
	private int maxDecisionTreeLevel;
	private boolean twigIsInvalid;
	private int[] childNum = new int[10];

	public AI(Board board, boolean debug) {
		this.debug = debug;
		rootNode = new DecisionNode(null, null, board, Player.USER);

		// These numbers determine how many moves ahead the AI thinks
		maxDecisionTreeLevel = 1;
		maxTwigLevel = 2;

		growDecisionTree(rootNode, 0);

		System.out.println("Tree grown");
		// expandGoodDecisions(rootNode,2);

		if (debug) {
			countChildren(rootNode, 0);
			for (int i = 0; i < 10; i++) {
				System.out.println(childNum[i] + " at level " + i);
			}
		}
	}

	/**
	 * This method notifies the AI that the user has moved and also tells gives
	 * it the move that the user made.
	 * 
	 * @param usersMove
	 *            The users move. This has a reference to the position on the
	 *            decision tree that the user effectively selected.
	 * @return The new root node, which is the AI response to 'userMove'.
	 */
	public DecisionNode move(Move usersMove) {

		DecisionNode usersDecision = usersMove.getNode();

		if (debug) {
			if (usersDecision == rootNode.getChosenChild()) {
				System.out.println("AI predicted the users move!!");
			}
		}

		setRoot(usersDecision);

		// Game Over?
		if (rootNode.getStatus() != GameStatus.CHECKMATE && rootNode.getStatus() != GameStatus.STALEMATE) {
			setRoot(usersDecision.getChosenChild());
			long time = 0;

			time = new Date().getTime();

			growDecisionTree(rootNode, 0);

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

		return rootNode;
	}

	private int growDecisionTree(DecisionNode branch, int level) {

		Player player = branch.getPlayer();
		Player nextPlayer = getNextPlayer(player);
		Board board = branch.getBoard();

		int suggestedPathValue;

		if (!branch.hasChildren()) {

			// Node was at the bottom of the tree. This "branch" has to grow.

			Vector<Piece> pieces;
			Piece piece;
			Vector<Move> moves;
			Move move;
			int moveValue;
			DecisionNode newNode;
			Board newBoard;

			// If board is in check, castleing isn't valid
			board.setInCheck(isInCheck(nextPlayer, board));

			// get the the pieces of whose turn it is
			pieces = board.getPlayerPieces(player);

			// check all moves of all pieces
			for (int p = 0; p < pieces.size(); p++) {
				piece = pieces.elementAt(p);
				piece.generateValidMoves();
				moves = piece.getValidMoves();

				for (int m = 0; m < moves.size(); m++) {

					move = moves.elementAt(m);
					moveValue = move.getValue();

					// Check to see if the move in question resulted in
					// the loss of your king. Such a move is invalid because
					// you can't move into check.
					if (move.getNote() == MoveNote.TAKE_PIECE && move.getPieceTaken() == PieceID.KING) {
						branch.getMove().setNote(MoveNote.INVALIDATED);
						System.out.println("Move " + move.toString() + " invalidated");
						return 0;
					}

					newBoard = board.getCopy();
					newBoard.adjustKnightValue();
					newBoard.moveChessPiece(move, player);
					newNode = new DecisionNode(branch, move, newBoard, nextPlayer);

					// check to see if you are at the bottom of the branch
					if (level < maxDecisionTreeLevel) {
						suggestedPathValue = growDecisionTree(newNode, level + 1);
					} else {
						suggestedPathValue = moveValue - expandTwig(newNode);
					}

					newNode.setChosenPathValue(suggestedPathValue);

					// Check if the newNode move was invalidated
					if (newNode.getMove().isValidated()) {

						// System.out.println("addedNew Node");
						branch.addChild(newNode);
					}

				}
			}

		} else {
			// Node has already been created and has children
			int childChosenPathValue;
			int childrenSize = branch.getChildrenSize();
			DecisionNode nextChild;
			DecisionNode currentChild = branch.getHeadChild();
			for (int i = 0; i < childrenSize; i++) {

				// System.out.println(" next child" + i);
				nextChild = currentChild.getNextSibling();

				// save current chosen path value to see if it changes. If it
				// changes then the DecisionNode needs to resort it into its
				// children
				childChosenPathValue = currentChild.getChosenPathValue();

				// explore down tree
				suggestedPathValue = growDecisionTree(currentChild, level + 1);

				// Checks if that move was valid. This invalidated check is used
				// after every move by the user. It would remove user moves that
				// put the user in check.
				if (currentChild.getMove().isValidated()) {

					// If chosen path value has changed then it can be resorted
					// by removeing it from the list and re-adding it to the
					// branch. The add() method automatically sorts the added
					// node.
					if (childChosenPathValue != suggestedPathValue) {
						branch.removeChild(currentChild);
						branch.addChild(currentChild);
					}

				} else {
					// The child represents an invalid move. i.e moving into
					// check.
					branch.removeChild(currentChild);
				}

				currentChild = nextChild;
			}

		} // end of node already has children code

		// game is over at this point
		if (!branch.hasChildren()) {

			if (branch.getStatus() == GameStatus.CHECK) {
				branch.setStatus(GameStatus.CHECKMATE);
				branch.setChosenPathValue(2 * Values.KING_VALUE);
			} else {
				branch.setStatus(GameStatus.STALEMATE);
				branch.setChosenPathValue(Values.KING_VALUE);
			}
		}

		return branch.getChosenPathValue();
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
			bestMove = twigsBestSibling.getChosenPathValue();
		}

		int twigSuggestedPathValue = growDecisionTreeLite(twig.getBoard(), twig.getPlayer(), twig.getMoveValue(), bestMove, 0);

		// int twigSuggestedPathValue = growDecisionTreeLite(twig.getBoard(),
		// twig.getPlayer(), twig.getMoveValue(),Integer.MIN_VALUE, 0);

		if (twigIsInvalid) {
			twig.getMove().setNote(MoveNote.INVALIDATED);
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
		Board newBoard;
		Vector<Move> moves;
		Move move;
		int suggestedMove;
		int suggestedPathValue;
		int moveValue;

		int bestMove = Integer.MIN_VALUE;

		// If board is in check, castleing isn't valid
		board.setInCheck(isInCheck(getNextPlayer(player), board));

		Vector<Piece> pieces = board.getPlayerPieces(player);
		for (int p = 0; p < pieces.size(); p++) {
			pieces.elementAt(p).generateValidMoves();
			moves = pieces.elementAt(p).getValidMoves();
			for (int m = 0; m < moves.size(); m++) {
				move = moves.elementAt(m);

				// These global variables get around the fact that this
				// recursive method id lite weight and has no reference to
				// parent and grantparent
				if (move.getNote() == MoveNote.TAKE_PIECE && move.getPieceTaken() == PieceID.KING) {
					if (level == 0) {
						twigIsInvalid = true;
					}

					return Values.KING_VALUE;
				}

				moveValue = move.getValue();

				if (level < maxTwigLevel) {
					newBoard = board.getCopy();
					newBoard.adjustKnightValue();
					newBoard.moveChessPiece(move, player);

					suggestedMove = growDecisionTreeLite(newBoard, getNextPlayer(player), moveValue, bestMove, level + 1);
					suggestedPathValue = moveValue - suggestedMove;

					// The king was taken on the next move, which means this
					// move is invalid. Move on to the next move.
					if (suggestedMove == Values.KING_VALUE) {
						continue;
					}

				} else {
					suggestedPathValue = moveValue;
				}

				if (suggestedPathValue > bestMove) {
					bestMove = suggestedPathValue;
				}

				if (parentMoveValue - bestMove < parentBestMove) {
					// if(level == 0){
					// System.out.println("pruned at twig child level");
					// }
					return bestMove;
				}

			}
		}

		return bestMove;
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
		int suggestedPathValue = growDecisionTree(branch, 0);

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

	/**
	 * 
	 * @param player
	 *            The player who is putting the other player in check. Not the
	 *            player that might be in check, but the other one.
	 * @param board
	 *            Board being checked for check situation.
	 * @return Whether or not "player" has put his/her opponent in check
	 */
	private boolean isInCheck(Player player, Board board) {

		Vector<Piece> pieces = board.getPlayerPieces(player);
		Piece piece;
		Vector<Move> moves;
		Move move;

		for (int p = 0; p < pieces.size(); p++) {
			piece = pieces.elementAt(p);
			piece.generateValidMoves();
			moves = piece.getValidMoves();
			for (int m = 0; m < moves.size(); m++) {
				move = moves.elementAt(m);
				if (move.getNote() == MoveNote.TAKE_PIECE && move.getPieceTaken() == PieceID.KING) {
					return true;
				}
			}
			piece.clearValidMoves();
		}

		return false;

	}

	// private DecisionNode tieBreaker(Vector<DecisionNode> ties, Player player)
	// {
	// int randomIndex;
	// int leastChildren;
	// int mostChildren;
	// DecisionNode chosenNode;
	// DecisionNode candidateNode;
	// Vector<DecisionNode> candidateNodes = new Vector<DecisionNode>();
	//
	// candidateNode = ties.elementAt(0);
	//
	// if (candidateNode.hasChildren()) {
	// if (player == Player.USER) {
	// // chose node with least children
	// leastChildren = candidateNode.getChildrenSize();
	// candidateNodes.add(candidateNode);
	// for (int i = 1; i < ties.size(); i++) {
	// candidateNode = ties.elementAt(i);
	// if (candidateNode.getChildrenSize() < leastChildren) {
	// leastChildren = candidateNode.getChildrenSize();
	// candidateNodes.removeAllElements();
	// candidateNodes.add(candidateNode);
	// } else {
	// if (candidateNode.getChildrenSize() == leastChildren) {
	// candidateNodes.add(candidateNode);
	// }
	// }
	// }
	// } else {
	// // chose node with most children
	// mostChildren = candidateNode.getChildrenSize();
	// candidateNodes.add(candidateNode);
	// for (int i = 1; i < ties.size(); i++) {
	// candidateNode = ties.elementAt(i);
	// if (candidateNode.getChildrenSize() > mostChildren) {
	// leastChildren = candidateNode.getChildrenSize();
	// candidateNodes.removeAllElements();
	// candidateNodes.add(candidateNode);
	// } else {
	// if (candidateNode.getChildrenSize() == mostChildren) {
	// candidateNodes.add(candidateNode);
	// }
	// }
	// }
	// }
	//
	// if (candidateNodes.size() != 0) {
	// randomIndex = (int) (Math.random() * candidateNodes.size());
	// chosenNode = candidateNodes.elementAt(randomIndex);
	// } else {
	// chosenNode = candidateNodes.elementAt(0);
	// }
	// } else {
	// randomIndex = (int) (Math.random() * ties.size());
	// chosenNode = ties.elementAt(randomIndex);
	// }
	//
	// return chosenNode;
	// }

	public DecisionNode getRoot() {
		return rootNode;
	}

	private void setRoot(DecisionNode newRootNode) {
		rootNode = newRootNode;
		rootNode.setParent(null);

		if (rootNode.getStatus() != GameStatus.IN_PLAY) {
			System.out.println(rootNode.getStatus().toString());
		}

		System.gc();
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

}
