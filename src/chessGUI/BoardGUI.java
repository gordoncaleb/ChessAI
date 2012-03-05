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
import chessAI.MoveBook;
import chessAI.MoveBookNode;
import chessBackend.*;
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
	private JPanel lostAiPiecesPanel;
	private JPanel lostUserPiecesPanel;

	private JMenuItem gameAsBlackMenu;
	private JMenuItem gameAsWhiteMenu;
	private JMenuItem undoUserMoveMenu;

	private SquareGUI[][] chessSquares;
	private Image[][] chessPieceGraphics;
	private SquareGUI selectedSquare;
	private SquareGUI lastMovedSquare;
	private boolean flipBoard;
	private Player whitePlayer;

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

		lostAiPiecesPanel = new JPanel(new FlowLayout());
		lostAiPiecesPanel.setBackground(Color.GRAY);
		lostAiPiecesPanel.setPreferredSize(new Dimension(sidebarWidth, gameHeight));
		frame.add(lostAiPiecesPanel, BorderLayout.WEST);

		lostUserPiecesPanel = new JPanel(new FlowLayout());
		lostUserPiecesPanel.setBackground(Color.GRAY);
		lostUserPiecesPanel.setPreferredSize(new Dimension(sidebarWidth, gameHeight));
		frame.add(lostUserPiecesPanel, BorderLayout.EAST);

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

	public void newGame(Player whitePlayer) {
		this.whitePlayer = whitePlayer;

		if (debug) {
			adjudicator = new Adjudicator(Board.fromFile("testboard.txt"), whitePlayer);
		} else {
			adjudicator = new Adjudicator(new Board(), whitePlayer);
		}

		clearPiecesTaken();
		updateLastMovedSquare();
		attachValidMoves();
		setBoard(adjudicator.getCurrentBoard());
		colorSquaresDefault();
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
		String imgDir = "img" + "/" + "pieces/"; //+ File.separator;

		URL urlWhite;
		URL urlBlack;

		for (int i = 0; i < 6; i++) {
			try {

				urlWhite = BoardGUI.class.getResource(imgDir + "white_" + pieceNames[i] + ".png");
				urlBlack = BoardGUI.class.getResource(imgDir + "black_" + pieceNames[i] + ".png");
				//System.out.println(urlWhite);

				chessPieceGraphics[0][i] = ImageIO.read(urlBlack).getScaledInstance(imageWidth, imageHeight, Image.SCALE_SMOOTH);
				chessPieceGraphics[1][i] = ImageIO.read(urlWhite).getScaledInstance(imageWidth, imageHeight, Image.SCALE_SMOOTH);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public Image getChessImage(PieceID id, Player player) {
		int color;

		if (whitePlayer == player) {
			color = 1;
		} else {
			color = 0;
		}

		return chessPieceGraphics[color][id.ordinal()];
	}

	public void aiMoved(Move aisMove) {

		adjudicator.move(aisMove);
		// Update the gui to show move that AI made
		makeMove(aisMove);

		attachValidMoves();

	}

	private void userMoved(Move usersMove) {

		adjudicator.move(usersMove);
		clearValidMoves();
		makeMove(usersMove);
	}

	private void makeMove(Move move) {

		SquareGUI fromSquare = chessSquares[move.getFromRow()][move.getFromCol()];
		SquareGUI toSquare = chessSquares[move.getToRow()][move.getToCol()];

		if (move.getPieceTaken() != null) {

			pieceTaken(move.getPieceTaken().getPieceID(), move.getPieceTaken().getPlayer());

		}

		if (move.getNote() != MoveNote.NONE) {
			setBoard(adjudicator.getCurrentBoard());
		} else {
			toSquare.showChessPiece(fromSquare.getPieceID(), fromSquare.getPlayer());
			fromSquare.clearChessPiece();
		}

		updateLastMovedSquare();

		game.setGameSatus(adjudicator.getRoot().getStatus(), adjudicator.getRoot().getPlayer());

	}

	private void undoUserMove() {
		if (adjudicator.undo()) {
			setBoard(adjudicator.getCurrentBoard());
			attachValidMoves();
			refreshPiecesTaken();
			updateLastMovedSquare();
			game.undoUserMove();
		} else {
			System.out.println("Cannot undo move");
		}

	}

	public void setBoard(Board board) {
		int getRow;
		int getCol;

		for (int row = 0; row < 8; row++) {
			for (int col = 0; col < 8; col++) {

				if (flipBoard) {
					getRow = 7 - row;
					getCol = 7 - col;
				} else {
					getRow = row;
					getCol = col;
				}
				chessSquares[row][col].clearChessPiece();

				if (board.hasPiece(getRow, getCol)) {
					chessSquares[row][col].showChessPiece(board.getPieceID(getRow, getCol), board.getPlayer(getRow, getCol));
				}

				if (debug) {
					chessSquares[row][col].updateDebugInfo("");
				}
			}
		}

		this.selectedSquare = null;
	}

	private void attachValidMoves() {
		Vector<Move> validMoves = adjudicator.getValidMoves();

		Move move;
		for (int m = 0; m < validMoves.size(); m++) {
			move = validMoves.elementAt(m);
			chessSquares[move.getFromRow()][move.getFromCol()].addValidMove(move);
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
		int getRow;
		int getCol;

		for (int i = 0; i < validMoves.size(); i++) {
			move = validMoves.elementAt(i);
			// Some pieces think they have valid moves but are actually invalid
			// because of higher level game situations like the move puts the
			// king in check.
			if (move.isValidated()) {
				if (flipBoard) {
					getRow = 7 - move.getToRow();
					getCol = 7 - move.getToCol();
				} else {
					getRow = move.getToRow();
					getCol = move.getToCol();
				}
				chessSquares[getRow][getCol].showAsValidMove(valid);

				if (debug) {
					if (valid && move != null) {
						chessSquares[getRow][getCol].updateDebugInfo(game.getMoveChosenPathValue(move) + "");
					} else {
						chessSquares[getRow][getCol].updateDebugInfo("");
					}
				}
			}

		}
	}

	private void colorSquaresDefault() {
		for (int r = 0; r < 8; r++) {
			for (int c = 0; c < 8; c++) {
				chessSquares[r][c].showAsDefault();
			}
		}
	}

	private void pieceTaken(PieceID id, Player player) {
		JLabel picLabel = new JLabel();

		if (player == Player.AI) {
			System.out.println("Ai loses piece " + id);
			picLabel.setIcon(new ImageIcon(this.getChessImage(id, Player.AI)));
			lostAiPiecesPanel.add(picLabel);
		} else {
			System.out.println("user loses piece " + id);
			picLabel.setIcon(new ImageIcon(this.getChessImage(id, Player.USER)));
			lostUserPiecesPanel.add(picLabel);
		}
	}

	private void refreshPiecesTaken() {
		JLabel picLabel;

		clearPiecesTaken();

		Vector<PieceID> takenPieces = adjudicator.getPiecesTaken(Player.USER);

		for (int i = 0; i < takenPieces.size(); i++) {
			picLabel = new JLabel();
			picLabel.setIcon(new ImageIcon(this.getChessImage(takenPieces.elementAt(i), Player.USER)));
			lostUserPiecesPanel.add(picLabel);
		}

		takenPieces = adjudicator.getPiecesTaken(Player.AI);

		for (int i = 0; i < takenPieces.size(); i++) {
			picLabel = new JLabel();
			picLabel.setIcon(new ImageIcon(this.getChessImage(takenPieces.elementAt(i), Player.AI)));
			lostAiPiecesPanel.add(picLabel);
		}

	}

	private void clearPiecesTaken() {
		lostAiPiecesPanel.removeAll();
		lostAiPiecesPanel.updateUI();
		lostUserPiecesPanel.removeAll();
		lostUserPiecesPanel.updateUI();
	}

	private void updateLastMovedSquare() {
		if (lastMovedSquare != null) {
			lastMovedSquare.showAsLastMoved(false);
			lastMovedSquare = null;
		}

		Move lastMove = adjudicator.getRoot().getMove();
		if (lastMove != null) {
			lastMovedSquare = chessSquares[lastMove.getToRow()][lastMove.getToCol()];
			lastMovedSquare.showAsLastMoved(true);
		}

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

		int fromRow;
		int fromCol;
		int toRow;
		int toCol;

		if (selectedSquare == null) {
			if (clickedSquare.hasPiece()) {

				selectedSquare = clickedSquare;
				selectedSquare.showAsSelected(true);

				colorValidMoveSquares(clickedSquare, true);
			}

		} else {

			if (selectedSquare != clickedSquare) {

				if (flipBoard) {
					fromRow = selectedSquare.getRow();
					fromCol = selectedSquare.getCol();
					toRow = 7 - clickedSquare.getRow();
					toCol = 7 - clickedSquare.getCol();
				} else {
					fromRow = selectedSquare.getRow();
					fromCol = selectedSquare.getCol();
					toRow = clickedSquare.getRow();
					toCol = clickedSquare.getCol();
				}

				Move m = new Move(fromRow, fromCol, toRow, toCol);
				Move validMove = selectedSquare.checkIfValidMove(m);
				if (validMove != null) {
					selectedSquare.showAsSelected(false);
					colorValidMoveSquares(selectedSquare, false);

					selectedSquare = null;

					userMoved(validMove);
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
			game.newGame(Player.AI);
		}

		if (arg0.getSource() == gameAsWhiteMenu) {
			game.newGame(Player.USER);
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
