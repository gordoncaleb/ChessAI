package chessEthernet;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import chessAI.AI;
import chessBackend.Board;
import chessBackend.GameResults;
import chessBackend.Move;
import chessBackend.Player;
import chessBackend.PlayerContainer;
import chessIO.FileIO;
import chessIO.XMLParser;

public class EthernetPlayerServer implements EthernetMsgRxer, PlayerContainer {

	private JFrame frame;
	private JLabel statusTxt;
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

		frame.setPreferredSize(new Dimension(300,100));
		statusTxt = new JLabel(stat[0]);
		
		statusTxt.setFont(new Font("Courier New", Font.ITALIC, 45));
		statusTxt.setHorizontalAlignment(SwingConstants.CENTER);
		
		frame.add(statusTxt, BorderLayout.CENTER);
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
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
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
			sendMessage("<version>" + player.getVersion());
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
	public boolean makeMove(Move move) {
		sendMessage(move.toXML());
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

}
