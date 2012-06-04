package chessAI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import chessBackend.Move;

public class DecisionNode implements Comparable<DecisionNode> {

	// Children which represent all the possible moves of "player
	// private DecisionNode headChild;
	// private DecisionNode nextSibling;
	//
	// private DecisionNode tailChild;
	// private DecisionNode previousSibling;

	private Object[] children;

	// The last move made on the attached board.
	private long move;

	private int chosenPathValue;

	public static void main(String[] args) {

		ArrayList<Integer> nums = new ArrayList<Integer>();

		DecisionNode root = new DecisionNode(0);

		int[][] rands = new int[1000000][30];

		for (int i = 0; i < rands.length; i++) {
			for (int n = 0; n < 30; n++) {
				rands[i][n] = (int) (Math.random() * 100);
			}
		}

		for (int i = 0; i < 30; i++) {
			rands[0][i] = 30 - i;
		}

		ArrayList<DecisionNode> nodes = new ArrayList<DecisionNode>();

		for (int i = 0; i < 30; i++) {
			nodes.add(new DecisionNode(0, rands[1][i]));
		}

		root.setChildren(nodes.toArray());

		// functional
		for (int i = 0; i < 10000; i++) {
			nums.clear();
			nodes.clear();
			for (int n = 0; n < 30; n++) {
				nums.add(new Integer(rands[i][n]));
				nodes.add(new DecisionNode(0, rands[i][n]));
			}

			root.setChildren(nodes.toArray());

			Collections.sort(nums, Collections.reverseOrder());
			root.sort();

			for (int n = 0; n < 30; n++) {
				if (nums.get(n) != root.getChild(n).getChosenPathValue()) {
					System.out.println("Sorting error");
				}
			}
		}

		nums.clear();
		root.removeAllChildren();
		nodes.clear();

		// /////////////////////////////////

		System.out.println("Random dataset sort");
		long time;

		// arraylist sort
		time = System.currentTimeMillis();

		for (int n = 0; n < 30; n++) {
			nums.add(new Integer(rands[0][n]));
		}

		for (int i = 0; i < rands.length; i++) {
			for (int n = 0; n < 30; n++) {
				nums.set(n, rands[i][n]);
			}
			Collections.sort(nums, Collections.reverseOrder());
		}

		System.out.println("Arraylist sort took " + (System.currentTimeMillis() - time));

		time = System.currentTimeMillis();

		for (int n = 0; n < 30; n++) {
			nodes.add(new DecisionNode(0, rands[0][n]));
		}

		root.setChildren(nodes.toArray());

		for (int i = 0; i < rands.length; i++) {
			for (int n = 0; n < 30; n++) {
				root.getChild(n).setChosenPathValue(rands[i][n]);
			}

			root.sort();
		}

		System.out.println("DecisionNode sort took " + (System.currentTimeMillis() - time));

		System.out.println("Done!");

	}

	public DecisionNode(long move) {
		this(move, 0);
	}

	public DecisionNode(long move, int chosenPathValue) {

		// Linked List data struct pointers
		// this.headChild = null;
		// this.nextSibling = null;
		//
		// this.tailChild = null;
		// this.previousSibling = null;

		children = null;

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

	@Override
	public int compareTo(DecisionNode o) {
		return this.chosenPathValue - o.getChosenPathValue();
	}

	public boolean equals(DecisionNode o) {
		return (this.chosenPathValue == o.getChosenPathValue());
	}

	public synchronized void sort() {
		Arrays.sort(children, Collections.reverseOrder());
	}

	public synchronized void sort(int toIndex) {
		Arrays.sort(children, 0, toIndex, Collections.reverseOrder());
	}

	// public void addChild(DecisionNode newChild) {
	//
	// children.add(newChild);
	// // addChild(newChild, newChild.getChosenPathValue());
	// }

	public void setChildren(Object[] children) {
		this.children = children;
	}

	public Object[] getChildren() {
		return children;
	}

	// public synchronized void addChildHead(DecisionNode newChild, int
	// newChildCPV) {
	//
	// DecisionNode currentChild = null;
	// DecisionNode nextChild = headChild;
	// // int newChildCPV = newChild.getChosenPathValue();
	//
	// while (nextChild != null) {
	// if (newChildCPV > nextChild.getChosenPathValue()) {
	//
	// newChild.setNextSibling(nextChild);
	// newChild.setPreviousSibling(currentChild);
	//
	// nextChild.setPreviousSibling(newChild);
	//
	// if (currentChild != null) {
	// currentChild.setNextSibling(newChild);
	// } else {
	// headChild = newChild;
	// }
	// break;
	// }
	//
	// currentChild = nextChild;
	// nextChild = currentChild.getNextSibling();
	// }
	//
	// if (nextChild == null) {
	//
	// newChild.setNextSibling(null);
	// newChild.setPreviousSibling(currentChild);
	//
	// if (currentChild != null) {
	// currentChild.setNextSibling(newChild);
	// tailChild = newChild;
	// } else {
	// headChild = newChild;
	// tailChild = newChild;
	// }
	// }
	//
	// // childrenSize++;
	//
	// }
	//
	// public synchronized void addChild(DecisionNode newChild, int newChildCPV)
	// {
	//
	// DecisionNode currentChild = null;
	// DecisionNode nextChild = tailChild;
	// // int newChildCPV = newChild.getChosenPathValue();
	//
	// while (nextChild != null) {
	// if (newChildCPV < nextChild.getChosenPathValue()) {
	//
	// newChild.setPreviousSibling(nextChild);
	// newChild.setNextSibling(currentChild);
	//
	// nextChild.setNextSibling(newChild);
	//
	// if (currentChild != null) {
	// currentChild.setPreviousSibling(newChild);
	// } else {
	// tailChild = newChild;
	// }
	// break;
	// }
	//
	// currentChild = nextChild;
	// nextChild = currentChild.getPreviousSibling();
	// }
	//
	// if (nextChild == null) {
	//
	// newChild.setPreviousSibling(null);
	// newChild.setNextSibling(currentChild);
	//
	// if (currentChild != null) {
	// currentChild.setPreviousSibling(newChild);
	// headChild = newChild;
	// } else {
	// headChild = newChild;
	// tailChild = newChild;
	// }
	// }
	//
	// }

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

	// public DecisionNode getPreviousSibling() {
	// return previousSibling;
	// }
	//
	// public void setPreviousSibling(DecisionNode previousSibling) {
	// this.previousSibling = previousSibling;
	//
	// }

	public void removeAllChildren() {
		// headChild = null;
		// tailChild = null;
		children = null;
		// childrenSize = 0;
	}

	public DecisionNode getChild(int i) {
		return (DecisionNode) children[i];
	}

	public DecisionNode getHeadChild() {
		// return headChild;
		return (DecisionNode) children[0];
	}

	public DecisionNode getTailChild() {
		// return headChild.getLastSibling();
		// return tailChild;
		return (DecisionNode) children[children.length];
	}

	// public DecisionNode getNextSibling() {
	// return nextSibling;
	// }
	//
	// public void setNextSibling(DecisionNode nextSibling) {
	// this.nextSibling = nextSibling;
	// }

	// public DecisionNode getPreviousSibling() {
	// return previousSibling;
	// }
	//
	// public void setPreviousSibling(DecisionNode previousSibling) {
	// this.previousSibling = previousSibling;
	// }

	public int getChildrenSize() {

		// if (!hasChildren()) {
		// return 0;
		// }
		//
		// int childrenSize = 1;
		// DecisionNode sib = headChild;
		//
		// while (sib.getNextSibling() != null) {
		// sib = sib.getNextSibling();
		// childrenSize++;
		// }

		return children.length;
	}

	public boolean hasChildren() {
		// if (headChild != null)
		// return true;
		// else
		// return false;

		if (children != null) {
			return (children.length > 0);
		} else {
			return false;
		}
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
		// return headChild;
		return (DecisionNode) children[0];
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

	public void setChosenPathValue(int chosenPathValue) {
		this.chosenPathValue = chosenPathValue;
	}

	// public DecisionNode getLastSibling() {
	// DecisionNode sib = this;
	//
	// while (sib.getNextSibling() != null) {
	// sib = sib.getNextSibling();
	// }
	//
	// return sib;
	// }

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
			return "Chosen Path Value =" + this.getChosenPathValue();
	}

	public void printChildrenValues() {
		// DecisionNode currentChild = headChild;
		// while (currentChild != null) {
		// System.out.print(currentChild.getChosenPathValue() + ",");
		// currentChild = currentChild.getNextSibling();
		// }

		for (int i = 0; i < this.getChildrenSize(); i++) {
			System.out.print(((DecisionNode) children[i]).getChosenPathValue() + ",");
		}

		System.out.print("\n");
	}

}
