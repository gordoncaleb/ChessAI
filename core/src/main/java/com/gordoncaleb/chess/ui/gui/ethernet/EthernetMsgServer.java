package com.gordoncaleb.chess.ui.gui.ethernet;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EthernetMsgServer extends Thread {
	public static Logger logger = LoggerFactory.getLogger(EthernetMsgServer.class);

	private EthernetMsgRxer rxer;
	private ServerSocket serverSocket;

	public EthernetMsgServer(EthernetMsgRxer rxer, int port) {
		this.rxer = rxer;
		
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {

		while (true) {
			try {

				Socket connectionSocket = serverSocket.accept();
				BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));

				String msg;
				while (true) {
					msg = inFromClient.readLine();

					if (msg != null) {
						rxer.newMessage(msg);
					}
				}

			} catch (Exception e) {
				rxer.connectionReset();
				logger.info("Connection lost");
				e.printStackTrace();
			}
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
