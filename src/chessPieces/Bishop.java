package chessPieces;

import chessBackend.Board;
import chessBackend.MoveNote;
import chessBackend.Player;
import chessBackend.Move;

public class Bishop extends PieceBase implements Piece {
	private int[][] bishopMoves = { { 1, 1, -1, -1 }, { 1, -1, 1, -1 } };

	public Bishop(Player player, int xpos, int ypos) {
		super(player, xpos, ypos);
		setPieceValue(Values.BISHOP_VALUE);
	}
	
	public Bishop(Player player, int row, int col, boolean moved, int value) {
		super(player,row,col,moved,value);
	}

	public PieceID getPieceID(){
		return PieceID.BISHOP;
	}
	
	public static PieceID getID(){
		return PieceID.BISHOP;
	}
	
	public String getName(){
		return "Bishop";
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

		int i=1;
		for (int d = 0; d < 4; d++) {
			nextRow = currentRow + i*bishopMoves[0][d];
			nextCol = currentCol + i*bishopMoves[1][d];
			pieceStatus = board.checkPiece(nextRow,nextCol,player);
			
			while (pieceStatus == PositionStatus.NO_PIECE) {
				this.addValidMove(new Move(currentRow, currentCol, nextRow,nextCol,0,MoveNote.NONE));
				i++;
				nextRow = currentRow + i*bishopMoves[0][d];
				nextCol = currentCol + i*bishopMoves[1][d];
				pieceStatus = board.checkPiece(nextRow,nextCol,player);
			}
			
			if(pieceStatus == PositionStatus.ENEMY){
				Piece piece = board.getPiece(nextRow, nextCol);
				this.addValidMove(new Move(currentRow, currentCol, nextRow,nextCol,piece.getPieceValue(),piece.getPieceID()));
			}
			
			i=1;
		}

	}
	
	public Piece getCopy(){
		return new Bishop(this.getPlayer(),this.getRow(),this.getCol(),this.hasMoved(),this.getPieceValue());
	}

}
