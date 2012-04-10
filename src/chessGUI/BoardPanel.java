package chessGUI;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.Vector;

import javax.swing.*;

import chessBackend.*;
import chessIO.FileIO;
import chessPieces.Piece;
import chessPieces.PieceID;

public class BoardPanel extends JPanel implements MouseListener {

	private BoardGUI boardGUI;

	private int gameHeight;
	private int gameWidth;
	private int sidebarWidth;
	private int imageWidth;
	private int imageHeight;

	private JPanel squaresGUIPanel;
	private JPanel lostBlackPiecesPanel;
	private JPanel lostWhitePiecesPanel;

	private SquarePanel[][] chessSquares;
	private Image[][] chessPieceGraphics;
	private ImageIcon[][] chessPieceIcons;
	private SquarePanel selectedSquare;
	private SquarePanel lastMovedSquare;
	private boolean flipBoard;
	private Side userSide;

	private Adjudicator adjudicator;

	private boolean debug;

	public BoardPanel(BoardGUI boardGUI, boolean debug) {
		this.debug = debug;
		this.boardGUI = boardGUI;
		this.flipBoard = false;

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		gameHeight = (int) ((double) screenSize.height * 0.8);
		imageHeight = (int) ((double) gameHeight * 0.10);
		imageWidth = (int) ((double) imageHeight * 0.6);
		sidebarWidth = 2 * (int) ((double) imageWidth * 1.1);
		gameWidth = 2 * sidebarWidth + gameHeight;

		this.loadChessImages();

		lostBlackPiecesPanel = new JPanel(new FlowLayout());
		lostBlackPiecesPanel.setBackground(Color.GRAY);
		lostBlackPiecesPanel.setPreferredSize(new Dimension(sidebarWidth, gameHeight));
		this.add(lostBlackPiecesPanel, BorderLayout.WEST);

		squaresGUIPanel = new JPanel(new GridLayout(8, 8));
		squaresGUIPanel.setBackground(Color.GRAY);
		squaresGUIPanel.setPreferredSize(new Dimension(gameHeight, gameHeight));
		buildSquaresGUI();

		this.add(squaresGUIPanel, BorderLayout.CENTER);

		lostWhitePiecesPanel = new JPanel(new FlowLayout());
		lostWhitePiecesPanel.setBackground(Color.GRAY);
		lostWhitePiecesPanel.setPreferredSize(new Dimension(sidebarWidth, gameHeight));
		this.add(lostWhitePiecesPanel, BorderLayout.EAST);

		this.setBackground(Color.GRAY);

	}

	public void newGame(Side userSide, Board board) {
		this.userSide = userSide;

		if (userSide == Side.BLACK) {
			flipBoard = true;
		} else {
			flipBoard = false;
		}

		adjudicator = new Adjudicator(board);

		refreshBoard();

		colorSquaresDefault();
		clearPiecesTaken();
		attachValidMoves();

		updateLastMovedSquare();

	}

	private void buildSquaresGUI() {
		chessSquares = new SquarePanel[8][8];

		SquarePanel square;
		for (int row = 0; row < 8; row++) {
			for (int col = 0; col < 8; col++) {
				square = new SquarePanel(col % 2 == row % 2, row, col, debug);
				square.setGUI(this);
				square.addMouseListener(this);

				chessSquares[row][col] = square;
				squaresGUIPanel.add(chessSquares[row][col]);
			}
		}

	}

	private void loadChessImages() {

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

	public Image getChessImage(PieceID id, Side player) {
		return chessPieceGraphics[player.ordinal()][id.ordinal()];
	}

	public ImageIcon getChessIcon(PieceID id, Side player) {
		return chessPieceIcons[player.ordinal()][id.ordinal()];
	}

	public boolean makeMove(Move move) {

		if (adjudicator.move(move)) {
			refreshBoard();

			if (move.getPieceTaken() != null) {
				takePiece(move.getPieceTaken());
			}

			updateLastMovedSquare();

			attachValidMoves();

			setGameSatus(adjudicator.getGameStatus(), adjudicator.getTurn());
		} else {
			return false;
		}

		return true;

	}

	public void setGameSatus(GameStatus status, Side playerTurn) {

		if (status == GameStatus.CHECK) {
			System.out.println("Check!");
		}

		if (status == GameStatus.CHECKMATE) {
			if (playerTurn == userSide) {
				boardGUI.gameOverWin();
			} else {
				boardGUI.gameOverLose();
			}
		}

		if (status == GameStatus.STALEMATE) {
			boardGUI.gameOverStaleMate();
		}
	}

	public Move undoMove() {
		Move undoneMove = null;

		if (adjudicator.canUndo()) {

			undoneMove = adjudicator.undo();
			// full refresh of board info
			refreshBoard();

			// reset all squares to default border color
			colorSquaresDefault();

			// attach the new valid moves to appropriate squares
			attachValidMoves();

			// update side line view of pieces that have been taken
			if (undoneMove.getPieceTaken() != null) {
				refreshPiecesTaken();
			}

			// show what the last move made was before last two moves were made
			updateLastMovedSquare();

		} else {
			System.out.println("Cannot undo move");
		}

		return undoneMove;

	}

	public boolean canUndo() {
		return adjudicator.canUndo();
	}

	public Move redoMove() {
		Move redoneMove = null;

		if (adjudicator.canRedo()) {
			redoneMove = adjudicator.redo();

			refreshBoard();

			if (redoneMove.getPieceTaken() != null) {
				takePiece(redoneMove.getPieceTaken());
			}

			updateLastMovedSquare();

			attachValidMoves();

			setGameSatus(adjudicator.getGameStatus(), adjudicator.getTurn());
		}

		return redoneMove;
	}

	public boolean canRedo() {
		return adjudicator.canRedo();
	}

	private void refreshBoard() {

		for (int row = 0; row < 8; row++) {
			for (int col = 0; col < 8; col++) {

				if (adjudicator.getPieceID(row, col) == null && getChessSquare(row, col).getPieceID() != null) {
					getChessSquare(row, col).clearChessPiece();
				}

				if (adjudicator.getPieceID(row, col) != null) {
					getChessSquare(row, col).showChessPiece(adjudicator.getPieceID(row, col), adjudicator.getPiecePlayer(row, col));
				}

				if (debug) {
					chessSquares[row][col].updateDebugInfo("");
				}
			}
		}

		this.selectedSquare = null;
	}

	private void attachValidMoves() {

		clearValidMoves();

		Vector<Move> validMoves = adjudicator.getValidMoves();

		Move move;
		for (int m = 0; m < validMoves.size(); m++) {
			move = validMoves.elementAt(m);
			getChessSquare(move.getFromRow(), move.getFromCol()).addValidMove(move);
		}

		if (selectedSquare != null) {
			colorValidMoveSquares(selectedSquare, true);
		}
	}

	private void clearValidMoves() {
		for (int r = 0; r < 8; r++) {
			for (int c = 0; c < 8; c++) {
				chessSquares[r][c].removeAllValidMoves();
			}
		}
	}

	private void colorValidMoveSquares(SquarePanel square, boolean valid) {
		Vector<Move> validMoves = square.getValidMoves();
		Move move;

		for (int i = 0; i < validMoves.size(); i++) {
			move = validMoves.elementAt(i);

			getChessSquare(move.getToRow(), move.getToCol()).showAsValidMove(valid);

			if (debug) {
				// if (valid && move != null) {
				// getChessSquare(move.getToRow(),
				// move.getToCol()).updateDebugInfo(game.getMoveChosenPathValue(move)
				// + "");
				// } else {
				// getChessSquare(move.getToRow(),
				// move.getToCol()).updateDebugInfo("");
				// }
			}

		}
	}

	private void colorSquaresDefault() {

		selectedSquare = null;

		for (int r = 0; r < 8; r++) {
			for (int c = 0; c < 8; c++) {
				chessSquares[r][c].showAsDefault();
			}
		}
	}

	private void takePiece(Piece pieceTaken) {
		JLabel picLabel = new JLabel();
		picLabel.setIcon(this.getChessIcon(pieceTaken.getPieceID(), pieceTaken.getSide()));

		if (pieceTaken.getSide() == Side.WHITE) {
			lostWhitePiecesPanel.add(picLabel);
		} else {
			lostBlackPiecesPanel.add(picLabel);
		}
	}

	private void refreshPiecesTaken() {
		JLabel picLabel;

		clearPiecesTaken();

		Vector<Piece> takenPieces = adjudicator.getPiecesTaken(Side.WHITE);

		for (int i = 0; i < takenPieces.size(); i++) {
			picLabel = new JLabel();
			picLabel.setIcon(this.getChessIcon(takenPieces.elementAt(i).getPieceID(), Side.WHITE));
			lostWhitePiecesPanel.add(picLabel);
		}

		takenPieces = adjudicator.getPiecesTaken(Side.BLACK);

		for (int i = 0; i < takenPieces.size(); i++) {
			picLabel = new JLabel();
			picLabel.setIcon(this.getChessIcon(takenPieces.elementAt(i).getPieceID(), Side.BLACK));
			lostBlackPiecesPanel.add(picLabel);
		}

	}

	private void clearPiecesTaken() {
		lostBlackPiecesPanel.removeAll();
		lostBlackPiecesPanel.updateUI();
		lostWhitePiecesPanel.removeAll();
		lostWhitePiecesPanel.updateUI();
	}

	private void updateLastMovedSquare() {
		if (lastMovedSquare != null) {
			lastMovedSquare.showAsLastMoved(false);
			lastMovedSquare = null;
		}

		Move lastMove = adjudicator.getLastMoveMade();
		if (lastMove != null) {
			lastMovedSquare = getChessSquare(lastMove.getToRow(), lastMove.getToCol());
			lastMovedSquare.showAsLastMoved(true);
		}

	}

	private SquarePanel getChessSquare(int row, int col) {
		int getRow;
		int getCol;

		if (flipBoard) {
			getRow = 7 - row;
			getCol = 7 - col;
		} else {
			getRow = row;
			getCol = col;
		}

		return chessSquares[getRow][getCol];
	}

	public boolean isMyTurn() {
		return (userSide == adjudicator.getTurn());
	}

	private Move getOrientedMove(int fromRow, int fromCol, int toRow, int toCol) {
		int orienFromRow;
		int orienFromCol;
		int orienToRow;
		int orienToCol;

		if (flipBoard) {
			orienFromRow = 7 - fromRow;
			orienFromCol = 7 - fromCol;
			orienToRow = 7 - toRow;
			orienToCol = 7 - toCol;
		} else {
			orienFromRow = fromRow;
			orienFromCol = fromCol;
			orienToRow = toRow;
			orienToCol = toCol;
		}

		return new Move(orienFromRow, orienFromCol, orienToRow, orienToCol);

	}

	// Listener Methods

	@Override
	public void mousePressed(MouseEvent arg0) {
		SquarePanel clickedSquare;
		Component c = arg0.getComponent();

		if (c instanceof SquarePanel) {
			clickedSquare = (SquarePanel) c;
		} else {
			return;
		}

		// lock board when it's not the users turn
		if (userSide != adjudicator.getTurn() && userSide != Side.BOTH) {
			return;
		}

		if (selectedSquare == null) {
			if (clickedSquare.hasPiece()) {

				selectedSquare = clickedSquare;
				selectedSquare.showAsSelected(true);

				colorValidMoveSquares(clickedSquare, true);
			}

		} else {

			if (selectedSquare != clickedSquare) {

				Move m = getOrientedMove(selectedSquare.getRow(), selectedSquare.getCol(), clickedSquare.getRow(), clickedSquare.getCol());

				Move validMove = selectedSquare.checkIfValidMove(m);
				if (validMove != null) {
					selectedSquare.showAsSelected(false);
					colorValidMoveSquares(selectedSquare, false);

					selectedSquare = null;

					makeMove(validMove);
					boardGUI.makeMove(validMove);
				} else {
					if (clickedSquare.hasPiece()) {

						colorValidMoveSquares(selectedSquare, false);
						colorValidMoveSquares(clickedSquare, true);

						selectedSquare.showAsSelected(false);
						selectedSquare = clickedSquare;
						selectedSquare.showAsSelected(true);

					} else {
						System.out.println("Invalid move");
					}
				}
			} else {

			}
		}

	}

	// unused listener methods

	public void mouseClicked(MouseEvent arg0) {
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
	}

}
