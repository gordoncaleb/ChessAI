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
	public void updateValue();
	public boolean hasMoved();
	
	public Vector<Move> generateValidMoves(Board board);
	
	public Piece getCopy(Board board);

}
