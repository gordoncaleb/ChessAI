package com.gordoncaleb.chess.engine.legacy;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;

import com.gordoncaleb.chess.board.Move;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DecisionNode implements Comparable<DecisionNode> {
    public static Logger logger = LoggerFactory.getLogger(DecisionNode.class);

    private Object[] children;

    private Move move;

    private int chosenPathValue;

    public DecisionNode(Move move) {
        this(move, 0);
    }

    public DecisionNode(Move move, int chosenPathValue) {

        // Linked List data struct pointers
        // this.headChild = null;
        // this.nextSibling = null;
        //
        // this.tailChild = null;
        // this.previousSibling = null;

        children = null;

        this.chosenPathValue = chosenPathValue;
        // bound = ValueBounds.NA;

        this.move = move;
    }

    @Override
    public int compareTo(DecisionNode o) {
        return this.chosenPathValue - o.getChosenPathValue();
    }

    public void setHeadChild(Move m) {
        for (int i = 0; i < children.length; i++) {
            DecisionNode node = (DecisionNode) children[i];
            if (node.getMove() == m) {
                Object a = children[0];
                children[0] = node;
                children[i] = a;
                break;
            }
        }
    }

    public boolean equals(DecisionNode o) {
        return (this.chosenPathValue == o.getChosenPathValue());
    }

    public synchronized void sort(int toIndex) {
        Arrays.sort(children, 0, toIndex, Collections.reverseOrder());
    }

    public void setChildren(Object[] children) {
        this.children = children;
    }

    public DecisionNode getChild(int i) {
        return (DecisionNode) children[i];
    }

    public DecisionNode getHeadChild() {
        return (DecisionNode) children[0];
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
        return (hasBeenVisited() && (children.length == 0));
    }

    public boolean hasBeenVisited() {
        return (children != null);
    }

    public Move getMove() {
        return move;
    }

    public void setMove(Move nodeMove) {
        this.move = nodeMove;
    }

    public int getChosenPathValue() {
        return chosenPathValue;
    }

    public void setChosenPathValue(int chosenPathValue) {
        this.chosenPathValue = chosenPathValue;
    }

    public String toString() {
        String me = "Chosen Path Value = " + " " + this.getChosenPathValue();

        if (move != null) {
            me += " " + move.toString();
        }

        if (isGameOver()) {
            me += " GAMEOVER";
        }

        // me += "(" + alpha + "," + beta + ")";

        return me;

    }

}
