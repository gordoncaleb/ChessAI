package chessPieces;

import java.util.Vector;

import chessBackend.Board;
import chessBackend.Player;
import chessBackend.Move;

public class Rook extends Piece{
	private static int[][] ROOKMOVES = { { 1, -1, 0, 0 }, { 0, 0, 1, -1 } };

	public Rook(Player player, int row, int col, boolean moved) {
		super(player, row, col, moved);
	}

	public PieceID getPieceID() {
		return PieceID.ROOK;
	}

	public String getName() {
		return "Rook";
	}
	
	public String getStringID(){
		return "R";
	}

	public Vector<Move> generateValidMoves(Board board, Move lastMoveMade) {
		Vector<Move> validMoves = new Vector<Move>();
		int currentRow = this.getRow();
		int currentCol = this.getCol();
		Player player = this.getPlayer();
		int nextRow;
		int nextCol;
		PositionStatus pieceStatus;
		Move move;

		int i = 1;
		for (int d = 0; d < 4; d++) {
			nextRow = currentRow + i * ROOKMOVES[0][d];
			nextCol = currentCol + i * ROOKMOVES[1][d];
			pieceStatus = board.checkPiece(nextRow, nextCol, player);

			while (pieceStatus == PositionStatus.NO_PIECE) {

				if (!this.hasMoved() && !board.kingHasMoved(player)) {
					move = new Move(currentRow, currentCol, nextRow, nextCol, Values.CASTLE_ABILITY_LOST_VALUE);
				} else {
					move = new Move(currentRow, currentCol, nextRow, nextCol);
				}

				validMoves.add(move);

				i++;
				nextRow = currentRow + i * ROOKMOVES[0][d];
				nextCol = currentCol + i * ROOKMOVES[1][d];
				pieceStatus = board.checkPiece(nextRow, nextCol, player);
			}

			if (pieceStatus == PositionStatus.ENEMY) {
				Piece piece = board.getPiece(nextRow, nextCol);
				move = new Move(currentRow, currentCol, nextRow, nextCol);
				move.setPieceTaken(piece);
				if (!this.hasMoved() && !board.kingHasMoved(player)) {
					move.setValue(board.getPieceValue(nextRow,nextCol) + Values.CASTLE_ABILITY_LOST_VALUE);
				} else {
					move.setValue(board.getPieceValue(nextRow,nextCol));
				}
				validMoves.add(move);
			}

			i = 1;
		}

		return validMoves;

	}

	public Piece getCopy(Board board) {
		return new Rook(this.getPlayer(), this.getRow(), this.getCol(), this.hasMoved());
	}
}
