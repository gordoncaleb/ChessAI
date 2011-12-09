package chessGUI;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

import chessAI.DecisionNode;
import chessBackend.*;
import chessPieces.Piece;
import chessPieces.PieceID;

public class BoardGUI implements MouseListener, KeyListener {

	private DecisionNode rootDecision;
	private Game game;
	private JFrame frame;
	private JPanel boardGUIPanel;
	private JPanel lostAiPiecesPanel;
	private JPanel lostUserPiecesPanel;
	private PiecePositionGUI[][] chessSquares;
	private Image[][] chessPieceGraphics;
	private PiecePositionGUI selectedSquare;
	private PiecePositionGUI lastMovedSquare;

	// debug components
	private boolean debug;
	private JFrame debugFrame;
	private JTree decisionTreeGUI;
	private DefaultMutableTreeNode rootGUI;
	private JScrollPane treeView;

	public BoardGUI(Game game, DecisionNode rootDecision, boolean debug) {
		frame = new JFrame("CHESS AI");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());

		boardGUIPanel = new JPanel(new GridLayout(8, 8));
		boardGUIPanel.setBackground(Color.BLACK);
		boardGUIPanel.setPreferredSize(new Dimension(800, 800));
		// boardGUIPanel.setBorder(BorderFactory.createLineBorder(Color.black));
		frame.add(boardGUIPanel, BorderLayout.CENTER);

		lostAiPiecesPanel = new JPanel();
		lostAiPiecesPanel.setBackground(Color.BLACK);
		lostAiPiecesPanel.setPreferredSize(new Dimension(100, 800));
		frame.add(lostAiPiecesPanel, BorderLayout.WEST);

		lostUserPiecesPanel = new JPanel();
		lostUserPiecesPanel.setBackground(Color.BLACK);
		lostUserPiecesPanel.setPreferredSize(new Dimension(100, 800));
		frame.add(lostUserPiecesPanel, BorderLayout.EAST);

		this.rootDecision = rootDecision;
		this.game = game;
		this.loadChessImages();
		this.buildBoardGUI();

		frame.setSize(1000, 800);
		// frame.setResizable(false);
		// frame.pack();
		frame.setVisible(true);

		this.debug = debug;
		if (debug) {
			debugFrame = new JFrame("Decision Tree Observer");
			debugFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			debugFrame.setLayout(new BorderLayout(1, 1));
			debugFrame.setSize(800, 800);
			// debugFrame.setResizable(false);
			debugFrame.setVisible(true);
			buildDebugGUI();
			setRootDecisionTree(rootDecision);
		}
	}

	private void buildBoardGUI() {
		chessSquares = new PiecePositionGUI[8][8];
		PiecePositionGUI square;

		for (int row = 0; row < 8; row++) {
			for (int col = 0; col < 8; col++) {
				square = new PiecePositionGUI(col % 2 == row % 2, row, col);
				square.setGUI(this);
				square.addMouseListener(this);

				if (rootDecision.getBoard().getPiece(row, col) != null) {
					square.setChessPiece(rootDecision.getBoard().getPiece(row, col));
				}

				chessSquares[row][col] = square;
				boardGUIPanel.add(chessSquares[row][col]);
			}
		}
	}

	private void loadChessImages() {
		chessPieceGraphics = new Image[2][6];
		String pieceNames[] = { "rook", "knight", "bishop", "queen", "king", "pawn" };
		String imgDir = ".\\img\\pieces2\\";

		for (int i = 0; i < 6; i++) {
			try {
				chessPieceGraphics[0][i] = ImageIO.read(new File(imgDir + "black_" + pieceNames[i] + ".png")).getScaledInstance(60, 100,
						Image.SCALE_SMOOTH);
				chessPieceGraphics[1][i] = ImageIO.read(new File(imgDir + "white_" + pieceNames[i] + ".png")).getScaledInstance(60, 100,
						Image.SCALE_SMOOTH);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public Image getChessImage(PieceID id, Player player) {
		return chessPieceGraphics[player.ordinal()][id.ordinal()];
	}

	public void moveChessPiece(Move m) {
		Piece p = chessSquares[m.getFromRow()][m.getFromCol()].getPiece();
		chessSquares[m.getToRow()][m.getToCol()].setChessPiece(p);
		chessSquares[m.getFromRow()][m.getFromCol()].clearChessPiece();

		if (lastMovedSquare != null)
			lastMovedSquare.setLastMoved(false);

		lastMovedSquare = chessSquares[m.getToRow()][m.getToCol()];
		lastMovedSquare.setLastMoved(true);

		// chessSquares[m.getToRow()][m.getToCol()].invalidate();
	}

	public void aiMove(DecisionNode aiDecision) {

		rootDecision = aiDecision;
		setBoard(rootDecision.getBoard());

		if (debug)
			setRootDecisionTree(rootDecision);

		// check for taken pieces
		if(aiDecision.getNodeMove().getNote() == MoveNote.TAKE_PIECE){
			userPieceTaken(aiDecision.getNodeMove().getPieceTaken());
		}

	}

	public void setBoard(Board board) {
		Vector<Piece> pieces;
		Piece piece;

		for (int row = 0; row < 8; row++) {
			for (int col = 0; col < 8; col++) {
				chessSquares[row][col].clearChessPiece();
			}
		}

		this.selectedSquare = null;

		pieces = board.getPlayerPieces(Player.USER);
		for (int i = 0; i < pieces.size(); i++) {
			piece = pieces.elementAt(i);
			chessSquares[piece.getRow()][piece.getCol()].setChessPiece(piece);
		}

		pieces = board.getPlayerPieces(Player.AI);
		for (int i = 0; i < pieces.size(); i++) {
			piece = pieces.elementAt(i);
			chessSquares[piece.getRow()][piece.getCol()].setChessPiece(piece);
		}

		Piece lastMovedPiece = board.getLastMovedPiece();
		if (lastMovedPiece != null) {
			chessSquares[lastMovedPiece.getRow()][lastMovedPiece.getCol()].setLastMoved(true);
		}
	}

	private void colorValidMoveSquares(Piece piece, boolean valid) {
		Vector<Move> validMoves = piece.getValidMoves();
		Move move;

		for (int i = 0; i < validMoves.size(); i++) {
			move = validMoves.elementAt(i);
			// Some pieces think they have valid moves but are actually invalid
			// because of higher level game situations like the move puts the
			// king in check.
			if (move.isValid()) {
				chessSquares[move.getToRow()][move.getToCol()].setValidMove(valid);
			}
		}
	}

	private void aiPieceTaken(PieceID pieceTaken) {
		System.out.println("Ai loses piece " + pieceTaken.toString());
	}

	private void userPieceTaken(PieceID pieceTaken) {
		System.out.println("user loses piece " + pieceTaken.toString());
	}

	// Debug methods
	private void buildDebugGUI() {
		rootGUI = new DefaultMutableTreeNode("Current Board");
		decisionTreeGUI = new JTree(rootGUI);
		decisionTreeGUI.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		decisionTreeGUI.addKeyListener(this);
		decisionTreeGUI.addMouseListener(this);
		// decisionTreeGUI.setSize(600, 600);
		treeView = new JScrollPane(decisionTreeGUI);
		// treeView.setSize(800, 800);
		debugFrame.add(treeView, BorderLayout.CENTER);
		// buildDecisionTreeGUI(rootGUI, decisionTreeRoot);
	}

	public void setRootDecisionTree(DecisionNode decisionTreeRoot) {
		rootGUI.removeAllChildren();
		buildDecisionTreeGUI(rootGUI, decisionTreeRoot);
		decisionTreeGUI.updateUI();
	}

	private void buildDecisionTreeGUI(DefaultMutableTreeNode branchGUI, DecisionNode branch) {
		DefaultMutableTreeNode childGUI;
		DecisionNode child;

		branchGUI.setUserObject(branch);

		Vector<DecisionNode> children = branch.getChildren();
		for (int i = 0; i < children.size(); i++) {
			child = children.elementAt(i);
			childGUI = new DefaultMutableTreeNode(child);
			branchGUI.add(childGUI);

			if (child.hasChildren()) {
				buildDecisionTreeGUI(childGUI, child);
			}
		}
	}

	// Listener Methods

	public void mouseClicked(MouseEvent arg0) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) decisionTreeGUI.getLastSelectedPathComponent();

		if (node == null || !arg0.isShiftDown())
			// Nothing is selected.
			return;

		Object nodeInfo = node.getUserObject();

		if (nodeInfo instanceof DecisionNode) {
			// System.out.println("Mouse Clicked " + nodeInfo.toString());
			this.setBoard(((DecisionNode) nodeInfo).getBoard());
		}
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		PiecePositionGUI clickedSquare;
		Component c = arg0.getComponent();

		if (c instanceof PiecePositionGUI) {
			clickedSquare = (PiecePositionGUI) c;
		} else {
			return;
		}

		if (selectedSquare == null) {
			if (clickedSquare.hasPiece()) {
				if (clickedSquare.getPiece().getPlayer() == Player.USER) {

					selectedSquare = clickedSquare;
					selectedSquare.setSelected(true);
					colorValidMoveSquares(selectedSquare.getPiece(), true);

					// selectedSquare.getPiece().generateValidMoves(board);
				}
			}

		} else {

			if (selectedSquare != clickedSquare) {
				Piece p = selectedSquare.getPiece();
				Move m = new Move(p.getRow(), p.getCol(), clickedSquare.getRow(), clickedSquare.getCol());
				Move validMove = p.checkIfValidMove(m);
				if (validMove != null) {
					// board.moveChessPiece(validMove);
					selectedSquare.setSelected(false);
					// clickedSquare.attachChessPiece(selectedSquare.getPiece());
					// selectedSquare.clearChessPiece();
					selectedSquare = null;
					
					if(validMove.getNote() == MoveNote.TAKE_PIECE){
						aiPieceTaken(validMove.getPieceTaken());
					}
					
					game.userMoved(validMove);
				} else {
					if (clickedSquare.hasPiece()) {
						if (clickedSquare.getPiece().getPlayer() == Player.USER) {

							colorValidMoveSquares(selectedSquare.getPiece(), false);
							colorValidMoveSquares(clickedSquare.getPiece(), true);

							selectedSquare.setSelected(false);
							selectedSquare = clickedSquare;
							selectedSquare.setSelected(true);
							// selectedSquare.getPiece().generateValidMoves(board);
						}
					} else {
						System.out.println("Invalid move");
					}
				}
			} else {

			}
		}

	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) decisionTreeGUI.getLastSelectedPathComponent();

		// System.out.println("Key typed");
		if (node == null)
			// Nothing is selected.
			return;

		Object nodeInfo = node.getUserObject();

		if (nodeInfo instanceof DecisionNode) {

			if (((int) arg0.getKeyChar()) == 10) {
				System.out.println("Enter pressed on " + nodeInfo.toString());

			} else {
				if (arg0.isShiftDown() && arg0.isActionKey()) {
					this.setBoard(((DecisionNode) nodeInfo).getBoard());
				}
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// System.out.println("Key released");
	}

	@Override
	public void keyTyped(KeyEvent arg0) {

	}

}
