package chessPieces;

import java.util.Vector;

import chessBackend.Board;
import chessBackend.MoveNote;
import chessBackend.Player;
import chessBackend.Move;

public class Rook extends PieceBase implements Piece {
	private static int[][] ROOKMOVES = { { 1, -1, 0, 0 }, { 0, 0, 1, -1 } };
	boolean rightRook;
	
	public Rook(Player player, int row, int col, boolean rightRook) {
		super(player,row,col);
		setPieceValue(Values.ROOK_VALUE);
		this.rightRook = rightRook;
	}
	
	public Rook(Player player, int row, int col, boolean moved, int value, boolean rightRook) {
		super(player,row,col,moved,value);
		this.rightRook = rightRook;
	}
	
	public PieceID getPieceID(){
		return PieceID.ROOK;
	}
	
	public static PieceID getID(){
		return PieceID.ROOK;
	}
	
	public String getName(){
		return "Rook";
	}
	
	public Vector<Move> generateValidMoves(Board board) {
		Vector<Move> validMoves = new Vector<Move>();
		int currentRow = this.getRow();
		int currentCol = this.getCol();
		Player player = this.getPlayer();
		int nextRow;
		int nextCol;
		PositionStatus pieceStatus;

		int i=1;
		for (int d = 0; d < 4; d++) {
			nextRow = currentRow + i*ROOKMOVES[0][d];
			nextCol = currentCol + i*ROOKMOVES[1][d];
			pieceStatus = board.checkPiece(nextRow,nextCol,player);
			
			while (pieceStatus == PositionStatus.NO_PIECE) {
				if(rightRook && board.canCastleNear(player) || !rightRook && board.canCastleFar(player)){
					validMoves.add(new Move(currentRow, currentCol, nextRow,nextCol,Values.CASTLE_ABILITY_LOST_VALUE,MoveNote.NONE));
				}else{
					validMoves.add(new Move(currentRow, currentCol, nextRow,nextCol,0,MoveNote.NONE));
				}
				i++;
				nextRow = currentRow + i*ROOKMOVES[0][d];
				nextCol = currentCol + i*ROOKMOVES[1][d];
				pieceStatus = board.checkPiece(nextRow,nextCol,player);
			}
			
			if(pieceStatus == PositionStatus.ENEMY){
				Piece piece = board.getPiece(nextRow, nextCol);
				if(rightRook && board.canCastleNear(player) || !rightRook && board.canCastleFar(player)){
					validMoves.add(new Move(currentRow, currentCol, nextRow,nextCol,piece.getPieceValue() + Values.CASTLE_ABILITY_LOST_VALUE,piece.getPieceID()));
				}else{
					validMoves.add(new Move(currentRow, currentCol, nextRow,nextCol,piece.getPieceValue(),piece.getPieceID()));
				}
			}
			
			i=1;
		}
		
		return validMoves;
		
	}
	
	public Piece getCopy(Board board){
		return new Rook(this.getPlayer(),this.getRow(),this.getCol(),this.hasMoved(),this.getPieceValue(),rightRook);
	}
}
