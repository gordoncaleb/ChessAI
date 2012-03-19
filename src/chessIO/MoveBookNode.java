package chessIO;

import chessBackend.Move;

public class MoveBookNode {
	// Previous move/board/status information
	private MoveBookNode parent;

	// Children which represent all the possible moves of "player
	private MoveBookNode headChild;
	private MoveBookNode tailChild;
	private MoveBookNode nextSibling;
	private MoveBookNode previousSibling;
	private int childrenSize;

	// The last move made on the attached board.
	private Move move;
	
	private int moveBookValue;

	public MoveBookNode(MoveBookNode parent, Move move) {
		this.parent = parent;

		// Linked List data struct pointers
		this.headChild = null;
		this.tailChild = null;
		this.childrenSize = 0;
		this.nextSibling = null;
		this.previousSibling = null;

		this.move = move;
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
	public synchronized void addChild(MoveBookNode newChild) {
		if (childrenSize == 0) {
			headChild = newChild;
			tailChild = newChild;
			headChild.setNextSibling(tailChild);
			headChild.setPreviousSibling(tailChild);
			childrenSize = 1;
		} else {
			int childrenSize = this.getChildrenSize();
			int childNum = 0;
			MoveBookNode currentChild = headChild;

			while (newChild.getMoveBookValue() < currentChild.getMoveBookValue() && childNum < childrenSize) {
				currentChild = currentChild.getNextSibling();
				childNum++;
			}

			if (newChild.getMoveBookValue() >= currentChild.getMoveBookValue()) {

				while (newChild.getMoveBookValue() == currentChild.getMoveBookValue() && Math.random() > 0.5) {
					currentChild = currentChild.getNextSibling();
					childNum++;
				}

				if (currentChild == headChild && childNum == 0) {
					headChild = newChild;
				}

				insertChild(newChild, currentChild.getPreviousSibling(), currentChild);
			} else {
				insertChild(newChild, headChild.getPreviousSibling(), headChild);
			}
		}

	}

	private void insertChild(MoveBookNode child, MoveBookNode previousChild, MoveBookNode nextChild) {
		child.setNextSibling(nextChild);
		child.setPreviousSibling(previousChild);
		previousChild.setNextSibling(child);
		nextChild.setPreviousSibling(child);
		childrenSize++;
	}

	public void removeChild(MoveBookNode child) {
		if (childrenSize != 0) {
			if (child == headChild)
				headChild = child.getNextSibling();

			if (child == tailChild)
				tailChild = child.getPreviousSibling();

			child.getPreviousSibling().setNextSibling(child.getNextSibling());
			child.getNextSibling().setPreviousSibling(child.getPreviousSibling());

		} else {
			headChild = null;
			tailChild = null;
		}

		childrenSize--;
	}

	public void removeAllChildren() {
		headChild = null;
		tailChild = null;
		childrenSize = 0;
	}

	public MoveBookNode getHeadChild() {
		return headChild;
	}

	public MoveBookNode getNextSibling() {
		return nextSibling;
	}

	public void setNextSibling(MoveBookNode nextSibling) {
		this.nextSibling = nextSibling;
	}

	public MoveBookNode getPreviousSibling() {
		return previousSibling;
	}

	public void setPreviousSibling(MoveBookNode previousSibling) {
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

	public void setParent(MoveBookNode parent) {
		this.parent = parent;
	}

	public MoveBookNode getParent() {
		return parent;
	}

	public MoveBookNode getChosenChild() {
		return headChild;
	}
	
	public int getMoveBookValue(){
		return moveBookValue;
	}
	
	public void setMoveBookValue(int moveBookValue){
		this.moveBookValue = moveBookValue;
	}

	public String toString() {
		boolean chosen;
		if (parent != null)
			chosen = parent.getChosenChild() == this;
		else
			chosen = false;

		if (move != null)
			return move.toString() +  " Move Book Value =" + this.getMoveBookValue() + " Chosen: " + chosen;
		else
			return "Game Start";
	}

}
