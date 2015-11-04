package com.gordoncaleb.chess.ui.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

import com.gordoncaleb.chess.board.Move;
import com.gordoncaleb.chess.board.Side;
import com.gordoncaleb.chess.board.pieces.Piece;

public class SquarePanel extends JPanel implements PieceGUI {
	private static final long serialVersionUID = 1L;

	private int row;
	private int col;
	private int id;
	private int player;
	private ArrayList<Move> validMoves;

	// Debug components
	private JLabel debugLocationLabel;
	private JLabel debugScoreLabel;
	private JLabel debugPieceValue;
	private boolean debug;

	private JLabel picLabel;
	private boolean selectedSquare;
	private boolean lightSquare;
	private boolean lastMovedSquare;
	private boolean validMoveSquare;
	private boolean highLightedSquare;

	private Color dark = new Color(181, 136, 99);
	private Color light = new Color(240, 217, 181);

	private Color darkSelected = new Color(50, 50, 50);
	private Color lightSelected = new Color(230, 230, 230);

	private JPanel imagePane;

	public static Color validColorDefault = Color.YELLOW;

	public static int borderSize = 3;

	private Color lastMoved = new Color(255, 0, 0);
	private Color validColor = Color.YELLOW;
	
	
	private BoardPanel boardPanel;

	public SquarePanel(BoardPanel boardPanel, boolean lightSquare, int row, int col, boolean debug) {
		super(new BorderLayout());
		
		this.boardPanel = boardPanel;

		validMoves = new ArrayList<Move>();
		this.row = row;
		this.col = col;

		this.lightSquare = lightSquare;

		picLabel = new JLabel();

		this.debug = debug;

		imagePane = new JPanel(new FlowLayout());
		imagePane.setBackground(this.getBackground());
		imagePane.add(picLabel);

		updateBackgroundColor();
		updateBorderColor();

		this.add(imagePane, BorderLayout.PAGE_START);

		if (debug) {
			JPanel debugPane = new JPanel(new BorderLayout());
			debugPane.setBackground(this.getBackground());
			debugLocationLabel = new JLabel("(" + row + "," + col + ")");
			debugLocationLabel.setFont(new Font("Arial", Font.ITALIC, 10));
			debugLocationLabel.setForeground(Color.BLACK);
			debugPane.add(debugLocationLabel, BorderLayout.LINE_START);
			debugScoreLabel = new JLabel();
			debugScoreLabel.setFont(new Font("Arial", Font.BOLD, 12));
			debugScoreLabel.setOpaque(true);
			debugScoreLabel.setBackground(Color.BLACK);
			debugScoreLabel.setForeground(Color.YELLOW);
			debugPane.add(debugScoreLabel, BorderLayout.LINE_END);
			debugPieceValue = new JLabel();
			debugPieceValue.setFont(new Font("Arial", Font.BOLD, 12));
			// debugPieceValue.setOpaque(true);
			// debugPieceValue.setBackground(Color.BLACK);
			debugPieceValue.setForeground(Color.BLUE);
			debugPane.add(debugPieceValue, BorderLayout.CENTER);
			this.add(debugPane, BorderLayout.PAGE_END);
		}
	}

	public void updateBackgroundColor() {

		if (highLightedSquare) {
			this.setBackground(Color.YELLOW);
		} else {
			if (lightSquare) {
				this.setBackground(light);
			} else {
				this.setBackground(dark);
			}
		}

		imagePane.setBackground(this.getBackground());

	}

	public void updateBorderColor() {

		if (highLightedSquare) {
			// this.setBorder(BorderFactory.createLineBorder(Color.ORANGE));
			this.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		} else {
			if (validMoveSquare) {

				this.setBorder(BorderFactory.createLineBorder(validColor, borderSize));

				// this.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED,validMove,Color.black));
			} else {
				if (lastMovedSquare) {
					this.setBorder(BorderFactory.createLineBorder(lastMoved, borderSize));
					// this.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED,lastMoved,Color.black));
				} else {
					if (selectedSquare) {
						this.setBorder(BorderFactory.createLineBorder(Color.black, borderSize));
					} else {
						// this.setBorder(BorderFactory.createLineBorder(Color.black));
						this.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
						// this.setBorder(BorderFactory.createEtchedBorder());
					}
				}
			}
		}
	}

	public void showAsHighLighted(boolean highLightedSquare) {
		this.highLightedSquare = highLightedSquare;
		updateBackgroundColor();
		updateBorderColor();
	}

	public void showAsSelected(boolean selected) {
		this.selectedSquare = selected;
		updateBorderColor();
	}

	public void showAsValidMove(boolean validMoveSquare, Color validColor) {
		this.validMoveSquare = validMoveSquare;
		this.validColor = validColor;
		updateBorderColor();
	}

	public void showAsValidMove(boolean validMoveSquare) {
		this.validMoveSquare = validMoveSquare;
		this.validColor = SquarePanel.validColorDefault;
		updateBorderColor();
	}

	public void showAsLastMoved(boolean lastMovedSquare) {
		this.lastMovedSquare = lastMovedSquare;
		updateBorderColor();
	}

	public void showAsDefault() {
		this.selectedSquare = false;
		this.validMoveSquare = false;
		this.lastMovedSquare = false;
		this.highLightedSquare = false;

		updateBorderColor();
		updateBackgroundColor();
	}

	public void showChessPiece(int id, int player) {

		if (this.id == id && this.player == player) {
			return;
		}

		this.id = id;
		this.player = player;

		// Image image = gui.getChessImage(id, player);
		// ImageIcon icon = new ImageIcon(image);
		picLabel.setIcon(boardPanel.getChessIcon(id, player));
		picLabel.updateUI();

	}

	public void updateIcon() {
		if (id != Piece.PieceID.NONE && player != Side.NONE) {
			picLabel.setIcon(boardPanel.getChessIcon(id, player));
			picLabel.updateUI();
		}
	}

	public void addValidMove(Move move) {
		validMoves.add(move);
	}

	public void removeAllValidMoves() {
		validMoves.clear();
	}

	public ArrayList<Move> getValidMoves() {
		return validMoves;
	}

	public Move checkIfValidMove(Move newMove) {
		Move validMove;
		for (int m = 0; m < validMoves.size(); m++) {
			validMove = validMoves.get(m);
			if (Move.equals(validMove, newMove))
				return validMove;
		}

		return null;
	}

	public void clearChessPiece() {

		picLabel.setIcon(null);
		id = Piece.PieceID.NONE;

		if (debug)
			debugPieceValue.setText("");
	}

	public boolean hasPiece() {
		return id != Piece.PieceID.NONE;
	}

	public int getPieceID() {
		return id;
	}

	public int getRow() {
		return row;
	}

	public int getCol() {
		return col;
	}

	public int getPlayer() {
		return player;
	}

	public void updateDebugInfo(String score) {
		debugScoreLabel.setText(score + "");
	}

}
