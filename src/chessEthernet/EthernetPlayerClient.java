package chessEthernet;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import chessBackend.Board;
import chessBackend.GameStatus;
import chessBackend.Move;
import chessBackend.Player;
import chessBackend.PlayerContainer;
import chessIO.FileIO;
import chessIO.XMLParser;

public class EthernetPlayerClient extends Thread implements Player, EthernetMsgRxer {

	private PlayerContainer game;

	private Socket clientSocket;

	private int port = 1234;
	private String dest = "localhost";

	private String payload;

	private ArrayList<String> messages = new ArrayList<String>();

	public EthernetPlayerClient() {
		EthernetMsgServer server = new EthernetMsgServer(this, 2345);
		server.start();
		this.start();
	}

	public void run() {
		synchronized (this) {
			while (true) {

				try {
					this.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				while (messages.size() > 0) {
					processMessage(messages.get(0));
					messages.remove(0);
				}
			}
		}
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
			FileIO.log("Connection to ethernet player could not be made.");
		}

		System.out.println("tx message " + message);
		EthernetMsgServer.sendMessage(message, clientSocket);

	}

	public synchronized void newMessage(String message) {

		if (message.equals("ACK")) {
			payload = message;
		} else {
			this.messages.add(message);
		}
		
		
		System.out.println("Rx message = " + message);
		notifyAll();
	}

	public void processMessage(String message) {

		int tagStart = message.indexOf("<");
		int tagEnd = message.indexOf(">");

		if (tagStart < 0 || tagEnd < 0) {
			FileIO.log("Client unrecognized message received: \n" + message);
			return;
		}

		String tag = message.substring(tagStart, tagEnd + 1);
		payload = message.substring(tagEnd + 1, message.length());

		// System.out.println("Message tag = " + tag);

		switch (tag) {

		case "<move>":
			sendMessage("ACK");
			game.makeMove(XMLParser.XMLToMove(message));
			break;
		case "<newGame>":
			sendMessage("ACK");
			game.newGame(null, false);
			break;
		case "<board>":
			sendMessage("ACK");
			game.newGame(XMLParser.XMLToBoard(message), false);
			break;
		case "<undoMove>":
			sendMessage("ACK");
			game.undoMove();
			break;
		case "<pause>":
			sendMessage("ACK");
			game.pause();
			break;
		case "<version>":
			notifyAll();
			break;
		case "<progress>":
			sendMessage("ACK");
			game.showProgress(Integer.parseInt(payload));
			break;
		case "<recommendation>":
			sendMessage("ACK");
			game.recommendationMade(Long.parseLong(payload));
			break;
		case "<recommend>":
			sendMessage("ACK");
			game.requestRecommendation();
			break;
		default:
			FileIO.log("Client unrecognized tag received: \n" + message);
			break;

		}

		// System.out.println("Rx:\n" + message);
	}

	@Override
	public synchronized long undoMove() {
		sendMessage("<undoMove>");
		if (!getResponse().equals("ACK")) {
			FileIO.log("UndoMove command didn't receive an ACK");
		}
		return 0;
	}

	@Override
	public synchronized void newGame(Board board) {
		sendMessage(board.toXML(true));
		if (!getResponse().equals("ACK")) {
			FileIO.log("Board new game command didn't receive an ACK");
		}
	}

	@Override
	public void setGame(PlayerContainer game) {
		this.game = game;

	}

	@Override
	public synchronized void makeMove() {
		sendMessage("<makeMove>");
		if (!getResponse().equals("ACK")) {
			FileIO.log("MakeMove command didn't receive an ACK");
		}

	}

	@Override
	public synchronized boolean moveMade(long move) {
		sendMessage(Move.toXML(move));
		if (!getResponse().equals("ACK")) {
			FileIO.log("Move command didn't receive an ACK. Payload =" + payload);
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
	public GameStatus getGameStatus() {
		return null;
	}

	@Override
	public Board getBoard() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public synchronized String getVersion() {
		sendMessage("<version>");
		return getResponse();
	}

	public String getResponse() {

		try {
			this.wait(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return payload;

	}

	@Override
	public void connectionReset() {
		if (game != null) {
			game.endGame();
		}
	}

	public synchronized void gameOver(int winlose) {
		if (winlose > 0) {
			sendMessage("<gameOver>win");
		} else {
			if (winlose < 0) {
				sendMessage("<gameOver>lose");
			} else {
				sendMessage("<gameOver>draw");
			}
		}

		if (!getResponse().equals("ACK")) {
			FileIO.log("Gameover command didn't receive an ACK");
		}
	}

	@Override
	public void endGame() {
		try {
			clientSocket.close();
			clientSocket = null;

		} catch (IOException e) {
			e.printStackTrace();
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
