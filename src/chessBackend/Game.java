package chessBackend;

import java.util.Vector;

import chessAI.AI;
import chessAI.DecisionNode;
import chessGUI.DecisionTreeGUI;
import chessGUI.ObserverGUI;
import chessGUI.PlayerGUI;
import chessIO.FileIO;
import chessIO.XMLParser;

public class Game {
	public static String VERSION = "0.3.031912";

	private boolean debug;

	private Board board;

	private DecisionTreeGUI decisionTreeGUI;

	private Player[] players;
	private Vector<Player> observers;
	private Side turn;
	private boolean paused;
	private GameClock clock;
	private Adjudicator adjudicator;

	private Boolean gameActive;

	public Game(GameType gameType) {

		debug = true;

		paused = false;

		observers = new Vector<Player>();
		players = new Player[2];

		switch (gameType) {
		case GUI_VS_AI:
			players[0] = new PlayerGUI(this, debug);
			players[1] = new AI(this, debug);
			break;
		case AI_VS_AI:
			players[0] = new AI(this, debug);
			players[1] = new AI(this, debug);
			break;
		case TWO_GUI:
			players[0] = new PlayerGUI(this, debug);
			players[1] = new PlayerGUI(this, debug);
		case ONE_GUI:
			players[0] = new PlayerGUI(this, debug);
			players[1] = players[0];
		}

		if (debug) {
			// decisionTreeGUI = new DecisionTreeGUI(this, players[1]);
		}

		gameActive = new Boolean(true);

	}

	public Game(Player playerOne, Player playerTwo) {

		paused = false;
		debug = false;

		players = new Player[2];
		observers = new Vector<Player>();

		players[0] = playerOne;
		players[1] = playerTwo;

		gameActive = new Boolean(true);

	}

	public static void main(String[] args) {

		boolean debug = true;
		Game game;
		GameResults results;

		int whiteWins = 0;
		int blackWins = 0;
		int draws = 0;

		long whiteTime = 0;
		long blackTime = 0;

		Player observer = new ObserverGUI(null, false);

		// Game game = new Game(GameType.AI_VS_AI);

		for (int i = 0; i < 10; i++) {
			Player playerOne = new AI(null, debug);
			Player playerTwo = new AI(null, debug);

			game = new Game(playerOne, playerTwo);

			playerOne.setGame(game);
			playerTwo.setGame(game);
			game.addObserver(observer);

			results = game.newGame(true);

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

		System.out.println("White wins: " + whiteWins + " with " + whiteTime + "\nBlack wins: " + blackWins + " with " + blackTime + "\nDraws: " + draws);
	}

	public GameResults newGame(boolean block) {

		synchronized (this) {

			if (debug) {
				board = XMLParser.XMLToBoard(FileIO.readFile("default.xml"));
			} else {
				board = XMLParser.XMLToBoard(FileIO.readFile("default.xml"));
			}

			turn = board.getTurn();

			adjudicator = new Adjudicator(board.getCopy());

			adjudicator.getValidMoves();

			clock = new GameClock("White", "Black", 0, 0, turn);

			for (int i = 0; i < observers.size(); i++) {
				observers.elementAt(i).newGame(Side.NONE, board.getCopy());
			}

			Side tempSide;
			if (players[0] instanceof PlayerGUI) {
				if (players[1] == players[0]) {
					players[0].newGame(Side.BOTH, board);
				} else {
					tempSide = players[0].newGame(null, board);
					players[1].newGame(tempSide.otherSide(), board.getCopy());
				}
			} else {
				players[0].newGame(turn, board);
				players[1].newGame(turn.otherSide(), board.getCopy());
			}

		}

		if (block) {

			synchronized (gameActive) {
				try {
					gameActive.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			return new GameResults(adjudicator.getWinner(), 0, clock.getTime(adjudicator.getWinner()), clock.getTime(adjudicator.getWinner().otherSide()));
		}

		return null;

	}

	public synchronized boolean undoMove(Player mover) {

		for (int i = 0; i < players.length; i++) {
			if (players[i] != mover) {
				players[i].undoMove();
			}
		}

		for (int i = 0; i < observers.size(); i++) {
			if (observers.elementAt(i) != mover) {
				observers.elementAt(i).undoMove();
			}
		}

		return true;
	}

	public synchronized boolean makeMove(Move move, Player mover) {

		if (clock.hit()) {
			System.out.println("Game Over " + turn.otherSide() + " wins by time!");
			//adjudicator.getBoard().setBoardStatus(GameStatus.TIMES_UP);

			synchronized (gameActive) {
				gameActive.notifyAll(); 
			}

			return false;
		}

		turn = turn.otherSide();

		for (int i = 0; i < observers.size(); i++) {
			if (observers.elementAt(i) != mover) {
				observers.elementAt(i).moveMade(move);
			}
		}

		for (int i = 0; i < players.length; i++) {
			if (players[i] != mover) {
				players[i].moveMade(move);
			}
		}

		adjudicator.move(move);
		adjudicator.getValidMoves();

		if (adjudicator.isGameOver()) {

			players[0].endGame();
			players[1].endGame();

			synchronized (gameActive) {
				gameActive.notifyAll();
			}

			return false;
		}

		return true;
	}

	public void pause(Player pauser) {

		paused = !paused;

		for (int i = 0; i < players.length; i++) {
			if (players[i] != pauser) {
				players[i].pause();
			}
		}

		for (int i = 0; i < observers.size(); i++) {
			if (observers.elementAt(i) != pauser) {
				observers.elementAt(i).pause();
			}
		}

	}

	public synchronized void addObserver(Player observer) {
		// observer.newGame(Side.NONE, board.getCopy());
		observer.setGame(this);
		observers.add(observer);
	}

	// public int getMoveChosenPathValue(Move m) {
	// return ai.getMoveChosenPathValue(m);
	// }

	public void setDecisionTreeRoot(DecisionNode rootDecision) {
		// decisionTreeGUI.setRootDecisionTree(rootDecision);
	}

	public boolean isPaused() {
		return paused;
	}

}
