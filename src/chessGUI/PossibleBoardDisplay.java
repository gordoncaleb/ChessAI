package chessGUI;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import chessAI.AI;
import chessBackend.Board;
import chessBackend.Move;

public class PossibleBoardDisplay implements MouseListener {

	private JFrame frame;

	private AI ai;

	private ArrayList<BoardPanel> boardPanels;
	private ArrayList<JLabel> boardLabels;

	public static void main(String[] args) {
		new PossibleBoardDisplay(null, 4);
	}

	public PossibleBoardDisplay(AI ai, int displayNum) {

		this.ai = ai;

		frame = new JFrame("Possible Boards");
		frame.setSize(new Dimension(1000, 1000));

		frame.setLayout(new BorderLayout());

		boardPanels = new ArrayList<BoardPanel>();
		boardLabels = new ArrayList<JLabel>();

		JPanel centerPanel = new JPanel(new GridLayout(2, 2));
		BoardPanel temp;
		JLabel tempLabel;
		JPanel wrapper;
		for (int i = 0; i < displayNum; i++) {
			temp = new BoardPanel(null, false, true);
			wrapper = new JPanel(new BorderLayout());
			tempLabel = new JLabel("Board label");
			tempLabel.setHorizontalAlignment(SwingConstants.CENTER);

			temp.setSize(new Dimension(500, 500));
			boardPanels.add(temp);
			boardLabels.add(tempLabel);
			wrapper.add(temp, BorderLayout.CENTER);
			wrapper.add(tempLabel, BorderLayout.SOUTH);
			centerPanel.add(wrapper);
		}

		frame.add(centerPanel, BorderLayout.CENTER);

		JButton refreshBtn = new JButton("Refresh");
		refreshBtn.addMouseListener(this);

		frame.add(refreshBtn, BorderLayout.SOUTH);

		frame.setVisible(true);

	}

	public void showPossibilites() {

		if (ai == null) {
			return;
		}

		Board tempBoard;
		ArrayList<Move> pvMoves = new ArrayList<Move>();
		for (int i = 0; i < 4; i++) {
			tempBoard = ai.getBoard().getCopy();
			pvMoves.clear();
			ai.getMovePV(ai.getRootNode().getChild(i), pvMoves);
			for (int m = 0; m < pvMoves.size(); m++) {
				tempBoard.makeMove(pvMoves.get(m).getMoveLong());
			}
			boardPanels.get(i).newGame(tempBoard);
			boardLabels.get(i).setText("Score = " + tempBoard.staticScore());
		}

	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub

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
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		showPossibilites();
	}

}
