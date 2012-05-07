package chessBackend;

import java.util.Stack;
import java.util.Vector;

import chessPieces.*;

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
			untakePiece(lastMove.getPieceTaken());
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
	
	public Vector<Move> getMoveHistory(){
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
	
	public Piece getPiece(int row, int col){
		return board.getPiece(row,col);
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

		Vector<Piece> whitePieces = board.getPiecesFor(Side.WHITE);
		whitePiecesTaken = getFullPieceSet(Side.WHITE);

		Piece piecePresent;
		for (int p = 0; p < whitePieces.size(); p++) {
			piecePresent = whitePieces.elementAt(p);

			for (int t = 0; t < whitePiecesTaken.size(); t++) {
				if (whitePiecesTaken.elementAt(t).getPieceID() == piecePresent.getPieceID()) {
					whitePiecesTaken.remove(t);
					break;
				}
			}
		}

		Vector<Piece> blackPieces = board.getPiecesFor(Side.BLACK);
		blackPiecesTaken = getFullPieceSet(Side.BLACK);

		for (int p = 0; p < blackPieces.size(); p++) {
			piecePresent = blackPieces.elementAt(p);

			for (int t = 0; t < whitePiecesTaken.size(); t++) {
				if (blackPiecesTaken.elementAt(t).getPieceID() == piecePresent.getPieceID()) {
					blackPiecesTaken.remove(t);
					break;
				}
			}
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

	private void untakePiece(Piece pieceTaken) {

		if (pieceTaken != null) {

			if (pieceTaken.getSide() == Side.BLACK) {
				blackPiecesTaken.remove(pieceTaken);
			} else {
				whitePiecesTaken.remove(pieceTaken);
			}

		}
	}
	
	public void placePiece(Piece piece, int toRow, int toCol){
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

	private Vector<Piece> getFullPieceSet(Side player) {
		Vector<Piece> pieces = new Vector<Piece>(16);

		for (int i = 0; i < 8; i++) {
			pieces.add(new Pawn(player, 0, 0, false));
		}

		for (int i = 0; i < 8; i++) {
			pieces.add(new Rook(player, 0, 0, false));
		}

		for (int i = 0; i < 8; i++) {
			pieces.add(new Knight(player, 0, 0, false));
		}

		for (int i = 0; i < 8; i++) {
			pieces.add(new Bishop(player, 0, 0, false));
		}

		pieces.add(new King(player, 0, 0, false));
		pieces.add(new Queen(player, 0, 0, false));

		return pieces;
	}

}
