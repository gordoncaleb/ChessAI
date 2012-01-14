package chessPieces;

import java.util.Vector;

import chessBackend.Board;
import chessBackend.MoveNote;
import chessBackend.Player;
import chessBackend.Move;

public class Pawn extends PieceBase implements Piece {

	public Pawn(Player player, int xpos, int ypos) {
		super(player, xpos, ypos);
		this.setPieceValue(Values.PAWN_VALUE);
	}

	public Pawn(Player player, int row, int col, boolean moved, int value) {
		super(player, row, col, moved, value);
	}

	public PieceID getPieceID() {
		return PieceID.PAWN;
	}
	
	public String getName(){
		return "Pawn";
	}
	
	public void updateValue() {
		int bonus = PositionBonus.getPawnPositionBonus(this.getRow(), this.getCol(), this.getPlayer());
		setPieceValue(Values.PAWN_VALUE + bonus);
	}

	public Vector<Move> generateValidMoves(Board board) {
		Vector<Move> validMoves = new Vector<Move>();
		int currentRow = this.getRow();
		int currentCol = this.getCol();
		Player player = this.getPlayer();
		int dir;
		int bonus;
		Move validMove;

		if (player == Player.USER) {
			dir = -1;
		} else {
			dir = 1;
		}

		if (board.checkPiece(currentRow + dir, currentCol, player) == PositionStatus.NO_PIECE) {
			bonus = PositionBonus.getPawnPositionBonus(currentRow, currentCol, currentRow + dir, currentCol, this.getPlayer());
			validMove = new Move(currentRow, currentCol, currentRow + dir, currentCol,bonus,MoveNote.NONE);
			if((currentRow + dir) == 0 || (currentRow + dir) == 7){
				validMove.setNote(MoveNote.NEW_QUEEN);
				validMove.setValue(Values.QUEEN_VALUE);
			}
			validMoves.add(validMove);
			

			if (!this.hasMoved() && board.checkPiece(currentRow + 2*dir, currentCol, player) == PositionStatus.NO_PIECE) {
				bonus = PositionBonus.getPawnPositionBonus(currentRow, currentCol, currentRow + 2 * dir, currentCol, this.getPlayer());
				validMoves.add(new Move(currentRow, currentCol, currentRow + 2 * dir, currentCol,bonus,MoveNote.NONE));
			}

		}

		if (board.checkPiece(currentRow + dir, currentCol - 1, player) == PositionStatus.ENEMY) {
			Piece piece = board.getPiece(currentRow + dir, currentCol - 1);
			validMove = new Move(currentRow, currentCol, currentRow + dir, currentCol - 1);
			if((currentRow + dir) == 0 || (currentRow + dir) == 7){
				validMove.setNote(MoveNote.NEW_QUEEN);
				validMove.setValue(Values.QUEEN_VALUE + piece.getPieceValue());
			}else{
				validMove.setValue(piece.getPieceValue());
			}
			validMove.setPieceTaken(piece);
			validMoves.add(validMove);
		}

		if (board.checkPiece(currentRow + dir, currentCol + 1, player) == PositionStatus.ENEMY) {
			Piece piece = board.getPiece(currentRow + dir, currentCol + 1);
			validMove = new Move(currentRow, currentCol, currentRow + dir, currentCol + 1);
			if((currentRow + dir) == 0 || (currentRow + dir) == 7){
				validMove.setNote(MoveNote.NEW_QUEEN);
				validMove.setValue(Values.QUEEN_VALUE + piece.getPieceValue());
			}else{
				validMove.setValue(piece.getPieceValue());
			}
			
			validMove.setPieceTaken(piece);
			validMoves.add(validMove);
		}
		
		return validMoves;

	}

	public Piece getCopy(Board board) {
		return new Pawn(this.getPlayer(), this.getRow(), this.getCol(), this.hasMoved(), this.getPieceValue());
	}

}
