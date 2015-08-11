package com.gordoncaleb.chess.ethernet;

public interface EthernetMsgRxer {
	
	public void newMessage(String message);
	public void connectionReset();

}
