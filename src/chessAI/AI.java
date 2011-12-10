package chessAI;

import java.util.Vector;

import chessBackend.Board;
import chessBackend.GameStatus;
import chessBackend.MoveNote;
import chessBackend.Player;
import chessBackend.Move;
import chessGUI.BoardGUI;
import chessPieces.Piece;
import chessPieces.PieceID;
import chessPieces.Values;

public class AI {
	private DecisionNode rootNode;
	private int maxTwigLevel;
	private int maxDecisionTreeLevel;
	private boolean twigIsInCheck;
	private boolean twigIsInvalid;
	private boolean twigParentIsInCheck;
	private int[] childNum = new int[10];

	public AI(Board board) {
		rootNode = new DecisionNode(null, null, 0, board, Player.USER);

		// These numbers determine how many moves ahead the AI thinks
		maxDecisionTreeLevel = 1;
		maxTwigLevel = 1;

		growDecisionTree(rootNode, maxDecisionTreeLevel);
		countChildren(rootNode, 0);
		for (int i = 0; i < 10; i++) {
			System.out.println(childNum[i] + " at level " + i);
		}
	}

	public DecisionNode move(Move usersMove) {

		DecisionNode usersDecision = usersMove.getNode();

		if (usersDecision == rootNode.getChosenChild()) {
			System.out.println("AI predicted the users move!!");
		}

		// growDecisionTree(usersDecision, levels);

		setRoot(usersDecision);

		if (rootNode.getStatus() != GameStatus.CHECKMATE && rootNode.getStatus() != GameStatus.STALEMATE) { // Game
																											// Over
																											// //
																											// Over
			setRoot(usersDecision.getChosenChild());
			growDecisionTree(rootNode, maxDecisionTreeLevel);
		}

		// expandDecisionTree(rootNode, 0);

		for (int i = 0; i < 10; i++) {
			childNum[i] = 0;
		}

		countChildren(rootNode, 0);

		for (int i = 0; i < 10; i++) {
			System.out.println(childNum[i] + " at level " + i);
		}

		System.out.println("AI chose move worth " + rootNode.getChosenPathValue());

		return rootNode;
	}

	private int growDecisionTree(DecisionNode branch, int level) {

		Player nextPlayer;
		Player player = branch.getPlayer();
		Board board = branch.getBoard();

		nextPlayer = getNextPlayer(player);

		int suggestedPathValue;
		int bestMoveValue = Integer.MIN_VALUE;
		Vector<Integer> invalidMoves = new Vector<Integer>();
		Vector<DecisionNode> bestMoves = new Vector<DecisionNode>();
		DecisionNode chosenNode;

		if (!branch.hasChildren()) {

			// Node was at the bottom of the tree. This "branch" has to grow.

			Vector<Piece> pieces;
			Piece piece;
			Vector<Move> moves;
			Move move;
			int moveValue;
			DecisionNode newNode;
			Board newBoard;

			pieces = board.getPlayerPieces(player); // get the the pieces of
													// whose
													// turn it is

			for (int p = 0; p < pieces.size(); p++) { // check all moves of all
														// pieces
				piece = pieces.elementAt(p);
				piece.generateValidMoves();
				moves = piece.getValidMoves();

				for (int m = 0; m < moves.size(); m++) {

					move = moves.elementAt(m);
					moveValue = move.getValue();
					// check to see if the move in question resulted in
					// the loss of your king. Such a move is invalid because
					// you can't move into check.
					if (move.getNote() == MoveNote.TAKE_PIECE && move.getPieceTaken() == PieceID.KING) {
						// A move note of DO_NOTHING is what the board would
						// look like if the user passed up his/her move and
						// allowed the AI to take what is available. If the
						// AI
						// can take the king in that instance, then the user
						// is in check.
						if (branch.getNodeMove().getNote() == MoveNote.DO_NOTHING) {
							branch.getParent().setStatus(GameStatus.CHECK);
						}

						branch.getNodeMove().setNote(MoveNote.INVALIDATED);
						return 0;
					}

					newBoard = board.getCopy();
					newBoard.moveChessPiece(move, player);
					newNode = new DecisionNode(branch, move, moveValue, newBoard, nextPlayer);
					branch.addChild(newNode);

					// check to see if you are at the bottom of the branch
					if (level > 0) {
						suggestedPathValue = growDecisionTree(newNode, level - 1);
					} else {
						suggestedPathValue = moveValue - expandTwig(newNode);
						newNode.setChosenPathValue(suggestedPathValue);
					}

					// Check if the newNode move was invalidated
					if (!newNode.getNodeMove().isValid()) {
						branch.removeChild(newNode);
						continue;
					}

					// Find the maximum move value
					if (suggestedPathValue > bestMoveValue) {
						bestMoveValue = suggestedPathValue;
						branch.setChosenPathValue(branch.getMoveValue() - bestMoveValue);
						bestMoves.removeAllElements();
						bestMoves.add(newNode);
					} else {
						if (suggestedPathValue == bestMoveValue) {
							bestMoves.add(newNode);
						}
					}

				}
			}

		} else {
			// Node has already been created and has children
			Vector<DecisionNode> children = branch.getChildren();
			DecisionNode child;
			for (int i = 0; i < children.size(); i++) {
				child = children.elementAt(i);

				// explore down tree
				suggestedPathValue = growDecisionTree(child, level - 1);

				// Checks if that move was valid. This invalidated check is used
				// after every move by the user. It would remove user moves that
				// put the user in check.
				if (!child.getNodeMove().isValid()) {
					invalidMoves.add(new Integer(i));
					continue;
				}

				// Assume the user and AI should choose highest value move
				if (suggestedPathValue > bestMoveValue) {
					bestMoveValue = suggestedPathValue;
					bestMoves.removeAllElements();
					bestMoves.add(child);
					branch.setChosenPathValue(branch.getMoveValue() - suggestedPathValue);
				} else {
					if (suggestedPathValue == bestMoveValue) {
						bestMoves.add(child);
					}
				}

			}

			// cleanup invalid moves
			for (int i = 0; i < invalidMoves.size(); i++) {
				children.remove(invalidMoves.elementAt(i).intValue());
			}

		} // end of node already has children code

		// game isn't over at this point
		if (branch.hasChildren()) {

			if (bestMoves.size() != 0) {
				chosenNode = tieBreaker(bestMoves, player);
			} else {
				chosenNode = bestMoves.elementAt(0);
			}

			branch.setChosenChild(chosenNode);

		} else { // check for checkmate/stalemate conditions

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

	private int expandTwig(DecisionNode twig) {
		twigIsInCheck = false;
		twigIsInvalid = false;
		twigParentIsInCheck = false;
		
		int twigSuggestedPathValue = growDecisionTreeLite(twig.getBoard(), twig.getPlayer(), twig.getNodeMove(), 0);

		if (twigIsInCheck) {
			twig.setStatus(GameStatus.CHECK);
		}
		
		if(twigIsInvalid){
			twig.getNodeMove().setNote(MoveNote.INVALIDATED);
		}
		
		if(twigParentIsInCheck){
			twig.getParent().setStatus(GameStatus.CHECK);
		}	

		return twigSuggestedPathValue;
	}

	private int growDecisionTreeLite(Board board, Player player, Move parentMove, int level) {
		Board newBoard;
		Vector<Piece> pieces = board.getPlayerPieces(player);
		Vector<Move> moves;
		Move move;
		int suggestedMove;
		int suggestedPathValue;
		int moveValue;

		int bestMove = Integer.MIN_VALUE;

		for (int p = 0; p < pieces.size(); p++) {
			pieces.elementAt(p).generateValidMoves();
			moves = pieces.elementAt(p).getValidMoves();
			for (int m = 0; m < moves.size(); m++) {
				move = moves.elementAt(m);

				// These global variables get around the fact that this recursive method id lite weight and has no reference to parent and grantpar
				if (move.getNote() == MoveNote.TAKE_PIECE && move.getPieceTaken() == PieceID.KING) {
					if (level == 0) {
						if (parentMove.getNote() == MoveNote.DO_NOTHING) {
							twigParentIsInCheck = true;
						}
						twigIsInvalid = true;
					}
					if (level == 1) {
						if (parentMove.getNote() == MoveNote.DO_NOTHING) {
							twigIsInCheck = true;
						}
					}

					return Values.KING_VALUE;
				}

				moveValue = move.getValue();

				if (level < maxTwigLevel) {
					newBoard = board.getCopy();
					newBoard.moveChessPiece(move, player);
					suggestedMove = growDecisionTreeLite(newBoard, getNextPlayer(player), move, level + 1);
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

			}
		}

		return bestMove;
	}

	private DecisionNode tieBreaker(Vector<DecisionNode> ties, Player player) {
		int randomIndex;
		int leastChildren;
		int mostChildren;
		DecisionNode chosenNode;
		DecisionNode candidateNode;
		Vector<DecisionNode> candidateNodes = new Vector<DecisionNode>();

		candidateNode = ties.elementAt(0);

		if (candidateNode.hasChildren()) {
			if (player == Player.USER) {
				// chose node with least children
				leastChildren = candidateNode.getChildrenSize();
				candidateNodes.add(candidateNode);
				for (int i = 1; i < ties.size(); i++) {
					candidateNode = ties.elementAt(i);
					if (candidateNode.getChildrenSize() < leastChildren) {
						leastChildren = candidateNode.getChildrenSize();
						candidateNodes.removeAllElements();
						candidateNodes.add(candidateNode);
					} else {
						if (candidateNode.getChildrenSize() == leastChildren) {
							candidateNodes.add(candidateNode);
						}
					}
				}
			} else {
				// chose node with most children
				mostChildren = candidateNode.getChildrenSize();
				candidateNodes.add(candidateNode);
				for (int i = 1; i < ties.size(); i++) {
					candidateNode = ties.elementAt(i);
					if (candidateNode.getChildrenSize() > mostChildren) {
						leastChildren = candidateNode.getChildrenSize();
						candidateNodes.removeAllElements();
						candidateNodes.add(candidateNode);
					} else {
						if (candidateNode.getChildrenSize() == mostChildren) {
							candidateNodes.add(candidateNode);
						}
					}
				}
			}

			if (candidateNodes.size() != 0) {
				randomIndex = (int) (Math.random() * candidateNodes.size());
				chosenNode = candidateNodes.elementAt(randomIndex);
			} else {
				chosenNode = candidateNodes.elementAt(0);
			}
		} else {
			randomIndex = (int) (Math.random() * ties.size());
			chosenNode = ties.elementAt(randomIndex);
		}

		return chosenNode;
	}

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

	private void countChildren(DecisionNode branch, int depth) {
		Vector<DecisionNode> children = branch.getChildren();

		childNum[depth]++;

		for (int i = 0; i < children.size(); i++) {
			countChildren(children.elementAt(i), depth + 1);
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
