package chessGUI;

import java.util.Vector;

import chessAI.DecisionNode;
import chessBackend.Board;
import chessBackend.GameStatus;
import chessBackend.Move;
import chessBackend.MoveNote;
import chessBackend.Player;
import chessPieces.PieceID;
import chessPieces.Values;

public class Adjudicator {
	AdjudicatorNode root;
	Vector<PieceID> userPiecesTaken;
	Vector<PieceID> aiPiecesTaken;

	public Adjudicator(Board board, Player firstPlayer) {
		root = new AdjudicatorNode(null, null, board, firstPlayer);
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
				root = currentNode;

				if (move.getPieceTaken() != null) {
					if (root.getPlayer() == Player.USER) {
						userPiecesTaken.add(move.getPieceTaken().getPieceID());
					} else {
						aiPiecesTaken.add(move.getPieceTaken().getPieceID());
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

		if (root.getPlayer() != Player.USER) {
			return false;
		}

		AdjudicatorNode parentDecision = root.getParent();

		if (parentDecision != null) {

			if (root.getMove().getPieceTaken() != null) {
				if (root.getPlayer() == Player.AI) {
					aiPiecesTaken.remove(aiPiecesTaken.lastElement());
				} else {
					userPiecesTaken.remove(userPiecesTaken.lastElement());
				}
			}

			if (parentDecision.getMove().getPieceTaken() != null) {
				if (parentDecision.getPlayer() == Player.AI) {
					aiPiecesTaken.remove(aiPiecesTaken.lastElement());
				} else {
					userPiecesTaken.remove(userPiecesTaken.lastElement());
				}
			}

			root = root.getParent().getParent();
			root.removeAllChildren();
			extendTree(root, 0);

			return true;
		} else {
			return false;
		}

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
		// This is the player making the child moves, not the player that made
		// the branch.
		Player player = branch.getPlayer();

		// This is the player that made the branch as well as the player making
		// moves on children of branch.
		Player nextPlayer = getNextPlayer(player);

		// Current state at branch
		Board board = branch.getBoard();

		if (!branch.hasChildren()) {

			// Node was at the bottom of the tree. This "branch" has to grow.

			Vector<Move> moves;
			Move move;
			AdjudicatorNode newNode;
			Board newBoard;

			// If board is in check, castleing isn't valid
			board.setInCheck(calcInCheck(nextPlayer, board));

			if (board.isInCheck()) {
				branch.setStatus(GameStatus.CHECK);
			}

			// check all moves of all pieces
			moves = board.generateValidMoves(player);

			for (int m = 0; m < moves.size(); m++) {

				move = moves.elementAt(m);

				// Check to see if the move in question resulted in
				// the loss of your king. Such a move is invalid because
				// you can't move into check.
				if (move.getPieceTaken() != null) {
					if (move.getPieceTaken().getPieceID() == PieceID.KING) {
						branch.getMove().setNote(MoveNote.INVALIDATED);
						return;
					}
				}

				newBoard = board.getCopy();
				newBoard.makeMove(move, player);
				newNode = new AdjudicatorNode(branch, move, newBoard, nextPlayer);

				// check to see if you are at the bottom of the branch
				if (ply < 1) {
					extendTree(newNode, ply + 1);
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
					extendTree(currentChild, ply + 1);
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
				;
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
	private int calcInCheck(Player player, Board board) {
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
}
