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

		newGame();
	}

	public static void main(String[] args) {

		Player observer = new ObserverGUI(null, false);

		Game game = new Game(GameType.AI_VS_AI);
		game.addObserver(observer);
	}

	public synchronized void newGame() {

		if (debug) {
			board = XMLParser.XMLToBoard(FileIO.readFile("default.xml"));
		} else {
			board = XMLParser.XMLToBoard(FileIO.readFile("default.xml"));
		}

		turn = board.getTurn();

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

		for (int i = 0; i < players.length; i++) {
			if (players[i] != mover) {
				players[i].moveMade(move);
			}
		}

		for (int i = 0; i < observers.size(); i++) {
			if (observers.elementAt(i) != mover) {
				observers.elementAt(i).moveMade(move);
			}
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
		observer.newGame(Side.NONE, board.getCopy());
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
