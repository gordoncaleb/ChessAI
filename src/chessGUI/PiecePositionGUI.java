package chessGUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

import chessPieces.Piece;

public class PiecePositionGUI extends JPanel implements ComponentListener {
	private Piece piece;
	private BoardGUI gui;
	private int[] pos = new int[2];

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

	private Color dark = new Color(181, 136, 99);
	private Color light = new Color(240, 217, 181);

	private Color darkSelected = new Color(50, 50, 50);
	private Color lightSelected = new Color(230, 230, 230);

	private Color lastMoved = new Color(255, 0, 0);
	private Color validMove = new Color(255, 255, 0);

	public PiecePositionGUI(boolean lightSquare, int row, int col, boolean debug) {
		super(new BorderLayout());
		this.lightSquare = lightSquare;
		pos[0] = row;
		pos[1] = col;

		this.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));

		updateBackgroundColor();

		picLabel = new JLabel();

		this.debug = debug;
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
			//debugPieceValue.setOpaque(true);
			//debugPieceValue.setBackground(Color.BLACK);
			debugPieceValue.setForeground(Color.BLUE);
			debugPane.add(debugPieceValue,BorderLayout.CENTER);
			this.add(debugPane, BorderLayout.PAGE_END);
		}

		JPanel imagePane = new JPanel(new FlowLayout());
		imagePane.setBackground(this.getBackground());
		imagePane.add(picLabel);

		this.add(imagePane, BorderLayout.PAGE_START);
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
					if (lightSquare) {
						this.setBackground(lightSelected);
					} else {
						this.setBackground(darkSelected);
					}
				} else {
					if (lightSquare) {
						this.setBackground(light);
					} else {
						this.setBackground(dark);
					}
				}
			}
		}
	}

	public void updateBorderColor() {

		if (validMoveSquare) {
			this.setBorder(BorderFactory.createLineBorder(validMove));
			
			//this.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED,validMove,Color.black));
		} else {
			if (lastMovedSquare) {
				this.setBorder(BorderFactory.createLineBorder(lastMoved));
				//this.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED,lastMoved,Color.black));
			} else {
				if (selectedSquare) {
					this.setBorder(BorderFactory.createLineBorder(Color.black));
				} else {
					//this.setBorder(BorderFactory.createLineBorder(Color.black));
					this.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
					//this.setBorder(BorderFactory.createEtchedBorder());
				}
			}
		}
	}

	public void setSelected(boolean selected) {
		this.selectedSquare = selected;
		// updateBackgroundColor();
		updateBorderColor();
	}

	public void setValidMove(boolean validMoveSquare) {
		this.validMoveSquare = validMoveSquare;
		// updateBackgroundColor();
		updateBorderColor();
	}

	public void setLastMoved(boolean lastMovedSquare) {
		this.lastMovedSquare = lastMovedSquare;
		// updateBackgroundColor();
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

		if(debug)
			debugPieceValue.setText(piece.getPieceValue()+"");
			
	}

	public void clearChessPiece() {

		this.selectedSquare = false;
		this.validMoveSquare = false;
		this.lastMovedSquare = false;
		// updateBackgroundColor();
		updateBorderColor();

		picLabel.setIcon(null);
		this.piece = null;
		
		if(debug)
			debugPieceValue.setText("");
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

	public void updateDebugInfo(String score) {
		debugScoreLabel.setText(score + "");
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
