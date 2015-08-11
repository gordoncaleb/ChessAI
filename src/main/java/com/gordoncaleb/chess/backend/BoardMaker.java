package com.gordoncaleb.chess.backend;

import java.util.ArrayList;
import java.util.Stack;

import com.gordoncaleb.chess.pieces.Piece;
import com.gordoncaleb.chess.pieces.PieceID;

public class BoardMaker {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println(BoardMaker.getRandomChess960Board().toString());
		
		System.out.println(BoardMaker.getStandardChessBoard().toString());

	}

	public static Board getStandardChessBoard() {

		return makeBoard(1, 2, 2, 1, 2);
	}

	public static Board getRandomChess960Board() {

		return makeBoard(rollDie(4), rollDie(4), rollDie(6), rollDie(5), rollDie(4));
	}

	public static Board makeBoard(int r1, int r2, int r3, int r4, int r5) {
		ArrayList<Piece>[] pieces = new ArrayList[2];
		pieces[0] = new ArrayList<Piece>();
		pieces[1] = new ArrayList<Piece>();

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
				temp = new Piece(PieceID.PAWN, Side.values()[s], pawnRow[s], p, false);
				pieces[s].add(temp);
			}
		}

		PieceID[] setup = new PieceID[8];

		setup[r1 * 2] = PieceID.BISHOP;
		setup[r2 * 2 + 1] = PieceID.BISHOP;

		setup[ithEmptyPosition(r3 + 1, setup)] = PieceID.QUEEN;

		setup[ithEmptyPosition(r4 + 1, setup)] = PieceID.KNIGHT;
		setup[ithEmptyPosition(r5 + 1, setup)] = PieceID.KNIGHT;

		setup[ithEmptyPosition(2, setup)] = PieceID.KING;

		setup[ithEmptyPosition(1, setup)] = PieceID.ROOK;
		setup[ithEmptyPosition(1, setup)] = PieceID.ROOK;

		for (int s = 0; s < 2; s++) {
			for (int p = 0; p < 8; p++) {
				temp = new Piece(setup[p], Side.values()[s], mainRow[s], p, false);
				pieces[s].add(temp);
			}
		}

		Board board = new Board(pieces, Side.WHITE, new Stack<Move>(), null, null);

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
