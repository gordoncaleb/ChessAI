package chessPieces;

import java.util.Vector;

import chessBackend.Board;
import chessBackend.MoveNote;
import chessBackend.Player;
import chessBackend.Move;

public class King extends PieceBase implements Piece {
	private static int[][] KINGMOVES = { { 1, 1, -1, -1, 1, -1, 0, 0 }, { 1, -1, 1, -1, 0, 0, 1, -1 } };

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
	
	public static PieceID getID() {
		return PieceID.KING;
	}

	public String getName() {
		return "King";
	}

	public Vector<Move> generateValidMoves(Board board) {
		Vector<Move> validMoves = new Vector<Move>();
		int currentRow = this.getRow();
		int currentCol = this.getCol();
		Player player = this.getPlayer();
		int nextRow;
		int nextCol;
		PositionStatus pieceStatus;
		Move move;
		
		boolean canCastleFar = board.canCastleFar(player);
		boolean canCastleNear = board.canCastleNear(player);

		for (int d = 0; d < 8; d++) {
			nextRow = currentRow + KINGMOVES[0][d];
			nextCol = currentCol + KINGMOVES[1][d];
			pieceStatus = board.checkPiece(nextRow, nextCol, player);

			if (pieceStatus == PositionStatus.NO_PIECE) {

				if (canCastleFar || canCastleNear) {
					// The player loses points for losing the ability to castle
					move = new Move(currentRow, currentCol, nextRow, nextCol, Values.CASTLE_ABILITY_LOST_VALUE);
					move.setFirstMove(!this.hasMoved());
					validMoves.add(move);
				} else {
					move = new Move(currentRow, currentCol, nextRow, nextCol, 0, MoveNote.NONE);
					move.setFirstMove(!this.hasMoved());
					validMoves.add(move);
				}
			}

			if (pieceStatus == PositionStatus.ENEMY) {
				Piece piece = board.getPiece(nextRow, nextCol);
				move = new Move(currentRow, currentCol, nextRow, nextCol, piece.getPieceValue());
				move.setPieceTaken(piece);
				move.setFirstMove(!this.hasMoved());
				validMoves.add(move);
			}

		}

		//add possible castle move
		if(canCastleFar && !board.isInCheck()){
			validMoves.add(new Move(currentRow, currentCol, currentRow, currentCol-2, Values.CASTLE_VALUE, MoveNote.CASTLE_FAR));
		}
		
		if(canCastleNear && !board.isInCheck()){
			validMoves.add(new Move(currentRow, currentCol, currentRow, currentCol+2, Values.CASTLE_VALUE, MoveNote.CASTLE_NEAR));
		}
		
		return validMoves;

	}

	public Piece getCopy(Board board) {
		return new King(this.getPlayer(), this.getRow(), this.getCol(), this.hasMoved(), this.getPieceValue());
	}
}
