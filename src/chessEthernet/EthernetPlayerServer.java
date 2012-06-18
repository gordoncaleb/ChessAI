package chessEthernet;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import chessAI.AI;
import chessBackend.Board;
import chessBackend.GameResults;
import chessBackend.Move;
import chessBackend.Player;
import chessBackend.PlayerContainer;
import chessBackend.Side;
import chessGUI.AISettingsGUI;
import chessIO.FileIO;
import chessIO.XMLParser;

public class EthernetPlayerServer implements EthernetMsgRxer, PlayerContainer, MouseListener {

	private JFrame frame;
	private JLabel statusTxt;
	private JButton showAISettings;
	private String[] stat;
	private int statNum;

	private Player player;
	private int port = 2345;
	private String dest = "localhost";
	private Socket clientSocket;

	public EthernetPlayerServer(Player player) {
		this.player = player;
		EthernetMsgServer server = new EthernetMsgServer(this, 1234);
		server.start();

		String[] stat = { "|", "/", "-", "\\" };
		this.stat = stat;
		statNum = 0;

		frame = new JFrame(player.getVersion());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());

		frame.setVisible(true);

		frame.setPreferredSize(new Dimension(300, 100));
		statusTxt = new JLabel(stat[0]);

		statusTxt.setFont(new Font("Courier New", Font.ITALIC, 45));
		statusTxt.setHorizontalAlignment(SwingConstants.CENTER);

		showAISettings = new JButton("AI Settings");
		showAISettings.addMouseListener(this);

		frame.add(statusTxt, BorderLayout.CENTER);
		frame.add(showAISettings, BorderLayout.SOUTH);
		frame.pack();
	}

	public static void main(String[] args) {
		FileIO.setLogEnabled(false);
		AI ai = new AI(null, true);
		ai.setUseBook(true);
		EthernetPlayerServer server = new EthernetPlayerServer(ai);
		ai.setGame(server);

	}

	private void sendMessage(String message) {

		if (clientSocket == null) {
			try {
				clientSocket = new Socket(dest, port);
			} catch (UnknownHostException e) {
				FileIO.log("Problem game host name");
			} catch (IOException e) {
				FileIO.log("Connection to game lost");
			}
		}

		EthernetMsgServer.sendMessage(message, clientSocket);
	}

	public void newMessage(String message) {

		updateStatus();

		int tagStart = message.indexOf("<");
		int tagEnd = message.indexOf(">");

		String tag = message.substring(tagStart, tagEnd + 1);

		switch (tag) {

		case "<makeMove>":
			player.makeMove();
			break;
		case "<board>":
			Board board = XMLParser.XMLToBoard(message);
			System.out.println("got new board:\n" + board.toString());
			player.newGame(board);
			break;
		case "<move>":
			player.moveMade(XMLParser.XMLToMove(message));
			break;
		case "<undoMove>":
			player.undoMove();
			break;
		case "<pause>":
			player.pause();
			break;
		case "<version>":
			sendMessage("<version>" + player.getVersion() + "_ethernet");
			break;
		case "<gameOver>":
			if (message.equals("win")) {
				player.gameOver(1);
			} else {
				if (message.equals("lose")) {
					player.gameOver(-1);
				} else {
					player.gameOver(0);
				}
			}
			break;
		case "<recommendation>":
			sendMessage(player.makeRecommendation() + "");
			break;
		default:
			System.out.println("Server unrecognized command received: \n" + message);
			break;

		}
	}

	private void updateStatus() {
		statusTxt.setText(stat[statNum % 4]);
		statNum++;
	}

	@Override
	public boolean makeMove(long move) {
		sendMessage(Move.toXML(move));
		return true;
	}

	@Override
	public GameResults newGame(Board board, boolean block) {

		if (board != null) {
			sendMessage(board.toXML(true));
		} else {
			sendMessage("<newGame>");
		}
		return null;
	}

	@Override
	public boolean undoMove() {
		sendMessage("<undoMove>");
		return false;
	}

	@Override
	public void pause() {
		sendMessage("<pause>");

	}

	@Override
	public boolean isPaused() {
		return false;
	}

	@Override
	public String getPlayerName(Side side) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getPlayerTime(Side side) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void switchSides() {
		sendMessage("<switch>");
	}

	@Override
	public void setSide(Side side, Player player) {

	}

	@Override
	public void connectionReset() {
		try {
			clientSocket.close();
			clientSocket = null;
			this.endGame();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void endGame() {
		player.endGame();
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
		if (arg0.getSource() == showAISettings) {
			new AISettingsGUI("Ethernet AI Settings");
		}

	}

}
