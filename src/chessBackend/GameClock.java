package chessBackend;

public class GameClock {
	private long[] time;
	private long startTime;
	private boolean active;
	private Side turn;
	private String[] name;

	public GameClock(String whitePlayerName, String blackPlayerName, long whitePlayerTime, long blackPlayerTime, Side turn) {
		this.name[0] = whitePlayerName;
		this.name[1] = blackPlayerName;
		this.turn = turn;

		time = new long[2];
		time[0] = whitePlayerTime;
		time[1] = blackPlayerTime;

		active = false;
	}

	public void reset() {
		time[0] = 0;
		time[1] = 0;
		active = false;
	}

	public void hit() {

		if (active) {
			time[turn.ordinal()] += System.currentTimeMillis() - startTime;
		}else{
			active = true;
		}

		startTime = System.currentTimeMillis();
		turn = turn.otherSide();
	}
	
	public long getTime(Side side){
		return time[side.ordinal()];
	}
	
	public String getName(Side side){
		return name[side.ordinal()];
	}
	
	public boolean isActive(){
		return active;
	}

}
