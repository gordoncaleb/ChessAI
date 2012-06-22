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

	private String payload;

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
		FileIO.setDebugOutput(true);
		AI ai = new AI(null, true);
		ai.setUseBook(true);
		EthernetPlayerServer server = new EthernetPlayerServer(ai);
		ai.setGame(server);

	}

	private synchronized void sendMessage(String message) {

		if (clientSocket == null) {
			try {
				clientSocket = new Socket(dest, port);
			} catch (UnknownHostException e) {
				FileIO.log("Problem game host name");
			} catch (IOException e) {
				FileIO.log("Connection to game lost");
			}
		}

		System.out.println("tx message " + message);
		EthernetMsgServer.sendMessage(message, clientSocket);
	}

	public synchronized void newMessage(String message) {

		updateStatus();

		System.out.println("Rx message = " + message);

		if (message.trim().equals("ACK")) {
			payload = message;
			notifyAll();
			return;
		}

		int tagStart = message.indexOf("<");
		int tagEnd = message.indexOf(">");

		if (tagStart < 0 || tagEnd < 0) {
			FileIO.log("Server unrecognized mesage received: \n" + message);
			return;
		}

		String tag = message.substring(tagStart, tagEnd + 1);
		payload = message.substring(tagEnd + 1, message.length());

		switch (tag) {

		case "<makeMove>":
			player.makeMove();
			sendMessage("ACK");
			break;
		case "<board>":
			Board board = XMLParser.XMLToBoard(message);
			FileIO.log("got new board:\n" + board.toString());
			player.newGame(board);
			sendMessage("ACK");
			break;
		case "<move>":
			player.moveMade(XMLParser.XMLToMove(message));
			sendMessage("ACK");
			break;
		case "<undoMove>":
			player.undoMove();
			sendMessage("ACK");
			break;
		case "<pause>":
			player.pause();
			sendMessage("ACK");
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
			sendMessage("ACK");
			break;
		case "<progress>":
			player.showProgress(Integer.parseInt(payload));
			sendMessage("ACK");
			break;
		case "<recommend>":
			player.requestRecommendation();
			sendMessage("ACK");
			break;
		case "<recommendation>":
			player.recommendationMade(Long.parseLong(payload));
			sendMessage("ACK");
			break;
		default:
			FileIO.log("Server unrecognized tag received: \n" + message);
			sendMessage("NACK");
			break;

		}

		notifyAll();
	}

	public String getResponse() {

		try {
			this.wait(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return payload;

	}

	private void updateStatus() {
		statusTxt.setText(stat[statNum % 4]);
		statNum++;
	}

	@Override
	public synchronized boolean makeMove(long move) {
		sendMessage(Move.toXML(move));
		if (!getResponse().equals("ACK")) {
			FileIO.log("MakeMove command didn't receive an ACK");
		}
		return true;
	}

	@Override
	public GameResults newGame(Board board, boolean block) {

		if (board != null) {
			sendMessage(board.toXML(true));
		} else {
			sendMessage("<newGame>");
		}

		if (!getResponse().equals("ACK")) {
			FileIO.log("Board new game command didn't receive an ACK");
		}

		return null;
	}

	@Override
	public synchronized boolean undoMove() {
		sendMessage("<undoMove>");
		if (!getResponse().equals("ACK")) {
			FileIO.log("UndoMove command didn't receive an ACK");
		}
		return false;
	}

	@Override
	public synchronized void pause() {
		sendMessage("<pause>");
		if (!getResponse().equals("ACK")) {
			FileIO.log("Pause command didn't receive an ACK");
		}
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
	public synchronized void switchSides() {
		sendMessage("<switch>");
		if (!getResponse().equals("ACK")) {
			FileIO.log("SwitchSides command didn't receive an ACK");
		}
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
		if (player != null) {
			player.endGame();
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
		if (arg0.getSource() == showAISettings) {
			new AISettingsGUI("Ethernet AI Settings");
		}

	}

	@Override
	public void showProgress(int progress) {
		// sendMessage("<progress>" + progress);
	}

	@Override
	public synchronized void requestRecommendation() {
		sendMessage("<recommend>");
		if (!getResponse().equals("ACK")) {
			FileIO.log("RequestRecommendation command didn't receive an ACK");
		}
	}

	@Override
	public synchronized void recommendationMade(long move) {
		sendMessage("<recommendation>" + move);
		if (!getResponse().equals("ACK")) {
			FileIO.log("RecommendationMade command didn't receive an ACK");
		}
	}

}
