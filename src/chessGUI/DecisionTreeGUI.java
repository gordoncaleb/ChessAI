package chessGUI;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

import chessAI.DecisionNode;
import chessBackend.Game;

public class DecisionTreeGUI implements KeyListener, MouseListener {
	BoardPanel gui;

	// debug components
	private boolean debug;
	private JFrame debugFrame;
	private JTree decisionTreeGUI;
	private DefaultMutableTreeNode rootGUI;
	private JScrollPane treeView;
	private DecisionNode rootDecision;

	public DecisionTreeGUI( BoardPanel gui) {
		this.gui = gui;

		debugFrame = new JFrame("Decision Tree Observer");
		debugFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		debugFrame.setLayout(new BorderLayout(1, 1));
		debugFrame.setSize(800, 800);
		debugFrame.setVisible(true);
		buildDebugGUI();

	}

	private void buildDebugGUI() {
		rootGUI = new DefaultMutableTreeNode("Current Board");
		decisionTreeGUI = new JTree(rootGUI);
		decisionTreeGUI.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		decisionTreeGUI.addKeyListener(this);
		decisionTreeGUI.addMouseListener(this);
		treeView = new JScrollPane(decisionTreeGUI);
		debugFrame.add(treeView, BorderLayout.CENTER);
	}

	public void setRootDecisionTree(DecisionNode rootDecision) {
		this.rootDecision = rootDecision;
		rootGUI.removeAllChildren();
		buildDecisionTreeGUI(rootGUI, rootDecision, 0);
		decisionTreeGUI.updateUI();
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
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}
}
