package chessGUI;

import java.awt.BorderLayout;

import javax.swing.JFrame;

import chessBackend.Board;
import chessBackend.Game;
import chessBackend.Move;
import chessBackend.Player;
import chessBackend.Side;

public class ObserverGUI implements Player, BoardGUI {
	private JFrame frame;
	private BoardPanel boardPanel;

	private Game game;
	private Side playerSide;

	public ObserverGUI(Game game, boolean debug) {
		this.game = game;

		frame = new JFrame("Oh,Word? " + Game.VERSION);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());

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

}

