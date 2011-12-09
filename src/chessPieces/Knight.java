package chessPieces;

import chessBackend.Board;
import chessBackend.MoveNote;
import chessBackend.Player;
import chessBackend.Move;

public class Knight extends PieceBase implements Piece {
	private int[][] knightMoves = {{2,2,-2,-2,1,-1,1,-1},{1,-1,1,-1,2,2,-2,-2}};

	public Knight(Player player, int xpos, int ypos) {
		super(player,xpos,ypos);
		setPieceValue(Values.KNIGHT_VALUE);
	}
	
	public Knight(Player player, int row, int col, boolean moved, int value) {
		super(player,row,col,moved,value);
	}
	
	public PieceID getPieceID(){
		return PieceID.KNIGHT;
	}
	
	public String getName(){
		return "Knight";
	}
	
	public void generateValidMoves() {
		Board board = this.getBoard();
		int currentRow = this.getRow();
		int currentCol = this.getCol();
		int nextRow;
		int nextCol;
		PositionStatus pieceStatus;
		Player player = this.getPlayer();
		
		this.clearValidMoves();
		
		for(int i=0;i<8;i++){
			nextRow = currentRow + knightMoves[0][i];
			nextCol = currentCol + knightMoves[1][i];
			pieceStatus = board.checkPiece(nextRow, nextCol, player);
			
			if(pieceStatus == PositionStatus.NO_PIECE){
				this.addValidMove(new Move(currentRow,currentCol,nextRow,nextCol,0, MoveNote.NONE));
			}
			
			if(pieceStatus == PositionStatus.ENEMY){
				PieceID pieceID = board.getPiece(nextRow, nextCol).getPieceID();
				this.addValidMove(new Move(currentRow,currentCol,nextRow,nextCol,pieceID));
			}
		}

	}
	
	public Piece getCopy(){
		return new Knight(this.getPlayer(),this.getRow(),this.getCol(),this.hasMoved(),this.getPieceValue());
	}
}
