package chessBackend;

import java.util.Vector;

import javax.swing.JOptionPane;

import chessAI.AI;
import chessAI.DecisionNode;
import chessAI.MoveBook;
import chessGUI.BoardGUI;
import chessGUI.DecisionTreeGUI;
import chessGUI.MoveBookGUI;

public class Game {
	public static String VERSION = "0.2.021812";

	private BoardGUI gui;
	private AI ai;
	private MoveBookGUI goodMoveDBGUI;
	private DecisionTreeGUI decisionTreeGUI;

	public Game() {

		boolean debug = false;
		boolean learn = false;

		ai = new AI(this, debug);
		ai.start();
		gui = new BoardGUI(this, debug);
		
		
		if(learn){
			goodMoveDBGUI = new MoveBookGUI(ai.getGoodMoveDB());
		}
		
		if(debug){
			decisionTreeGUI = new DecisionTreeGUI(this,gui);
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
			gui.newGame(Player.USER);
			ai.setMakeNewGame(Player.USER);
		} else {
			gui.newGame(Player.AI);
			ai.setMakeNewGame(Player.AI);
		}

	}

	public void newGame(Player whitePlayer) {
		gui.newGame(whitePlayer);
		ai.setMakeNewGame(whitePlayer);
	}

	public void userMoved(Move usersMove) {
		System.out.println("User Moved");
		ai.setUserDecision(usersMove);
		System.out.println("AI notified");
	}
	
	public void setGameSatus(GameStatus status, Player causedStatus){
		if(status == GameStatus.CHECK){
			System.out.println("Check!");
		}
		
		if (causedStatus == Player.USER) {

			if (status == GameStatus.CHECKMATE) {
				Object[] options = { "Yes, please", "Nah, I'm kinda a bitch." };
				int n = JOptionPane.showOptionDialog(gui.getFrame(), "You just got schooled homie.\nWanna try again?", "Ouch!", JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE, null, // do not use a
															// custom
															// Icon
						options, // the titles of buttons
						options[0]); // default button title

				if (n == JOptionPane.YES_OPTION) {
					newGame();
				} else {
					System.exit(0);
				}

			}

			if (status == GameStatus.STALEMATE) {
				Object[] options = { "Yes, please", "Nah, maybe later." };
				int n = JOptionPane.showOptionDialog(gui.getFrame(), "Stalemate...hmmm close call.\nWanna try again?", "", JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE, null, // do not use a
															// custom
															// Icon
						options, // the titles of buttons
						options[0]); // default button title

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
						JOptionPane.QUESTION_MESSAGE, null, // do not use a
															// custom
															// Icon
						options, // the titles of buttons
						options[0]); // default button title

				if (n == JOptionPane.YES_OPTION) {
					newGame();
				} else {
					System.exit(0);
				}

			}

			if (status == GameStatus.STALEMATE) {
				Object[] options = { "Yes, please", "Nah, maybe later." };
				int n = JOptionPane.showOptionDialog(gui.getFrame(), "Stalemate...hmmm close call.\nWanna try again?", "", JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE, null, // do not use a
															// custom
															// Icon
						options, // the titles of buttons
						options[0]); // default button title

				if (n == JOptionPane.YES_OPTION) {
					newGame();
				} else {
					System.exit(0);
				}
			}

		}
	}

	public void aiMoved(Move aisMove) {
		gui.aiMoved(aisMove);
	}

	public void undoUserMove() {
		ai.setUndoUserMove();
	}
	
	public int getMoveChosenPathValue(Move m){
		return ai.getMoveChosenPathValue(m);
	}
	
	public void setDecisionTreeRoot(DecisionNode rootDecision){
		decisionTreeGUI.setRootDecisionTree(rootDecision);
	}
	
	public void growBranch(DecisionNode branch) {
		ai.growBranch(branch);
	}

}
