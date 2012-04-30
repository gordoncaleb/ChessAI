package chessGUI;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

import chessBackend.Move;
import chessIO.MoveBook;

public class MoveBookGUI implements KeyListener, MouseListener {
	private JFrame moveBookGUIFrame;
	private JTree moveBookTree;
	private DefaultMutableTreeNode moveBookRootGUI;
	private MoveBook moveBook;


	private JTextField fromTxt;
	private JTextField toTxt;
	private JTextField valueTxt;
	private JLabel detailsLbl;
	private JButton refreshButton;
	private JButton saveButton;
	private JButton deleteButton;
	private JButton addChildButton;
	private JButton exportFileButton;

	private int numTreeNodes;
	private int maxDepth;

	public MoveBookGUI(MoveBook goodMoveDB) {

		moveBookGUIFrame = new JFrame("Good Move DB Builder");
		moveBookGUIFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		moveBookGUIFrame.setLayout(new BorderLayout());
		moveBookGUIFrame.setSize(800, 800);
		moveBookGUIFrame.setVisible(true);

		moveBookRootGUI = new DefaultMutableTreeNode("Good Moves!");
		moveBookTree = new JTree(moveBookRootGUI);
		moveBookTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		moveBookTree.addKeyListener(this);
		moveBookTree.addMouseListener(this);
		JScrollPane goodMoveTreeView = new JScrollPane(moveBookTree);
		// goodMoveTreeView.setPreferredSize(new Dimension(700,750));
		moveBookGUIFrame.add(goodMoveTreeView, BorderLayout.CENTER);

		JPanel editPanel = new JPanel(new FlowLayout());
		editPanel.setPreferredSize(new Dimension(150, 100));
		// editPanel.setBackground(Color.blue);
		JPanel fromPanel = new JPanel(new FlowLayout());
		JLabel fromLbl = new JLabel("From: ");
		fromPanel.add(fromLbl, BorderLayout.LINE_START);
		fromTxt = new JTextField("", 5);
		fromPanel.add(fromTxt, BorderLayout.CENTER);
		editPanel.add(fromPanel);

		JPanel toPanel = new JPanel(new BorderLayout());
		JLabel toLbl = new JLabel("To: ");
		toPanel.add(toLbl, BorderLayout.LINE_START);
		toTxt = new JTextField("", 5);
		toPanel.add(toTxt, BorderLayout.CENTER);
		editPanel.add(toPanel);

		JPanel valuePanel = new JPanel(new BorderLayout());
		JLabel valueLbl = new JLabel("Value: ");
		valuePanel.add(valueLbl, BorderLayout.LINE_START);
		valueTxt = new JTextField("", 5);
		valuePanel.add(valueTxt, BorderLayout.CENTER);
		editPanel.add(valuePanel);

		saveButton = new JButton("Save");
		saveButton.addMouseListener(this);
		deleteButton = new JButton("Delete");
		deleteButton.addMouseListener(this);
		addChildButton = new JButton("Add Move");
		addChildButton.addMouseListener(this);
		exportFileButton = new JButton("Export To File");
		exportFileButton.addMouseListener(this);
		editPanel.add(saveButton);
		editPanel.add(deleteButton);
		editPanel.add(addChildButton);
		editPanel.add(exportFileButton);

		moveBookGUIFrame.add(editPanel, BorderLayout.LINE_END);

		JPanel detailsPanel = new JPanel(new BorderLayout());
		// detailsPanel.setBackground(Color.red);
		detailsLbl = new JLabel("#Nodes and Deepest Level");
		detailsPanel.add(detailsLbl, BorderLayout.LINE_START);
		refreshButton = new JButton("Refresh Tree");
		refreshButton.addMouseListener(this);
		detailsPanel.add(refreshButton, BorderLayout.LINE_END);
		moveBookGUIFrame.add(detailsPanel, BorderLayout.PAGE_END);

		setMoveBook(goodMoveDB);
	}

	public void setMoveBook(MoveBook moveBook) {
		this.moveBook = moveBook;
		refreshTree();
	}


	private void refreshTree() {

	}

	private String getTreeDetails() {
		return "";
	}

	
	private int getValueFromString(String value) throws Exception{
		return Integer.parseInt(value);
	}
	
	private Move getMoveFromString(String from, String to) throws Exception{
		
		String[] fromValues = from.split(",");
		String[] toValues = to.split(",");
		
		if(fromValues.length != 2 || toValues.length != 2){
			throw new Exception();
		}
		
		int fromRow = Integer.parseInt(fromValues[0]);
		int fromCol = Integer.parseInt(fromValues[1]);
		int toRow = Integer.parseInt(toValues[0]);
		int toCol = Integer.parseInt(toValues[1]);
		
		if(fromRow >= 0 && fromRow < 8 && fromCol >= 0 && fromCol < 8 && toRow >= 0 && toRow < 8 && toCol >= 0 && toCol < 8 ){
			return new Move(fromRow,fromCol,toRow,toCol);
		}else{
			throw new Exception();
		}
	}

	
	private void exportToFile(){
		moveBook.saveMoveBook();
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
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
