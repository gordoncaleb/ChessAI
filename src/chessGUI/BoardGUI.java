package chessGUI;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

import chessAI.DecisionNode;
import chessBackend.*;
import chessIO.FileIO;
import chessIO.MoveBook;
import chessIO.MoveBookNode;
import chessIO.XMLParser;
import chessPieces.Piece;
import chessPieces.PieceID;

public class BoardGUI implements MouseListener, KeyListener, ActionListener {

	private Game game;
	private int gameHeight;
	private int gameWidth;
	private int sidebarWidth;
	private int imageWidth;
	private int imageHeight;

	private JFrame frame;
	private JPanel boardGUIPanel;
	private JPanel lostBlackPiecesPanel;
	private JPanel lostWhitePiecesPanel;

	private JMenuItem gameAsBlackMenu;
	private JMenuItem gameAsWhiteMenu;
	private JMenuItem undoUserMoveMenu;

	private SquareGUI[][] chessSquares;
	private Image[][] chessPieceGraphics;
	private SquareGUI selectedSquare;
	private SquareGUI lastMovedSquare;
	private boolean flipBoard;
	private Player userSide;

	private Adjudicator adjudicator;

	private boolean debug;

	public BoardGUI(Game game, boolean debug) {
		this.debug = debug;
		this.flipBoard = false;

		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		gameHeight = (int) ((double) dim.height * 0.8);
		imageHeight = (int) ((double) gameHeight * 0.10);
		imageWidth = (int) ((double) imageHeight * 0.6);
		sidebarWidth = 2 * (int) ((double) imageWidth * 1.1);
		gameWidth = 2 * sidebarWidth + gameHeight;

		frame = new JFrame("Oh,Word? " + Game.VERSION);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());

		JMenuBar menuBar = new JMenuBar();
		JMenu gameMenu = new JMenu("Game");
		gameMenu.setMnemonic(KeyEvent.VK_G);
		gameAsBlackMenu = new JMenuItem("New Game As Black");
		gameAsBlackMenu.addActionListener(this);
		gameMenu.add(gameAsBlackMenu);
		gameAsWhiteMenu = new JMenuItem("New Game As White");
		gameAsWhiteMenu.addActionListener(this);
		gameMenu.add(gameAsWhiteMenu);
		undoUserMoveMenu = new JMenuItem("Undo Last Move");
		undoUserMoveMenu.addActionListener(this);
		gameMenu.add(undoUserMoveMenu);

		menuBar.add(gameMenu);
		frame.setJMenuBar(menuBar);

		boardGUIPanel = new JPanel(new GridLayout(8, 8));
		boardGUIPanel.setBackground(Color.GRAY);
		boardGUIPanel.setPreferredSize(new Dimension(gameHeight, gameHeight));
		frame.add(boardGUIPanel, BorderLayout.CENTER);

		lostBlackPiecesPanel = new JPanel(new FlowLayout());
		lostBlackPiecesPanel.setBackground(Color.GRAY);
		lostBlackPiecesPanel.setPreferredSize(new Dimension(sidebarWidth, gameHeight));
		frame.add(lostBlackPiecesPanel, BorderLayout.WEST);

		lostWhitePiecesPanel = new JPanel(new FlowLayout());
		lostWhitePiecesPanel.setBackground(Color.GRAY);
		lostWhitePiecesPanel.setPreferredSize(new Dimension(sidebarWidth, gameHeight));
		frame.add(lostWhitePiecesPanel, BorderLayout.EAST);

		this.game = game;
		this.loadChessImages();
		this.buildBoardGUI();

		frame.setSize(gameWidth, gameHeight);
		frame.setResizable(false);
		frame.setVisible(true);

		this.debug = debug;
		if (debug) {

		}

	}

	public void newGame(Player userSide) {
		this.userSide = userSide;

		if (userSide == Player.BLACK) {
			flipBoard = true;
		} else {
			flipBoard = false;
		}

		if (debug) {
			adjudicator = new Adjudicator(XMLParser.XMLToBoard(FileIO.readFile("testboard.xml")));
		} else {
			adjudicator = new Adjudicator(XMLParser.XMLToBoard(FileIO.readFile("default.xml")));
		}

		refreshBoard();

		colorSquaresDefault();
		clearPiecesTaken();
		attachValidMoves();

		updateLastMovedSquare();

	}

	private void buildBoardGUI() {
		chessSquares = new SquareGUI[8][8];

		SquareGUI square;
		for (int row = 0; row < 8; row++) {
			for (int col = 0; col < 8; col++) {
				square = new SquareGUI(col % 2 == row % 2, row, col, debug);
				square.setGUI(this);
				square.addMouseListener(this);

				chessSquares[row][col] = square;
				boardGUIPanel.add(chessSquares[row][col]);
			}
		}

	}

	private void loadChessImages() {

		chessPieceGraphics = new Image[2][6];
		String pieceNames[] = { "rook", "knight", "bishop", "queen", "king", "pawn" };
		String imgDir = "pieces";

		String whiteFileName;
		String blackFileName;

		for (int i = 0; i < 6; i++) {

			whiteFileName = imgDir + "/white_" + pieceNames[i] + ".png";
			blackFileName = imgDir + "/black_" + pieceNames[i] + ".png";

			chessPieceGraphics[0][i] = FileIO.readImage(blackFileName).getScaledInstance(imageWidth, imageHeight, Image.SCALE_SMOOTH);
			chessPieceGraphics[1][i] = FileIO.readImage(whiteFileName).getScaledInstance(imageWidth, imageHeight, Image.SCALE_SMOOTH);

		}
	}

	public Image getChessImage(PieceID id, Player player) {
		return chessPieceGraphics[player.ordinal()][id.ordinal()];
	}

	public void makeMove(Move move) {

		adjudicator.move(move);

		SquareGUI fromSquare = getChessSquare(move.getFromRow(),move.getFromCol());
		SquareGUI toSquare = getChessSquare(move.getToRow(),move.getToCol());

		if (move.getPieceTaken() != null) {

			pieceTaken(move.getPieceTaken().getPieceID(), move.getPieceTaken().getPlayer());

		}

		if (move.getNote() != MoveNote.NONE) {
			refreshBoard();
		} else {
			toSquare.showChessPiece(fromSquare.getPieceID(), fromSquare.getPlayer());
			fromSquare.clearChessPiece();
		}

		updateLastMovedSquare();

		attachValidMoves();

		game.setGameSatus(adjudicator.getGameStatus(), adjudicator.getPlayer());

	}

	private void undoUserMove() {

		if (adjudicator.undo()) {
			// full refresh of board info
			refreshBoard();

			// reset all squares to default border color
			colorSquaresDefault();

			// attach the new valid moves to appropriate squares
			attachValidMoves();

			// update side line view of pieces that have been taken
			refreshPiecesTaken();

			// show what the last move made was before last two moves were made
			updateLastMovedSquare();

			// notify game and ai object to undo the last two moves
			game.undoUserMove();

		} else {
			System.out.println("Cannot undo move");
		}

	}

	public void refreshBoard() {

		for (int row = 0; row < 8; row++) {
			for (int col = 0; col < 8; col++) {

				getChessSquare(row, col).clearChessPiece();

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

	private void colorValidMoveSquares(SquareGUI square, boolean valid) {
		Vector<Move> validMoves = square.getValidMoves();
		Move move;

		for (int i = 0; i < validMoves.size(); i++) {
			move = validMoves.elementAt(i);

			getChessSquare(move.getToRow(), move.getToCol()).showAsValidMove(valid);

			if (debug) {
				if (valid && move != null) {
					getChessSquare(move.getToRow(), move.getToCol()).updateDebugInfo(game.getMoveChosenPathValue(move) + "");
				} else {
					getChessSquare(move.getToRow(), move.getToCol()).updateDebugInfo("");
				}
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

	private void pieceTaken(PieceID id, Player player) {
		JLabel picLabel = new JLabel();

		if (player == Player.BLACK) {
			System.out.println("Black loses piece " + id);
			picLabel.setIcon(new ImageIcon(this.getChessImage(id, Player.BLACK)));
			lostBlackPiecesPanel.add(picLabel);
		} else {
			System.out.println("White loses piece " + id);
			picLabel.setIcon(new ImageIcon(this.getChessImage(id, Player.WHITE)));
			lostWhitePiecesPanel.add(picLabel);
		}
	}

	private void refreshPiecesTaken() {
		JLabel picLabel;

		clearPiecesTaken();

		Vector<Piece> takenPieces = adjudicator.getPiecesTaken(Player.WHITE);

		for (int i = 0; i < takenPieces.size(); i++) {
			picLabel = new JLabel();
			picLabel.setIcon(new ImageIcon(this.getChessImage(takenPieces.elementAt(i).getPieceID(), Player.WHITE)));
			lostWhitePiecesPanel.add(picLabel);
		}

		takenPieces = adjudicator.getPiecesTaken(Player.BLACK);

		for (int i = 0; i < takenPieces.size(); i++) {
			picLabel = new JLabel();
			picLabel.setIcon(new ImageIcon(this.getChessImage(takenPieces.elementAt(i).getPieceID(), Player.BLACK)));
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

	private SquareGUI getChessSquare(int row, int col) {
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

	public JFrame getFrame() {
		return frame;
	}

	// Listener Methods

	@Override
	public void mousePressed(MouseEvent arg0) {
		SquareGUI clickedSquare;
		Component c = arg0.getComponent();

		if (c instanceof SquareGUI) {
			clickedSquare = (SquareGUI) c;
		} else {
			return;
		}

		// lock board when it's not the users turn
		if (userSide != adjudicator.getPlayer()) {
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
					game.userMoved(validMove);
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

	@Override
	public void actionPerformed(ActionEvent arg0) {

		if (arg0.getSource() == gameAsBlackMenu) {
			game.newGame(Player.BLACK);
		}

		if (arg0.getSource() == gameAsWhiteMenu) {
			game.newGame(Player.WHITE);
		}

		if (arg0.getSource() == undoUserMoveMenu) {
			this.undoUserMove();
		}

	}

	// unused listener methods

	@Override
	public void keyReleased(KeyEvent arg0) {
	}

	public void mouseClicked(MouseEvent arg0) {
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

}
