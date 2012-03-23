package chessGUI;

import java.util.Vector;

import chessBackend.Board;
import chessBackend.GameStatus;
import chessBackend.Move;
import chessBackend.Side;
import chessPieces.Piece;
import chessPieces.PieceID;

public class Adjudicator {
	Vector<Piece> whitePiecesTaken;
	Vector<Piece> blackPiecesTaken;
	Vector<Move> validMoves;
	Board board;

	public Adjudicator(Board board) {
		whitePiecesTaken = new Vector<Piece>();
		blackPiecesTaken = new Vector<Piece>();
		this.board = board;
		loadPiecesTaken();
	}

	public boolean move(Move move) {
		Move matchingMove = getMatchingMove(move);
		
		if (board.makeMove(matchingMove)) {
			takePiece(matchingMove.getPieceTaken());
			return true;
		} else {
			return false;
		}

	}

	public boolean undo() {

		if (board.getMoveHistory().size() > 1) {
			replacePiece(board.undoMove().getPieceTaken());
			replacePiece(board.undoMove().getPieceTaken());
			return true;
		} else {
			return false;
		}

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
			if(validMoves.elementAt(i).equals(move)){
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

}
