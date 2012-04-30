package chessGUI;

import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import chessBackend.Board;
import chessBackend.Game;
import chessBackend.GameStatus;
import chessBackend.Move;
import chessBackend.Player;
import chessBackend.PlayerContainer;
import chessBackend.Side;

public class ObserverGUI implements Player, BoardGUI, MouseListener {
	private JFrame frame;
	private BoardPanel boardPanel;

	private JButton pauseButton;
	private JButton resetButton;
	private JButton undoButton;
	private JButton redoButton;

	private PlayerContainer game;
	private int gameNum;
	private boolean paused;

	public ObserverGUI(PlayerContainer game, boolean debug) {
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
		
		gameNum = 0;

		boardPanel = new BoardPanel(this, debug);
		frame.add(boardPanel, BorderLayout.CENTER);
		frame.add(controlPanel, BorderLayout.NORTH);
		frame.pack();

	}

	public void newGame(Board board) {
		gameNum++;
		frame.setTitle(getFrameTitle() + " Game Number #"  + gameNum);
		boardPanel.newGame(board);
	}
	
	public synchronized void stop(){
		
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
	public boolean moveMade(Move opponentsMove) {
		return boardPanel.moveMade(opponentsMove);
	}

	@Override
	public Move undoMove() {
		return boardPanel.undoMove();
	}

	@Override
	public void makeRecommendation() {

	}
	
	public void makeMove(){
		
	}

	public void setGame(PlayerContainer game) {
		this.game = game;
		paused = game.isPaused();
		undoButton.setEnabled(paused);
		redoButton.setEnabled(paused);
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		if (arg0.getSource() == resetButton) {
			game.newGame(null,false);
		}

		if (arg0.getSource() == pauseButton) {
			this.pause();
			game.pause();
		}

		if (arg0.getSource() == undoButton) {
			if (boardPanel.canUndo() && paused) {
				//boardPanel.undoMove();
				game.undoMove();
			}
		}

		if (arg0.getSource() == redoButton) {
			if (boardPanel.canRedo() && paused) {
				game.makeMove(boardPanel.redoMove());
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
	
	public Board getBoard(){
		return boardPanel.getBoard();
	}

	@Override
	public GameStatus getGameStatus() {
		return boardPanel.getBoard().getBoardStatus();
	}

}
