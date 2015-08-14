package com.gordoncaleb.chess.backend;

import java.security.SecureRandom;
import java.util.Random;

import com.gordoncaleb.chess.pieces.Piece;

public class RNGTable {

	public static RNGTable instance = new RNGTable();

	private static final byte[] seed = { -52, 45, -101, 26, -51, -99, -84, -79 };
	private final Random rng;
	private long[][][][] piecePerSquare;
	private long blackToMove;
	private long[][][][] castlingRights;
	private long[] enPassantFile;

	private RNGTable() {
		rng = new Random(123L);
		generatePiecePerSquare();
		generateBlackToMove();
		generateCastlingRights();
		generateEnPassantFile();
	}

	private void generatePiecePerSquare() {
		Piece.PieceID[] pieceIDs = Piece.PieceID.values();
		int numPieceType = pieceIDs.length;

		piecePerSquare = new long[2][numPieceType][8][8];

		for (int player = 0; player < 2; player++) {
			for (int pieceType = 0; pieceType < numPieceType; pieceType++) {
				for (int r = 0; r < 8; r++) {
					for (int c = 0; c < 8; c++) {
						piecePerSquare[player][pieceType][r][c] = rng.nextLong();
					}
				}
			}
		}
	}

	public long getPiecePerSquareRandom(Side player, Piece.PieceID id, int row, int col) {
		return piecePerSquare[player.ordinal()][id.ordinal()][row][col];
	}

	private void generateBlackToMove() {
		blackToMove = rng.nextLong();
	}

	public long getBlackToMoveRandom() {
		return blackToMove;
	}

	private void generateCastlingRights() {
		castlingRights = new long[2][2][2][2];

		for (int br = 0; br < 2; br++) {
			for (int bl = 0; bl < 2; bl++) {
				for (int wr = 0; wr < 2; wr++) {
					for (int wl = 0; wl < 2; wl++) {
						castlingRights[br][bl][wr][wl] = rng.nextLong();
					}
				}
			}
		}
	}

	public long getCastlingRightsRandom(boolean blackFarRook, boolean blackNearRook, boolean blackKing, boolean whiteFarRook, boolean whiteNearRook,
			boolean whiteKing) {

		int blackLeft = 0;
		int blackRight = 0;
		int whiteLeft = 0;
		int whiteRight = 0;

		if (!blackKing) {
			if (!blackFarRook) {
				blackLeft = 1;
			}
			if (!blackNearRook) {
				blackRight = 1;
			}
		}
		
		if(!whiteKing){
			if (!whiteFarRook) {
				whiteLeft = 1;
			}
			if (!whiteNearRook) {
				whiteRight = 1;
			}
		}

		return castlingRights[blackLeft][blackRight][whiteRight][whiteLeft];
	}

	private void generateEnPassantFile() {
		enPassantFile = new long[8];
		for (int f = 0; f < 8; f++) {
			enPassantFile[f] = rng.nextLong();
		}
	}

	public long getEnPassantFile(int file) {
		return enPassantFile[file];
	}
}
