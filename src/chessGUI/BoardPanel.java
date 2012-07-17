package chessGUI;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.*;
import javax.swing.border.Border;

import chessBackend.*;
import chessIO.ChessImages;
import chessPieces.Piece;
import chessPieces.PieceID;

public class BoardPanel extends JPanel implements MouseListener, ActionListener {

	private BoardGUI boardGUI;

	private int gameHeight;
	private int gameWidth;
	private int sidebarWidth;
	private int imageWidth;
	private int imageHeight;

	private JPanel squaresGUIPanel;
	private JPanel lostBlackPiecesPanel;
	private JPanel lostWhitePiecesPanel;
	private JPanel whiteTurnPanel;
	private JPanel blackTurnPanel;

	private JLabel whiteName;
	private JLabel blackName;
	private JLabel whiteTime;
	private JLabel blackTime;

	private JProgressBar whiteProgress;
	private JProgressBar blackProgress;

	private Border blackLine = BorderFactory.createLineBorder(Color.black);
	private Border redLine = BorderFactory.createLineBorder(Color.red);

	private SquarePanel[][] chessSquares;
	private ImageIcon[][] pieceIcons;
	private PieceGUI selectedComponent;
	private SquarePanel lastMovedSquare;

	private boolean flipBoard;
	private boolean makeMove;

	private boolean freelyMove;

	private int highLightCount;
	private long highLightMove;
	private Timer highLightTimer;

	private Timer turnTimer;

	private Adjudicator adjudicator;

	private boolean debug;

	public BoardPanel(BoardGUI boardGUI, boolean debug) {
		this(boardGUI, debug, false);
	}

	public BoardPanel(BoardGUI boardGUI, boolean debug, boolean showOnlyBoard) {
		super(new BorderLayout());

		this.addComponentListener(new ComponentListener() {

			@Override
			public void componentHidden(ComponentEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void componentMoved(ComponentEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void componentResized(ComponentEvent e) {
				// ImageIcon icon = ChessImages.getChessIcon(id, player);
				// Image img = icon.getImage();
				// Image newImage =
				// img.getScaledInstance(e.getComponent().getWidth(),
				// e.getComponent().getHeight(), Image.SCALE_SMOOTH);
				// picLabel.setIcon(new ImageIcon(newImage));

				pieceIcons = ChessImages.getScaledIcons((int) (e.getComponent().getHeight() * 1.25));
				for (int r = 0; r < 8; r++) {
					for (int c = 0; c < 8; c++) {
						chessSquares[r][c].updateIcon();
					}
				}
			}

			@Override
			public void componentShown(ComponentEvent e) {
				// TODO Auto-generated method stub

			}

		});

		this.adjudicator = new Adjudicator(null);
		this.debug = debug;
		this.boardGUI = boardGUI;
		this.flipBoard = false;

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		gameHeight = (int) ((double) screenSize.height * 0.8);
		imageHeight = (int) ((double) gameHeight * 0.10);
		imageWidth = (int) ((double) imageHeight * 0.6);
		sidebarWidth = 2 * (int) ((double) imageWidth * 1.15);
		gameWidth = 2 * sidebarWidth + gameHeight;

		lostBlackPiecesPanel = new JPanel(new FlowLayout());
		lostBlackPiecesPanel.setBackground(Color.GRAY);
		lostBlackPiecesPanel.setPreferredSize(new Dimension(sidebarWidth, gameHeight));
		lostBlackPiecesPanel.addMouseListener(this);
		if (!showOnlyBoard) {
			this.add(lostBlackPiecesPanel, BorderLayout.WEST);
		}

		squaresGUIPanel = new JPanel(new GridLayout(8, 8));
		squaresGUIPanel.setBackground(Color.GRAY);
		squaresGUIPanel.setPreferredSize(new Dimension(gameHeight, gameHeight));
		buildSquaresGUI();

		this.add(squaresGUIPanel, BorderLayout.CENTER);

		lostWhitePiecesPanel = new JPanel(new FlowLayout());
		lostWhitePiecesPanel.setBackground(Color.GRAY);
		lostWhitePiecesPanel.setPreferredSize(new Dimension(sidebarWidth, gameHeight));
		lostWhitePiecesPanel.addMouseListener(this);
		if (!showOnlyBoard) {
			this.add(lostWhitePiecesPanel, BorderLayout.EAST);
		}

		pieceIcons = ChessImages.getScaledIcons(gameHeight);

		this.setBackground(Color.GRAY);

		JPanel turnPanel = new JPanel();
		turnPanel.setBackground(Color.GRAY);

		whiteTurnPanel = new JPanel(new BorderLayout());
		whiteTurnPanel.setPreferredSize(new Dimension(200, 50));
		whiteTurnPanel.setBackground(Color.WHITE);
		whiteTurnPanel.setOpaque(true);
		whiteTurnPanel.addMouseListener(this);

		blackTurnPanel = new JPanel(new BorderLayout());
		blackTurnPanel.setPreferredSize(new Dimension(200, 50));
		blackTurnPanel.setBackground(Color.BLACK);
		blackTurnPanel.setOpaque(true);
		blackTurnPanel.addMouseListener(this);

		whiteName = new JLabel("White");
		whiteName.setHorizontalAlignment(SwingConstants.CENTER);
		whiteTurnPanel.add(whiteName, BorderLayout.NORTH);

		blackName = new JLabel("Black");
		blackName.setForeground(Color.WHITE);
		blackName.setHorizontalAlignment(SwingConstants.CENTER);
		blackTurnPanel.add(blackName, BorderLayout.NORTH);

		whiteTime = new JLabel("0:0");
		whiteTime.setHorizontalAlignment(SwingConstants.CENTER);
		whiteTurnPanel.add(whiteTime, BorderLayout.CENTER);

		blackTime = new JLabel("0:0");
		blackTime.setForeground(Color.WHITE);
		blackTime.setHorizontalAlignment(SwingConstants.CENTER);
		blackTurnPanel.add(blackTime, BorderLayout.CENTER);

		whiteProgress = new JProgressBar();
		whiteProgress.setValue(0);
		// whiteProgress.setStringPainted(true);
		whiteProgress.setBackground(Color.WHITE);
		whiteProgress.setForeground(Color.RED);
		whiteProgress.setBorderPainted(false);
		whiteProgress.setPreferredSize(new Dimension(10, 20));
		whiteTurnPanel.add(whiteProgress, BorderLayout.SOUTH);

		blackProgress = new JProgressBar();
		blackProgress.setValue(0);
		// blackProgress.setStringPainted(true);
		blackProgress.setBackground(Color.BLACK);
		blackProgress.setForeground(Color.RED);
		blackProgress.setBorderPainted(false);
		blackProgress.setPreferredSize(new Dimension(10, 20));
		blackTurnPanel.add(blackProgress, BorderLayout.SOUTH);

		whiteTurnPanel.setBorder(blackLine);
		blackTurnPanel.setBorder(blackLine);
		// TitledBorder border = BorderFactory.createTitledBorder(blackLine,
		// "Protocols");

		turnPanel.add(whiteTurnPanel);
		turnPanel.add(blackTurnPanel);

		if (!showOnlyBoard) {
			this.add(turnPanel, BorderLayout.PAGE_END);
		}

		highLightTimer = new Timer(200, this);

		turnTimer = new Timer(1000, this);
		turnTimer.start();

	}

	public void newGame(Board board) {

		adjudicator.newGame(board);

		refreshBoard();
		refreshPiecesTaken();

		colorSquaresDefault();

		attachValidMoves();

		updateLastMovedSquare();

		updateTurnPanels();

	}

	private void updateTurnPanels() {
		if (boardGUI != null) {
			whiteName.setText("Name: " + boardGUI.getPlayerName(Side.WHITE));
			blackName.setText("Name: " + boardGUI.getPlayerName(Side.BLACK));

			whiteTime.setText(getPlayerTimeString(Side.WHITE));
			blackTime.setText(getPlayerTimeString(Side.BLACK));
		}
	}

	private String getPlayerTimeString(Side side) {
		if (boardGUI != null) {
			long time = boardGUI.getPlayerTime(side);
			long min = time / 60000;
			long sec = (time / 1000) % 60;
			return min + "m:" + String.format("%02d", sec) + "s";
		} else {
			return "";
		}
	}

	public void showProgress(int progress) {
		if (getTurn() == Side.WHITE) {
			whiteProgress.setValue(progress);
			if (progress == 0) {
				whiteProgress.setStringPainted(false);
			} else {
				whiteProgress.setStringPainted(true);
			}

		} else {
			blackProgress.setValue(progress);
			if (progress == 0) {
				blackProgress.setStringPainted(false);
			} else {
				blackProgress.setStringPainted(true);
			}
		}
	}

	public ImageIcon getChessIcon(PieceID id, Side player) {
		return pieceIcons[player.ordinal()][id.ordinal()];
	}

	public void setFlipBoard(boolean flipBoard) {

		if (this.flipBoard != flipBoard) {

			this.flipBoard = flipBoard;

			colorSquaresDefault();

			if (adjudicator.getBoard() != null) {
				refreshBoard();

				updateLastMovedSquare();

				attachValidMoves();
			}
		}

	}

	public Side getTurn() {
		return adjudicator.getTurn();
	}

	public void flipBoard() {
		setFlipBoard(!flipBoard);
	}

	public void highlightMove(long move) {
		highLightCount = 5;
		highLightMove = move;
		highLightTimer.start();
		colorSquaresDefault();

	}

	public void setFreelyMove(boolean freelyMove) {

		if (this.freelyMove && !freelyMove) {
			// new game with freely moved setup
			refreshBoard();

			attachValidMoves();
		}

		this.freelyMove = freelyMove;

		if (freelyMove) {
			colorSquaresDefault();
		}
	}

	public boolean isFreelyMove() {
		return freelyMove;
	}

	private void freeMove(PieceGUI fromComponent, Component toComponent) {

		// normal square to square move
		if (fromComponent instanceof SquarePanel && toComponent instanceof SquarePanel) {

			SquarePanel fromSqr = (SquarePanel) fromComponent;
			SquarePanel toSqr = (SquarePanel) toComponent;

			Piece piece = adjudicator.getPiece(flipTrans(fromSqr.getRow()), flipTrans(fromSqr.getCol()));

			if (adjudicator.placePiece(piece, flipTrans(toSqr.getRow()), flipTrans(toSqr.getCol()))) {
				if (toSqr.getPieceID() != null) {
					takePiece(toSqr.getPieceID(), toSqr.getPlayer());
				}

				toSqr.showChessPiece(fromSqr.getPieceID(), fromSqr.getPlayer());

				fromSqr.clearChessPiece();

			}

		}

		// moving piece from board to side lines
		if (fromComponent instanceof SquarePanel && !(toComponent instanceof SquarePanel)) {
			SquarePanel fromSqr = (SquarePanel) fromComponent;

			Piece piece = adjudicator.getPiece(flipTrans(fromSqr.getRow()), flipTrans(fromSqr.getCol()));

			if (adjudicator.placePiece(piece, -1, -1)) {
				takePiece(fromSqr.getPieceID(), fromSqr.getPlayer());

				fromSqr.clearChessPiece();

			}
		}

		// moving piece from side lines to board
		if (fromComponent instanceof JPieceTakenLabel && toComponent instanceof SquarePanel) {
			SquarePanel toSqr = (SquarePanel) toComponent;
			JPieceTakenLabel fromLbl = (JPieceTakenLabel) fromComponent;

			Piece piece = new Piece(fromLbl.getPieceID(), fromLbl.getPlayer(), -1, -1, false);

			if (adjudicator.placePiece(piece, flipTrans(toSqr.getRow()), flipTrans(toSqr.getCol()))) {
				if (toSqr.getPieceID() != null) {
					takePiece(toSqr.getPieceID(), toSqr.getPlayer());
				}

				toSqr.showChessPiece(fromLbl.getPieceID(), fromLbl.getPlayer());

				fromLbl.getParent().remove(fromLbl);
				lostWhitePiecesPanel.updateUI();
				lostBlackPiecesPanel.updateUI();

			}

		}

	}

	private void buildSquaresGUI() {
		chessSquares = new SquarePanel[8][8];

		SquarePanel square;
		for (int row = 0; row < 8; row++) {
			for (int col = 0; col < 8; col++) {
				square = new SquarePanel(this, col % 2 == row % 2, row, col, debug);
				square.addMouseListener(this);

				chessSquares[row][col] = square;
				squaresGUIPanel.add(chessSquares[row][col]);
			}
		}

	}

	public boolean moveMade(long move) {

		if (adjudicator.move(move)) {
			refreshBoard();

			if (Move.hasPieceTaken(move)) {
				takePiece(Move.getPieceTakenID(move), adjudicator.getTurn());
			}

			updateLastMovedSquare();

			attachValidMoves();

			setGameSatus(adjudicator.getGameStatus(), adjudicator.getTurn());

		} else {
			return false;
		}

		return true;

	}

	public void makeMove() {
		this.makeMove = true;
		// setFlipBoard(adjudicator.getTurn() == Side.BLACK);
	}

	public void setGameSatus(GameStatus status, Side playerTurn) {

		if (status == GameStatus.CHECK) {
			System.out.println("Check!");
		}

		if (status == GameStatus.CHECKMATE) {
		}

		if (status == GameStatus.STALEMATE) {

		}
	}

	public long undoMove() {
		long undoneMove = 0;

		if (adjudicator.canUndo()) {

			undoneMove = adjudicator.undo();
			// full refresh of board info
			refreshBoard();

			// reset all squares to default border color
			colorSquaresDefault();

			// attach the new valid moves to appropriate squares
			attachValidMoves();

			// update side line view of pieces that have been taken
			if (Move.hasPieceTaken(undoneMove)) {
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

	public long redoMove() {
		long redoneMove = 0;

		if (adjudicator.hasUndoneMoves()) {
			redoneMove = adjudicator.getLastUndoneMove();
			boardGUI.makeMove(redoneMove);
		}

		return redoneMove;
	}

	public long getLastUndoneMove() {
		return adjudicator.getLastUndoneMove();
	}

	public boolean canRedo() {
		return adjudicator.hasUndoneMoves();
	}

	public void refreshTurn() {
		if (adjudicator.getTurn() == Side.WHITE) {
			whiteTurnPanel.setBorder(redLine);
			blackTurnPanel.setBorder(blackLine);
		} else {
			whiteTurnPanel.setBorder(blackLine);
			blackTurnPanel.setBorder(redLine);
		}
	}

	public void refreshBoard() {

		if (adjudicator == null) {
			return;
		}

		highLightCount = 0;

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

		refreshTurn();

		this.selectedComponent = null;
	}

	private void attachValidMoves() {

		clearValidMoves();

		ArrayList<Long> validMoves = adjudicator.getValidMoves();

		long move;
		for (int m = 0; m < validMoves.size(); m++) {
			move = validMoves.get(m);
			getChessSquare(Move.getFromRow(move), Move.getFromCol(move)).addValidMove(move);
		}

		if (selectedComponent instanceof SquarePanel) {
			colorValidMoveSquares((SquarePanel) selectedComponent, true);
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
		Vector<Long> validMoves = square.getValidMoves();
		long move;

		for (int i = 0; i < validMoves.size(); i++) {
			move = validMoves.elementAt(i);

			if (Move.getNote(move) == MoveNote.CASTLE_FAR || Move.getNote(move) == MoveNote.CASTLE_NEAR) {
				getChessSquare(Move.getToRow(move), Move.getToCol(move)).showAsValidMove(valid, Color.BLUE);
			} else {
				getChessSquare(Move.getToRow(move), Move.getToCol(move)).showAsValidMove(valid);
			}

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

		selectedComponent = null;

		for (int r = 0; r < 8; r++) {
			for (int c = 0; c < 8; c++) {
				chessSquares[r][c].showAsDefault();
			}
		}
	}

	private void takePiece(PieceID pieceTakenID, Side pieceTakenSide) {
		JPieceTakenLabel picLabel = new JPieceTakenLabel(pieceTakenID, pieceTakenSide);
		picLabel.addMouseListener(this);

		if (pieceTakenSide == Side.WHITE) {
			lostWhitePiecesPanel.add(picLabel);
		} else {
			lostBlackPiecesPanel.add(picLabel);
		}
	}

	private void refreshPiecesTaken() {
		JPieceTakenLabel picLabel;

		clearPiecesTaken();

		Vector<Piece> takenPieces = adjudicator.getPiecesTaken(Side.WHITE);

		for (int i = 0; i < takenPieces.size(); i++) {
			picLabel = new JPieceTakenLabel(takenPieces.elementAt(i).getPieceID(), takenPieces.elementAt(i).getSide());
			picLabel.addMouseListener(this);
			lostWhitePiecesPanel.add(picLabel);
		}

		takenPieces = adjudicator.getPiecesTaken(Side.BLACK);

		for (int i = 0; i < takenPieces.size(); i++) {
			picLabel = new JPieceTakenLabel(takenPieces.elementAt(i).getPieceID(), takenPieces.elementAt(i).getSide());
			picLabel.addMouseListener(this);
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

		long lastMove = adjudicator.getLastMoveMade();
		if (lastMove != 0) {
			lastMovedSquare = getChessSquare(Move.getToRow(lastMove), Move.getToCol(lastMove));
			lastMovedSquare.showAsLastMoved(true);
		}

	}

	private int flipTrans(int in) {
		if (flipBoard) {
			return 7 - in;
		} else {
			return in;
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

	public Board getBoard() {
		return adjudicator.getBoard();
	}

	private long getOrientedMove(int fromRow, int fromCol, int toRow, int toCol) {
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

		return Move.moveLong(orienFromRow, orienFromCol, orienToRow, orienToCol);

	}

	// Listener Methods

	@Override
	public void mousePressed(MouseEvent arg0) {
		SquarePanel clickedSquare = null;
		JPieceTakenLabel takenClicked = null;
		Component c = arg0.getComponent();

		if (arg0.getSource() == blackTurnPanel) {
			if (freelyMove) {
				adjudicator.getBoard().setTurn(Side.BLACK);
				refreshTurn();
			}
			return;
		}

		if (arg0.getSource() == whiteTurnPanel) {
			if (freelyMove) {
				adjudicator.getBoard().setTurn(Side.WHITE);
				refreshTurn();
			}
			return;
		}

		if (c instanceof SquarePanel) {
			clickedSquare = (SquarePanel) c;
		} else {
			if (c instanceof JPieceTakenLabel) {
				takenClicked = (JPieceTakenLabel) c;
			}
		}

		// lock board when it's not the users turn
		if (!makeMove) {
			System.out.println("Not your turn!");
			return;
		}

		if (clickedSquare != null) {

			if (selectedComponent == null) {
				if (clickedSquare.hasPiece()) {

					if (arg0.isControlDown() && freelyMove) {
						freeMove(clickedSquare, null);
					} else {
						selectedComponent = clickedSquare;
						selectedComponent.showAsSelected(true);

						if (!freelyMove) {
							colorValidMoveSquares(clickedSquare, true);
						}
					}
				}

			} else {

				if (selectedComponent != clickedSquare && selectedComponent instanceof SquarePanel) {
					// square selected and square clicked

					SquarePanel selectedSquare = (SquarePanel) selectedComponent;

					if (!freelyMove) {
						long m = getOrientedMove(selectedSquare.getRow(), selectedSquare.getCol(), clickedSquare.getRow(), clickedSquare.getCol());

						long validMove = selectedSquare.checkIfValidMove(m);
						if (validMove != 0) {
							selectedSquare.showAsSelected(false);
							colorValidMoveSquares(selectedSquare, false);

							selectedComponent = null;

							// makeMove(validMove);
							makeMove = false;

							boardGUI.makeMove(validMove);

						} else {
							if (clickedSquare.hasPiece()) {

								colorValidMoveSquares((SquarePanel) selectedComponent, false);
								colorValidMoveSquares(clickedSquare, true);

								((SquarePanel) selectedComponent).showAsSelected(false);
								selectedComponent = clickedSquare;
								((SquarePanel) selectedComponent).showAsSelected(true);

							} else {
								System.out.println("Invalid move");
							}
						}
					} else {
						// freely moving from square to square
						freeMove(selectedSquare, clickedSquare);
						selectedComponent.showAsSelected(false);
						selectedComponent = null;
					}

				}

			}

			// moving piece from side lines to board
			if (selectedComponent instanceof JPieceTakenLabel) {
				freeMove(selectedComponent, clickedSquare);
				selectedComponent.showAsSelected(false);
				selectedComponent = null;
			}
		}

		if (freelyMove) {
			// selecting side line piece for moving to board
			if (takenClicked != null) {

				if (selectedComponent != null) {
					selectedComponent.showAsSelected(false);
				}

				selectedComponent = takenClicked;
				selectedComponent.showAsSelected(true);
			}

			// moving from board to side lines
			if (takenClicked == null && clickedSquare == null && selectedComponent instanceof SquarePanel) {
				freeMove(selectedComponent, c);
				selectedComponent.showAsSelected(false);
				selectedComponent = null;
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

	@Override
	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == highLightTimer) {

			SquarePanel from = getChessSquare(Move.getFromRow(highLightMove), Move.getFromCol(highLightMove));
			SquarePanel to = getChessSquare(Move.getToRow(highLightMove), Move.getToCol(highLightMove));

			if (highLightCount > 0) {
				if (highLightCount % 2 == 0) {
					to.showAsHighLighted(false);
					from.showAsHighLighted(true);
				} else {
					to.showAsHighLighted(true);
					from.showAsHighLighted(false);
				}
			} else {
				highLightTimer.stop();
				to.showAsHighLighted(false);
				from.showAsHighLighted(false);
			}

			highLightCount--;
		}

		if (e.getSource() == turnTimer) {
			whiteTime.setText(getPlayerTimeString(Side.WHITE));
			blackTime.setText(getPlayerTimeString(Side.BLACK));
		}

	}
}
