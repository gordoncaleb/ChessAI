package chessBackend;

import chessAI.AI;
import chessAI.DecisionNode;
import chessGUI.BoardGUI;

public class Game {
	
	private BoardGUI gui;
	private AI ai;
	
	public Game(){
		Board board = new Board();
		ai = new AI(board);
		gui = new BoardGUI(this,ai.getRoot(), true);
	}
	
	public void userMoved(Move usersMove){
		System.out.println("User Moved");
		gui.aiMove(ai.move(usersMove));
	}
	
	public AI getAI(){
		return ai;
	}
	
}
