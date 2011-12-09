package chessGUI;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import chessPieces.Piece;

public class PiecePositionGUI extends JPanel implements ComponentListener {
	private Piece piece;
	private BoardGUI gui;
	private int[] pos = new int[2];

	private JLabel picLabel;
	private boolean selectedSquare;
	private boolean darkSquare;
	private boolean lastMovedSquare;
	private boolean validMoveSquare;

	private Color dark = new Color(100, 100, 100);
	private Color light = new Color(170, 200, 200);

	private Color darkSelected = new Color(50, 50, 50);
	private Color lightSelected = new Color(230, 230, 230);

	private Color lastMoved = new Color(255, 0, 0);
	private Color validMove = new Color(255, 255, 0);

	public PiecePositionGUI(boolean darkSquare, int row, int col) {
		super(new FlowLayout());
		this.darkSquare = darkSquare;
		pos[0] = row;
		pos[1] = col;

		this.setBorder(BorderFactory.createLineBorder(Color.black));

		updateBackgroundColor();

		picLabel = new JLabel();
		this.add(picLabel);
		this.addComponentListener(this);
	}

	public void updateBackgroundColor() {

		if (validMoveSquare) {
			this.setBackground(validMove);
		} else {
			if (lastMovedSquare) {
				this.setBackground(lastMoved);
			} else {
				if (selectedSquare) {
					if (darkSquare) {
						this.setBackground(darkSelected);
					} else {
						this.setBackground(lightSelected);
					}
				} else {
					if (darkSquare) {
						this.setBackground(dark);
					} else {
						this.setBackground(light);
					}
				}
			}
		}
	}

	public void updateBorderColor() {

		if (validMoveSquare) {
			this.setBorder(BorderFactory.createLineBorder(validMove));
		} else {
			if (lastMovedSquare) {
				this.setBorder(BorderFactory.createLineBorder(lastMoved));
			} else {
				if (selectedSquare) {
						this.setBorder(BorderFactory.createLineBorder(Color.white));
				} else {
						this.setBorder(BorderFactory.createLineBorder(Color.black));
				}
			}
		}
	}

	public void setSelected(boolean selected) {
		this.selectedSquare = selected;
		//updateBackgroundColor();
		updateBorderColor();
	}

	public void setValidMove(boolean validMoveSquare) {
		this.validMoveSquare = validMoveSquare;
		//updateBackgroundColor();
		updateBorderColor();
	}

	public void setLastMoved(boolean lastMovedSquare) {
		this.lastMovedSquare = lastMovedSquare;
		//updateBackgroundColor();
		updateBorderColor();
	}

	public void setChessPiece(Piece p) {

		this.piece = p;
		// rawWidth = p.getImage().getWidth();
		// rawHeight = p.getImage().getHeight();
		// int newHeight = this.getHeight();
		// int newWidth =
		// (int)((double)rawWidth*((double)newHeight/(double)rawHeight));

		Image image = gui.getChessImage(piece.getPieceID(), piece.getPlayer());
		ImageIcon icon = new ImageIcon(image);
		picLabel.setIcon(icon);

	}

	public void clearChessPiece() {

		this.selectedSquare = false;
		this.validMoveSquare = false;
		this.lastMovedSquare = false;
		//updateBackgroundColor();
		updateBorderColor();

		picLabel.setIcon(null);
		this.piece = null;
	}

	public void setGUI(BoardGUI gui) {
		this.gui = gui;
	}

	public boolean hasPiece() {
		return piece != null;
	}

	public Piece getPiece() {
		return piece;
	}

	public int getRow() {
		return pos[0];
	}

	public int getCol() {
		return pos[1];
	}

	@Override
	public void componentHidden(ComponentEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void componentMoved(ComponentEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void componentResized(ComponentEvent arg0) {
		// if(piece == null)
		// return;
		//
		// int newHeight = this.getHeight();
		// int newWidth =
		// (int)((double)rawWidth*((double)newHeight/(double)rawHeight));
		//
		// Image image = piece.getImage().getScaledInstance(newWidth, newHeight,
		// Image.SCALE_SMOOTH);
		// ImageIcon icon = new ImageIcon(image);
		// picLabel.setIcon(icon);
	}

	@Override
	public void componentShown(ComponentEvent arg0) {
		// TODO Auto-generated method stub

	}

}
