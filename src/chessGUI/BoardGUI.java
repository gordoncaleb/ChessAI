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
	private int gameHeight;
	private int gameWidth;
	private int sidebarWidth;
	private int imageWidth;
	private int imageHeight;
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

	public BoardGUI(Game game, boolean debug) {
		this.debug = debug;

		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		gameHeight = (int) ((double) dim.height * 0.8);
		imageHeight = (int) ((double) gameHeight * 0.10);
		imageWidth = (int) ((double) imageHeight * 0.6);
		sidebarWidth = 2 * (int) ((double) imageWidth * 1.1);
		gameWidth = 2 * sidebarWidth + gameHeight;

		frame = new JFrame("CHESS AI");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());

		boardGUIPanel = new JPanel(new GridLayout(8, 8));
		boardGUIPanel.setBackground(Color.GRAY);
		boardGUIPanel.setPreferredSize(new Dimension(gameHeight, gameHeight));
		// boardGUIPanel.setBorder(BorderFactory.createLineBorder(Color.black));
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
			//setRootDecisionTree(rootDecision);
		}
	}

	public void newGame(DecisionNode rootDecision) {
		this.rootDecision = rootDecision;
		clearPiecesTaken();
		setBoard(rootDecision.getBoard());
	}

	private void buildBoardGUI() {
		chessSquares = new PiecePositionGUI[8][8];
		PiecePositionGUI square;

		for (int row = 0; row < 8; row++) {
			for (int col = 0; col < 8; col++) {
				square = new PiecePositionGUI(col % 2 == row % 2, row, col, debug);
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
		String imgDir = "img\\pieces2\\";

		for (int i = 0; i < 6; i++) {
			try {
				chessPieceGraphics[0][i] = ImageIO.read(new File(imgDir + "black_" + pieceNames[i] + ".png")).getScaledInstance(imageWidth,
						imageHeight, Image.SCALE_SMOOTH);
				chessPieceGraphics[1][i] = ImageIO.read(new File(imgDir + "white_" + pieceNames[i] + ".png")).getScaledInstance(imageWidth,
						imageHeight, Image.SCALE_SMOOTH);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public Image getChessImage(PieceID id, Player player) {
		return chessPieceGraphics[player.ordinal()][id.ordinal()];
	}

	public void setAiResponse(DecisionNode aiResponse) {

		rootDecision = aiResponse;
		setBoard(rootDecision.getBoard());

		if (debug)
			setRootDecisionTree(rootDecision);

		// check for taken pieces
		if (aiResponse.getMove().getNote() == MoveNote.TAKE_PIECE) {
			userPieceTaken(aiResponse.getMove().getPieceTaken());
		}

		if (aiResponse.getStatus() == GameStatus.CHECK) {
			System.out.println("CHECK!");
		}

		if (aiResponse.getPlayer() == Player.USER) {

			if (aiResponse.getStatus() == GameStatus.CHECKMATE) {
				Object[] options = { "Yes, please", "Nah, I'm kinda a bitch." };
				int n = JOptionPane.showOptionDialog(frame, "You just got schooled homie.\nWanna try again?", "Ouch!", JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE, null, // do not use a
															// custom
															// Icon
						options, // the titles of buttons
						options[0]); // default button title

				if (n == JOptionPane.YES_OPTION) {
					game.newGame();
				} else {
					System.exit(0);
				}

			}

			if (aiResponse.getStatus() == GameStatus.STALEMATE) {
				Object[] options = { "Yes, please", "Nah, maybe later." };
				int n = JOptionPane.showOptionDialog(frame, "Stalemate...hmmm close call.\nWanna try again?", "", JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE, null, // do not use a
															// custom
															// Icon
						options, // the titles of buttons
						options[0]); // default button title

				if (n == JOptionPane.YES_OPTION) {
					game.newGame();
				} else {
					System.exit(0);
				}

			}

		} else {

			if (aiResponse.getStatus() == GameStatus.CHECKMATE) {
				Object[] options = { "Yeah, why not?", "Nah." };
				int n = JOptionPane.showOptionDialog(frame, "Nicely done boss.\nWanna rematch?", "Ouch!", JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE, null, // do not use a
															// custom
															// Icon
						options, // the titles of buttons
						options[0]); // default button title

				if (n == JOptionPane.YES_OPTION) {
					game.newGame();
				} else {
					System.exit(0);
				}

			}

			if (aiResponse.getStatus() == GameStatus.STALEMATE) {
				Object[] options = { "Yes, please", "Nah, maybe later." };
				int n = JOptionPane.showOptionDialog(frame, "Stalemate...hmmm close call.\nWanna try again?", "", JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE, null, // do not use a
															// custom
															// Icon
						options, // the titles of buttons
						options[0]); // default button title

				if (n == JOptionPane.YES_OPTION) {
					game.newGame();
				} else {
					System.exit(0);
				}
			}

		}

	}

	public void setBoard(Board board) {

		for (int row = 0; row < 8; row++) {
			for (int col = 0; col < 8; col++) {
				chessSquares[row][col].clearChessPiece();
				
				if(board.hasPiece(row, col)){
					chessSquares[row][col].setChessPiece(board.getPiece(row, col));
				}

				if (debug) {
					chessSquares[row][col].updateDebugInfo("");
				}
			}
		}

		this.selectedSquare = null;

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
			if (move.isValidated()) {
				chessSquares[move.getToRow()][move.getToCol()].setValidMove(valid);

				if (debug) {
					if (valid && move != null) {
						chessSquares[move.getToRow()][move.getToCol()].updateDebugInfo(move.getNode().getChosenPathValue() + "");
					} else {
						chessSquares[move.getToRow()][move.getToCol()].updateDebugInfo("");
					}
				}
			}

		}
	}

	private void aiPieceTaken(PieceID pieceTaken) {
		System.out.println("Ai loses piece " + pieceTaken.toString());
		JLabel picLabel = new JLabel();
		picLabel.setIcon(new ImageIcon(this.getChessImage(pieceTaken, Player.AI)));
		lostAiPiecesPanel.add(picLabel);
	}

	private void userPieceTaken(PieceID pieceTaken) {
		System.out.println("user loses piece " + pieceTaken.toString());
		JLabel picLabel = new JLabel();
		picLabel.setIcon(new ImageIcon(this.getChessImage(pieceTaken, Player.USER)));
		lostUserPiecesPanel.add(picLabel);
	}

	private void clearPiecesTaken() {
		lostAiPiecesPanel.removeAll();
		lostAiPiecesPanel.updateUI();
		lostUserPiecesPanel.removeAll();
		lostUserPiecesPanel.updateUI();
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
		// showDecisionTreeScores(decisionTreeRoot);
		rootGUI.removeAllChildren();
		buildDecisionTreeGUI(rootGUI, decisionTreeRoot);
		decisionTreeGUI.updateUI();
	}

	public void showDecisionTreeScores(DecisionNode decisionTreeRoot) {
		DecisionNode child;
		int toRow;
		int toCol;

		child = decisionTreeRoot.getHeadChild();
		for (int i = 0; i < decisionTreeRoot.getChildrenSize(); i++) {
			toRow = child.getMove().getToRow();
			toCol = child.getMove().getToCol();
			chessSquares[toRow][toCol].updateDebugInfo(child.getChosenPathValue() + "");
			child = child.getNextSibling();
		}
	}

	private void buildDecisionTreeGUI(DefaultMutableTreeNode branchGUI, DecisionNode branch) {
		DefaultMutableTreeNode childGUI;
		DecisionNode child;

		branchGUI.setUserObject(branch);

		child = branch.getHeadChild();
		for (int i = 0; i < branch.getChildrenSize(); i++) {
			childGUI = new DefaultMutableTreeNode(child);
			branchGUI.add(childGUI);

			if (child.hasChildren()) {
				buildDecisionTreeGUI(childGUI, child);
			}
			child = child.getNextSibling();
		}
	}

	// Listener Methods

	public void mouseClicked(MouseEvent arg0) {

		if (debug) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) decisionTreeGUI.getLastSelectedPathComponent();

			if (node == null || !arg0.isShiftDown())
				// Nothing is selected.
				return;

			Object nodeInfo = node.getUserObject();

			if (nodeInfo instanceof DecisionNode) {
				// System.out.println("Mouse Clicked " + nodeInfo.toString());
				this.setBoard(((DecisionNode) nodeInfo).getBoard());
				// showDecisionTreeScores(((DecisionNode) nodeInfo));
			}
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
				// if (clickedSquare.getPiece().getPlayer() == Player.USER) {

				selectedSquare = clickedSquare;
				selectedSquare.setSelected(true);

				// selectedSquare.getPiece().generateValidMoves(board);
				// }

				colorValidMoveSquares(clickedSquare.getPiece(), true);
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

					if (validMove.getNote() == MoveNote.TAKE_PIECE) {
						aiPieceTaken(validMove.getPieceTaken());
					}

					setBoard(validMove.getNode().getBoard());
					game.userMoved(validMove.getNode());
				} else {
					if (clickedSquare.hasPiece()) {
						// if (clickedSquare.getPiece().getPlayer() ==
						// Player.USER) {

						colorValidMoveSquares(selectedSquare.getPiece(), false);
						colorValidMoveSquares(clickedSquare.getPiece(), true);

						selectedSquare.setSelected(false);
						selectedSquare = clickedSquare;
						selectedSquare.setSelected(true);
						// selectedSquare.getPiece().generateValidMoves(board);
						// }

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

	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) decisionTreeGUI.getLastSelectedPathComponent();

		// System.out.println("Key typed");
		if (node == null)
			// Nothing is selected.
			return;

		Object nodeInfo = node.getUserObject();

		if (nodeInfo instanceof DecisionNode) {
			DecisionNode branch = ((DecisionNode) nodeInfo);

			if (((int) arg0.getKeyChar()) == 10) {
				System.out.println("Enter pressed on " + nodeInfo.toString());
				game.growBranch(branch);
				setRootDecisionTree(rootDecision);

			} else {
				if (arg0.isShiftDown() && arg0.isActionKey()) {
					this.setBoard(branch.getBoard());
					// showDecisionTreeScores(((DecisionNode) nodeInfo));
				}
			}
		}
	}

	@Override
	public void keyTyped(KeyEvent arg0) {

	}

}
