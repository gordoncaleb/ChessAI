package chessBackend;

import java.util.Hashtable;
import java.util.Stack;
import java.util.Vector;

import chessPieces.Piece;
import chessPieces.PieceID;

public class Adjudicator {
	private Vector<Piece> whitePiecesTaken;
	private Vector<Piece> blackPiecesTaken;
	private Vector<Move> validMoves;
	private Stack<Move> undoneMoves;
	private Board board;

	public Adjudicator(Board board) {
		whitePiecesTaken = new Vector<Piece>();
		blackPiecesTaken = new Vector<Piece>();
		undoneMoves = new Stack<Move>();
		this.board = board;
		loadPiecesTaken();
	}

	public boolean move(Move move) {

		undoneMoves.clear();

		Move matchingMove = getMatchingMove(move);

		if (board.makeMove(matchingMove)) {
			takePiece(matchingMove.getPieceTaken());

			return true;
		} else {
			return false;
		}

	}

	public Move undo() {
		Move lastMove = null;

		if (canUndo()) {
			lastMove = board.undoMove();
			replacePiece(lastMove.getPieceTaken());
			undoneMoves.push(lastMove);
		}

		return lastMove;

	}

	public boolean canUndo() {
		return (board.getMoveHistory().size() > 0);
	}

	public Move redo() {
		Move lastMoveUndone = null;

		if (canRedo()) {
			lastMoveUndone = undoneMoves.pop();
			board.makeMove(lastMoveUndone);
		}
		return lastMoveUndone;
	}

	public boolean canRedo() {
		return (undoneMoves.size() > 0);
	}

	public Vector<Move> getValidMoves() {
		validMoves = board.generateValidMoves();

		return validMoves;
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

	public Move getLastMoveMade() {
		return board.getLastMoveMade();
	}

	private Move getMatchingMove(Move move) {

		for (int i = 0; i < validMoves.size(); i++) {
			if (validMoves.elementAt(i).equals(move)) {
				return validMoves.elementAt(i);
			}
		}

		System.out.println("ERROR: Adjudicator says move is invalid");
		return null;
	}

	public Vector<Piece> getPiecesTaken(Side player) {
		if (player == Side.WHITE) {
			return whitePiecesTaken;
		} else {
			return blackPiecesTaken;
		}
	}

	private void loadPiecesTaken() {
		whitePiecesTaken.clear();
		blackPiecesTaken.clear();

		Vector<Move> moveHistory = board.getMoveHistory();

		for (int i = 0; i < moveHistory.size(); i++) {
			takePiece(moveHistory.elementAt(i).getPieceTaken());
		}
	}

	private void takePiece(Piece pieceTaken) {

		if (pieceTaken != null) {

			if (pieceTaken.getSide() == Side.BLACK) {
				blackPiecesTaken.add(pieceTaken);
			} else {
				whitePiecesTaken.add(pieceTaken);
			}

		}

	}

	private void replacePiece(Piece pieceTaken) {

		if (pieceTaken != null) {

			if (pieceTaken.getSide() == Side.BLACK) {
				blackPiecesTaken.remove(pieceTaken);
			} else {
				whitePiecesTaken.remove(pieceTaken);
			}

		}
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
