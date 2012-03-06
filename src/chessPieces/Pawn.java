package chessPieces;

import java.util.Vector;

import chessBackend.Board;
import chessBackend.MoveNote;
import chessBackend.Player;
import chessBackend.Move;

public class Pawn extends Piece {

	public Pawn(Player player, int row, int col, boolean moved) {
		super(player, row, col, moved);
	}

	public PieceID getPieceID() {
		return PieceID.PAWN;
	}

	public String getName() {
		return "Pawn";
	}

	public String getStringID() {
		return "P";
	}

	public Vector<Move> generateValidMoves(Board board) {
		Vector<Move> validMoves = new Vector<Move>();
		int currentRow = this.getRow();
		int currentCol = this.getCol();
		Player player = this.getPlayer();
		int dir;
		int fifthRank;
		int bonus;
		Move validMove;

		int[] lr = { 1, -1 };

		if (player == Player.USER) {
			dir = -1;
			fifthRank = 3;
		} else {
			dir = 1;
			fifthRank = 4;
		}

		if (board.checkPiece(currentRow + dir, currentCol, player) == PositionStatus.NO_PIECE) {
			bonus = PositionBonus.getPawnPositionBonus(currentRow, currentCol, currentRow + dir, currentCol, this.getPlayer());
			validMove = new Move(currentRow, currentCol, currentRow + dir, currentCol, bonus, MoveNote.NONE);
			if ((currentRow + dir) == 0 || (currentRow + dir) == 7) {
				validMove.setNote(MoveNote.NEW_QUEEN);
				validMove.setValue(Values.QUEEN_VALUE);
			}
			validMoves.add(validMove);

			if (!this.hasMoved() && board.checkPiece(currentRow + 2 * dir, currentCol, player) == PositionStatus.NO_PIECE) {
				bonus = PositionBonus.getPawnPositionBonus(currentRow, currentCol, currentRow + 2 * dir, currentCol, this.getPlayer());
				validMoves.add(new Move(currentRow, currentCol, currentRow + 2 * dir, currentCol, bonus, MoveNote.PAWN_LEAP));
			}

		}

		//Check left and right attack angles
		for (int i = 0; i < lr.length; i++) {
			if (board.checkPiece(currentRow + dir, currentCol + lr[i], player) == PositionStatus.ENEMY) {
				validMove = new Move(currentRow, currentCol, currentRow + dir, currentCol + lr[i]);
				if ((currentRow + dir) == 0 || (currentRow + dir) == 7) {
					validMove.setNote(MoveNote.NEW_QUEEN);
					validMove.setValue(Values.QUEEN_VALUE + board.getPieceValue(currentRow + dir, currentCol + lr[i]));
				} else {
					validMove.setValue(board.getPieceValue(currentRow + dir, currentCol + lr[i]));
				}

				validMove.setPieceTaken(board.getPiece(currentRow + dir, currentCol + lr[i]));
				validMoves.add(validMove);
			}
		}

		//Check left and right en passant rule
		if (currentRow == fifthRank && board.getLastMoveMade() != null) {
			for (int i = 0; i < lr.length; i++) {
				if (board.checkPiece(fifthRank, currentCol + lr[i], player) == PositionStatus.ENEMY) {
					if (board.getLastMoveMade().equals(fifthRank + 2 * dir, currentCol + lr[i], fifthRank, currentCol + lr[i]) && !board.getLastMoveMade().hadMoved()) {
						validMove = new Move(currentRow, currentCol, currentRow + dir, currentCol + lr[i]);
						validMove.setValue(board.getPieceValue(fifthRank, currentCol + lr[i]));
						validMove.setPieceTaken(board.getPiece(fifthRank, currentCol + lr[i]));
						validMove.setNote(MoveNote.ENPASSANT);
						validMoves.add(validMove);
						
					}
				}
			}
		}

		return validMoves;

	}

	public Piece getCopy(Board board) {
		return new Pawn(this.getPlayer(), this.getRow(), this.getCol(), this.hasMoved());
	}

}
