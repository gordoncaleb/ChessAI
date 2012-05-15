package chessPieces;

import java.util.Vector;

import chessBackend.BitBoard;
import chessBackend.Board;
import chessBackend.MoveNote;
import chessBackend.Side;
import chessBackend.Move;

public class Pawn{

	public Pawn() {
	}

	public static PieceID getPieceID() {
		return PieceID.PAWN;
	}

	public static String getName() {
		return "Pawn";
	}

	public static String getStringID() {
		return "P";
	}

	public static Vector<Move> generateValidMoves(Piece p, Board board, long[] nullMoveInfo, long[] posBitBoard) {
		Vector<Move> validMoves = new Vector<Move>();
		int currentRow = p.getRow();
		int currentCol = p.getCol();
		Side player = p.getSide();
		int dir;
		int fifthRank;
		int bonus;
		Move validMove;

		int[] lr = { 1, -1 };

		if (player == Side.WHITE) {
			dir = -1;
			fifthRank = 3;
		} else {
			dir = 1;
			fifthRank = 4;
		}

		if (board.checkPiece(currentRow + dir, currentCol, player) == PositionStatus.NO_PIECE) {

			if (p.isValidMove(currentRow + dir, currentCol, nullMoveInfo)) {

				bonus = PositionBonus.getPawnMoveBonus(currentRow, currentCol, currentRow + dir, currentCol, p.getSide());
				validMove = new Move(currentRow, currentCol, currentRow + dir, currentCol, bonus, MoveNote.NONE);
				if ((currentRow + dir) == 0 || (currentRow + dir) == 7) {
					validMove.setNote(MoveNote.NEW_QUEEN);
					validMove.setValue(Values.QUEEN_VALUE);
				}
				validMoves.add(validMove);

				if (!p.hasMoved() && board.checkPiece(currentRow + 2 * dir, currentCol, player) == PositionStatus.NO_PIECE) {
					if (p.isValidMove(currentRow + 2 * dir, currentCol, nullMoveInfo)) {
						bonus = PositionBonus.getPawnMoveBonus(currentRow, currentCol, currentRow + 2 * dir, currentCol, p.getSide());
						validMoves.add(new Move(currentRow, currentCol, currentRow + 2 * dir, currentCol, bonus, MoveNote.PAWN_LEAP));
					}
				}

			}

		}

		// Check left and right attack angles
		for (int i = 0; i < lr.length; i++) {
			if (board.checkPiece(currentRow + dir, currentCol + lr[i], player) == PositionStatus.ENEMY) {

				if (p.isValidMove(currentRow + dir, currentCol + lr[i], nullMoveInfo)) {

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
		}

		// Check left and right en passant rule
		if (currentRow == fifthRank && board.getLastMoveMade() != null) {
			for (int i = 0; i < lr.length; i++) {
				if (board.checkPiece(fifthRank, currentCol + lr[i], player) == PositionStatus.ENEMY) {

					if ((board.getLastMoveMade().getToCol() == (currentCol + lr[i])) && board.getLastMoveMade().getNote() == MoveNote.PAWN_LEAP) {

						if (p.isValidMove(currentRow + dir, currentCol + lr[i], nullMoveInfo)) {

							validMove = new Move(currentRow, currentCol, currentRow + dir, currentCol + lr[i]);
							validMove.setValue(board.getPieceValue(fifthRank, currentCol + lr[i]));
							validMove.setPieceTaken(board.getPiece(fifthRank, currentCol + lr[i]));
							validMove.setNote(MoveNote.ENPASSANT);
							validMoves.add(validMove);
						}

					}
				}
			}
		}

		return validMoves;

	}

	public static void getNullMoveInfo(Piece p, Board board, long[] nullMoveInfo) {

		int currentRow = p.getRow();
		int currentCol = p.getCol();
		int dir;
		Side player = p.getSide();
		PositionStatus pieceStatus;

		if (player == Side.WHITE) {
			dir = -1;
		} else {
			dir = 1;
		}

		pieceStatus = board.checkPiece(currentRow + dir, currentCol - 1, player);
		
		if (pieceStatus != PositionStatus.OFF_BOARD) {

			if (board.getPieceID(currentRow + dir, currentCol - 1) == PieceID.KING && pieceStatus == PositionStatus.ENEMY) {
				nullMoveInfo[1] &= p.getBit();
			}

			nullMoveInfo[0] |= BitBoard.getMask(currentRow + dir, currentCol - 1);
		}
		
		pieceStatus = board.checkPiece(currentRow + dir, currentCol + 1, player);

		if (pieceStatus != PositionStatus.OFF_BOARD) {

			if (board.getPieceID(currentRow + dir, currentCol + 1) == PieceID.KING && pieceStatus == PositionStatus.ENEMY) {
				nullMoveInfo[1] &= p.getBit();
			}

			nullMoveInfo[0] |= BitBoard.getMask(currentRow + dir, currentCol + 1);
		}

	}

}
