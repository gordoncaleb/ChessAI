package chessGUI;

import chessBackend.Board;
import chessBackend.GameStatus;
import chessBackend.Player;
import chessBackend.Move;

public class AdjudicatorNode {

	// Previous move/board/status information
	private AdjudicatorNode parent;

	// Children which represent all the possible moves of player
	private AdjudicatorNode headChild;
	private AdjudicatorNode nextSibling;
	private AdjudicatorNode previousSibling;
	private int childrenSize;

	// The board awaiting "player"'s move
	private Board board;

	// The player whose turn it is now
	private Player player;

	// The last move made on the attached board.
	private Move move;

	// Status of the current game state. ie. Check or Checkmate
	private GameStatus status;

	public AdjudicatorNode(AdjudicatorNode parent, Move move, Board board, Player player) {
		this.parent = parent;

		// Linked List data struct pointers
		this.headChild = null;
		this.childrenSize = 0;
		this.nextSibling = null;
		this.previousSibling = null;

		this.move = move;
		this.board = board;
		this.player = player;
		this.status = GameStatus.IN_PLAY;

	}

	/**
	 * Adds the newchild to the linked list. Does not sort them like in DecisionNode
	 * 
	 * @param newChild
	 *            The child to be added to the sorted linked list of children
	 */
	public synchronized void addChild(AdjudicatorNode newChild) {
		if (childrenSize == 0) {
			headChild = newChild;
			headChild.setNextSibling(headChild);
			headChild.setPreviousSibling(headChild);
			childrenSize = 1;
		} else {
			childrenSize++;
			insertChild(newChild,headChild.getPreviousSibling(),headChild);
			headChild = newChild;
		}

	}

	private void insertChild(AdjudicatorNode child, AdjudicatorNode previousChild, AdjudicatorNode nextChild) {
		child.setNextSibling(nextChild);
		child.setPreviousSibling(previousChild);
		previousChild.setNextSibling(child);
		nextChild.setPreviousSibling(child);
		childrenSize++;
	}

	public void removeChild(AdjudicatorNode child) {
		if (childrenSize != 0) {
			if (child == headChild)
				headChild = child.getNextSibling();

			child.getPreviousSibling().setNextSibling(child.getNextSibling());
			child.getNextSibling().setPreviousSibling(child.getPreviousSibling());

		} else {
			headChild = null;
		}

		childrenSize--;
	}
	
	public void removeAllChildren(){
		headChild = null;
		childrenSize = 0;
	}

	public AdjudicatorNode getHeadChild() {
		return headChild;
	}

	public AdjudicatorNode getNextSibling() {
		return nextSibling;
	}

	private void setNextSibling(AdjudicatorNode nextSibling) {
		this.nextSibling = nextSibling;
	}

	public AdjudicatorNode getPreviousSibling() {
		return previousSibling;
	}

	private void setPreviousSibling(AdjudicatorNode previousSibling) {
		this.previousSibling = previousSibling;
	}

	public int getChildrenSize() {
		return childrenSize;
	}

	public boolean hasChildren() {
		if (childrenSize != 0)
			return true;
		else
			return false;
	}

	public Move getMove() {
		return move;
	}

	public void setMove(Move nodeMove) {
		this.move = nodeMove;
	}

	public void setParent(AdjudicatorNode parent) {
		this.parent = parent;
	}

	public AdjudicatorNode getParent() {
		return parent;
	}

	public Board getBoard() {
		return board;
	}

	public Player getPlayer() {
		return player;
	}

	public GameStatus getStatus() {
		return status;
	}

	public void setStatus(GameStatus status) {
		this.status = status;
	}

	public String toString() {
		if (move != null)
			return move.toString() + " Status =" + getStatus().name();
		else
			return "Board Start";
	}

}

