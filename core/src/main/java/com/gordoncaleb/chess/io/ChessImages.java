package com.gordoncaleb.chess.io;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;

import javax.swing.ImageIcon;

import com.gordoncaleb.chess.backend.Side;
import com.gordoncaleb.chess.pieces.Piece;

public class ChessImages {

	private static Image[][] chessPieceGraphics;
	private static ImageIcon[][] chessPieceIcons;

	private static boolean loaded;

	private static void loadChessImages() {

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		int gameHeight = (int) ((double) screenSize.height * 0.8);
		int imageHeight = (int) ((double) gameHeight * 0.10);
		int imageWidth = (int) ((double) imageHeight * 0.6);

		chessPieceGraphics = new Image[2][6];
		chessPieceIcons = new ImageIcon[2][6];
		String pieceNames[] = { "rook", "knight", "bishop", "queen", "king", "pawn" };
		String imgDir = "pieces";

		String whiteFileName;
		String blackFileName;

		for (int i = 0; i < 6; i++) {

			whiteFileName = imgDir + "/white_" + pieceNames[i] + ".png";
			blackFileName = imgDir + "/black_" + pieceNames[i] + ".png";

			chessPieceGraphics[0][i] = FileIO.readImage(blackFileName).getScaledInstance(imageWidth, imageHeight, Image.SCALE_SMOOTH);
			chessPieceGraphics[1][i] = FileIO.readImage(whiteFileName).getScaledInstance(imageWidth, imageHeight, Image.SCALE_SMOOTH);

			chessPieceIcons[0][i] = new ImageIcon(chessPieceGraphics[0][i]);
			chessPieceIcons[1][i] = new ImageIcon(chessPieceGraphics[1][i]);
		}
	}

	public static void scaleIcons(int screenHeight) {

		if (chessPieceGraphics == null) {
			return;
		}

		int gameHeight = (int) ((double) screenHeight * 0.8);
		int imageHeight = (int) ((double) gameHeight * 0.10);
		int imageWidth = (int) ((double) imageHeight * 0.6);

		chessPieceIcons = new ImageIcon[2][6];

		for (int i = 0; i < 6; i++) {

			chessPieceIcons[0][i] = new ImageIcon(chessPieceGraphics[0][i].getScaledInstance(imageWidth, imageHeight, Image.SCALE_SMOOTH));
			chessPieceIcons[1][i] = new ImageIcon(chessPieceGraphics[1][i].getScaledInstance(imageWidth, imageHeight, Image.SCALE_SMOOTH));
		}
	}

	public static Image getChessImage(Piece.PieceID id, Side player) {

		if (!loaded) {
			loadChessImages();
			loaded = true;
		}
		return chessPieceGraphics[player.ordinal()][id.ordinal()];
	}

	public static ImageIcon[][] getScaledIcons(int height) {
		
		if (!loaded) {
			loadChessImages();
			loaded = true;
		}

		int gameHeight = (int) ((double) height * 0.8);
		int imageHeight = (int) ((double) gameHeight * 0.10);
		int imageWidth = (int) ((double) imageHeight * 0.6);

		ImageIcon[][] icons = new ImageIcon[2][6];

		for (int i = 0; i < 6; i++) {

			icons[0][i] = new ImageIcon(chessPieceGraphics[0][i].getScaledInstance(imageWidth, imageHeight, Image.SCALE_SMOOTH));
			icons[1][i] = new ImageIcon(chessPieceGraphics[1][i].getScaledInstance(imageWidth, imageHeight, Image.SCALE_SMOOTH));
		}
		
		return icons;
	}

	public static ImageIcon getChessIcon(Piece.PieceID id, Side player) {

		if (!loaded) {
			loadChessImages();
			loaded = true;
		}

		return chessPieceIcons[player.ordinal()][id.ordinal()];
	}

}
