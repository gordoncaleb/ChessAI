package chessBackend;

import javax.swing.JOptionPane;

import chessAI.AI;
import chessAI.DecisionNode;
import chessGUI.BoardGUI;

public class Game {
	public static String VERSION = "0.2.122011";
	private BoardGUI gui;
	private AI ai;

	public Game() {

		boolean debug = false;

		ai = new AI(this, debug);
		ai.start();

		gui = new BoardGUI(this, debug);

		newGame();
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Game game = new Game();
	}

	public void newGame() {

		Object[] options = { "White", "Black" };
		int n = JOptionPane.showOptionDialog(gui.getFrame(), "Wanna play as black or white?", "New Game", JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE, null, // do not use a
													// custom
													// Icon
				options, // the titles of buttons
				options[0]); // default button title

		if (n == JOptionPane.YES_OPTION) {
			gui.newGame(Player.USER);
			ai.setMakeNewGame(Player.USER);
		} else {
			gui.newGame(Player.AI);
			ai.setMakeNewGame(Player.AI);
		}

	}
	
	public void newGame(Player whitePlayer){
		gui.newGame(whitePlayer);
		ai.setMakeNewGame(whitePlayer);
	}

	public synchronized void userMoved(DecisionNode usersDecision) {
		ai.setUserDecision(usersDecision);
	}

	public synchronized void aiMoved(DecisionNode aiDecision) {
		gui.setAiResponse(aiDecision);
	}

	public synchronized void growBranch(DecisionNode branch) {
		ai.growBranch(branch);
	}
	
	public synchronized void undoUserMove(){
		ai.setUndoUserMove();
	}

}
