package com.gordoncaleb.chess.gui;

import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.gordoncaleb.chess.backend.Board;
import com.gordoncaleb.chess.backend.GameStatus;
import com.gordoncaleb.chess.backend.Player;
import com.gordoncaleb.chess.backend.PlayerContainer;
import com.gordoncaleb.chess.backend.Side;
import com.gordoncaleb.chess.io.FileIO;

public class ObserverGUI implements Player, BoardGUI, MouseListener {
	private JFrame frame;
	private BoardPanel boardPanel;

	private JButton pauseButton;
	private JButton resetButton;
	private JButton undoButton;
	private JButton redoButton;

	private JButton saveButton;

	private JButton showSettingsBtn;

	private PlayerContainer game;
	private boolean paused;

	private JFileChooser fc = new JFileChooser();

	public ObserverGUI(PlayerContainer game, boolean debug) {
		this.game = game;

		fc.setCurrentDirectory(new File("."));

		frame = new JFrame("Observer");
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

		saveButton = new JButton("Save");
		saveButton.addMouseListener(this);
		controlPanel.add(saveButton, BorderLayout.EAST);

		showSettingsBtn = new JButton("AI Settings");
		showSettingsBtn.addMouseListener(this);
		controlPanel.add(showSettingsBtn, BorderLayout.EAST);

		// frame.setSize(gameWidth, gameHeight);
		frame.setResizable(false);
		frame.setVisible(true);

		boardPanel = new BoardPanel(this, debug);
		frame.add(boardPanel, BorderLayout.CENTER);
		frame.add(controlPanel, BorderLayout.NORTH);
		frame.pack();

	}

	public void newGame(Board board) {
		// frame.setTitle(getFrameTitle() + " Game Number #" + gameNum);
		boardPanel.newGame(board);
	}

	public synchronized void stop() {

	}

	public void gameOverLose() {
	}

	public void gameOverWin() {
	}

	public void gameOverDraw() {
	}

	@Override
	public void makeMove(long move) {
		game.makeMove(move);
	}

	@Override
	public boolean moveMade(long opponentsMove) {
		return boardPanel.moveMade(opponentsMove);
	}

	@Override
	public long undoMove() {
		return boardPanel.undoMove();
	}

	public void makeMove() {

	}

	public void setGame(PlayerContainer game) {
		this.game = game;
		paused = game.isPaused();
		undoButton.setEnabled(paused);
		redoButton.setEnabled(paused);
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
		if (arg0.getSource() == resetButton) {
			game.newGame(null, false);
		}

		if (arg0.getSource() == pauseButton) {
			// this.pause();
			game.pause();
		}

		if (arg0.getSource() == undoButton) {
			if (boardPanel.canUndo() && paused) {
				// boardPanel.undoMove();
				game.undoMove();
			}
		}

		if (arg0.getSource() == redoButton) {
			if (boardPanel.canRedo() && paused) {
				boardPanel.redoMove();
			}
		}

		if (arg0.getSource() == saveButton) {

			int returnVal = fc.showSaveDialog(frame);

			if (returnVal == JFileChooser.APPROVE_OPTION) {

				FileIO.writeFile(fc.getSelectedFile().getPath(), boardPanel.getBoard().toXML(true), false);

			}
		}

		if (arg0.getSource() == showSettingsBtn) {
			new AISettingsGUI("Observer AI Settings");
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
	public void pause() {
		paused = !paused;

		if (paused) {
			pauseButton.setText(">");
		} else {
			pauseButton.setText("||");
		}
	}

	public void setFrameTitle(String title) {
		frame.setTitle(title);
	}

	public Board getBoard() {
		return boardPanel.getBoard();
	}

	@Override
	public GameStatus getGameStatus() {
		return boardPanel.getBoard().getBoardStatus();
	}

	@Override
	public String getVersion() {
		return "Observer";
	}

	@Override
	public String getPlayerName(Side side) {
		if (game != null) {
			return game.getPlayerName(side);
		} else {
			return "";
		}
	}

	@Override
	public long getPlayerTime(Side side) {
		if (game != null) {
			return game.getPlayerTime(side);
		} else {
			return 0;
		}
	}

	@Override
	public void endGame() {
		// TODO Auto-generated method stub

	}

	@Override
	public void gameOver(int winlose) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void showProgress(int progress) {
		boardPanel.showProgress(progress);
	}

	@Override
	public void requestRecommendation() {

	}

	@Override
	public void recommendationMade(long move) {
	}

}
