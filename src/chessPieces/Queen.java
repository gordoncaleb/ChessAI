package chessPieces;

import chessBackend.Board;
import chessBackend.MoveNote;
import chessBackend.Player;
import chessBackend.Move;

public class Queen extends PieceBase implements Piece {
	private int[][] queenMoves = { { 1, 1, -1, -1, 1, -1, 0, 0 }, { 1, -1, 1, -1, 0, 0, 1, -1 } };

	public Queen(Player player, int xpos, int ypos) {
		super(player, xpos, ypos);
		setPieceValue(Values.QUEEN_VALUE);
	}

	public Queen(Player player, int row, int col, boolean moved, int value) {
		super(player, row, col, moved, value);
	}

	public PieceID getPieceID() {
		return PieceID.QUEEN;
	}

	public String getName() {
		return "Queen";
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

		int i = 1;
		for (int d = 0; d < 8; d++) {
			nextRow = currentRow + i * queenMoves[0][d];
			nextCol = currentCol + i * queenMoves[1][d];
			pieceStatus = board.checkPiece(nextRow, nextCol, player);

			while (pieceStatus == PositionStatus.NO_PIECE) {
				this.addValidMove(new Move(currentRow, currentCol, nextRow, nextCol,Values.RISK_QUEEN,MoveNote.NONE));
				i++;
				nextRow = currentRow + i * queenMoves[0][d];
				nextCol = currentCol + i * queenMoves[1][d];
				pieceStatus = board.checkPiece(nextRow, nextCol, player);
			}

			if (pieceStatus == PositionStatus.ENEMY) {
				Piece piece = board.getPiece(nextRow, nextCol);
				this.addValidMove(new Move(currentRow, currentCol, nextRow, nextCol,piece.getPieceValue(),piece.getPieceID()));
			}

			i = 1;
		}

	}

	public Piece getCopy() {
		return new Queen(this.getPlayer(), this.getRow(), this.getCol(), this.hasMoved(), this.getPieceValue());
	}
}
