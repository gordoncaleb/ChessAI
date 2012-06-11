package chessEthernet;

public interface EthernetMsgRxer {
	
	public void newMessage(String message);
	public void connectionReset();

}
