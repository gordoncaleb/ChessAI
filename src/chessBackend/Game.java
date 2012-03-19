package chessBackend;

import javax.swing.JOptionPane;

import chessAI.AI;
import chessAI.DecisionNode;
import chessGUI.BoardGUI;
import chessGUI.DecisionTreeGUI;
import chessGUI.MoveBookGUI;

public class Game {
	public static String VERSION = "0.2.021812";

	private BoardGUI gui;
	private AI ai;
	private MoveBookGUI goodMoveDBGUI;
	private DecisionTreeGUI decisionTreeGUI;
	private Player userSide;

	public Game() {

		boolean debug = false;
		boolean learn = false;

		ai = new AI(this, debug);
		ai.start();
		gui = new BoardGUI(this, debug);

		if (learn) {
			goodMoveDBGUI = new MoveBookGUI(ai.getMoveBook());
		}

		if (debug) {
			decisionTreeGUI = new DecisionTreeGUI(this, gui);
		}

		newGame();
	}

	public static void main(String[] args) {

		Game game = new Game();
	}

	public void newGame() {

		Object[] options = { "White", "Black" };
		int n = JOptionPane.showOptionDialog(gui.getFrame(), "Wanna play as black or white?", "New Game", JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

		if (n == JOptionPane.YES_OPTION) {
			userSide = Player.WHITE;
			gui.newGame(Player.WHITE);
			ai.setMakeNewGame(Player.WHITE);
		} else {
			userSide = Player.BLACK;
			gui.newGame(Player.BLACK);
			ai.setMakeNewGame(Player.BLACK);
		}

	}

	public void newGame(Player whitePlayer) {
		gui.newGame(whitePlayer);
		ai.setMakeNewGame(whitePlayer);
	}

	public void userMoved(Move usersMove) {
		//System.out.println("User Moved");
		ai.setUserDecision(usersMove);
		//System.out.println("AI notified");
	}

	public void setGameSatus(GameStatus status, Player playerTurn) {
		if (status == GameStatus.CHECK) {
			System.out.println("Check!");
		}

		if (playerTurn == userSide) {

			if (status == GameStatus.CHECKMATE) {
				Object[] options = { "Yes, please", "Nah" };
				int n = JOptionPane.showOptionDialog(gui.getFrame(), "You just got schooled homie.\nWanna try again?", "Ouch!",
						JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

				if (n == JOptionPane.YES_OPTION) {
					newGame();
				} else {
					System.exit(0);
				}

			}

			if (status == GameStatus.STALEMATE) {
				Object[] options = { "Yes, please", "Nah, maybe later." };
				int n = JOptionPane.showOptionDialog(gui.getFrame(), "Stalemate...hmmm close call.\nWanna try again?", "", JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
				if (n == JOptionPane.YES_OPTION) {
					newGame();
				} else {
					System.exit(0);
				}

			}

		} else {

			if (status == GameStatus.CHECKMATE) {
				Object[] options = { "Yeah, why not?", "Nah." };
				int n = JOptionPane.showOptionDialog(gui.getFrame(), "Nicely done boss.\nWanna rematch?", "Ouch!", JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

				if (n == JOptionPane.YES_OPTION) {
					newGame();
				} else {
					System.exit(0);
				}

			}

			if (status == GameStatus.STALEMATE) {
				Object[] options = { "Yes, please", "Nah, maybe later." };
				int n = JOptionPane.showOptionDialog(gui.getFrame(), "Stalemate...hmmm close call.\nWanna try again?", "", JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
				if (n == JOptionPane.YES_OPTION) {
					newGame();
				} else {
					System.exit(0);
				}
			}

		}
	}

	public void aiMoved(Move aisMove) {
		gui.makeMove(aisMove);
	}

	public void undoUserMove() {
		ai.setUndoUserMove();
	}

	public int getMoveChosenPathValue(Move m) {
		return ai.getMoveChosenPathValue(m);
	}

	public void setDecisionTreeRoot(DecisionNode rootDecision) {
		decisionTreeGUI.setRootDecisionTree(rootDecision);
	}

}
