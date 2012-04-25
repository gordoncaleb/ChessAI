package chessEthernet;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

import chessAI.AI;
import chessBackend.Board;
import chessBackend.Game;
import chessBackend.GameStatus;
import chessBackend.Move;
import chessBackend.Player;

public class EthernetPlayerServer extends Thread implements EthernetMsgRxer {
	
	private Player player;
	private Vector<String> messages;
	private int port;

	public EthernetPlayerServer(Player player, int port) {
		this.port = port;
		this.player = player;
		messages = new Vector<String>();
		EthernetMsgServer server = new EthernetMsgServer(this, 1234);
		server.start();
	}
	
	public synchronized void newMessage(String message) {
		messages.add(message);
		notifyAll();
	}
	
	public static void main(String[] args){
		
		EthernetPlayerServer server = new EthernetPlayerServer(new AI(null,true),9876);
		server.start();
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		EthernetPlayerClient client = new EthernetPlayerClient(1234);
		client.start();
		
		client.makeMove();
//		try {
//			Thread.sleep(1000);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
		client.moveMade(new Move(1,2,3,4), false);
		
		client.moveMade(new Move(1,2,3,4), false);
		
		client.moveMade(new Move(1,2,3,4), false);
		
		client.moveMade(new Move(1,2,3,4), false);
		
	}
	
	public void run(){
		
		synchronized(this){
			
			while(true){
				
				try {
					wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				processMessage();
				
			}
			
		}
		
	}
	
	private void processMessage(){
		String message = messages.elementAt(0);
		messages.remove(0);
		
		
		System.out.println("Rx:\n" + message);
	}


}
