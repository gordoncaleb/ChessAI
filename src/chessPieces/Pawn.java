package chessPieces;

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

	public void generateValidMoves() {
		Board board = this.getBoard();
		int currentRow = this.getRow();
		int currentCol = this.getCol();
		Player player = this.getPlayer();
		int dir;
		Move validMove;

		this.clearValidMoves();

		if (player == Player.USER) {
			dir = -1;
		} else {
			dir = 1;
		}

		if (!board.hasPiece(currentRow + dir, currentCol)) {
			validMove = new Move(currentRow, currentCol, currentRow + dir, currentCol);
			if((currentRow + dir) == 0 || (currentRow + dir) == 7){
				validMove.setNote(MoveNote.NEW_QUEEN);
				validMove.setValue(Values.QUEEN_VALUE);
			}
			this.addValidMove(validMove);

			if (!this.hasMoved() && !board.hasPiece(currentRow + 2 * dir, currentCol)) {
				this.addValidMove(new Move(currentRow, currentCol, currentRow + 2 * dir, currentCol));
			}

		}

		if (board.checkPiece(currentRow + dir, currentCol - 1, player) == PositionStatus.ENEMY) {
			Piece piece = board.getPiece(currentRow + dir, currentCol - 1);
			validMove = new Move(currentRow, currentCol, currentRow + dir, currentCol - 1);
			if((currentRow + dir) == 0 || (currentRow + dir) == 7){
				validMove.setNote(MoveNote.NEW_QUEEN);
				validMove.setValue(Values.QUEEN_VALUE + piece.getPieceValue());
			}else{
				validMove.setNote(MoveNote.TAKE_PIECE);
				validMove.setValue(piece.getPieceValue());
			}
			validMove.setPieceTaken(piece.getPieceID());
			this.addValidMove(validMove);
		}

		if (board.checkPiece(currentRow + dir, currentCol + 1, player) == PositionStatus.ENEMY) {
			Piece piece = board.getPiece(currentRow + dir, currentCol + 1);
			validMove = new Move(currentRow, currentCol, currentRow + dir, currentCol + 1);
			if((currentRow + dir) == 0 || (currentRow + dir) == 7){
				validMove.setNote(MoveNote.NEW_QUEEN);
				validMove.setValue(Values.QUEEN_VALUE + piece.getPieceValue());
			}else{
				validMove.setNote(MoveNote.TAKE_PIECE);
				validMove.setValue(piece.getPieceValue());
			}
			
			validMove.setPieceTaken(piece.getPieceID());
			this.addValidMove(validMove);
		}

	}

	public Piece getCopy() {
		return new Pawn(this.getPlayer(), this.getRow(), this.getCol(), this.hasMoved(), this.getPieceValue());
	}

}
