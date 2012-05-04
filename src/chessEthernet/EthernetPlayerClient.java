package chessEthernet;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import chessBackend.Board;
import chessBackend.GameStatus;
import chessBackend.Move;
import chessBackend.Player;
import chessBackend.PlayerContainer;
import chessIO.XMLParser;

public class EthernetPlayerClient implements Player, EthernetMsgRxer {

	private PlayerContainer game;

	private Socket clientSocket;

	private int port = 1234;
	private String dest = "localhost";

	private String version;

	public EthernetPlayerClient() {
		version = "";
		EthernetMsgServer server = new EthernetMsgServer(this, 2345);
		server.start();

	}

	private void sendMessage(String message) {

		try {
			if (clientSocket == null) {
				clientSocket = new Socket(dest, port);
			} else {
				if (clientSocket.isClosed()) {
					clientSocket = new Socket(dest, port);
				}
			}

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		EthernetMsgServer.sendMessage(message, clientSocket);

	}

	public synchronized void newMessage(String message) {

		int tagStart = message.indexOf("<");
		int tagEnd = message.indexOf(">");

		String tag = message.substring(tagStart, tagEnd + 1);
		String payload = message.substring(tagEnd + 1, message.length());

		// System.out.println("Message tag = " + tag);

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
		case "<version>":
			version = payload;
			this.notifyAll();
			break;
		default:
			System.out.println("Client unrecognized command received: \n" + message);
			break;

		}

		// System.out.println("Rx:\n" + message);
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

	@Override
	public String getVersion() {
		sendMessage("<version>");

		synchronized (this) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		return version;
	}

}
