package chessAI;

public class AISettings {
	
	public static String version = "1.2.062512";
	public static boolean debugOutput = true;
	
	public static boolean useLite = false;
	
	public static boolean useExtraTime = true;
	public static boolean bonusEnable = true;
	
	public static int maxInCheckFrontierLevel = 2;
	public static int maxPieceTakenFrontierLevel = 2;
	
	public static long maxSearchTime = 5000;
	public static int minSearchDepth = 4;
	
	public static boolean alphaBetaPruningEnabled = true;
	public static boolean useBook = true;
	
	public static boolean useHashTable = true;
	public static int hashIndexSize = 25;
	public static int hashTableSize = (int) Math.pow(2, hashIndexSize);
	public static long hashIndexMask = (long) (Math.pow(2, AISettings.hashIndexSize) - 1);
	public static int staleHashAge = 10;
	
	public static int numOfThreads = 1; //Runtime.getRuntime().availableProcessors()
	
	public static boolean useKillerMove = true;
	public static int maxKillerMoves = 100;

}
