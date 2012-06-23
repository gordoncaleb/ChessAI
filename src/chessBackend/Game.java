package chessBackend;

import java.util.Hashtable;
import java.util.Vector;

import chessAI.DecisionNode;
import chessIO.FileIO;
import chessIO.XMLParser;

public class Game implements PlayerContainer {

	private Hashtable<Side, Player> players;
	private Vector<Player> observers;
	private Side turn;
	private boolean paused;
	private GameClock clock;
	private Adjudicator adjudicator;

	private Boolean gameActive;

	public Game(Hashtable<Side, Player> players) {

		paused = false;

		this.observers = new Vector<Player>();

		this.players = players;

		gameActive = new Boolean(true);

	}

	public GameResults newGame(boolean block) {

		String xmlBoard = FileIO.readFile("tempSave.xml");

		if (xmlBoard == null) {
			return newGame(Game.getDefaultBoard(), block);
		} else {
			return newGame(XMLParser.XMLToBoard(xmlBoard), block);
		}

	}

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

		showProgress(0);

		if (block) {

			synchronized (gameActive) {
				try {
					gameActive.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			return new GameResults(adjudicator.getGameStatus(), adjudicator.getWinner(), -adjudicator.getBoard().staticScore(),
					clock.getTime(Side.WHITE), clock.getTime(Side.BLACK), adjudicator.getMoveHistory().size(), clock.getMaxTime(Side.WHITE),
					clock.getMaxTime(Side.BLACK));
		}

		return null;

	}

	public void showProgress(int progress) {
		if (players.get(Side.BOTH) == null) {
			players.get(Side.WHITE).showProgress(progress);
			players.get(Side.BLACK).showProgress(progress);
		} else {
			players.get(Side.BOTH).showProgress(progress);
		}

	}

	public void requestRecommendation() {
		if (players.get(Side.BOTH) == null) {
			players.get(Side.WHITE).requestRecommendation();
			players.get(Side.BLACK).requestRecommendation();
		} else {
			players.get(Side.BOTH).requestRecommendation();
		}

		for (int i = 0; i < observers.size(); i++) {
			observers.elementAt(i).requestRecommendation();
		}
	}

	public void recommendationMade(long move) {
		if (players.get(Side.BOTH) == null) {
			players.get(Side.WHITE).recommendationMade(move);
			players.get(Side.BLACK).recommendationMade(move);
		} else {
			players.get(Side.BOTH).recommendationMade(move);
		}
	}

	public synchronized void switchSides() {
		Player whitePlayer = players.get(Side.WHITE);
		players.put(Side.WHITE, players.get(Side.BLACK));
		players.put(Side.BLACK, whitePlayer);

		if (players.get(Side.BOTH) == null) {
			players.get(turn).makeMove();
		} else {
			players.get(Side.BOTH).makeMove();
		}
	}

	public synchronized void setSide(Side side, Player player) {
		if (players.get(side) != player) {
			Player whitePlayer = players.get(Side.WHITE);
			players.put(Side.WHITE, players.get(Side.BLACK));
			players.put(Side.BLACK, whitePlayer);
		}
	}

	public synchronized boolean undoMove() {

		if (adjudicator.canUndo()) {
			adjudicator.undo();
			adjudicator.getValidMoves();

			if (players.get(Side.BOTH) == null) {
				players.get(Side.WHITE).undoMove();
				players.get(Side.BLACK).undoMove();
			} else {
				players.get(Side.BOTH).undoMove();
			}

			for (int i = 0; i < observers.size(); i++) {
				observers.elementAt(i).undoMove();
			}

			turn = turn.otherSide();

			return true;

		} else {
			return false;
		}
	}

	public synchronized boolean makeMove(long move) {

		if (clock.hit()) {
			System.out.println("Game Over " + turn.otherSide() + " wins by time!");
			// adjudicator.getBoard().setBoardStatus(GameStatus.TIMES_UP);

			synchronized (gameActive) {
				gameActive.notifyAll();
			}

			return false;
		}

		turn = turn.otherSide();

		if (adjudicator.move(move)) {

			adjudicator.getValidMoves();

			System.out.println("GamePhase = " + adjudicator.getBoard().calcGamePhase());

			for (int i = 0; i < observers.size(); i++) {
				observers.elementAt(i).moveMade(move);
			}

			if (players.get(Side.BOTH) == null) {
				players.get(Side.WHITE).moveMade(move);
				players.get(Side.BLACK).moveMade(move);
			} else {
				players.get(Side.BOTH).moveMade(move);
			}

		} else {
			adjudicator.getBoard().setBoardStatus(GameStatus.INVALID);
		}

		if (adjudicator.isGameOver()) {
			System.out.println("Game over");

			if (players.get(Side.BOTH) == null) {
				if (adjudicator.getGameStatus() == GameStatus.CHECKMATE) {
					players.get(adjudicator.getWinner()).gameOver(1);
					players.get(adjudicator.getWinner().otherSide()).gameOver(-1);
				} else {
					players.get(Side.WHITE).gameOver(0);
					players.get(Side.BLACK).gameOver(0);
				}
			}

			synchronized (gameActive) {
				gameActive.notifyAll();
			}

			return false;
		} else {
			if (players.get(Side.BOTH) == null) {
				players.get(turn).makeMove();
			} else {
				players.get(Side.BOTH).makeMove();
			}
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

	public String getPlayerName(Side side) {
		if (players.get(side) != null) {
			return players.get(side).getVersion();
		} else {
			return "";
		}
	}

	public long getPlayerTime(Side side) {
		if (clock != null) {
			return clock.getTime(side);
		} else {
			return 0;
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

	public static Board getDefaultBoard() {
		return XMLParser.XMLToBoard(FileIO.readFile("default.xml"));
	}

	@Override
	public void endGame() {
		if (players.get(Side.BOTH) == null) {
			players.get(Side.WHITE).endGame();
			players.get(Side.BLACK).endGame();
		} else {
			players.get(Side.BOTH).endGame();
		}

		for (int i = 0; i < observers.size(); i++) {
			observers.elementAt(i).endGame();
		}

	}

}
