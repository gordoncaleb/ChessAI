package chessPieces;

import java.util.Vector;

import chessBackend.Board;
import chessBackend.Player;
import chessBackend.Move;

public interface Piece {
	
	public int getRow();
	public int getCol();
	public void setPos(int row, int col);
	public void move(Move m);
	public Player getPlayer();
	public PieceID getPieceID();
	public String getName();
	public int getPieceValue();
	public void setPieceValue(int value);
	public Board getBoard();
	public void setBoard(Board board);
	public boolean hasMoved();
	
	public void generateValidMoves();
	public Move checkIfValidMove(Move newMove);
	public Vector<Move> getValidMoves();
	public void clearValidMoves();
	
	public Piece getCopy();

}
