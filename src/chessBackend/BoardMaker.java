package chessBackend;

import chessPieces.Piece;
import chessPieces.PieceID;

public class BoardMaker {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println(BoardMaker.getRandomChess960Board().toString());

	}

	public static Board getStandardChessBoard() {

		return null;
	}

	public static Board getRandomChess960Board() {

		Board board = new Board();
		
		System.out.println(board.toString());

		int[] pawnRow = new int[2];
		pawnRow[Side.BLACK.ordinal()] = 1;
		pawnRow[Side.WHITE.ordinal()] = 6;
		
		int[] mainRow = new int[2];
		mainRow[Side.BLACK.ordinal()] = 0;
		mainRow[Side.WHITE.ordinal()] = 7;

		// public Piece(PieceID id, Side player, int row, int col, boolean
		// moved) {
		Piece temp;
		for (int s = 0; s < 2; s++) {
			for (int p = 0; p < 8; p++) {
				temp = new Piece(PieceID.PAWN, Side.values()[s], 0, 0, false);
				board.placePiece(temp, pawnRow[Side.values()[s].ordinal()], p);
			}
		}

		PieceID[] setup = new PieceID[8];

		setup[rollDie(4) * 2] = PieceID.BISHOP;
		setup[rollDie(4) * 2 + 1] = PieceID.BISHOP;

		setup[ithEmptyPosition(rollDie(6) + 1, setup)] = PieceID.QUEEN;
		
		setup[ithEmptyPosition(rollDie(5) + 1, setup)] = PieceID.KNIGHT;
		setup[ithEmptyPosition(rollDie(4) + 1, setup)] = PieceID.KNIGHT;
		
		setup[ithEmptyPosition(2, setup)] = PieceID.KING;
		
		setup[ithEmptyPosition(1, setup)] = PieceID.ROOK;
		setup[ithEmptyPosition(1, setup)] = PieceID.ROOK;
		
		for (int s = 0; s < 2; s++) {
			for (int p = 0; p < 8; p++) {
				temp = new Piece(setup[p], Side.values()[s], -1, -1, false);
				board.placePiece(temp, mainRow[Side.values()[s].ordinal()], p);
			}
		}

		return board;
	}

	private static int ithEmptyPosition(int i, PieceID[] setup) {
		for (int n = 0; n < setup.length; n++) {

			if (setup[n] == null) {
				i--;
			}

			if (i <= 0) {
				return n;
			}
		}

		return setup.length;
	}

	public static int rollDie(int dieSize) {
		return (int) (Math.random() * (double) dieSize);
	}

}
