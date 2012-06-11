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
import chessBackend.BoardMaker;
import chessBackend.Game;
import chessBackend.GameResults;
import chessBackend.GameStatus;
import chessBackend.Move;
import chessBackend.Player;
import chessBackend.PlayerContainer;
import chessBackend.Side;
import chessIO.FileIO;
import chessIO.XMLParser;

public class PlayerGUI implements Player, BoardGUI, MouseListener {
	private JFrame frame;
	
	private JMenuItem newGameMenu;
	private JMenuItem new960GameMenu;
	private JMenuItem loadGameMenu;
	private JMenuItem saveGameMenu;
	private JMenuItem undoUserMoveMenu;
	private JMenuItem switchSidesMenu;
	
	private JMenuItem boardFreeSetupMenu;
	private JMenuItem flipBoardMenu;

	private JMenuItem getAIRecommendationMenu;
	
	private BoardPanel boardPanel;

	private AI ai;

	private JPanel ctrlPanel;
	private JPanel boardCtrlPanel;
	private JPanel aiCtrlPanel;
	private JPanel gameCtrlPanel;

//	private JButton loadGameBtn;
//	private JButton saveGameBtn;
//	private JButton newGameBtn;
//	private JButton undoBtn;
//	private JButton switchSidesBtn;
//
//	private JButton boardFreeSetupBtn;
//	private JButton flipBoardBtn;
//
//	private JButton getAIRecommendationBtn;

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
		JMenu AIMenu = new JMenu("AI");
		JMenu boardMenu = new JMenu("Board");
		gameMenu.setMnemonic(KeyEvent.VK_G);
		
		newGameMenu = new JMenuItem("New Game");
		newGameMenu.addMouseListener(this);
		gameMenu.add(newGameMenu);
		
		new960GameMenu = new JMenuItem("New Chess960 Game");
		new960GameMenu.addMouseListener(this);
		gameMenu.add(new960GameMenu);

		undoUserMoveMenu = new JMenuItem("Undo Last Move");
		undoUserMoveMenu.addMouseListener(this);
		gameMenu.add(undoUserMoveMenu);

		switchSidesMenu = new JMenuItem("Switch Side");
		switchSidesMenu.addMouseListener(this);
		gameMenu.add(switchSidesMenu);

		loadGameMenu = new JMenuItem("Load Game");
		loadGameMenu.addMouseListener(this);
		gameMenu.add(loadGameMenu);

		saveGameMenu = new JMenuItem("Save Game");
		saveGameMenu.addMouseListener(this);
		gameMenu.add(saveGameMenu);

		boardFreeSetupMenu = new JMenuItem("Board Setup");
		boardFreeSetupMenu.addMouseListener(this);
		boardMenu.add(boardFreeSetupMenu);

		flipBoardMenu = new JMenuItem("Flip Board");
		flipBoardMenu.addMouseListener(this);
		boardMenu.add(flipBoardMenu);

		getAIRecommendationMenu = new JMenuItem("Reccomendation");
		getAIRecommendationMenu.addMouseListener(this);
		AIMenu.add(getAIRecommendationMenu);
		
		menuBar.add(gameMenu);
		menuBar.add(boardMenu);
		menuBar.add(AIMenu);
		frame.setJMenuBar(menuBar);

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
		int n = JOptionPane.showOptionDialog(frame, "Wanna play as black or white?", "New Game", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options,
				options[0]);

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

		undoUserMoveMenu.setEnabled(boardPanel.canUndo());

	}

	public void gameOverLose() {
		Object[] options = { "Yes, please", "Nah" };
		int n = JOptionPane.showOptionDialog(frame, "You just got schooled homie.\nWanna try again?", "Ouch!", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
				options, options[0]);

		if (n == JOptionPane.YES_OPTION) {
			game.newGame(null, false);
		} else {
			System.exit(0);
		}

	}

	public void gameOverWin() {
		Object[] options = { "Yeah, why not?", "Nah." };
		int n = JOptionPane.showOptionDialog(frame, "Nicely done boss.\nWanna rematch?", "Ouch!", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options,
				options[0]);

		if (n == JOptionPane.YES_OPTION) {
			game.newGame(null, false);
		} else {
			System.exit(0);
		}

	}

	public void gameOverStaleMate() {
		Object[] options = { "Yes, please", "Nah, maybe later." };
		int n = JOptionPane.showOptionDialog(frame, "Stalemate...hmmm close call.\nWanna try again?", "", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
				options, options[0]);
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
		undoUserMoveMenu.setEnabled(boardPanel.canUndo());

		return suc;
	}

	public synchronized void getMove() {

	}

	@Override
	public long undoMove() {
		ai.undoMove();

		long suc = boardPanel.undoMove();
		undoUserMoveMenu.setEnabled(boardPanel.canUndo());

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

		if (arg0.getSource() == loadGameMenu) {
			int returnVal = fc.showOpenDialog(frame);

			if (returnVal == JFileChooser.APPROVE_OPTION) {

				Board board = XMLParser.XMLToBoard(FileIO.readFile(fc.getSelectedFile().getPath()));

				Side side = optionForSide();
				game.setSide(side, this);
				game.newGame(board, false);

			}

		}

		if (arg0.getSource() == saveGameMenu) {
			int returnVal = fc.showSaveDialog(frame);

			if (returnVal == JFileChooser.APPROVE_OPTION) {

				FileIO.writeFile(fc.getSelectedFile().getPath(), boardPanel.getBoard().toXML(true), false);

			}

		}

		if (arg0.getSource() == newGameMenu) {
			Board board = Game.getDefaultBoard();

			Side side = optionForSide();
			game.setSide(side, this);
			game.newGame(board, false);

		}
		
		if (arg0.getSource() == new960GameMenu) {
			Board board = BoardMaker.getRandomChess960Board();

			Side side = optionForSide();
			game.setSide(side, this);
			game.newGame(board, false);

		}

		if (arg0.getSource() == undoUserMoveMenu) {
			if (boardPanel.canUndo()) {
				game.undoMove();
				game.undoMove();
			}

		}

		if (arg0.getSource() == switchSidesMenu) {
			boardPanel.flipBoard();
			game.switchSides();

		}

		if (arg0.getSource() == boardFreeSetupMenu) {

			if (boardPanel.isFreelyMove()) {
				game.newGame(boardPanel.getBoard().getCopy(), false);
			}

			boardPanel.setFreelyMove(!boardPanel.isFreelyMove());

			if (boardPanel.isFreelyMove()) {
				boardFreeSetupMenu.setText("Set it up!");
			} else {
				boardFreeSetupMenu.setText("Board Setup");
			}

		}

		if (arg0.getSource() == flipBoardMenu) {

			boardPanel.flipBoard();

		}

		if (arg0.getSource() == getAIRecommendationMenu) {
			if (boardPanel.isFreelyMove()) {
				ai.newGame(boardPanel.getBoard().getCopy());
			}

			long rec = ai.makeRecommendation();

			if (rec != 0) {
				boardPanel.highlightMove(rec);
			}
		}

	}

	@Override
	public void endGame() {

	}

}
