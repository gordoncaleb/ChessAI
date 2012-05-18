package chessBackend;

import java.util.ArrayList;
import java.util.Stack;
import java.util.Vector;

import chessPieces.*;

public class Adjudicator {
	private ArrayList<Move> validMoves;
	private Stack<Move> undoneMoves;
	private Board board;

	public Adjudicator(Board board) {
		undoneMoves = new Stack<Move>();
		this.board = board;
	}

	public boolean move(Move move) {

		if (undoneMoves.size() > 0) {
			if (move.equals(undoneMoves.peek())) {
				undoneMoves.pop();
			} else {
				undoneMoves.clear();
			}
		}

		Move matchingMove = getMatchingMove(move);

		if (board.makeMove(matchingMove)) {

			return true;
		} else {
			return false;
		}

	}

	public Move undo() {
		Move lastMove = null;

		if (canUndo()) {
			lastMove = board.undoMove();
			undoneMoves.push(lastMove);
		}

		return lastMove;

	}

	public boolean canUndo() {
		return (board.getMoveHistory().size() > 0);
	}

	public Move getLastUndoneMove() {
		Move lastMoveUndone = null;

		if (hashUndoneMoves()) {
			lastMoveUndone = undoneMoves.peek();
			// board.makeMove(lastMoveUndone);
		}
		return lastMoveUndone;
	}

	public boolean hashUndoneMoves() {
		return (undoneMoves.size() > 0);
	}

	public ArrayList<Move> getValidMoves() {
		board.makeNullMove();
		validMoves = board.generateValidMoves(false);

		return validMoves;
	}

	public Vector<Move> getMoveHistory() {
		return board.getMoveHistory();
	}

	public Side getTurn() {
		return board.getTurn();
	}

	public PieceID getPieceID(int row, int col) {
		return board.getPieceID(row, col);
	}

	public Side getPiecePlayer(int row, int col) {
		return board.getPieceSide(row, col);
	}

	public Piece getPiece(int row, int col) {
		return board.getPiece(row, col);
	}

	public Move getLastMoveMade() {
		return board.getLastMoveMade();
	}

	private Move getMatchingMove(Move move) {

		for (int i = 0; i < validMoves.size(); i++) {
			if (validMoves.get(i).equals(move)) {
				return validMoves.get(i);
			}
		}

		System.out.println("ERROR: Adjudicator says " + move + " move is invalid");
		return null;
	}

	public Vector<Piece> getPiecesTaken(Side player) {
		return board.getPiecesTakenFor(player);
	}

	public void placePiece(Piece piece, int toRow, int toCol) {
		board.placePiece(piece, toRow, toCol);
	}

	public GameStatus getGameStatus() {

		return board.getBoardStatus();

	}

	public boolean isGameOver() {

		return board.isGameOver();
	}

	public Side getWinner() {
		if (board.isGameOver()) {
			if (board.isDraw() || board.isInStaleMate()) {
				return Side.NONE;
			} else {
				return board.getTurn().otherSide();
			}
		} else {
			return null;
		}
	}

	public Board getBoard() {
		return board;
	}

}
