package chessAI;

import chessBackend.MoveNote;
import chessBackend.Move;

public class DecisionNode {

	// Children which represent all the possible moves of "player
	private DecisionNode headChild;
	private DecisionNode nextSibling;

	private DecisionNode tailChild;
	private DecisionNode previousSibling;

	// The last move made on the attached board.
	private long move;

	private int chosenPathValue;

	public static void main(String[] args) {
		// for (int i = 0; i < 1; i++) {
		// Runtime.getRuntime().gc();
		// }
		// long heapFreeSize = Runtime.getRuntime().freeMemory();
		//
		// DecisionNode test = new DecisionNode(null, new Move(0, 0, 0, 0, 100,
		// MoveNote.NONE, new King(Side.BLACK, 1, 1, false)));
		//
		// int nodes = 4000000;
		// for (int i = 0; i < nodes; i++) {
		// test.addChild(new DecisionNode(test, new Move(0, 0, 0, 0, 100,
		// MoveNote.NONE, new King(Side.BLACK, 1, 1, false))));
		// }
		//
		// for (int i = 0; i < 1; i++) {
		// Runtime.getRuntime().gc();
		// }
		//
		// long objectSize = (heapFreeSize - Runtime.getRuntime().freeMemory())
		// / nodes;
		//
		// System.out.println("Max mem = " + Runtime.getRuntime().maxMemory() /
		// (1024 * 1024));
		// System.out.println("used space = " +
		// (Runtime.getRuntime().maxMemory() -
		// Runtime.getRuntime().freeMemory()) / (1024 * 1024));
		// System.out.println(objectSize);
		//
		// System.out.println(4000000 / objectSize + " bytes in 4million objs");
		//
		// int childSize = test.childrenSize;
		// System.out.println(childSize + "");

		int[] nums = { 3, 7, 6, 5, 8, 3, 1, 2 };
		DecisionNode root = new DecisionNode(Move.moveLong(0, 0, 0, 0, 100, MoveNote.NONE));
		DecisionNode child;
		for (int i = 0; i < nums.length; i++) {
			child = new DecisionNode(Move.moveLong(0, 0, 0, 0, nums[i], MoveNote.NONE));
			child.setChosenPathValue(nums[i]);
			root.addChild(child);
		}
		System.out.println("Sorted Values");

		root.printChildrenValues();

		DecisionNode currentChild = root.getHeadChild();
		int c = 0;
		while (currentChild != null) {
			currentChild.setChosenPathValue(nums[c]);
			currentChild = currentChild.getNextSibling();
			c++;
		}

		System.out.println("Changed Values");
		root.printChildrenValues();

		for (int i = 0; i < 100; i++) {
			System.out.println((int) Math.round(Math.random()));
		}

		// root.sort();
		//
		// System.out.println("Resorted Values");
		// root.printChildrenValues();

	}

	public DecisionNode(long move) {
		this(move, 0);
	}

	public DecisionNode(long move, int chosenPathValue) {

		// Linked List data struct pointers
		this.headChild = null;
		this.nextSibling = null;

		this.tailChild = null;
		this.previousSibling = null;

		this.chosenPathValue = chosenPathValue;

		this.move = move;
	}

	// public void sort() {
	// DecisionNode currentChild = headChild;
	// DecisionNode nextChild;
	// DecisionNode misplacedChild;
	// DecisionNode properSibling;
	//
	// for (int c = 0; c < childrenSize - 1; c++) {
	// nextChild = currentChild.getNextSibling();
	//
	// if (currentChild.getChosenPathValue(0) < nextChild.getChosenPathValue(0))
	// {
	// misplacedChild = nextChild;
	// properSibling = currentChild;
	// while (misplacedChild.getChosenPathValue(0) >
	// properSibling.getChosenPathValue(0) && properSibling != headChild) {
	// properSibling = properSibling.getPreviousSibling();
	// }
	//
	// removeChild(misplacedChild);
	//
	// if (properSibling == headChild) {
	// if (misplacedChild.getChosenPathValue(0) >
	// headChild.getChosenPathValue(0)) {
	// insertChild(misplacedChild, headChild.getPreviousSibling(), headChild);
	// headChild = misplacedChild;
	// } else {
	// insertChild(misplacedChild, headChild, headChild.getNextSibling());
	// }
	// } else {
	// insertChild(misplacedChild, properSibling,
	// properSibling.getNextSibling());
	// }
	//
	// } else {
	// currentChild = nextChild;
	// }
	//
	// }
	// }

	// /**
	// * Adds and sorts the newChild into the linked list. It leaves
	// currentChild
	// * pointing to the most recently added for use in the remove method. Note:
	// * the remove method can only remove the most recently added child. For
	// this
	// * algorithm that isn't a problem.
	// *
	// * @param newChild
	// * The child to be added to the sorted linked list of children
	// */
	// public synchronized void addChild(DecisionNode newChild) {
	// if (childrenSize == 0) {
	// headChild = newChild;
	// headChild.setNextSibling(headChild);
	// headChild.setPreviousSibling(headChild);
	// childrenSize = 1;
	// } else {
	// int childrenSize = this.getChildrenSize();
	// int childNum = 0;
	// DecisionNode currentChild = headChild;
	// int newChildChosenPathValue = newChild.getChosenPathValue(0);
	//
	// while (newChildChosenPathValue < currentChild.getChosenPathValue(0) &&
	// childNum < childrenSize) {
	// currentChild = currentChild.getNextSibling();
	// childNum++;
	// }
	//
	// if (newChildChosenPathValue >= currentChild.getChosenPathValue(0)) {
	//
	// while (newChildChosenPathValue == currentChild.getChosenPathValue(0)) {//
	// &&
	// // Math.random()
	// // >
	// // 0.5)
	// // {
	// currentChild = currentChild.getNextSibling();
	// childNum++;
	// }
	//
	// if (currentChild == headChild && childNum == 0) {
	// headChild = newChild;
	// }
	//
	// insertChild(newChild, currentChild.getPreviousSibling(), currentChild);
	// } else {
	// insertChild(newChild, headChild.getPreviousSibling(), headChild);
	// }
	// }
	//
	// }

	public void addChild(DecisionNode newChild) {
		addChild(newChild, newChild.getChosenPathValue());
	}

	public synchronized void addChild(DecisionNode newChild, int newChildCPV) {

		DecisionNode currentChild = null;
		DecisionNode nextChild = headChild;
		// int newChildCPV = newChild.getChosenPathValue();

		while (nextChild != null) {
			if (newChildCPV > nextChild.getChosenPathValue()) {

				newChild.setNextSibling(nextChild);
				newChild.setPreviousSibling(currentChild);

				nextChild.setPreviousSibling(newChild);

				if (currentChild != null) {
					currentChild.setNextSibling(newChild);
				} else {
					headChild = newChild;
				}
				break;
			}

			currentChild = nextChild;
			nextChild = currentChild.getNextSibling();
		}

		if (nextChild == null) {

			newChild.setNextSibling(null);
			newChild.setPreviousSibling(currentChild);

			if (currentChild != null) {
				currentChild.setNextSibling(newChild);
				tailChild = newChild;
			} else {
				headChild = newChild;
				tailChild = newChild;
			}
		}

		// childrenSize++;

	}

	public synchronized void addChildTail(DecisionNode newChild, int newChildCPV) {

		DecisionNode currentChild = null;
		DecisionNode nextChild = tailChild;
		// int newChildCPV = newChild.getChosenPathValue();

		while (nextChild != null) {
			if (newChildCPV < nextChild.getChosenPathValue()) {

				newChild.setPreviousSibling(nextChild);
				newChild.setNextSibling(currentChild);

				nextChild.setNextSibling(newChild);

				if (currentChild != null) {
					currentChild.setPreviousSibling(newChild);
				} else {
					tailChild = newChild;
				}
				break;
			}

			currentChild = nextChild;
			nextChild = currentChild.getPreviousSibling();
		}

		if (nextChild == null) {

			newChild.setPreviousSibling(null);
			newChild.setNextSibling(currentChild);

			if (currentChild != null) {
				currentChild.setPreviousSibling(newChild);
				headChild = newChild;
			} else {
				headChild = newChild;
				tailChild = newChild;
			}
		}

	}

	// private void insertChild(DecisionNode child, DecisionNode previousChild,
	// DecisionNode nextChild) {
	// child.setNextSibling(nextChild);
	// child.setPreviousSibling(previousChild);
	// previousChild.setNextSibling(child);
	// nextChild.setPreviousSibling(child);
	// childrenSize++;
	// }

	// public void removeChild(DecisionNode child) {
	// if (childrenSize > 0) {
	//
	// if (childrenSize == 1) {
	// headChild = null;
	// } else {
	// if (child == headChild)
	// headChild = child.getNextSibling();
	//
	// child.getPreviousSibling().setNextSibling(child.getNextSibling());
	// child.getNextSibling().setPreviousSibling(child.getPreviousSibling());
	// }
	//
	// childrenSize--;
	// }
	// }

	public DecisionNode getPreviousSibling() {
		return previousSibling;
	}

	public void setPreviousSibling(DecisionNode previousSibling) {
		this.previousSibling = previousSibling;

	}

	public void removeAllChildren() {
		headChild = null;
		tailChild = null;
		// childrenSize = 0;
	}

	public DecisionNode getHeadChild() {
		return headChild;
	}

	public DecisionNode getTailChild() {
		// return headChild.getLastSibling();
		return tailChild;
	}

	public DecisionNode getNextSibling() {
		return nextSibling;
	}

	public void setNextSibling(DecisionNode nextSibling) {
		this.nextSibling = nextSibling;
	}

	// public DecisionNode getPreviousSibling() {
	// return previousSibling;
	// }
	//
	// public void setPreviousSibling(DecisionNode previousSibling) {
	// this.previousSibling = previousSibling;
	// }

	public int getChildrenSize() {

		if (!hasChildren()) {
			return 0;
		}

		int childrenSize = 1;
		DecisionNode sib = headChild;

		while (sib.getNextSibling() != null) {
			sib = sib.getNextSibling();
			childrenSize++;
		}

		return childrenSize;
	}

	public boolean hasChildren() {
		if (headChild != null)
			return true;
		else
			return false;
	}

	public long getMove() {
		return move;
	}

	public void setMove(long nodeMove) {
		this.move = nodeMove;
	}

	// private void setParent(DecisionNode parent) {
	// this.parent = parent;
	// }
	//
	// private DecisionNode getParent() {
	// return parent;
	// }

	public DecisionNode getChosenChild() {
		return headChild;
	}

	public int getMoveValue() {
		if (move != 0)
			return Move.getValue(move);
		else
			return 0;
	}

	// public int getChosenPathValue(int depth) {
	// int value;
	//
	// // if (childrenSize != 0) {
	// // value = this.getMoveValue() - headChild.getChosenPathValue(depth +
	// // 1);
	// // } else {
	// // value = chosenPathValue;
	// // }
	//
	// if (chosenPathValue == null) {
	// if (headChild != null) {
	// value = this.getMoveValue() - headChild.getChosenPathValue(depth + 1);
	// } else {
	// value = 0;
	// }
	// } else {
	// value = chosenPathValue;
	// }
	//
	// if (status == GameStatus.CHECKMATE) {
	// value = Values.CHECKMATE_MOVE - Values.CHECKMATE_DEPTH_INC * depth;
	// }
	//
	// // if (status == GameStatus.STALEMATE || status == GameStatus.DRAW) {
	// // if (depth % 2 == 0) {
	// // value = -Values.STALEMATE_MOVE;
	// // }else{
	// // value = Values.STALEMATE_MOVE;
	// // }
	// // }
	//
	// return value;
	// }

	public int getChosenPathValue() {
		return chosenPathValue;
	}

	public void setChosenPathValue(Integer chosenPathValue) {
		this.chosenPathValue = chosenPathValue;
	}

	public DecisionNode getLastSibling() {
		DecisionNode sib = this;

		while (sib.getNextSibling() != null) {
			sib = sib.getNextSibling();
		}

		return sib;
	}

	public void finalize() {
		// System.out.println("Move (" + this.getMove().toString() +
		// ") has been destroyed!");
	}

	// private GameStatus getStatus() {
	// return status;
	// }
	//
	// private void setStatus(GameStatus status) {
	// this.status = status;
	// }

	public boolean hasPieceTaken() {
		if (move == 0) {
			return false;
		} else {
			if (Move.hasPieceTaken(move)) {
				return true;
			} else {
				return false;
			}
		}
	}

	// private int getPieceTakenValue() {
	// if (hasPieceTaken()) {
	// return Values.getPieceValue(move.getPieceTakenID());
	// } else {
	// return 0;
	// }
	// }

	// private boolean isGameOver() {
	// if (status == GameStatus.CHECKMATE || status == GameStatus.STALEMATE ||
	// status == GameStatus.DRAW) {
	// return true;
	// } else {
	// return false;
	// }
	// }
	//
	// private boolean isInCheck() {
	// if (status == GameStatus.CHECK) {
	// return true;
	// } else {
	// return false;
	// }
	// }

	public String toString() {

		if (move != 0)
			return Move.toString(move) + " Chosen Path Value =" + this.getChosenPathValue();
		else
			return "Board Start";
	}

	public void printChildrenValues() {
		DecisionNode currentChild = headChild;
		while (currentChild != null) {
			System.out.print(currentChild.getChosenPathValue() + ",");
			currentChild = currentChild.getNextSibling();
		}

		System.out.print("\n");
	}

}
