package chessBackend;

import chessAI.AI;
import chessAI.DecisionNode;
import chessGUI.BoardGUI;

public class Game {

	private BoardGUI gui;
	private AI ai;

	public Game() {

		boolean debug = true;

		gui = new BoardGUI(this, debug);
		ai = new AI(this, debug);
		
		ai.start();
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Game game = new Game();
	}
	
	public synchronized void newGame(){
		gui.newGame();
		ai.setMakeNewGame();
	}

	public synchronized void userMoved(DecisionNode usersDecision) {
		ai.setUserDecision(usersDecision);
	}
	
	public synchronized void aiMoved(DecisionNode aiDecision){
		gui.setAiResponse(aiDecision);
	}
	
	public synchronized void growBranch(DecisionNode branch){
		ai.growBranch(branch);
	}


}
