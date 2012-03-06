package chessGUI;

import java.util.Vector;

import chessAI.DecisionNode;
import chessBackend.Board;
import chessBackend.GameStatus;
import chessBackend.Move;
import chessBackend.MoveNote;
import chessBackend.Player;
import chessPieces.Piece;
import chessPieces.PieceID;
import chessPieces.Values;

public class Adjudicator {
	AdjudicatorNode root;
	Vector<PieceID> userPiecesTaken;
	Vector<PieceID> aiPiecesTaken;

	public Adjudicator(Board board) {
		root = new AdjudicatorNode(null, null, board);
		userPiecesTaken = new Vector<PieceID>();
		aiPiecesTaken = new Vector<PieceID>();
		extendTree(root, 0);
	}

	public boolean move(Move move) {
		int childrenSize = root.getChildrenSize();

		AdjudicatorNode currentNode = root.getHeadChild();
		for (int c = 0; c < childrenSize; c++) {
			if (move.equals(currentNode.getMove())) {
				root.removeAllChildren();
				root.addChild(currentNode);
				root.getBoard().makeMove(currentNode.getMove());
				
				root = currentNode;

				if (currentNode.getMove().getPieceTaken() != null) {
					if (root.getBoard().getPlayer() == Player.USER) {
						userPiecesTaken.add(currentNode.getMove().getPieceTaken().getPieceID());
					} else {
						aiPiecesTaken.add(currentNode.getMove().getPieceTaken().getPieceID());
					}
				}

				
				extendTree(root, 0);
				return true;
			}
			currentNode = currentNode.getNextSibling();
		}

		return false;

	}

	public boolean undo() {

		if (root.getParent() != null) {
			if (root.getParent().getParent() != null) {

				root.getBoard().undoMove();
				root.getBoard().undoMove();

				AdjudicatorNode oldRootNode = root.getParent().getParent();
				root = oldRootNode;
				root.removeAllChildren();

				// reinit tree
				extendTree(root,0);

			} else {
				return false;
			}
		} else {
			return false;
		}

		return true;

	}

	public Vector<Move> getValidMoves() {
		int childrenSize = root.getChildrenSize();
		Vector<Move> validMoves = new Vector<Move>(childrenSize);

		AdjudicatorNode currentNode = root.getHeadChild();
		for (int c = 0; c < childrenSize; c++) {
			validMoves.add(currentNode.getMove());
			currentNode = currentNode.getNextSibling();
		}

		return validMoves;
	}

	public Board getCurrentBoard() {
		return root.getBoard();
	}

	public Vector<PieceID> getPiecesTaken(Player player) {
		if (player == Player.USER) {
			return userPiecesTaken;
		} else {
			return aiPiecesTaken;
		}
	}

	public AdjudicatorNode getRoot() {
		return root;
	}

	public GameStatus getGameStatus() {
		return root.getStatus();
	}

	private void extendTree(AdjudicatorNode branch, int ply) {

		// Current state at branch
		Board board = branch.getBoard();

		if (!branch.hasChildren()) {

			// Node was at the bottom of the tree. This "branch" has to grow.

			Vector<Move> moves;
			Move move;
			AdjudicatorNode newNode;

			// If board is in check, castleing isn't valid
			board.clearBoardState();
			board.setInCheckDetails(calcInCheck(board));

			if (board.isInCheck()) {
				branch.setStatus(GameStatus.CHECK);
			}

			// check all moves of all pieces
			moves = board.generateValidMoves();

			for (int m = 0; m < moves.size(); m++) {

				move = moves.elementAt(m);

				// Check to see if the move in question resulted in
				// the loss of your king. Such a move is invalid because
				// you can't move into check.
				if (move.isKingTaken()) {
					branch.getMove().invalidate();
					return;
				}

				newNode = new AdjudicatorNode(branch, move, board);

				// check to see if you are at the bottom of the branch
				if (ply < 1) {
					board.makeMove(move);
					extendTree(newNode, ply + 1);
					board.undoMove();
				}

				// Check if the newNode move was invalidated
				if (newNode.getMove().isValidated()) {

					// System.out.println("addedNew Node");
					branch.addChild(newNode);
				}

			}

		} else {
			// Node has already been created and has children
			int childrenSize = branch.getChildrenSize();
			AdjudicatorNode currentChild = branch.getHeadChild();
			for (int i = 0; i < childrenSize; i++) {

				if (ply < 1) {
					board.makeMove(currentChild.getMove());
					extendTree(currentChild, ply + 1);
					board.undoMove();
				}
				// Checks if that move was valid. This invalidated check is used
				// after every move by the user. It would remove user moves that
				// put the user in check.
				if (!currentChild.getMove().isValidated()) {
					// The child represents an invalid move. i.e moving into
					// check.
					branch.removeChild(currentChild);
				}

				currentChild = currentChild.getNextSibling();

			}

		} // end of node already has children code

		// game is over at this point
		if (!branch.hasChildren()) {

			if (branch.getStatus() == GameStatus.CHECK) {
				branch.setStatus(GameStatus.CHECKMATE);
			} else {
				branch.setStatus(GameStatus.STALEMATE);
			}
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
	private int calcInCheck(Board board) {
		int[][] kingLeftRight = { { 7, 3, 7, 5 }, { 0, 3, 0, 5 } };
		Vector<Move> moves;
		Move move;
		int inCheck = 0;
		
		Player player = board.getNextPlayer();

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

}
