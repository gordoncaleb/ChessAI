package chessAI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import chessBackend.Move;
import chessBackend.ValueBounds;

public class DecisionNode implements Comparable<DecisionNode> {

	private Object[] children;

	private long move;

	private int chosenPathValue;
	private ValueBounds bound;

	private int alpha;
	private int beta;

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

		// functional test
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
		bound = ValueBounds.NA;

		this.move = move;
	}

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

	public void setChildren(Object[] children) {
		this.children = children;
	}

	public Object[] getChildren() {
		return children;
	}

	public void removeAllChildren() {
		children = null;
	}

	public DecisionNode getChild(int i) {
		return (DecisionNode) children[i];
	}

	public void setChild(int i, DecisionNode child) {
		children[i] = child;
	}

	public DecisionNode getHeadChild() {
		return (DecisionNode) children[0];
	}

	public DecisionNode getTailChild() {
		return (DecisionNode) children[children.length];
	}

	public int getChildrenSize() {
		if (children != null) {
			return children.length;
		} else {
			return 0;
		}

	}

	public boolean hasChildren() {
		if (hasBeenVisited()) {
			return (children.length > 0);
		} else {
			return false;
		}
	}

	public boolean isGameOver() {
		if (hasBeenVisited()) {
			return (children.length == 0);
		} else {
			return false;
		}
	}

	public boolean hasBeenVisited() {
		return (children != null);
	}

	public long getMove() {
		return move;
	}

	public void setMove(long nodeMove) {
		this.move = nodeMove;
	}

	public int getMoveValue() {
		if (move != 0)
			return Move.getValue(move);
		else
			return 0;
	}

	public int getChosenPathValue() {
		return chosenPathValue;
	}

	public void setChosenPathValue(int chosenPathValue) {
		this.chosenPathValue = chosenPathValue;
	}

	public ValueBounds getBound() {
		return bound;
	}

	public void setBound(ValueBounds bound) {
		this.bound = bound;
	}

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

	public int getAlpha() {
		return alpha;
	}

	public void setAlpha(int alpha) {
		this.alpha = alpha;
	}

	public int getBeta() {
		return beta;
	}

	public void setBeta(int beta) {
		this.beta = beta;
	}

	public String toString() {
		String me = "Chosen Path Value = " + bound + " " + this.getChosenPathValue();

		if (move != 0) {
			me += " " + Move.toString(move);
		}

		if (isGameOver()) {
			me += " GAMEOVER";
		}

		me += "(" + alpha + "," + beta + ")";

		return me;

	}

	public void printChildrenValues() {

		for (int i = 0; i < this.getChildrenSize(); i++) {
			System.out.print(((DecisionNode) children[i]).getChosenPathValue() + ",");
		}

		System.out.print("\n");
	}

}
