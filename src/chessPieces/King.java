package chessPieces;

import java.util.Vector;

import chessBackend.BitBoard;
import chessBackend.Board;
import chessBackend.MoveNote;
import chessBackend.Side;
import chessBackend.Move;

public class King extends Piece {
	private static int[][] KINGMOVES = { { 1, 1, -1, -1, 1, -1, 0, 0 }, { 1, -1, 1, -1, 0, 0, 1, -1 } };

	public King(Side player, int row, int col, boolean moved) {
		super(player, row, col, moved);
	}

	public PieceID getPieceID() {
		return PieceID.KING;
	}

	public String getName() {
		return "King";
	}

	public String getStringID() {
		return "K";
	}

	public Vector<Move> generateValidMoves(Board board, long[] nullMoveInfo, long[] posBitBoard) {
		Vector<Move> validMoves = new Vector<Move>();
		int currentRow = this.getRow();
		int currentCol = this.getCol();
		Side player = this.getSide();
		int nextRow;
		int nextCol;
		PositionStatus pieceStatus;
		Move move;

		for (int d = 0; d < 8; d++) {
			nextRow = currentRow + KINGMOVES[0][d];
			nextCol = currentCol + KINGMOVES[1][d];
			pieceStatus = board.checkPiece(nextRow, nextCol, player);

			if (pieceStatus == PositionStatus.NO_PIECE) {

				if (isValidMove(nextRow, nextCol, nullMoveInfo)) {
					if (!this.hasMoved() && (!board.farRookHasMoved(player) || !board.nearRookHasMoved(player))) {
						// The player loses points for losing the ability to
						// castle
						move = new Move(currentRow, currentCol, nextRow, nextCol, Values.CASTLE_ABILITY_LOST_VALUE);
						validMoves.add(move);
					} else {
						move = new Move(currentRow, currentCol, nextRow, nextCol, 0, MoveNote.NONE);
						validMoves.add(move);
					}
				}
			}

			if (pieceStatus == PositionStatus.ENEMY) {
				if (isValidMove(nextRow, nextCol, nullMoveInfo)) {
					move = new Move(currentRow, currentCol, nextRow, nextCol, board.getPieceValue(nextRow, nextCol));
					move.setPieceTaken(board.getPiece(nextRow, nextCol));
					validMoves.add(move);
				}
			}

		}

		long allPosBitBoard = posBitBoard[0] | posBitBoard[1];

		// add possible castle move
		if (canCastleFar(board, player, nullMoveInfo, allPosBitBoard) && !board.isInCheck()) {
			if (isValidMove(currentRow, currentCol - 2, nullMoveInfo)) {
				validMoves.add(new Move(currentRow, currentCol, currentRow, currentCol - 2, Values.CASTLE_VALUE, MoveNote.CASTLE_FAR));
			}
		}

		if (canCastleNear(board, player, nullMoveInfo, allPosBitBoard) && !board.isInCheck()) {
			if (isValidMove(currentRow, currentCol + 2, nullMoveInfo)) {
				validMoves.add(new Move(currentRow, currentCol, currentRow, currentCol + 2, Values.CASTLE_VALUE, MoveNote.CASTLE_NEAR));
			}
		}

		return validMoves;

	}

	public void getNullMoveInfo(Board board, long[] nullMoveInfo) {

		int currentRow = this.getRow();
		int currentCol = this.getCol();

		for (int i = 0; i < 8; i++) {
			nullMoveInfo[0] |= BitBoard.getMask(currentRow + KINGMOVES[0][i], currentCol + KINGMOVES[1][i]);
		}

	}

	public boolean isValidMove(int toRow, int toCol, long[] nullMoveInfo) {
		long mask = BitBoard.getMask(toRow, toCol);

		if ((mask & (nullMoveInfo[0] | nullMoveInfo[2])) == 0) {
			return true;
		} else {
			return false;
		}
	}

	public boolean canCastleFar(Board board, Side player, long[] nullMoveInfo, long allPosBitBoard) {

		if (board.kingHasMoved(player) || board.farRookHasMoved(player)) {
			return false;
		}

		long posClearMask;
		long checkFar;
		if (player == Side.BLACK) {
			posClearMask = BitBoard.BLACK_CASTLE_FAR;
			checkFar = BitBoard.BLACK_CHECK_FAR;
		} else {
			posClearMask = BitBoard.WHITE_CASTLE_FAR;
			checkFar = BitBoard.WHITE_CHECK_FAR;
		}

		if ((posClearMask & allPosBitBoard) == 0) {
			if ((checkFar & nullMoveInfo[0]) == 0) {
				return true;
			}
		}

		return false;

	}

	public boolean canCastleNear(Board board, Side player, long[] nullMoveInfo, long allPosBitBoard) {

		if (board.kingHasMoved(player) || board.nearRookHasMoved(player)) {
			return false;
		}

		long posClearMask;
		long checkNear;
		if (player == Side.BLACK) {
			posClearMask = BitBoard.BLACK_CASTLE_NEAR;
			checkNear = BitBoard.BLACK_CHECK_NEAR;
		} else {
			posClearMask = BitBoard.WHITE_CASTLE_NEAR;
			checkNear = BitBoard.WHITE_CHECK_NEAR;
		}

		if ((posClearMask & allPosBitBoard) == 0) {
			if ((checkNear & nullMoveInfo[0]) == 0) {
				return true;
			}
		}

		return false;
	}

	public Piece getCopy(Board board) {
		return new King(this.getSide(), this.getRow(), this.getCol(), this.hasMoved());
	}
}
