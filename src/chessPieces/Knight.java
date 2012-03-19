package chessPieces;

import java.util.Vector;

import chessBackend.BitBoard;
import chessBackend.Board;
import chessBackend.Player;
import chessBackend.Move;

public class Knight extends Piece {
	private static int[][] KNIGHTMOVES = { { 2, 2, -2, -2, 1, -1, 1, -1 }, { 1, -1, 1, -1, 2, 2, -2, -2 } };

	public Knight(Player player, int row, int col, boolean moved) {
		super(player, row, col, moved);
	}

	public PieceID getPieceID() {
		return PieceID.KNIGHT;
	}

	public String getName() {
		return "Knight";
	}

	public String getStringID() {
		return "N";
	}

	public Vector<Move> generateValidMoves(Board board, long[] nullMoveInfo, long[] posBitBoard) {
		Vector<Move> validMoves = new Vector<Move>();
		int currentRow = this.getRow();
		int currentCol = this.getCol();
		int nextRow;
		int nextCol;
		int bonus;
		PositionStatus pieceStatus;
		Player player = this.getPlayer();
		Move move;

		for (int i = 0; i < 8; i++) {
			nextRow = currentRow + KNIGHTMOVES[0][i];
			nextCol = currentCol + KNIGHTMOVES[1][i];
			pieceStatus = board.checkPiece(nextRow, nextCol, player);

			if (pieceStatus != PositionStatus.OFF_BOARD) {
				bonus = PositionBonus.getKnightPositionBonus(currentRow, currentCol, nextRow, nextCol, this.getPlayer());

				if (pieceStatus == PositionStatus.NO_PIECE) {
					if (isValidMove(nextRow, nextCol, nullMoveInfo)) {
						move = new Move(currentRow, currentCol, nextRow, nextCol, bonus);
						validMoves.add(move);
					}
				}

				if (pieceStatus == PositionStatus.ENEMY) {
					if (isValidMove(nextRow, nextCol, nullMoveInfo)) {
						int pieceValue = board.getPieceValue(nextRow, nextCol);
						move = new Move(currentRow, currentCol, nextRow, nextCol, pieceValue + bonus);
						move.setPieceTaken(board.getPiece(nextRow, nextCol));
						validMoves.add(move);
					}
				}

			}
		}

		return validMoves;

	}

	public void getNullMoveInfo(Board board, long[] nullMoveInfo) {

		int currentRow = this.getRow();
		int currentCol = this.getCol();
		int nextRow;
		int nextCol;
		PositionStatus pieceStatus;

		for (int i = 0; i < 8; i++) {
			nextRow = currentRow + KNIGHTMOVES[0][i];
			nextCol = currentCol + KNIGHTMOVES[1][i];

			pieceStatus = board.checkPiece(nextRow, nextCol, getPlayer());

			if (pieceStatus != PositionStatus.OFF_BOARD) {

				if (board.getPieceID(nextRow, nextCol) == PieceID.KING && pieceStatus == PositionStatus.ENEMY) {
					nullMoveInfo[1] &= this.getBit();
				}

				nullMoveInfo[0] |= BitBoard.getMask(nextRow, nextCol);
			}
		}

	}

	public Piece getCopy(Board board) {
		return new Knight(this.getPlayer(), this.getRow(), this.getCol(), this.hasMoved());
	}
}
