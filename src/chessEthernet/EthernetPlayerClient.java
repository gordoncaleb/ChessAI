package chessEthernet;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Vector;

import chessBackend.Board;
import chessBackend.Game;
import chessBackend.GameStatus;
import chessBackend.Move;
import chessBackend.Player;
import chessBackend.PlayerContainer;
import chessIO.XMLParser;

public class EthernetPlayerClient extends Thread implements Player, EthernetMsgRxer {

	private Vector<String> messages;
	private PlayerContainer game;

	private Socket clientSocket;

	private int port = 1234;

	public EthernetPlayerClient() {
		messages = new Vector<String>();

		EthernetMsgServer server = new EthernetMsgServer(this, 2345);
		server.start();
	}

	public void run() {

		synchronized (this) {

			while (true) {

				if (messages.size() == 0) {
					try {
						wait(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				} else {

					//processMessage();
				}

			}

		}

	}

	public void newMessage(String message) {
		messages.add(message);
		processMessage();
		System.out.println("Client rx: " + message);
	}

	private void sendMessage(String message) {

		System.out.println("Sending:\n" + message);

		try {
			clientSocket = new Socket("localhost", port);
			// String sentence;
			// String modifiedSentence;
			// BufferedReader inFromUser = new BufferedReader(new
			// InputStreamReader(System.in));

			DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
			// BufferedReader inFromServer = new BufferedReader(new
			// InputStreamReader(clientSocket.getInputStream()));
			// sentence = inFromUser.readLine();

			// outToServer.writeChars(message);

			outToServer.writeBytes(message);
			// modifiedSentence = inFromServer.readLine();
			// System.out.println("FROM SERVER: " + modifiedSentence);
			clientSocket.close();

		} catch (Exception e) {

		}

	}

	private void processMessage() {
		String message = messages.elementAt(0);
		messages.remove(0);

		int tagStart = message.indexOf("<");
		int tagEnd = message.indexOf(">");

		String tag = message.substring(tagStart, tagEnd + 1);

		System.out.println("Message tag = " + tag);

		switch (tag) {

		case "<move>":
			game.makeMove(XMLParser.XMLToMove(message));
			break;
		case "<newGame>":
			game.newGame(null, false);
			break;
		case "<board>":
			game.newGame(XMLParser.XMLToBoard(message), false);
			break;
		case "<undoMove>":
			game.undoMove();
			break;
		case "<pause>":
			game.pause();
			break;
		default:
			System.out.println("Client unrecognized command received: \n" + message);
			break;

		}

		System.out.println("Rx:\n" + message);
	}

	@Override
	public Move undoMove() {
		sendMessage("<undoMove>");
		return null;
	}

	@Override
	public void newGame(Board board) {
		sendMessage(board.toXML(true));
	}

	@Override
	public void setGame(PlayerContainer game) {
		this.game = game;

	}

	@Override
	public void makeMove() {
		sendMessage("<makeMove>");

	}

	@Override
	public Move makeRecommendation() {
		return null;
	}

	@Override
	public boolean moveMade(Move move) {
		sendMessage(move.toXML());
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void pause() {
		sendMessage("<pause>");
		// TODO Auto-generated method stub

	}

	@Override
	public GameStatus getGameStatus() {

		return null;
	}

	@Override
	public Board getBoard() {
		// TODO Auto-generated method stub
		return null;
	}

}
