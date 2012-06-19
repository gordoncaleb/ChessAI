package chessEthernet;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import chessBackend.Board;
import chessBackend.GameStatus;
import chessBackend.Move;
import chessBackend.Player;
import chessBackend.PlayerContainer;
import chessIO.FileIO;
import chessIO.XMLParser;

public class EthernetPlayerClient implements Player, EthernetMsgRxer {

	private PlayerContainer game;

	private Socket clientSocket;

	private int port = 1234;
	private String dest = "localhost";

	private String payload;

	public EthernetPlayerClient() {
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
			FileIO.log("Connection to ethernet player could not be made.");
		}

		EthernetMsgServer.sendMessage(message, clientSocket);

	}

	public synchronized void newMessage(String message) {

		int tagStart = message.indexOf("<");
		int tagEnd = message.indexOf(">");
		
		if (tagStart < 0 || tagEnd < 0) {
			return;
		}

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
			this.payload = payload;
			break;
		case "<progress>":
			game.showProgress(Integer.parseInt(payload));
			break;
		case "<recommendation>":
			game.recommendationMade(Long.parseLong(payload));
			break;
		case "<recommend>":
			game.requestRecommendation();
			break;
		default:
			System.out.println("Client unrecognized command received: \n" + message);
			break;

		}

		this.notifyAll();

		// System.out.println("Rx:\n" + message);
	}

	@Override
	public long undoMove() {
		sendMessage("<undoMove>");
		return 0;
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
	public boolean moveMade(long move) {
		sendMessage(Move.toXML(move));
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

		return getResponse();
	}

	public String getResponse() {

		synchronized (this) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		return payload;

	}

	@Override
	public void connectionReset() {
		game.endGame();
	}

	public void gameOver(int winlose) {
		if (winlose > 0) {
			sendMessage("<gameOver>win");
		} else {
			if (winlose < 0) {
				sendMessage("<gameOver>lose");
			} else {
				sendMessage("<gameOver>draw");
			}
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
		//sendMessage("<progress>" + progress);
	}

	@Override
	public void requestRecommendation() {
		sendMessage("<recommend>");
	}

	@Override
	public void recommendationMade(long move) {
		sendMessage("<recommendation>" + move);
	}

}
