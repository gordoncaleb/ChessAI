package chessGUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import chessAI.AI;
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

public class PlayerGUI implements Player, BoardGUI, ActionListener, MouseListener {
	private JFrame frame;
	private JMenuItem newGameMenu;
	private JMenuItem undoUserMoveMenu;
	private JMenuItem switchSidesMenu;
	private BoardPanel boardPanel;

	private AI ai;

	private JPanel ctrlPanel;
	private JPanel boardCtrlPanel;
	private JPanel aiCtrlPanel;
	private JPanel gameCtrlPanel;

	private JButton loadGameBtn;
	private JButton saveGameBtn;
	private JButton newGameBtn;
	private JButton undoBtn;
	private JButton switchSidesBtn;

	private JButton boardFreeSetupBtn;
	private JButton flipBoardBtn;

	private JButton getAIRecommendationBtn;

	private PlayerContainer game;

	private JFileChooser fc = new JFileChooser();

	public static void main(String[] args) {
		boolean debug = true;

		FileIO.setDebugOutput(true);
		FileIO.setLogEnabled(false);

		PlayerGUI playerOne = new PlayerGUI(null, false);
		AI playerTwo = new AI(null, debug);
		playerTwo.setUseBook(true);

		Hashtable<Side, Player> players = new Hashtable<Side, Player>();

		Side humanSide = playerOne.optionForSide();

		players.put(humanSide, playerOne);
		players.put(humanSide.otherSide(), playerTwo);

		Game game = new Game(players);

		playerOne.setGame(game);
		playerTwo.setGame(game);

		game.newGame(Game.getDefaultBoard(), false);

	}

	public PlayerGUI(PlayerContainer game, boolean debug) {
		this.game = game;

		ai = new AI(null, false);

		frame = new JFrame("Oh,Word? " + AI.version);
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

		ctrlPanel = new JPanel();
		ctrlPanel.setPreferredSize(new Dimension(300, 500));

		boardCtrlPanel = new JPanel();
		boardCtrlPanel.setPreferredSize(new Dimension(300, 100));

		aiCtrlPanel = new JPanel();
		aiCtrlPanel.setPreferredSize(new Dimension(300, 100));

		gameCtrlPanel = new JPanel();
		gameCtrlPanel.setPreferredSize(new Dimension(300, 100));

		gameCtrlPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Game"));
		boardCtrlPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Board"));
		aiCtrlPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "AI"));

		loadGameBtn = new JButton("Load Game");
		loadGameBtn.addMouseListener(this);

		saveGameBtn = new JButton("Save Game");
		saveGameBtn.addMouseListener(this);

		newGameBtn = new JButton("New Game");
		newGameBtn.addMouseListener(this);

		undoBtn = new JButton("Undo");
		undoBtn.addMouseListener(this);

		switchSidesBtn = new JButton("Swith Sides");
		switchSidesBtn.addMouseListener(this);

		boardFreeSetupBtn = new JButton("Board Setup");
		boardFreeSetupBtn.addMouseListener(this);

		flipBoardBtn = new JButton("Flip Board");
		flipBoardBtn.addMouseListener(this);

		getAIRecommendationBtn = new JButton("Reccomendation");
		getAIRecommendationBtn.addMouseListener(this);

		boardCtrlPanel.add(boardFreeSetupBtn);
		boardCtrlPanel.add(flipBoardBtn);

		gameCtrlPanel.add(loadGameBtn);
		gameCtrlPanel.add(saveGameBtn);
		gameCtrlPanel.add(newGameBtn);
		gameCtrlPanel.add(undoBtn);
		gameCtrlPanel.add(switchSidesBtn);

		aiCtrlPanel.add(getAIRecommendationBtn);

		ctrlPanel.add(gameCtrlPanel);
		ctrlPanel.add(boardCtrlPanel);
		ctrlPanel.add(aiCtrlPanel);

		frame.add(ctrlPanel, BorderLayout.EAST);

		// frame.setSize(gameWidth, gameHeight);
		frame.setResizable(false);
		frame.setVisible(true);

		boardPanel = new BoardPanel(this, debug);
		frame.add(boardPanel, BorderLayout.CENTER);
		frame.pack();

	}

	public Side optionForSide() {

		Side playerSide;

		Object[] options = { "White", "Black" };
		int n = JOptionPane.showOptionDialog(frame, "Wanna play as black or white?", "New Game", JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

		if (n == JOptionPane.YES_OPTION) {
			playerSide = Side.WHITE;
			boardPanel.setFlipBoard(false);

		} else {
			playerSide = Side.BLACK;
			boardPanel.setFlipBoard(true);
		}

		return playerSide;
	}

	public synchronized void newGame(Board board) {
		ai.newGame(board);
		boardPanel.newGame(board);

		undoBtn.setEnabled(boardPanel.canUndo());

	}

	public void gameOverLose() {
		Object[] options = { "Yes, please", "Nah" };
		int n = JOptionPane.showOptionDialog(frame, "You just got schooled homie.\nWanna try again?", "Ouch!", JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

		if (n == JOptionPane.YES_OPTION) {
			game.newGame(null, false);
		} else {
			System.exit(0);
		}

	}

	public void gameOverWin() {
		Object[] options = { "Yeah, why not?", "Nah." };
		int n = JOptionPane.showOptionDialog(frame, "Nicely done boss.\nWanna rematch?", "Ouch!", JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

		if (n == JOptionPane.YES_OPTION) {
			game.newGame(null, false);
		} else {
			System.exit(0);
		}

	}

	public void gameOverStaleMate() {
		Object[] options = { "Yes, please", "Nah, maybe later." };
		int n = JOptionPane.showOptionDialog(frame, "Stalemate...hmmm close call.\nWanna try again?", "", JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
		if (n == JOptionPane.YES_OPTION) {
			game.newGame(null, false);
		} else {
			System.exit(0);
		}
	}

	@Override
	public void makeMove(long move) {
		game.makeMove(move);
	}

	@Override
	public synchronized boolean moveMade(long move) {
		ai.moveMade(move);

		boolean suc = boardPanel.moveMade(move);
		undoBtn.setEnabled(boardPanel.canUndo());

		return suc;
	}

	public synchronized void getMove() {

	}

	@Override
	public long undoMove() {
		ai.undoMove();

		long suc = boardPanel.undoMove();
		undoBtn.setEnabled(boardPanel.canUndo());

		return suc;
	}

	@Override
	public long makeRecommendation() {
		return 0;
	}

	public void makeMove() {
		boardPanel.makeMove();
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource() == newGameMenu) {
			game.newGame(null, false);
		}

		if (arg0.getSource() == undoUserMoveMenu) {
			// boardPanel.undoMove();
			game.undoMove();
		}

		if (arg0.getSource() == switchSidesMenu) {

		}

	}

	@Override
	public void setGame(PlayerContainer game) {
		this.game = game;
	}

	@Override
	public void pause() {

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
		return "Dumb Human";
	}

	@Override
	public String getPlayerName(Side side) {
		return game.getPlayerName(side);
	}

	@Override
	public long getPlayerTime(Side side) {
		return game.getPlayerTime(side);
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub

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
		// private JButton loadGameBtn;
		// private JButton saveGameBtn;
		// private JButton newGameBtn;
		// private JButton undoBtn;
		// private JButton redoBtn;
		//
		// private JButton boardFreeSetupBtn;
		// private JButton flipBoardBtn;
		//
		// private JButton getAIRecommendationBtn;

		if (arg0.getSource() == loadGameBtn) {
			int returnVal = fc.showOpenDialog(frame);

			if (returnVal == JFileChooser.APPROVE_OPTION) {

				Board board = XMLParser.XMLToBoard(FileIO.readFile(fc.getSelectedFile().getPath()));

				game.newGame(board, false);

			}

		}

		if (arg0.getSource() == saveGameBtn) {
			int returnVal = fc.showSaveDialog(frame);

			if (returnVal == JFileChooser.APPROVE_OPTION) {

				FileIO.writeFile(fc.getSelectedFile().getPath(), boardPanel.getBoard().toXML(true), false);

			}

		}

		if (arg0.getSource() == newGameBtn) {
			Board board = Game.getDefaultBoard();

			Side side = optionForSide();
			game.newGame(board, false);
			game.setSide(side, this);

		}

		if (arg0.getSource() == undoBtn) {
			if (boardPanel.canUndo()) {
				game.undoMove();
				game.undoMove();
			}

		}

		if (arg0.getSource() == switchSidesBtn) {
			boardPanel.flipBoard();
			game.switchSides();

		}

		if (arg0.getSource() == boardFreeSetupBtn) {

			if (boardPanel.isFreelyMove()) {
				game.newGame(boardPanel.getBoard().getCopy(), false);
			}

			boardPanel.setFreelyMove(!boardPanel.isFreelyMove());

			if (boardPanel.isFreelyMove()) {
				boardFreeSetupBtn.setText("Set it up!");
			} else {
				boardFreeSetupBtn.setText("Board Setup");
			}

		}

		if (arg0.getSource() == flipBoardBtn) {

			boardPanel.flipBoard();

		}

		if (arg0.getSource() == getAIRecommendationBtn) {
			if (boardPanel.isFreelyMove()) {
				ai.newGame(boardPanel.getBoard().getCopy());
			}

			long rec = ai.makeRecommendation();

			if (rec != 0) {
				boardPanel.highlightMove(rec);
			}
		}

	}

}
