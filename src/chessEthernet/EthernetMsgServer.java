package chessEthernet;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class EthernetMsgServer extends Thread {

	private EthernetMsgRxer rxer;

	private int port;

	public EthernetMsgServer(EthernetMsgRxer rxer, int port) {
		this.rxer = rxer;
		this.port = port;
	}

	public void run() {

		try {

			ServerSocket welcomeSocket = new ServerSocket(port);

			Socket connectionSocket = welcomeSocket.accept();
			BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));

			String msg;
			while (true) {
				msg = inFromClient.readLine();

				if (msg != null) {
					rxer.newMessage(msg);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void sendMessage(String message, Socket clientSocket) {

		try {

			DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());

			String outMsg = message.replace("\n", "") + "\n";

			outToServer.writeBytes(outMsg);

		} catch (Exception e) {

		}

	}

}
