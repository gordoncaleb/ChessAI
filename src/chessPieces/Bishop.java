package chessPieces;

import java.util.Vector;

import chessBackend.BitBoard;
import chessBackend.Board;
import chessBackend.MoveNote;
import chessBackend.Side;
import chessBackend.Move;

public class Bishop extends Piece {
	private static int[][] BISHOPMOVES = { { 1, 1, -1, -1 }, { 1, -1, 1, -1 } };

	public Bishop(Side player, int row, int col, boolean moved) {
		super(player, row, col, moved);
	}

	public PieceID getPieceID() {
		return PieceID.BISHOP;
	}

	public String getName() {
		return "Bishop";
	}

	public String getStringID() {
		return "B";
	}

	public Vector<Move> generateValidMoves(Board board, long[] nullMoveInfo, long[] posBitBoard) {
		Vector<Move> validMoves = new Vector<Move>();
		int currentRow = this.getRow();
		int currentCol = this.getCol();
		int nextRow;
		int nextCol;
		PositionStatus pieceStatus;
		Side player = this.getSide();

		int i = 1;
		for (int d = 0; d < 4; d++) {
			nextRow = currentRow + i * BISHOPMOVES[0][d];
			nextCol = currentCol + i * BISHOPMOVES[1][d];
			pieceStatus = board.checkPiece(nextRow, nextCol, player);

			while (pieceStatus == PositionStatus.NO_PIECE) {

				if (isValidMove(nextRow, nextCol, nullMoveInfo)) {
					validMoves.add(new Move(currentRow, currentCol, nextRow, nextCol, 0, MoveNote.NONE));
				}

				i++;
				nextRow = currentRow + i * BISHOPMOVES[0][d];
				nextCol = currentCol + i * BISHOPMOVES[1][d];
				pieceStatus = board.checkPiece(nextRow, nextCol, player);

			}

			if (pieceStatus == PositionStatus.ENEMY) {
				if (isValidMove(nextRow, nextCol, nullMoveInfo)) {
					Move move = new Move(currentRow, currentCol, nextRow, nextCol, board.getPieceValue(nextRow, nextCol));
					move.setPieceTaken(board.getPiece(nextRow, nextCol));
					validMoves.add(move);
				}
			}

			i = 1;
		}

		return validMoves;

	}

	public void getNullMoveInfo(Board board, long[] nullMoveInfo) {
		long bitAttackVector = 0;
		long bitAttackCompliment = 0;
		boolean inCheck = false;
		Piece blockingPiece;

		int currentRow = this.getRow();
		int currentCol = this.getCol();
		int nextRow;
		int nextCol;
		PositionStatus pieceStatus;
		Side player = this.getSide();
		
		long bitPosition = this.getBit();

		int i = 1;
		for (int d = 0; d < 4; d++) {
			nextRow = currentRow + i * BISHOPMOVES[0][d];
			nextCol = currentCol + i * BISHOPMOVES[1][d];
			pieceStatus = board.checkPiece(nextRow, nextCol, player);

			while (pieceStatus == PositionStatus.NO_PIECE) {
				bitAttackVector |= BitBoard.getMask(nextRow, nextCol);
				i++;
				nextRow = currentRow + i * BISHOPMOVES[0][d];
				nextCol = currentCol + i * BISHOPMOVES[1][d];
				pieceStatus = board.checkPiece(nextRow, nextCol, player);
			}

			bitAttackVector |= BitBoard.getMask(nextRow, nextCol);

			if (pieceStatus == PositionStatus.ENEMY) {
				blockingPiece = board.getPiece(nextRow, nextCol);

				if (blockingPiece.getPieceID() == PieceID.KING) {
					nullMoveInfo[1] &= (bitAttackVector | bitPosition);
					inCheck = true;
				}
				
				i++;
				nextRow = currentRow + i * BISHOPMOVES[0][d];
				nextCol = currentCol + i * BISHOPMOVES[1][d];
				pieceStatus = board.checkPiece(nextRow, nextCol, player);

				while (pieceStatus == PositionStatus.NO_PIECE) {
					bitAttackCompliment |= BitBoard.getMask(nextRow, nextCol);
					i++;
					nextRow = currentRow + i * BISHOPMOVES[0][d];
					nextCol = currentCol + i * BISHOPMOVES[1][d];
					pieceStatus = board.checkPiece(nextRow, nextCol, player);
				}

				if (pieceStatus != PositionStatus.OFF_BOARD) {
					if (board.getPieceID(nextRow, nextCol) == PieceID.KING) {
						blockingPiece.setBlockingVector(bitAttackCompliment | bitAttackVector | bitPosition);
					}
				}

			}

			nullMoveInfo[0] |= bitAttackVector;

			if (inCheck) {
				nullMoveInfo[2] |= bitAttackCompliment;
			}

			bitAttackVector = 0;

			i = 1;
		}

	}

	public Piece getCopy(Board board) {
		return new Bishop(this.getSide(), this.getRow(), this.getCol(), this.hasMoved());
	}

}
