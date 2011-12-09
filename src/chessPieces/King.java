package chessPieces;

import java.util.Vector;

import chessBackend.Board;
import chessBackend.MoveNote;
import chessBackend.Player;
import chessBackend.Move;

public class King extends PieceBase implements Piece {
	private int[][] kingMoves = { { 1, 1, -1, -1, 1, -1, 0, 0 }, { 1, -1, 1, -1, 0, 0, 1, -1 } };

	public King(Player player, int xpos, int ypos) {
		super(player, xpos, ypos);
		setPieceValue(Values.KING_VALUE);
	}

	public King(Player player, int row, int col, boolean moved, int value) {
		super(player, row, col, moved, value);
	}

	public PieceID getPieceID() {
		return PieceID.KING;
	}

	public String getName() {
		return "King";
	}

	public void generateValidMoves() {
		Board board = this.getBoard();
		int currentRow = this.getRow();
		int currentCol = this.getCol();
		Player player = this.getPlayer();
		int nextRow;
		int nextCol;
		PositionStatus pieceStatus;

		this.clearValidMoves();

		this.addValidMove(new Move(currentRow, currentCol, currentRow, currentCol, 0, MoveNote.DO_NOTHING));
		
		boolean canCastleFar = board.canCastleFar(player);
		boolean canCastleNear = board.canCastleNear(player);

		for (int d = 0; d < 8; d++) {
			nextRow = currentRow + kingMoves[0][d];
			nextCol = currentCol + kingMoves[1][d];
			pieceStatus = board.checkPiece(nextRow, nextCol, player);

			if (pieceStatus == PositionStatus.NO_PIECE) {

				if (this.hasMoved()) {
					this.addValidMove(new Move(currentRow, currentCol, nextRow, nextCol, 0, MoveNote.NONE));
				} else {
					// The player loses points for losing the ability to castle
					if (canCastleFar || canCastleNear) {
						this.addValidMove(new Move(currentRow, currentCol, nextRow, nextCol, Values.CASTLE_ABILITY_LOST_VALUE, MoveNote.NONE));
					}
				}
			}

			if (pieceStatus == PositionStatus.ENEMY) {
				PieceID pieceID = board.getPiece(nextRow, nextCol).getPieceID();
				this.addValidMove(new Move(currentRow, currentCol, nextRow, nextCol, pieceID));
			}

		}

		//add possible castle move
		if(canCastleFar){
			this.addValidMove(new Move(currentRow, currentCol, currentRow, currentCol-2, Values.CASTLE_VALUE, MoveNote.CASTLE_FAR));
		}
		
		if(canCastleNear){
			this.addValidMove(new Move(currentRow, currentCol, currentRow, currentCol+2, Values.CASTLE_VALUE, MoveNote.CASTLE_NEAR));
		}

	}

	// Since generateValidMoves is ran before the board is known to be in check
	// or not, this function is used to remove the possibility of castling that
	// the piece may have thought possible. You can't castle out of check!
	public boolean noCastle() {
		boolean changedValidMoves = false;
		Vector<Move> moves = this.getValidMoves();
		Move move;
		for (int m = 0; m < moves.size(); m++) {
			move = moves.elementAt(m);
			if (move.getNote() == MoveNote.CASTLE_FAR || move.getNote() == MoveNote.CASTLE_NEAR) {
				move.setNote(MoveNote.INVALIDATED);
				changedValidMoves = true;
			}
		}

		return changedValidMoves;
	}

	public Piece getCopy() {
		return new King(this.getPlayer(), this.getRow(), this.getCol(), this.hasMoved(), this.getPieceValue());
	}
}
