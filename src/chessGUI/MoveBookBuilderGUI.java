package chessGUI;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import chessAI.AI;
import chessBackend.Board;
import chessBackend.Game;
import chessBackend.GameStatus;
import chessBackend.Move;
import chessBackend.Player;
import chessBackend.PlayerContainer;
import chessBackend.Side;
import chessIO.FileIO;
import chessIO.MoveBook;

public class MoveBookBuilderGUI implements Player, BoardGUI, MouseListener {
	private JFrame frame;
	private AI ai;
	private boolean paused;

	private BoardPanel boardPanel;
	private JList<Move> moveList;
	private DefaultListModel<Move> listModel;
	private JButton recordBtn;
	private JButton deleteEntryBtn;
	private JButton undoBtn;
	private JButton saveBtn;
	private JButton recommendBtn;
	private JButton aiRecommendBtn;
	private JButton freelyMoveBtn;
	private JButton clearHashTableBtn;
	
	private DecisionTreeGUI dtgui;

	private MoveBook moveBook;
	private boolean record;

	public static void main(String[] args) {
		FileIO.setLogEnabled(false);

		java.util.Hashtable<Side, Player> players = new java.util.Hashtable<Side, Player>();

		MoveBookBuilderGUI mbBuilder = new MoveBookBuilderGUI();

		players.put(Side.BOTH, mbBuilder);

		Game game = new Game(players);

		mbBuilder.setGame(game);

		game.newGame(Game.getDefaultBoard(), false);
	}

	public MoveBookBuilderGUI() {

		this.ai = new AI(null,true);
		this.moveBook = new MoveBook();
		moveBook.loadVerboseMoveBook();
		this.record = false;

		frame = new JFrame("Move Book Builder");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());

		JPanel eastPanel = new JPanel(new BorderLayout());
		listModel = new DefaultListModel<Move>();
		moveList = new JList<Move>(listModel);
		moveList.setPreferredSize(new Dimension(300, 100));
		moveList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		moveList.setLayoutOrientation(JList.VERTICAL);
		moveList.setVisibleRowCount(-1);
		moveList.addMouseListener(this);

		JScrollPane scroll = new JScrollPane(moveList);

		eastPanel.add(scroll, BorderLayout.CENTER);

		JPanel controlBtnsPanel = new JPanel();

		controlBtnsPanel.setPreferredSize(new Dimension(300, 100));

		recordBtn = new JButton("Record");
		recordBtn.addMouseListener(this);

		deleteEntryBtn = new JButton("Delete");
		deleteEntryBtn.addMouseListener(this);

		undoBtn = new JButton("Undo");
		undoBtn.addMouseListener(this);

		saveBtn = new JButton("Save");
		saveBtn.addMouseListener(this);

		recommendBtn = new JButton("Recommend");
		recommendBtn.addMouseListener(this);
		
		aiRecommendBtn = new JButton("AI Recommend");
		aiRecommendBtn.addMouseListener(this);
				
		freelyMoveBtn = new JButton("Free Move?");
		freelyMoveBtn.addMouseListener(this);
		
		clearHashTableBtn = new JButton("Clear Hashtable");
		clearHashTableBtn.addMouseListener(this);

		controlBtnsPanel.add(recordBtn);
		controlBtnsPanel.add(deleteEntryBtn);
		controlBtnsPanel.add(undoBtn);
		controlBtnsPanel.add(saveBtn);
		controlBtnsPanel.add(recommendBtn);
		controlBtnsPanel.add(aiRecommendBtn);
		controlBtnsPanel.add(freelyMoveBtn);
		controlBtnsPanel.add(clearHashTableBtn);

		eastPanel.add(controlBtnsPanel, BorderLayout.SOUTH);

		// frame.setSize(gameWidth, gameHeight);
		frame.setResizable(false);
		frame.setVisible(true);

		boardPanel = new BoardPanel(this, false);
		frame.add(boardPanel, BorderLayout.CENTER);
		frame.add(eastPanel, BorderLayout.EAST);
		// frame.add(controlPanel, BorderLayout.NORTH);
		frame.pack();
		
		dtgui = new DecisionTreeGUI(boardPanel);

	}

	private void populateMoveList() {
		listModel.removeAllElements();

		Vector<Move> moves = moveBook.getAllRecommendations(boardPanel.getBoard().getHashCode());

		if (moves != null) {
			for (int i = 0; i < moves.size(); i++) {
				listModel.addElement(moves.elementAt(i));
			}
		}

	}

	@Override
	public Move undoMove() {
		Move undone = boardPanel.undoMove();
		ai.undoMove();
		populateMoveList();
		return undone;
	}

	@Override
	public void newGame(Board board) {
		boardPanel.newGame(board.getCopy());
		ai.newGame(board.getCopy());

		populateMoveList();

	}

	@Override
	public void setGame(PlayerContainer game) {


	}

	@Override
	public Move makeRecommendation() {
		return null;
	}

	@Override
	public boolean moveMade(Move move) {
		boardPanel.moveMade(move);
		ai.moveMade(move);

		populateMoveList();
		
		boardPanel.makeMove();

		return true;
	}

	@Override
	public void makeMove() {

		boardPanel.makeMove();
	}

	@Override
	public void makeMove(Move move) {

		if (record) {
			moveBook.addEntry(boardPanel.getBoard().toXML(false), boardPanel.getBoard().getHashCode(), move);
		}

		this.moveMade(move);

	}

	@Override
	public void pause() {
		this.paused = !paused;

	}

	@Override
	public GameStatus getGameStatus() {
		return boardPanel.getBoard().getBoardStatus();
	}

	@Override
	public Board getBoard() {
		return boardPanel.getBoard();
	}

	@Override
	public void gameOverLose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void gameOverWin() {
		// TODO Auto-generated method stub

	}

	@Override
	public void gameOverStaleMate() {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseClicked(MouseEvent e) {

		if (e.getSource() == recordBtn) {
			record = !record;
			if (record) {
				recordBtn.setText("Stop");
			} else {
				recordBtn.setText("Record");
			}
		}

		if (e.getSource() == undoBtn) {
			if (boardPanel.canUndo()) {
				this.undoMove();
			}
		}

		if (e.getSource() == deleteEntryBtn) {
			Move move = moveList.getSelectedValue();
			moveBook.removeEntry(boardPanel.getBoard().toXML(false), boardPanel.getBoard().getHashCode(), move);
			populateMoveList();
		}

		if (e.getSource() == saveBtn) {
			moveBook.saveMoveBook();
		}

		if (e.getSource() == recommendBtn) {
			Move rec = moveBook.getRecommendation(boardPanel.getBoard().getHashCode());

			if (rec != null) {
				boardPanel.highlightMove(rec);
			}
		}
		
		if (e.getSource() == aiRecommendBtn) {
			
			if(boardPanel.isFreelyMove()){
				ai.newGame(boardPanel.getBoard().getCopy());
			}
			
			Move rec = ai.makeRecommendation();
			
			dtgui.setRootDecisionTree(ai.getRootNode());

			if (rec != null) {
				boardPanel.highlightMove(rec);
			}
		}

		if (e.getSource() == moveList) {
			Move move = moveList.getSelectedValue();
			boardPanel.highlightMove(move);
		}
		
		if(e.getSource() == freelyMoveBtn){
			
			if(boardPanel.isFreelyMove()){
				ai.newGame(boardPanel.getBoard().getCopy());
			}
			
			boardPanel.setFreelyMove(!boardPanel.isFreelyMove());
			
			if(boardPanel.isFreelyMove()){
				freelyMoveBtn.setText("Moving Freely");
			}else{
				freelyMoveBtn.setText("Free Move?");
				populateMoveList();
			}
		}
		
		if (e.getSource() == clearHashTableBtn) {
			ai.cleanHashTable();
		}
		
		

	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getVersion() {
		return "MoveBookBuilder";
	}

}
