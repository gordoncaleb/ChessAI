package com.gordoncaleb.chess.ui.gui.ethernet;

public interface EthernetMsgRxer {
	
	public void newMessage(String message);
	public void connectionReset();

}
