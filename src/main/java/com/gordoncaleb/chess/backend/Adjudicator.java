package com.gordoncaleb.chess.backend;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.gordoncaleb.chess.pieces.*;

public class Adjudicator {
	private ArrayList<Long> validMoves;
	private Stack<Long> undoneMoves;
	private Board board;

	public Adjudicator(Board board) {
		undoneMoves = new Stack<Long>();
		this.board = board;
	}

	public void newGame(Board board) {
		undoneMoves = new Stack<Long>();
		validMoves = new ArrayList<Long>();
		this.board = board;
	}

	public boolean move(long move) {

		if (undoneMoves.size() > 0) {
			if (Move.equals(undoneMoves.peek(), move)) {
				undoneMoves.pop();
			} else {
				undoneMoves.clear();
			}
		}

		long matchingMove = getMatchingMove(move);

		if (matchingMove != 0) {
			if (board.makeMove(matchingMove)) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}

	}

	public long undo() {
		long lastMove = 0;

		if (canUndo()) {
			lastMove = board.undoMove();
			undoneMoves.push(lastMove);
		}

		return lastMove;

	}

	public boolean canUndo() {
		return board.canUndo();
	}

	public long getLastUndoneMove() {

		if (hasUndoneMoves()) {
			return undoneMoves.peek();
			// board.makeMove(lastMoveUndone);
		} else {
			return 0;
		}

	}

	public boolean hasUndoneMoves() {
		return (undoneMoves.size() > 0);
	}

	public ArrayList<Long> getValidMoves() {
		board.makeNullMove();
		validMoves = board.generateValidMoves();

		return validMoves;
	}

	public List<Move> getMoveHistory() {
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

	public Long getLastMoveMade() {
		return board.getLastMoveMade();
	}

	private long getMatchingMove(long move) {

		for (int i = 0; i < validMoves.size(); i++) {
			if (Move.equals(validMoves.get(i), move)) {
				return validMoves.get(i);
			}
		}

		System.out.println("ERROR: Adjudicator says " + (new Move(move)) + " move is invalid");
		return 0;
	}

	public List<Piece> getPiecesTaken(Side player) {
		return board.getPiecesTakenFor(player);
	}

	public boolean placePiece(Piece piece, int toRow, int toCol) {
		return board.placePiece(piece, toRow, toCol);
	}

	public GameStatus getGameStatus() {

		return board.getBoardStatus();

	}

	public boolean isGameOver() {
		return board.isGameOver();
	}

	public Side getWinner() {
		if (board.isGameOver()) {
			return board.getTurn().otherSide();
		} else {
			return null;
		}
	}

	public Board getBoard() {
		return board;
	}

}
