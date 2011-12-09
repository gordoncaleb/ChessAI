package chessAI;

import java.util.Vector;

import chessBackend.Board;
import chessBackend.GameStatus;
import chessBackend.MoveNote;
import chessBackend.Player;
import chessBackend.Move;

public class DecisionNode {
	private DecisionNode parent;
	private Vector<DecisionNode> children;
	private Board board;
	private Player player;
	private Move nodeMove;
	private int moveValue;
	private int chosenPathValue;
	private DecisionNode chosenChild;
	private GameStatus status;
	
	public DecisionNode(DecisionNode parent, Move nodeMove, int moveValue, Board board, Player player){
		this.parent = parent;
		this.children = new Vector<DecisionNode>();
		this.nodeMove = nodeMove;
		this.board = board;
		this.player = player;
		this.moveValue = moveValue;
		this.status = GameStatus.IN_PLAY;
		
		if(nodeMove != null)
			this.nodeMove.setNode(this);
	}
	
	public DecisionNode getChild(int child){
		return children.elementAt(child);
	}
	
	public int getChildrenSize(){
		return children.size();
	}
	
	public boolean hasChildren(){
		if(children.size() != 0)
			return true;
		else
			return false;
	}
	
	public void addChild(DecisionNode node){
		children.add(node);
	}
	
	public void removeChild(DecisionNode node){
		children.remove(node);
	}
	
	public void removeChild(int nodePos){
		children.remove(nodePos);
	}
	
	public void removeAllChildren(){
		children.removeAllElements();
	}

	public Move getNodeMove() {
		return nodeMove;
	}

	public void setNodeMove(Move nodeMove) {
		this.nodeMove = nodeMove;
	}
	
	public void setParent(DecisionNode parent){
		this.parent = parent;
	}

	public DecisionNode getParent() {
		return parent;
	}
	
	public void setChosenChild(DecisionNode chosenChild){
		this.chosenChild = chosenChild;
	}
	
	public DecisionNode getChosenChild(){
		return chosenChild;
	}

	public Vector<DecisionNode> getChildren() {
		return children;
	}
	
	public int getMoveValue(){
		return moveValue;
	}
	
	public int getChosenPathValue() {
		return chosenPathValue;
	}

	public void setChosenPathValue(int chosenPathValue) {
		this.chosenPathValue = chosenPathValue;
	}

	public Board getBoard() {
		return board;
	}

	public Player getPlayer() {
		return player;
	}
	
	public void finalize(){
		//System.out.println("Move (" + nodeMove.toString() + ") has been destroyed!");
	}
	
	public GameStatus getStatus() {
		return status;
	}

	public void setStatus(GameStatus status) {
		this.status = status;
		
		if(status == GameStatus.CHECK){
			boolean changedValidMoves = board.inCheck(player);
			
			if(changedValidMoves){
				Vector<DecisionNode> invalidMoves = new Vector<DecisionNode>();
				DecisionNode child;
				for(int c=0;c<children.size();c++){
					child = children.elementAt(c);
					if(child.getNodeMove().getNote() == MoveNote.INVALIDATED){
						invalidMoves.add(child);
					}
				}
				
				for(int c=0;c<invalidMoves.size();c++){
					children.remove(invalidMoves.elementAt(c));
				}
			}
		}
	}

	public String toString(){
		boolean chosen;
		if(parent!=null)
			chosen = parent.getChosenChild()==this;
		else
			chosen = false;
		
		if(nodeMove!=null)
			return nodeMove.toString() + " Move Value =" + moveValue + " Chosen Path Value =" + chosenPathValue + " Chosen: " + chosen;
		else
			return "Board Start";
	}

}
