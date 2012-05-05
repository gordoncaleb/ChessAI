package chessGUI;

import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Hashtable;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import chessAI.AI;
import chessEthernet.EthernetPlayerClient;
import chessBackend.Board;
import chessBackend.Game;
import chessBackend.GameResults;
import chessBackend.GameStatus;
import chessBackend.Move;
import chessBackend.Player;
import chessBackend.PlayerContainer;
import chessBackend.Side;
import chessIO.FileIO;
import chessIO.XMLParser;

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

	public static void main(String[] args) {

		boolean debug = true;
		Game game;
		GameResults results;

		int whiteWins = 0;
		int blackWins = 0;
		int draws = 0;

		long whiteTime = 0;
		long blackTime = 0;

		ObserverGUI observer = new ObserverGUI(null, false);

		// Game game = new Game(GameType.AI_VS_AI);

		Player playerOne = new AI(null, debug);
		// Player playerTwo = new AI(null, debug);

		Player playerTwo = new EthernetPlayerClient();

		System.out.println("Player One: " + playerOne.getVersion());
		System.out.println("Player Two: " + playerTwo.getVersion());

		((AI) playerOne).setUseBook(true);

		Board defaultBoard = XMLParser.XMLToBoard(FileIO.readFile("default.xml"));

		Hashtable<Side, Player> players = new Hashtable<Side, Player>();

		players.put(Side.WHITE, playerOne);
		players.put(Side.BLACK, playerTwo);

		game = new Game(players);

		playerOne.setGame(game);
		playerTwo.setGame(game);
		game.addObserver(observer);

		String stats;
		for (int i = 0; i < 1000; i++) {
			
			stats = "White wins: " + whiteWins + " with " + whiteTime + "ms Black wins: " + blackWins + " with " + blackTime + "ms Draws: " + draws;
			observer.setFrameTitle(stats);
			FileIO.log(stats);
			
			results = game.newGame(defaultBoard, true);

			if (results.getWinner() == Side.WHITE) {
				whiteWins++;
				whiteTime += results.getWinnerTime();
				blackTime += results.getLoserTime();
			} else {
				if (results.getWinner() == Side.BLACK) {
					blackWins++;
					blackTime += results.getWinnerTime();
					whiteTime += results.getLoserTime();
				} else {
					draws++;
				}
			}		

		}

		FileIO.log("Tournament done");
	}

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
		// frame.setTitle(getFrameTitle() + " Game Number #" + gameNum);
		boardPanel.newGame(board);
	}

	public synchronized void stop() {

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
	public Move makeRecommendation() {
		return null;
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
		if (arg0.getSource() == resetButton) {
			game.newGame(null, false);
		}

		if (arg0.getSource() == pauseButton) {
			this.pause();
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

}
