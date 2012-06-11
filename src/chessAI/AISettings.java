package chessAI;

import chessBackend.BoardHashEntry;

public class AISettings {
	
	public static final String version = "1.2.060912b";
	public static boolean debugOutput = true;
	
	public static final boolean useLite = false;
	
	public static final boolean useExtraTime = false;
	public static final boolean bonusEnable = true;
	
	public static final int maxInCheckFrontierLevel = 2;
	public static final int maxPieceTakenFrontierLevel = 2;
	
	public static final long maxSearchTime = 5000;
	public static final int minSearchDepth = 4;
	
	public static final boolean alphaBetaPruningEnabled = true;
	public static final boolean useBook = true;
	
	public static final boolean useHashTable = true;
	public static final int hashTableSize = (int) Math.pow(2, BoardHashEntry.hashIndexSize);
	public static final int staleHashAge = 10;
	
	public static final int numOfThreads = 1; //Runtime.getRuntime().availableProcessors()

}
