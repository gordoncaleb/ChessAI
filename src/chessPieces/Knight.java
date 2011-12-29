package chessPieces;

import java.util.Vector;

import chessBackend.Board;
import chessBackend.MoveNote;
import chessBackend.Player;
import chessBackend.Move;

public class Knight extends PieceBase implements Piece {
	private static int[][] KNIGHTMOVES = { { 2, 2, -2, -2, 1, -1, 1, -1 }, { 1, -1, 1, -1, 2, 2, -2, -2 } };

	public Knight(Player player, int xpos, int ypos) {
		super(player, xpos, ypos);
		int bonus = PositionBonus.getKnightPositionBonus(this.getRow(), this.getCol(), this.getPlayer());
		setPieceValue(Values.KING_VALUE+bonus);
	}

	public Knight(Player player, int row, int col, boolean moved, int value) {
		super(player, row, col, moved, value);
	}

	public PieceID getPieceID() {
		return PieceID.KNIGHT;
	}
	
	public static PieceID getID() {
		return PieceID.KNIGHT;
	}

	public String getName() {
		return "Knight";
	}

	public void updateValue(Board board) {
		int bonus = PositionBonus.getKnightPositionBonus(this.getRow(), this.getCol(), this.getPlayer());
		setPieceValue(board.getKnightValue()+bonus);
	}

	public Vector<Move> generateValidMoves(Board board) {
		Vector<Move> validMoves = new Vector<Move>();
		int currentRow = this.getRow();
		int currentCol = this.getCol();
		int nextRow;
		int nextCol;
		int bonus;
		PositionStatus pieceStatus;
		Player player = this.getPlayer();

		for (int i = 0; i < 8; i++) {
			nextRow = currentRow + KNIGHTMOVES[0][i];
			nextCol = currentCol + KNIGHTMOVES[1][i];
			pieceStatus = board.checkPiece(nextRow, nextCol, player);
			
			if (pieceStatus != PositionStatus.OFF_BOARD) {
				bonus = PositionBonus.getKnightPositionBonus(currentRow, currentCol, nextRow, nextCol, this.getPlayer());

				if (pieceStatus == PositionStatus.NO_PIECE) {
					validMoves.add(new Move(currentRow, currentCol, nextRow, nextCol, bonus, MoveNote.NONE));
				}

				if (pieceStatus == PositionStatus.ENEMY) {
					Piece piece = board.getPiece(nextRow, nextCol);
					int pieceValue = piece.getPieceValue();
					PieceID pieceID = piece.getPieceID();
					Move move = new Move(currentRow, currentCol, nextRow, nextCol, pieceValue + bonus, MoveNote.TAKE_PIECE);
					move.setPieceTaken(pieceID);
					validMoves.add(move);
				}
			}
		}
		
		return validMoves;

	}

	public Piece getCopy(Board board) {
		updateValue(board);
		return new Knight(this.getPlayer(), this.getRow(), this.getCol(), this.hasMoved(), this.getPieceValue());
	}
}
