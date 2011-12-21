package chessAI;

import java.util.Vector;

import chessBackend.Board;
import chessBackend.GameStatus;
import chessBackend.Move;
import chessBackend.MoveNote;
import chessBackend.Player;
import chessPieces.CastleDetails;
import chessPieces.Piece;
import chessPieces.PieceID;
import chessPieces.Values;

public class AIProcessor extends Thread {
	private DecisionNode rootNode;
	private DecisionNode branchNode;
	private int taskSize;
	private boolean isNewTask;

	private int maxTreeLevel;
	private int maxTwigLevel;
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

			ai.processorDone();

		}
	}

	public void setTask(DecisionNode rootNode, DecisionNode branchNode, int taskSize) {
		this.rootNode = rootNode;
		this.branchNode = branchNode;
		this.taskSize = taskSize;
		System.out.println("Processor " + this.getId() + " receieved task size of " + taskSize);
		this.isNewTask = true;
	}

	private void executeTask() {
		DecisionNode nextBranch;

		for (int i = 0; i < taskSize; i++) {
			
			//System.out.println("Processing move " + branchNode.getMove().toString());
			growDecisionTree(branchNode, 0);
			
			nextBranch = branchNode.getNextSibling();
			
			if(rootNode!=null && branchNode.getMove().isValidated()){
				rootNode.addChild(branchNode);
			}
			
			//System.out.println("Processor " + this.getId() + " completed a task.");
			
			branchNode = nextBranch;
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
	private int growDecisionTree(DecisionNode branch, int level) {

		// This is the player making the child moves, not the player that made
		// the branch.
		Player player = branch.getPlayer();

		// This is the player that made the branch as well as the player making
		// moves on children of branch.
		Player nextPlayer = getNextPlayer(player);

		// Current state at branch
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
			if (isInCheck(nextPlayer, board)) {
				branch.setStatus(GameStatus.CHECK);
			}

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
					if (move.getPieceTaken() == PieceID.KING) {
						branch.getMove().setNote(MoveNote.INVALIDATED);
						return 0;
					}

					newBoard = board.getCopy();
					newBoard.adjustKnightValue();
					newBoard.moveChessPiece(move, player);
					newNode = new DecisionNode(branch, move, newBoard, nextPlayer);

					// check to see if you are at the bottom of the branch
					if (level < maxTreeLevel) {
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

					// alpha beta pruning
					if (nextPlayer == Player.AI && branch.getParent() != null) {
						if (branch.getParent().getHeadChild() != null) {
							if (branch.getMoveValue() - suggestedPathValue < branch.getParent().getHeadChild().getChosenPathValue()) {
								branch.getMove().setNote(MoveNote.INVALIDATED);
								return suggestedPathValue;
							}
						}
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
				branch.setChosenPathValue(Values.CHECKMATE_MOVE);
			} else {
				branch.setStatus(GameStatus.STALEMATE);
				branch.setChosenPathValue(Values.STALEMATE_MOVE);
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
		
		if(twig.getBoard().isInCheck()){
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
		Board newBoard;
		Vector<Move> moves;
		Move move;
		boolean hasMove = false;
		int suggestedMove;
		int suggestedPathValue;
		int moveValue;

		int bestMove = Integer.MIN_VALUE;
		Player nextPlayer = getNextPlayer(player);

		// If board is in check, castleing isn't valid
		board.setInCheck(isInCheck(getNextPlayer(player), board));

		Vector<Piece> pieces = board.getPlayerPieces(player);

		piecesLoop: for (int p = 0; p < pieces.size(); p++) {
			pieces.elementAt(p).generateValidMoves();
			moves = pieces.elementAt(p).getValidMoves();

			movesLoop: for (int m = 0; m < moves.size(); m++) {
				move = moves.elementAt(m);

				// These global variables get around the fact that this
				// recursive method id lite weight and has no reference to
				// parent and grantparent
				if (move.getPieceTaken() == PieceID.KING) {
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

					suggestedMove = growDecisionTreeLite(newBoard, nextPlayer, moveValue, bestMove, level + 1);
					suggestedPathValue = moveValue - suggestedMove;

					// The king was taken on the next move, which means this
					// move is invalid. Move on to the next move.
					if (suggestedMove == Values.KING_VALUE) {
						continue movesLoop;
					}

				} else {
					suggestedPathValue = moveValue;
				}

				hasMove = true;

				if (suggestedPathValue > bestMove) {
					bestMove = suggestedPathValue;
				}

				if (parentMoveValue - bestMove < parentBestMove && player == Player.USER) {
					break piecesLoop;
				}

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
	private boolean isInCheck(Player player, Board board) {
		int[][] kingLeftRight = { { 7, 3, 7, 5 }, { 0, 3, 0, 5 } };
		Vector<Piece> pieces = board.getPlayerPieces(player);
		Piece piece;
		Vector<Move> moves;
		Move move;
		boolean inCheck = false;
		boolean canFar = true;
		boolean canNear = true;

		for (int p = 0; p < pieces.size(); p++) {
			piece = pieces.elementAt(p);
			piece.generateValidMoves();
			moves = piece.getValidMoves();
			for (int m = 0; m < moves.size(); m++) {
				move = moves.elementAt(m);
				if (move.getNote() == MoveNote.TAKE_PIECE && move.getPieceTaken() == PieceID.KING) {
					inCheck = true;
				}

				if (move.getToRow() == kingLeftRight[player.ordinal()][0] && move.getToCol() == kingLeftRight[player.ordinal()][1]) {
					canFar = false;
				}

				if (move.getToRow() == kingLeftRight[player.ordinal()][2] && move.getToCol() == kingLeftRight[player.ordinal()][3]) {
					canNear = false;
				}
			}
			piece.clearValidMoves();
		}

		setCastleDetails(board, canFar, canNear, inCheck);

		return inCheck;

	}

	private void setCastleDetails(Board board, boolean canFar, boolean canNear, boolean inCheck) {
		CastleDetails castleDetails = CastleDetails.NO_CASTLE;

		if (!inCheck) {
			if (canNear && canFar) {
				castleDetails = CastleDetails.CASTLE_BOTH;
			} else {
				if (canNear) {
					castleDetails = CastleDetails.CASTLE_NEAR;
				}

				if (canFar) {
					castleDetails = CastleDetails.CASTLE_FAR;
				}
			}
		}

		board.setCastleDetails(castleDetails);

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

}