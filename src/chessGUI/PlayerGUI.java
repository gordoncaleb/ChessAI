package chessGUI;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import chessBackend.Board;
import chessBackend.Game;
import chessBackend.Move;
import chessBackend.Player;
import chessBackend.Side;

public class PlayerGUI implements Player, BoardGUI, ActionListener {
	private JFrame frame;
	private JMenuItem newGameMenu;
	private JMenuItem undoUserMoveMenu;
	private JMenuItem switchSidesMenu;
	private BoardPanel boardPanel;

	private Game game;
	private Side playerSide;

	public PlayerGUI(Game game, boolean debug) {
		this.game = game;

		frame = new JFrame("Oh,Word? " + Game.VERSION);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());

		JMenuBar menuBar = new JMenuBar();
		JMenu gameMenu = new JMenu("Game");
		gameMenu.setMnemonic(KeyEvent.VK_G);

		newGameMenu = new JMenuItem("New Game");
		newGameMenu.addActionListener(this);
		gameMenu.add(newGameMenu);

		undoUserMoveMenu = new JMenuItem("Undo Last Move");
		undoUserMoveMenu.addActionListener(this);
		gameMenu.add(undoUserMoveMenu);

		switchSidesMenu = new JMenuItem("Switch Side");
		switchSidesMenu.addActionListener(this);
		gameMenu.add(switchSidesMenu);

		menuBar.add(gameMenu);
		frame.setJMenuBar(menuBar);

		// frame.setSize(gameWidth, gameHeight);
		frame.setResizable(false);
		frame.setVisible(true);

		boardPanel = new BoardPanel(this, debug);
		frame.add(boardPanel, BorderLayout.CENTER);
		frame.pack();

	}

	public synchronized Side newGame(Side playerSide, Board board) {

		if (playerSide == null) {

			Object[] options = { "White", "Black" };
			int n = JOptionPane.showOptionDialog(frame, "Wanna play as black or white?", "New Game", JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

			if (n == JOptionPane.YES_OPTION) {
				this.playerSide = Side.WHITE;

			} else {
				this.playerSide = Side.BLACK;
			}

		}else{
			this.playerSide = playerSide;
		}

		boardPanel.newGame(this.playerSide, board);

		return this.playerSide;

	}
	
	public void gameOverLose(){
		Object[] options = { "Yes, please", "Nah" };
		int n = JOptionPane.showOptionDialog(frame, "You just got schooled homie.\nWanna try again?", "Ouch!", JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

		if (n == JOptionPane.YES_OPTION) {
			game.newGame();
		} else {
			System.exit(0);
		}
		
	}
	public void gameOverWin(){
		Object[] options = { "Yeah, why not?", "Nah." };
		int n = JOptionPane.showOptionDialog(frame, "Nicely done boss.\nWanna rematch?", "Ouch!", JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

		if (n == JOptionPane.YES_OPTION) {
			game.newGame();
		} else {
			System.exit(0);
		}
		
	}
	public void gameOverStaleMate(){
		Object[] options = { "Yes, please", "Nah, maybe later." };
		int n = JOptionPane.showOptionDialog(frame, "Stalemate...hmmm close call.\nWanna try again?", "", JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
		if (n == JOptionPane.YES_OPTION) {
			game.newGame();
		} else {
			System.exit(0);
		}
	}
	
	@Override
	public void makeMove(Move move) {
		game.makeMove(move);
	}

	@Override
	public synchronized boolean opponentMoved(Move opponentsMove) {
		return boardPanel.makeMove(opponentsMove);
	}

	@Override
	public boolean undoMove() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Move getRecommendation() {
		return null;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource() == newGameMenu) {
			game.newGame();
		}

		if (arg0.getSource() == undoUserMoveMenu) {
			game.undoMove(playerSide);
		}

		if (arg0.getSource() == switchSidesMenu) {

		}

	}

	@Override
	public Side getSide() {
		return playerSide;
	}

	@Override
	public void setSide(Side side) {
		// TODO Auto-generated method stub
		
	}
	
	public boolean isMyTurn(){
		return boardPanel.isMyTurn();
	}

}
