package chessEthernet;


import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Vector;
import chessAI.AI;
import chessBackend.Board;
import chessBackend.GameResults;
import chessBackend.Move;
import chessBackend.Player;
import chessBackend.PlayerContainer;
import chessIO.XMLParser;

public class EthernetPlayerServer extends Thread implements EthernetMsgRxer, PlayerContainer {

	private Player player;
	private Vector<String> messages;
	private int port = 2345;
	private Socket clientSocket;

	public static void main(String[] args) {
	
		AI ai = new AI(null, true);
		EthernetPlayerServer server = new EthernetPlayerServer(ai);
		ai.setGame(server);
		server.start();
	
	}

	public EthernetPlayerServer(Player player) {
		this.player = player;
		messages = new Vector<String>();
		EthernetMsgServer server = new EthernetMsgServer(this, 1234);
		server.start();
	}

	public synchronized void newMessage(String message) {
		messages.add(message);
		System.out.println("Server rx: " + message);
		
		processMessage();
		//notifyAll();
	}

	public void run() {

		synchronized (this) {

			while (true) {

				try {
					wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				//processMessage();

			}

		}

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
		default:
			System.out.println("Server unrecognized command received: \n" + message);
			break;

		}

		System.out.println("Rx:\n" + message);
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
