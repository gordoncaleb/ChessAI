package chessBackend;

public class GameClock {
	private long[] time;
	private long startTime;
	private boolean active;
	private Side turn;
	private String[] name;
	private long timeLimit;
	private boolean paused;

	public GameClock(String whitePlayerName, String blackPlayerName,
			long whitePlayerTime, long blackPlayerTime, Side turn) {
		
		this.name = new String[2];
		
		this.name[0] = whitePlayerName;
		this.name[1] = blackPlayerName;
		this.turn = turn;

		time = new long[2];
		time[0] = whitePlayerTime;
		time[1] = blackPlayerTime;

		active = false;

		timeLimit = 0;
		paused = false;
	}

	public void setTimeLimit(long timeLimit) {
		this.timeLimit = timeLimit;
	}

	public void reset() {
		time[0] = 0;
		time[1] = 0;
		active = false;
	}

	public boolean hit() {

		if (active) {
			time[turn.ordinal()] += System.currentTimeMillis() - startTime;

			if (time[turn.ordinal()] > timeLimit && timeLimit != 0) {
				// game over
				return true;
			}

		} else {
			active = true;
		}

		startTime = System.currentTimeMillis();
		turn = turn.otherSide();

		return false;
	}

	public long getTime(Side side) {
		
		if(side == Side.NONE){
			return 0;
		}
		
		return time[side.ordinal()];
	}

	public String getName(Side side) {
		return name[side.ordinal()];
	}

	public boolean isActive() {
		return active;
	}

	public void pause() {

		if (!paused) {
			time[turn.ordinal()] += System.currentTimeMillis() - startTime;
		} else {
			startTime = System.currentTimeMillis();
		}

	}

}