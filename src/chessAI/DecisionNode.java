package chessAI;

import chessBackend.Board;
import chessBackend.GameStatus;
import chessBackend.MoveNote;
import chessBackend.Side;
import chessBackend.Move;
import chessPieces.King;
import chessPieces.Values;

public class DecisionNode {

	// Previous move/status information
	private DecisionNode parent;

	// Children which represent all the possible moves of "player
	private DecisionNode headChild;
	private DecisionNode nextSibling;
	private DecisionNode previousSibling;
	private int childrenSize;

	// The last move made on the attached board.
	private Move move;

	// The points scored by choosing the greatest scoring/value child and then
	// their greatest scoring/value child and so on until the bottom of the
	// tree, which is known as the twigs
	private int chosenPathValue;

	// Status of the current game state. ie. Check or Checkmate
	private GameStatus status;

	public static void main(String[] args) {
		for (int i = 0; i < 1; i++) {
			Runtime.getRuntime().gc();
		}
		long heapFreeSize = Runtime.getRuntime().freeMemory();

		DecisionNode test = new DecisionNode(null, new Move(0, 0, 0, 0, 100, MoveNote.NONE, new King(Side.BLACK, 1, 1, false)));

		int nodes = 4000000;
		for (int i = 0; i < nodes; i++) {
			test.addChild(new DecisionNode(test, new Move(0, 0, 0, 0, 100, MoveNote.NONE, new King(Side.BLACK, 1, 1, false))));
		}

		for (int i = 0; i < 1; i++) {
			Runtime.getRuntime().gc();
		}

		long objectSize = (heapFreeSize - Runtime.getRuntime().freeMemory()) / nodes;

		System.out.println("Max mem = " + Runtime.getRuntime().maxMemory() / (1024 * 1024));
		System.out.println("used space = " + (Runtime.getRuntime().maxMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024));
		System.out.println(objectSize);

		System.out.println(4000000 / objectSize + " bytes in 4million objs");

		int childSize = test.childrenSize;
		System.out.println(childSize + "");

		int[] nums = { 3, 7, 6, 5, 8, 3, 1, 2 };
		DecisionNode root = new DecisionNode(null, new Move(0, 0, 0, 0, 100, MoveNote.NONE));
		DecisionNode child;
		for (int i = 0; i < nums.length; i++) {
			child = new DecisionNode(null, new Move(0, 0, 0, 0, nums[i], MoveNote.NONE));
			child.setChosenPathValue(nums[i]);
			root.addChild(child);
		}
		System.out.println("Sorted Values");

		root.printChildrenValues();

		DecisionNode currentChild = root.getHeadChild();
		for (int c = 0; c < root.getChildrenSize(); c++) {
			currentChild.setChosenPathValue(nums[c]);
			currentChild = currentChild.getNextSibling();
		}

		System.out.println("Changed Values");
		root.printChildrenValues();

		root.sort();

		System.out.println("Resorted Values");
		root.printChildrenValues();

	}

	public DecisionNode(DecisionNode parent, Move move) {
		this.parent = parent;

		// Linked List data struct pointers
		this.headChild = null;
		this.childrenSize = 0;
		this.nextSibling = null;
		this.previousSibling = null;

		this.move = move;
		this.status = GameStatus.IN_PLAY;

	}

	public void sort() {
		DecisionNode currentChild = headChild;
		DecisionNode nextChild;
		DecisionNode misplacedChild;
		DecisionNode properSibling;

		for (int c = 0; c < childrenSize - 1; c++) {
			nextChild = currentChild.getNextSibling();

			if (currentChild.getChosenPathValue(0) < nextChild.getChosenPathValue(0)) {
				misplacedChild = nextChild;
				properSibling = currentChild;
				while (misplacedChild.getChosenPathValue(0) > properSibling.getChosenPathValue(0) && properSibling != headChild) {
					properSibling = properSibling.getPreviousSibling();
				}

				removeChild(misplacedChild);

				if (properSibling == headChild) {
					if (misplacedChild.getChosenPathValue(0) > headChild.getChosenPathValue(0)) {
						insertChild(misplacedChild, headChild.getPreviousSibling(), headChild);
						headChild = misplacedChild;
					} else {
						insertChild(misplacedChild, headChild, headChild.getNextSibling());
					}
				} else {
					insertChild(misplacedChild, properSibling, properSibling.getNextSibling());
				}

			} else {
				currentChild = nextChild;
			}

		}
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
	public synchronized void addChild(DecisionNode newChild) {
		if (childrenSize == 0) {
			headChild = newChild;
			headChild.setNextSibling(headChild);
			headChild.setPreviousSibling(headChild);
			childrenSize = 1;
		} else {
			int childrenSize = this.getChildrenSize();
			int childNum = 0;
			DecisionNode currentChild = headChild;
			int newChildChosenPathValue = newChild.getChosenPathValue(0);

			while (newChildChosenPathValue < currentChild.getChosenPathValue(0) && childNum < childrenSize) {
				currentChild = currentChild.getNextSibling();
				childNum++;
			}

			if (newChildChosenPathValue >= currentChild.getChosenPathValue(0)) {

				while (newChildChosenPathValue == currentChild.getChosenPathValue(0) && Math.random() > 0.5) {
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

	private void insertChild(DecisionNode child, DecisionNode previousChild, DecisionNode nextChild) {
		child.setNextSibling(nextChild);
		child.setPreviousSibling(previousChild);
		previousChild.setNextSibling(child);
		nextChild.setPreviousSibling(child);
		childrenSize++;
	}

	public void removeChild(DecisionNode child) {
		if (childrenSize > 0) {

			if (childrenSize == 1) {
				headChild = null;
			} else {
				if (child == headChild)
					headChild = child.getNextSibling();

				child.getPreviousSibling().setNextSibling(child.getNextSibling());
				child.getNextSibling().setPreviousSibling(child.getPreviousSibling());
			}

			childrenSize--;
		}
	}

	public void removeAllChildren() {
		headChild = null;
		childrenSize = 0;
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

	public int getChosenPathValue(int depth) {
		int value;

		if (childrenSize != 0) {
			value = this.getMoveValue() - headChild.getChosenPathValue(depth + 1);
		} else {
			value = chosenPathValue;
		}

		if (status == GameStatus.CHECKMATE) {
			value = Values.CHECKMATE_MOVE - Values.CHECKMATE_DEPTH_INC * depth;
		}

		if (status == GameStatus.STALEMATE) {
			value = -Values.STALEMATE_MOVE;
		}

		return value;
	}

	public void setChosenPathValue(int chosenPathValue) {
		this.chosenPathValue = chosenPathValue;
	}

	public void finalize() {
		// System.out.println("Move (" + this.getMove().toString() +
		// ") has been destroyed!");
	}

	public GameStatus getStatus() {
		return status;
	}

	public void setStatus(GameStatus status) {
		this.status = status;
	}

	public boolean hasPieceTaken() {
		if (move == null) {
			return false;
		} else {
			if (move.getPieceTaken() == null) {
				return false;
			} else {
				return true;
			}
		}
	}

	public boolean isGameOver() {
		if (status == GameStatus.CHECKMATE || status == GameStatus.STALEMATE) {
			return true;
		} else {
			return false;
		}
	}

	public boolean isInCheck() {
		if (status == GameStatus.CHECK) {
			return true;
		} else {
			return false;
		}
	}

	public String toString() {
		boolean chosen;
		if (parent != null)
			chosen = parent.getChosenChild() == this;
		else
			chosen = false;

		if (move != null)
			return move.toString() + " Status =" + getStatus().name() + " Move Value =" + this.getMoveValue() + " Chosen Path Value ="
					+ this.getChosenPathValue(0) + " Chosen: " + chosen;
		else
			return "Board Start";
	}

	public void printChildrenValues() {
		DecisionNode currentChild = headChild;
		for (int c = 0; c < childrenSize; c++) {
			System.out.print(currentChild.getChosenPathValue(0) + ",");
			currentChild = currentChild.getNextSibling();
		}

		System.out.print("\n");
	}

}
