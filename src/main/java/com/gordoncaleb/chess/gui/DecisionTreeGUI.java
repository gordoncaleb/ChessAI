package com.gordoncaleb.chess.gui;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

import com.gordoncaleb.chess.ai.AI;
import com.gordoncaleb.chess.ai.DecisionNode;
import com.gordoncaleb.chess.backend.Player;
import com.gordoncaleb.chess.pieces.Values;

public class DecisionTreeGUI implements KeyListener, MouseListener {
	Player gui;

	// debug components
	private boolean debug;
	private JFrame debugFrame;
	private JPanel ctrlPanel;
	private JCheckBox enableStepThrough;
	private JButton refreshBtn;

	private JTree decisionTreeGUI;
	private DefaultMutableTreeNode rootGUI;
	private JScrollPane treeView;
	private DecisionNode rootDecision;

	private int[] nodeCount = new int[20];
	private int distanceFromRoot = 0;

	public DecisionTreeGUI(Player gui) {
		this.gui = gui;

		debugFrame = new JFrame("Decision Tree Observer");
		// debugFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		debugFrame.setLayout(new BorderLayout());

		rootGUI = new DefaultMutableTreeNode("Current Board");
		decisionTreeGUI = new JTree(rootGUI);
		decisionTreeGUI.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		decisionTreeGUI.addKeyListener(this);
		decisionTreeGUI.addMouseListener(this);
		treeView = new JScrollPane(decisionTreeGUI);

		ctrlPanel = new JPanel();

		enableStepThrough = new JCheckBox("Step Through", false);
		ctrlPanel.add(enableStepThrough);

		refreshBtn = new JButton("Refresh");
		refreshBtn.addMouseListener(this);
		ctrlPanel.add(refreshBtn);

		debugFrame.add(treeView, BorderLayout.CENTER);
		debugFrame.add(ctrlPanel, BorderLayout.SOUTH);
		debugFrame.setSize(800, 800);
		debugFrame.setVisible(true);

	}

	public void setRootDecisionTree(DecisionNode rootDecision) {
		this.rootDecision = rootDecision;
		distanceFromRoot = 0;
		refreshTree();
	}

	private void refreshTree() {
		rootGUI.removeAllChildren();
		resetNodeCount();
		buildDecisionTreeGUI(rootGUI, rootDecision, 0);
		decisionTreeGUI.updateUI();
	}

	private void resetNodeCount() {
		for (int i = 0; i < nodeCount.length; i++) {
			nodeCount[i] = 0;
		}
	}

	private void buildDecisionTreeGUI(DefaultMutableTreeNode branchGUI, DecisionNode branch, int level) {
		DefaultMutableTreeNode childGUI;

		branchGUI.setUserObject(branch);

		for (int i = 0; i < branch.getChildrenSize(); i++) {
			childGUI = new DefaultMutableTreeNode(branch.getChild(i));
			branchGUI.add(childGUI);

			if (branch.getChild(i).hasChildren()) {
				buildDecisionTreeGUI(childGUI, branch.getChild(i), level + 1);
			}

		}
	}
	
	public JFrame getFrame(){
		return debugFrame;
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) decisionTreeGUI.getLastSelectedPathComponent();

		// System.out.println("Key typed");
		if (node == null)
			// Nothing is selected.
			return;

		Object nodeInfo = node.getUserObject();

	}

	@Override
	public void keyTyped(KeyEvent arg0) {
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) decisionTreeGUI.getLastSelectedPathComponent();

		// System.out.println("Key typed");
		if (node == null)
			// Nothing is selected.
			return;

		Object nodeInfo = node.getUserObject();

	}

	// Listener Methods
	public void mouseClicked(MouseEvent arg0) {

	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (e.getSource() == refreshBtn) {

			rootDecision = ((AI) gui).getRootNode();
			refreshTree();
		}

		if (e.getSource() == decisionTreeGUI) {

			if (enableStepThrough.isSelected()) {
				for (int i = 0; i < distanceFromRoot; i++) {
					gui.undoMove();
				}

				distanceFromRoot = 0;

				DefaultMutableTreeNode node = (DefaultMutableTreeNode) decisionTreeGUI.getLastSelectedPathComponent();
				Object[] path = node.getUserObjectPath();

				for (int i = 1; i < path.length; i++) {
					gui.moveMade(((DecisionNode) path[i]).getMove());
					distanceFromRoot++;
				}
				
				System.out.println(Values.printBoardScoreBreakDown(gui.getBoard()));
			}

			

		}


	}
}
