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

	Game game;
	BoardGUI gui;

	// debug components
	private boolean debug;
	private JFrame debugFrame;
	private JTree decisionTreeGUI;
	private DefaultMutableTreeNode rootGUI;
	private JScrollPane treeView;
	private DecisionNode rootDecision;

	public DecisionTreeGUI(Game game, BoardGUI gui) {
		this.game = game;
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
		DecisionNode child;

		branchGUI.setUserObject(branch);

		child = branch.getHeadChild();
		for (int i = 0; i < branch.getChildrenSize(); i++) {
			childGUI = new DefaultMutableTreeNode(child);
			branchGUI.add(childGUI);

			if (child.hasChildren() && level < 5) {
				buildDecisionTreeGUI(childGUI, child, level + 1);
			}

			child = child.getNextSibling();
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

		if (nodeInfo instanceof DecisionNode) {
			DecisionNode branch = ((DecisionNode) nodeInfo);

			if (((int) arg0.getKeyChar()) == 10) {
				System.out.println("Enter pressed on " + nodeInfo.toString());
				game.growBranch(branch);
				setRootDecisionTree(rootDecision);

			} else {
				if (arg0.isShiftDown() && arg0.isActionKey()) {
					gui.setBoard(branch.getBoard());
					// showDecisionTreeScores(((DecisionNode) nodeInfo));
				}
			}
		}
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

		if (nodeInfo instanceof DecisionNode) {
			DecisionNode branch = ((DecisionNode) nodeInfo);

			if (((int) arg0.getKeyChar()) == 10) {
				System.out.println("Enter pressed on " + nodeInfo.toString());
				game.growBranch(branch);
				setRootDecisionTree(rootDecision);

			} else {
				if (arg0.isShiftDown() && arg0.isActionKey()) {
					gui.setBoard(branch.getBoard());
					// showDecisionTreeScores(((DecisionNode) nodeInfo));
				}
			}
		}

	}

	// Listener Methods
	public void mouseClicked(MouseEvent arg0) {

		if (arg0.getComponent() == decisionTreeGUI) {

			if (debug) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) decisionTreeGUI.getLastSelectedPathComponent();

				if (node == null || !arg0.isShiftDown()) {
					return;
				}

				Object nodeInfo = node.getUserObject();

				if (nodeInfo instanceof DecisionNode) {
					gui.setBoard(((DecisionNode) nodeInfo).getBoard());
				}
			}
		}
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
