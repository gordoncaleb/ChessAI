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
			char[] clientSentence = new char[500];

			// String capitalizedSentence;
			ServerSocket welcomeSocket = new ServerSocket(port);

			while (true) {

				Socket connectionSocket = welcomeSocket.accept();
				BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
				// DataOutputStream outToClient = new
				// DataOutputStream(connectionSocket.getOutputStream());

				int recvMsgSize;

				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				while (inFromClient.ready()) {
					
					String fullMsg = "";
					char[] temp;
					while ((recvMsgSize = inFromClient.read(clientSentence)) != -1) {
						temp = new char[recvMsgSize];
						System.arraycopy(clientSentence, 0, temp, 0, recvMsgSize);
						fullMsg += new String(temp);
					}
					// inFromClient.read(clientSentence);

					// System.out.println("read buffer = " + new
					// String(clientSentence));

					rxer.newMessage(fullMsg);

				}

				synchronized (rxer) {
					rxer.notifyAll();
				}

				// System.out.println("Received: " + clientSentence);
				// capitalizedSentence = clientSentence.toUpperCase() + '\n';
				// outToClient.writeBytes(capitalizedSentence);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
