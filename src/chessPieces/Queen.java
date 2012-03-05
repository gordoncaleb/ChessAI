package chessPieces;

import java.util.Vector;

import chessBackend.Board;
import chessBackend.MoveNote;
import chessBackend.Player;
import chessBackend.Move;

public class Queen extends Piece{
	private static int[][] QUEENMOVES = { { 1, 1, -1, -1, 1, -1, 0, 0 }, { 1, -1, 1, -1, 0, 0, 1, -1 } };

	public Queen(Player player, int row, int col, boolean moved) {
		super(player, row, col, moved);
	}

	public PieceID getPieceID() {
		return PieceID.QUEEN;
	}

	public String getName() {
		return "Queen";
	}
	
	public String getStringID(){
		return "Q";
	}

	public Vector<Move> generateValidMoves(Board board, Move lastMoveMade) {
		Vector<Move> validMoves = new Vector<Move>();
		int currentRow = this.getRow();
		int currentCol = this.getCol();
		Player player = this.getPlayer();
		int nextRow;
		int nextCol;
		PositionStatus pieceStatus;

		int i = 1;
		for (int d = 0; d < 8; d++) {
			nextRow = currentRow + i * QUEENMOVES[0][d];
			nextCol = currentCol + i * QUEENMOVES[1][d];
			pieceStatus = board.checkPiece(nextRow, nextCol, player);

			while (pieceStatus == PositionStatus.NO_PIECE) {
				validMoves.add(new Move(currentRow, currentCol, nextRow, nextCol,Values.RISK_QUEEN,MoveNote.NONE));
				i++;
				nextRow = currentRow + i * QUEENMOVES[0][d];
				nextCol = currentCol + i * QUEENMOVES[1][d];
				pieceStatus = board.checkPiece(nextRow, nextCol, player);
			}

			if (pieceStatus == PositionStatus.ENEMY) {
				Piece piece = board.getPiece(nextRow, nextCol);
				Move move = new Move(currentRow, currentCol, nextRow, nextCol,board.getPieceValue(nextRow,nextCol));
				move.setPieceTaken(piece);
				validMoves.add(move);
			}

			i = 1;
		}
		
		return validMoves;

	}

	public Piece getCopy(Board board) {
		return new Queen(this.getPlayer(), this.getRow(), this.getCol(), this.hasMoved());
	}
}
