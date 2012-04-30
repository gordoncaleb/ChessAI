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

import chessBackend.Board;
import chessBackend.GameStatus;
import chessBackend.Move;
import chessBackend.Player;
import chessBackend.PlayerContainer;
import chessIO.MoveBook;

public class MoveBookBuilderGUI implements Player, BoardGUI, MouseListener{
	private JFrame frame;
	private PlayerContainer game;
	private boolean paused;
	
	private BoardPanel boardPanel;
	private JList<Move> moveList;
	private DefaultListModel<Move> listModel;
	private JButton recordBtn;
	private JButton deleteEntryBtn;
	private JButton undoBtn;
	
	private MoveBook moveBook;
	private boolean record;
	
	
	public MoveBookBuilderGUI(PlayerContainer game){
		
		this.game = game;
		this.moveBook = new MoveBook();
		moveBook.loadVerboseMoveBook("moveBook.xml");
		this.record = false;

		frame = new JFrame("Move Book Builder");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());

		JPanel eastPanel = new JPanel(new BorderLayout());
		listModel = new DefaultListModel<Move>();
		moveList = new JList<Move>(listModel);
		moveList.setPreferredSize(new Dimension(200,100));
		moveList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		moveList.setLayoutOrientation(JList.VERTICAL);
		moveList.setVisibleRowCount(-1);
		
		JScrollPane scroll = new JScrollPane(moveList);
		
		eastPanel.add(scroll, BorderLayout.CENTER);
		
		JPanel controlBtnsPanel = new JPanel();
		
		recordBtn = new JButton("Record");
		recordBtn.addMouseListener(this);
		
		deleteEntryBtn = new JButton("Delete");
		undoBtn = new JButton("Undo");
		
		controlBtnsPanel.add(recordBtn);
		controlBtnsPanel.add(deleteEntryBtn);
		controlBtnsPanel.add(undoBtn);
		
		eastPanel.add(controlBtnsPanel,BorderLayout.SOUTH);

		// frame.setSize(gameWidth, gameHeight);
		frame.setResizable(false);
		frame.setVisible(true);

		boardPanel = new BoardPanel(this, false);
		frame.add(boardPanel, BorderLayout.CENTER);
		frame.add(eastPanel,BorderLayout.EAST);
		//frame.add(controlPanel, BorderLayout.NORTH);
		frame.pack();
		
	}
	
	private void populateMoveList(){
		listModel.removeAllElements();

		Vector<Move> moves = moveBook.getAllRecommendations(boardPanel.getBoard().getHashCode());
		
		for(int i=0;i<moves.size();i++){
			listModel.addElement(moves.elementAt(i));
		}
		
	}

	@Override
	public Move undoMove() {
		return boardPanel.undoMove();
	}

	@Override
	public void newGame(Board board) {
		boardPanel.newGame(board);
		
		populateMoveList();
		
	}

	@Override
	public void setGame(PlayerContainer game) {
		this.game = game;
		
	}

	@Override
	public void makeRecommendation() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean moveMade(Move move) {
		boardPanel.moveMade(move);
		
		populateMoveList();
		
		return true;
	}

	@Override
	public void makeMove() {
		
		
		boardPanel.makeMove();
	}

	@Override
	public void makeMove(Move move) {
		
		if(record){
			moveBook.addEntry(boardPanel.getBoard().toXML(false), move);
		}
		
		game.makeMove(move);
		
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
		
		if(e.getSource() == recordBtn){
			record = !record;
			if(record){
				recordBtn.setText("Stop");
			}else{
				recordBtn.setText("Record");
			}
		}
		
		if(e.getSource() == undoBtn){
			if(boardPanel.canUndo()){
				game.undoMove();
			}
		}
		
		if(e.getSource() == deleteEntryBtn){
			Move move = moveList.getSelectedValue();
			moveBook.removeEntry(boardPanel.getBoard().toXML(false), move);
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

}
