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

	public Game(GameType gameType) {

		debug = false;

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
			players[1] = null;
		}

		if (debug) {
			// decisionTreeGUI = new DecisionTreeGUI(this, players[1]);
		}

		newGame();
	}

	public static void main(String[] args) {

		Game game = new Game(GameType.GUI_VS_AI);
		//game.addObserver(new ObserverGUI(game, false));
	}

	public void newGame() {

		if (debug) {
			board = XMLParser.XMLToBoard(FileIO.readFile("testboard.xml"));
		} else {
			board = XMLParser.XMLToBoard(FileIO.readFile("default.xml"));
		}

		turn = board.getTurn();

		Side tempSide;
		if (players[0] instanceof PlayerGUI) {
			if (players[1] == null) {
				tempSide = players[0].newGame(Side.BOTH, board);
			} else {
				tempSide = players[0].newGame(null, board);
				players[1].newGame(tempSide.otherSide(), board.getCopy());
			}
		} else {
			players[0].newGame(turn, board);
			players[1].newGame(turn.otherSide(), board.getCopy());
		}

	}

	public boolean undoMove(Side side) {

		return false;
	}

	public synchronized void makeMove(Move move) {

		if (players[0].getSide() != Side.BOTH) {
			getPlayer(turn.otherSide()).opponentMoved(move);
		}

		turn = turn.otherSide();

		for (int i = 0; i < observers.size(); i++) {
			observers.elementAt(i).opponentMoved(move);
		}
	}

	private Player getPlayer(Side side) {
		if (players[0].getSide() == side) {
			return players[0];
		} else {
			return players[1];
		}
	}

	public synchronized void addObserver(Player observer) {
		observer.newGame(Side.NONE, board.getCopy());
		observers.add(observer);
	}

	// public int getMoveChosenPathValue(Move m) {
	// return ai.getMoveChosenPathValue(m);
	// }

	public void setDecisionTreeRoot(DecisionNode rootDecision) {
		decisionTreeGUI.setRootDecisionTree(rootDecision);
	}

}
