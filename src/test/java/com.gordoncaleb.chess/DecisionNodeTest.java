package com.gordoncaleb.chess;

import com.gordoncaleb.chess.ai.DecisionNode;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;

public class DecisionNodeTest {

    public static Logger logger = LoggerFactory.getLogger(DecisionNodeTest.class);

    @Test
    public void test() {

        ArrayList<Integer> nums = new ArrayList<>();

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
                    logger.error("Sorting error");
                }
            }
        }

        nums.clear();
        root.removeAllChildren();
        nodes.clear();

        // /////////////////////////////////

        logger.debug("Random dataset sort");
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

        logger.debug("Arraylist sort took " + (System.currentTimeMillis() - time));

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

        logger.debug("DecisionNode sort took " + (System.currentTimeMillis() - time));

        logger.debug("Done!");

    }
}
