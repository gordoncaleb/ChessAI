package chessBackend;

import chessAI.AI;
import chessAI.DecisionNode;
import chessGUI.BoardGUI;

public class Game {

	private BoardGUI gui;
	private AI ai;

	public Game() {

		boolean debug = true;

		Board board = new Board();
		ai = new AI(board, debug);
		gui = new BoardGUI(this, ai.getRoot(), debug);
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Game game = new Game();
	}

	public void userMoved(DecisionNode usersDecision) {
		System.out.println("User Moved");
		gui.aiMove(ai.move(usersDecision));
	}

	public AI getAI() {
		return ai;
	}

}
