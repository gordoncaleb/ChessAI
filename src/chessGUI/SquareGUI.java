package chessGUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

import chessBackend.Move;
import chessBackend.Player;
import chessPieces.PieceID;

public class SquareGUI extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private int row;
	private int col;
	private PieceID id;
	private Player player;
	private Vector<Move> validMoves;
	
	private BoardGUI gui;
	
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

	public SquareGUI(boolean lightSquare, int row, int col, boolean debug) {
		super(new BorderLayout());
		
		validMoves = new Vector<Move>();
		this.row = row;
		this.col = col;

		this.lightSquare = lightSquare;

		updateBackgroundColor();
		updateBorderColor();

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

	public void showAsSelected(boolean selected) {
		this.selectedSquare = selected;
		updateBorderColor();
	}

	public void showAsValidMove(boolean validMoveSquare) {
		this.validMoveSquare = validMoveSquare;
		updateBorderColor();
	}

	public void showAsLastMoved(boolean lastMovedSquare) {
		this.lastMovedSquare = lastMovedSquare;
		updateBorderColor();
	}
	
	public void showAsDefault(){
		this.selectedSquare = false;
		this.validMoveSquare = false;
		this.lastMovedSquare = false;
		
		updateBorderColor();
	}

	public void showChessPiece(PieceID id, Player player) {

		this.id = id;
		this.player = player;

		Image image = gui.getChessImage(id, player);
		ImageIcon icon = new ImageIcon(image);
		picLabel.setIcon(icon);
		picLabel.updateUI();
			
	}
	
	public void addValidMove(Move move){
		validMoves.add(move);
	}
	
	public void removeAllValidMoves(){
		validMoves.removeAllElements();
	}
	
	public Vector<Move> getValidMoves(){
		return validMoves;
	}
	
	public Move checkIfValidMove(Move newMove) {
		Move validMove;
		for (int m = 0; m < validMoves.size(); m++) {
			validMove = validMoves.elementAt(m);
			if (validMove.equals(newMove) && validMove.isValidated())
				return validMove;
		}

		return null;
	}

	public void clearChessPiece() {

		picLabel.setIcon(null);
		id = PieceID.NONE;
		
		if(debug)
			debugPieceValue.setText("");
	}

	public void setGUI(BoardGUI gui) {
		this.gui = gui;
	}

	public boolean hasPiece() {
		return id != PieceID.NONE;
	}

	public PieceID getPieceID() {
		return id;
	}

	public int getRow() {
		return row;
	}

	public int getCol() {
		return col;
	}
	
	public Player getPlayer(){
		return player;
	}
	

	public void updateDebugInfo(String score) {
		debugScoreLabel.setText(score + "");
	}

}
