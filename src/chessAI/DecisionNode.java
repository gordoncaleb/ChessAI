package chessAI;

import chessBackend.Board;
import chessBackend.GameStatus;
import chessBackend.MoveNote;
import chessBackend.Player;
import chessBackend.Move;

public class DecisionNode {

	// Previous move/board/status information
	private DecisionNode parent;

	// Children which represent all the possible moves of "player
	private DecisionNode headChild;
	private DecisionNode tailChild;
	private DecisionNode currentChild;
	private DecisionNode nextSibling;
	private DecisionNode previousSibling;
	private int childrenSize;

	// The board awaiting "player"'s move
	private Board board;

	// The player whose turn it is now
	private Player player;

	// The last move made on the attached board.
	private Move move;

	// The points scored by choosing the greatest scoring/value child and then
	// their greatest scoring/value child and so on until the bottom of the
	// tree, which is known as the twigs
	private int chosenPathValue;

	// Status of the current game state. ie. Check or Checkmate
	private GameStatus status;

	public DecisionNode(DecisionNode parent, Move move, Board board, Player player) {
		this.parent = parent;

		// Linked List data struct pointers
		this.headChild = null;
		this.tailChild = null;
		this.currentChild = null;
		this.childrenSize = 0;
		this.nextSibling = null;
		this.previousSibling = null;

		this.move = move;
		this.board = board;
		this.player = player;
		this.status = GameStatus.IN_PLAY;

		if (move != null)
			this.move.setNode(this);
	}

	/**
	 * Adds and sorts the newChild into the linked list. It leaves currentChild
	 * pointing to the most recently added for use in the remove method. Note:
	 * the remove method can only remove the most recently added child. For this
	 * algorithm that isn't a problem.
	 * 
	 * @param newChild
	 *            The child to be added to the sorted linked list of children
	 */
	public void addChild(DecisionNode newChild) {
		if (childrenSize == 0) {
			headChild = newChild;
			tailChild = newChild;
			headChild.setNextSibling(headChild);
			headChild.setPreviousSibling(headChild);
			childrenSize = 1;
		} else {
			currentChild = headChild;
			while (newChild.getChosenPathValue() < currentChild.getChosenPathValue() && currentChild != tailChild) {
				currentChild = currentChild.getNextSibling();
			}

			if (newChild.getChosenPathValue() >= currentChild.getChosenPathValue()) {
				
//				if(newChild.getChosenPathValue() == currentChild.getChosenPathValue()){
//					while(newChild.getChosenPathValue() == currentChild.getChosenPathValue() && Math.random()>0.5){
//						currentChild = currentChild.getNextSibling();
//					}
//				}
				
				if (currentChild == headChild) {
					headChild = newChild;
				}

				insertChild(newChild, currentChild.getPreviousSibling(), currentChild);
			} else {
				insertChild(newChild, tailChild, headChild);
				tailChild = newChild;
			}
		}

		currentChild = newChild;
	}

	private void insertChild(DecisionNode child, DecisionNode previousChild, DecisionNode nextChild) {
		child.setNextSibling(nextChild);
		child.setPreviousSibling(previousChild);
		previousChild.setNextSibling(child);
		nextChild.setPreviousSibling(child);
		childrenSize++;
	}

	public void removeChild() {
		
		removeChild(currentChild);
		
		if(currentChild!=null){
			currentChild = currentChild.getNextSibling();
		}
		
	}

	public void removeChild(DecisionNode child) {
		if (childrenSize != 0) {
			if (child == headChild)
				headChild = child.getNextSibling();

			if (child == tailChild)
				tailChild = child.getPreviousSibling();

			child.getPreviousSibling().setNextSibling(child.getNextSibling());
			child.getNextSibling().setPreviousSibling(child.getPreviousSibling());

		}else{
			headChild = null;
			tailChild = null;
			currentChild = null;
		}
		
		childrenSize--;
	}

	public DecisionNode getHeadChild() {
		return headChild;
	}

	public DecisionNode getNextSibling() {
		return nextSibling;
	}

	public void setNextSibling(DecisionNode nextSibling) {
		this.nextSibling = nextSibling;
	}

	public DecisionNode getPreviousSibling() {
		return previousSibling;
	}

	public void setPreviousSibling(DecisionNode previousSibling) {
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

	public void setParent(DecisionNode parent) {
		this.parent = parent;
	}

	public DecisionNode getParent() {
		return parent;
	}

	public DecisionNode getChosenChild() {
		return headChild;
	}

	public int getMoveValue() {
		if (move != null)
			return move.getValue();
		else
			return 0;
	}

	public int getChosenPathValue() {
		if(childrenSize != 0){
			return this.getMoveValue() - headChild.getChosenPathValue();
		}else{
			return chosenPathValue;
		}
	}
	
	public void setChosenPathValue(int chosenPathValue){
		this.chosenPathValue = chosenPathValue;
	}

	public Board getBoard() {
		return board;
	}

	public Player getPlayer() {
		return player;
	}

	public void finalize() {
		// System.out.println("Move (" + nodeMove.toString() +
		// ") has been destroyed!");
	}

	public GameStatus getStatus() {
		return status;
	}

	public void setStatus(GameStatus status) {
		this.status = status;

		if (status == GameStatus.CHECK) {
			boolean changedValidMoves = board.inCheck(player);

			if (changedValidMoves) {
				currentChild = headChild;
				for (int c = 0; c < childrenSize; c++) {

					if (currentChild.getMove().getNote() == MoveNote.INVALIDATED) {
						removeChild();
					}

					currentChild = currentChild.getNextSibling();
				}

			}
		}
	}

	public String toString() {
		boolean chosen;
		if (parent != null)
			chosen = parent.getChosenChild() == this;
		else
			chosen = false;

		if (move != null)
			return move.toString() + " Move Value =" + this.getMoveValue() + " Chosen Path Value =" + this.getChosenPathValue() + " Chosen: " + chosen;
		else
			return "Board Start";
	}

}
