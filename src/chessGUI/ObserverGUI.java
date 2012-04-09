package chessGUI;

import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JFrame;

import chessBackend.Board;
import chessBackend.Game;
import chessBackend.Move;
import chessBackend.Player;
import chessBackend.Side;

public class ObserverGUI implements Player, BoardGUI, MouseListener {
	private JFrame frame;
	private BoardPanel boardPanel;
	
	private JButton pauseButton;
	private JButton resetButton;

	private Game game;
	private Side playerSide;

	public ObserverGUI(Game game, boolean debug) {
		this.game = game;

		
		frame = new JFrame("Oh,Word? " + Game.VERSION);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		
		pauseButton = new JButton("Pause");
		pauseButton.addMouseListener(this);
		frame.add(pauseButton,BorderLayout.NORTH);
		
		resetButton = new JButton("Reset");
		resetButton.addMouseListener(this);
		frame.add(resetButton,BorderLayout.SOUTH);

		// frame.setSize(gameWidth, gameHeight);
		frame.setResizable(false);
		frame.setVisible(true);

		boardPanel = new BoardPanel(this, debug);
		frame.add(boardPanel, BorderLayout.CENTER);
		frame.pack();

	}

	public synchronized Side newGame(Side playerSide, Board board) {

		boardPanel.newGame(this.playerSide, board);

		return this.playerSide;

	}
	
	public void gameOverLose(){
	}
	public void gameOverWin(){
	}
	public void gameOverStaleMate(){
	}
	
	@Override
	public void makeMove(Move move) {
	}

	@Override
	public synchronized boolean opponentMoved(Move opponentsMove) {
		return boardPanel.makeMove(opponentsMove);
	}

	@Override
	public boolean undoMove() {
		return false;
	}

	@Override
	public Move getRecommendation() {
		return null;
	}

	@Override
	public Side getSide() {
		return playerSide;
	}

	@Override
	public void setSide(Side side) {
		
	}
	
	public boolean isMyTurn(){
		return boardPanel.isMyTurn();
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		if (arg0.getSource() == resetButton) {
			game.newGame();
		}
		
		if (arg0.getSource() == pauseButton) {
			game.pause();
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

