package chessBackend;

import java.util.Hashtable;
import java.util.Vector;

import chessAI.AI;
import chessAI.DecisionNode;
import chessGUI.DecisionTreeGUI;
import chessGUI.MoveBookBuilderGUI;
import chessGUI.ObserverGUI;
import chessGUI.PlayerGUI;
import chessIO.FileIO;
import chessIO.XMLParser;

public class Game implements PlayerContainer{
	public static String VERSION = "0.3.031912";

	private boolean debug;

	private Board board;

	private DecisionTreeGUI decisionTreeGUI;

	private Hashtable<Side, Player> players;
	private Vector<Player> observers;
	private Side turn;
	private boolean paused;
	private GameClock clock;
	private Adjudicator adjudicator;

	private Boolean gameActive;

	// public Game(GameType gameType) {
	//
	// debug = true;
	//
	// paused = false;
	//
	// observers = new Vector<Player>();
	// players = new Player[2];
	//
	// switch (gameType) {
	// case GUI_VS_AI:
	// players[0] = new PlayerGUI(this, debug);
	// players[1] = new AI(this, debug);
	// break;
	// case AI_VS_AI:
	// players[0] = new AI(this, debug);
	// players[1] = new AI(this, debug);
	// break;
	// case TWO_GUI:
	// players[0] = new PlayerGUI(this, debug);
	// players[1] = new PlayerGUI(this, debug);
	// case ONE_GUI:
	// players[0] = new PlayerGUI(this, debug);
	// players[1] = players[0];
	// }
	//
	// if (debug) {
	// // decisionTreeGUI = new DecisionTreeGUI(this, players[1]);
	// }
	//
	// gameActive = new Boolean(true);
	//
	// }

	public Game(Hashtable<Side, Player> players) {

		paused = false;
		debug = false;

		this.observers = new Vector<Player>();

		this.players = players;

		gameActive = new Boolean(true);

	}
	
	public static void main(String[] args){
		
		Hashtable<Side,Player> players = new Hashtable<Side,Player>();
		
		MoveBookBuilderGUI mbBuilder = new MoveBookBuilderGUI(null);
		
		players.put(Side.BOTH, mbBuilder);
		
		Game game = new Game(players);
		
		mbBuilder.setGame(game);
		
		game.newGame(getDefaultBoard(), false);
	}

//	public static void main(String[] args) {
//
//		boolean debug = true;
//		Game game;
//		GameResults results;
//
//		int whiteWins = 0;
//		int blackWins = 0;
//		int draws = 0;
//
//		long whiteTime = 0;
//		long blackTime = 0;
//		
//		FileIO.initLog();
//
//		Player observer = new ObserverGUI(null, false);
//
//		// Game game = new Game(GameType.AI_VS_AI);
//
//		Player playerOne = new AI(null, debug);
//		Player playerTwo = new AI(null, debug);
//
//		Board defaultBoard = XMLParser.XMLToBoard(FileIO.readFile("default.xml"));
//
//		Hashtable<Side, Player> players = new Hashtable<Side, Player>();
//
//		players.put(Side.WHITE, playerOne);
//		players.put(Side.BLACK, playerTwo);
//
//
//		game = new Game(players);
//
//		playerOne.setGame(game);
//		playerTwo.setGame(game);
//		game.addObserver(observer);
//
//		for (int i = 0; i < 1000; i++) {
//
//			results = game.newGame(defaultBoard, true);
//
//			if (results.getWinner() == Side.WHITE) {
//				whiteWins++;
//				whiteTime += results.getWinnerTime();
//				blackTime += results.getLoserTime();
//			} else {
//				if (results.getWinner() == Side.BLACK) {
//					blackWins++;
//					blackTime += results.getWinnerTime();
//					whiteTime += results.getLoserTime();
//				} else {
//					draws++;
//				}
//			}
//			
//			System.out.println("White wins: " + whiteWins + " with " + whiteTime + "\nBlack wins: " + blackWins + " with " + blackTime + "\nDraws: "
//					+ draws);
//
//		}
//
//		System.out.println("Tournament done");
//	}

	public GameResults newGame(Board board, boolean block) {

		if (board == null) {
			board = XMLParser.XMLToBoard(FileIO.readFile("default.xml"));
		}

		turn = board.getTurn();

		adjudicator = new Adjudicator(board.getCopy());
		adjudicator.getValidMoves();

		clock = new GameClock("White", "Black", 0, 0, turn);

		for (int i = 0; i < observers.size(); i++) {
			observers.elementAt(i).newGame(board.getCopy());
		}

		if (players.get(Side.BOTH) == null) {
			players.get(Side.WHITE).newGame(board.getCopy());
			players.get(Side.BLACK).newGame(board.getCopy());
			players.get(turn).makeMove();
		} else {
			players.get(Side.BOTH).newGame(board.getCopy());
			players.get(Side.BOTH).makeMove();
		}


		if (block) {

			synchronized (gameActive) {
				try {
					gameActive.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			return new GameResults(adjudicator.getWinner(), 0, clock.getTime(adjudicator.getWinner()), clock.getTime(adjudicator.getWinner()
					.otherSide()));
		}

		return null;

	}

	public synchronized boolean undoMove() {

		if (players.get(Side.BOTH) == null) {
			players.get(Side.WHITE).undoMove();
			players.get(Side.BLACK).undoMove();
		} else {
			players.get(Side.BOTH).undoMove();
		}

		for (int i = 0; i < observers.size(); i++) {
			observers.elementAt(i).undoMove();
		}

		return true;
	}

	public synchronized boolean makeMove(Move move) {

		if (clock.hit()) {
			System.out.println("Game Over " + turn.otherSide() + " wins by time!");
			// adjudicator.getBoard().setBoardStatus(GameStatus.TIMES_UP);

			synchronized (gameActive) {
				gameActive.notifyAll();
			}

			return false;
		}

		turn = turn.otherSide();

		adjudicator.move(move);
		adjudicator.getValidMoves();

		if (adjudicator.isGameOver()) {
			System.out.println("Game over");
		}

		for (int i = 0; i < observers.size(); i++) {
			observers.elementAt(i).moveMade(move);
		}

		if (players.get(Side.BOTH) == null) {
			players.get(Side.WHITE).moveMade(move);
			players.get(Side.BLACK).moveMade(move);
		} else {
			players.get(Side.BOTH).moveMade(move);
		}

		if (adjudicator.isGameOver()) {

			synchronized (gameActive) {
				gameActive.notifyAll();
			}

			return false;
		}

		if (players.get(Side.BOTH) == null) {
			players.get(turn).makeMove();
		} else {
			players.get(Side.BOTH).makeMove();
		}

		return true;
	}

	public void pause() {

		paused = !paused;

		if (players.get(Side.BOTH) == null) {
			players.get(Side.WHITE).pause();
			players.get(Side.BLACK).pause();
		} else {
			players.get(Side.BOTH).pause();
		}

		for (int i = 0; i < observers.size(); i++) {
			observers.elementAt(i).pause();
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
	
	public static Board getDefaultBoard(){
		return XMLParser.XMLToBoard(FileIO.readFile("default.xml"));
	}

}
