package chessGUI;

import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

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
	private JButton undoButton;
	private JButton redoButton;

	private Game game;
	private Side playerSide;
	private boolean paused;

	public ObserverGUI(Game game, boolean debug) {
		this.game = game;

		frame = new JFrame(getFrameTitle());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());

		resetButton = new JButton("Reset");
		resetButton.addMouseListener(this);
		frame.add(resetButton, BorderLayout.SOUTH);

		JPanel controlPanel = new JPanel();

		undoButton = new JButton("|<");
		undoButton.addMouseListener(this);
		controlPanel.add(undoButton, BorderLayout.WEST);

		pauseButton = new JButton("||");
		pauseButton.addMouseListener(this);
		controlPanel.add(pauseButton, BorderLayout.CENTER);

		redoButton = new JButton(">|");
		redoButton.addMouseListener(this);
		controlPanel.add(redoButton, BorderLayout.EAST);

		// frame.setSize(gameWidth, gameHeight);
		frame.setResizable(false);
		frame.setVisible(true);

		boardPanel = new BoardPanel(this, debug);
		frame.add(boardPanel, BorderLayout.CENTER);
		frame.add(controlPanel, BorderLayout.NORTH);
		frame.pack();

	}

	public synchronized Side newGame(Side playerSide, Board board) {

		boardPanel.newGame(this.playerSide, board);

		return this.playerSide;

	}

	public void gameOverLose() {
	}

	public void gameOverWin() {
	}

	public void gameOverStaleMate() {
	}

	@Override
	public void makeMove(Move move) {
	}

	@Override
	public synchronized boolean moveMade(Move opponentsMove) {
		return boardPanel.makeMove(opponentsMove);
	}

	@Override
	public Move undoMove() {
		return boardPanel.undoMove();
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

	public void setGame(Game game) {
		this.game = game;
		paused = game.isPaused();
		undoButton.setEnabled(paused);
		redoButton.setEnabled(paused);
	}

	public boolean isMyTurn() {
		return boardPanel.isMyTurn();
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		if (arg0.getSource() == resetButton) {
			game.newGame();
		}

		if (arg0.getSource() == pauseButton) {
			this.pause();
			game.pause(this);
		}

		if (arg0.getSource() == undoButton) {
			if (boardPanel.canUndo() && paused) {
				boardPanel.undoMove();
				game.undoMove(this);
			}
		}

		if (arg0.getSource() == redoButton) {
			if (boardPanel.canRedo() && paused) {
				game.makeMove(boardPanel.redoMove(), this);
			}
		}

		if (paused) {
			undoButton.setEnabled(boardPanel.canUndo());
			redoButton.setEnabled(boardPanel.canRedo());
		} else {
			undoButton.setEnabled(false);
			redoButton.setEnabled(false);
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

	@Override
	public void pause() {
		paused = !paused;

		if (paused) {
			pauseButton.setText(">");
		} else {
			pauseButton.setText("||");
		}

		frame.setTitle(getFrameTitle());
	}

	private String getFrameTitle() {
		if (paused) {
			return "Oh,Word? Observer" + Game.VERSION + " (**PAUSED**)";
		} else {
			return "Oh,Word? Observer" + Game.VERSION;
		}
	}

}
