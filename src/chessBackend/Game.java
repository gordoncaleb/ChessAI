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

	public void userMoved(Move usersMove) {
		System.out.println("User Moved");
		gui.aiMove(ai.move(usersMove));
	}

	public AI getAI() {
		return ai;
	}

}
