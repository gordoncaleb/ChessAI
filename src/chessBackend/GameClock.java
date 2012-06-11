package chessBackend;

import java.util.Timer;

public class GameClock {
	private long[] time;
	private long startTime;
	private boolean active;
	private Side turn;
	private String[] name;
	private long timeLimit;
	private boolean paused;
	private long[] maxTime;

	private Timer timer;

	public GameClock(String whitePlayerName, String blackPlayerName, long whitePlayerTime, long blackPlayerTime, Side turn) {

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

		maxTime = new long[2];
		maxTime[0] = 0;
		maxTime[1] = 0;

	}

	public void setTimeLimit(long timeLimit) {
		this.timeLimit = timeLimit;
	}

	public void reset() {
		time[0] = 0;
		time[1] = 0;
		maxTime[0] = 0;
		maxTime[1] = 0;
		active = false;
	}

	public boolean hit() {
		long timeDiff;

		if (active) {
			timeDiff = System.currentTimeMillis() - startTime;
			time[turn.ordinal()] += timeDiff;

			if (timeDiff > maxTime[turn.ordinal()]) {
				maxTime[turn.ordinal()] = timeDiff;
			}

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

		if (side == Side.NONE) {
			return 0;
		}

		if (active) {
			if (side != turn) {

				return time[side.ordinal()];
			} else {
				return time[side.ordinal()] + (System.currentTimeMillis() - startTime);
			}
		} else {
			return time[side.ordinal()];
		}
	}

	public long getMaxTime(Side side) {
		if (side == Side.NONE) {
			return 0;
		}

		return maxTime[side.ordinal()];
	}

	public String getName(Side side) {
		return name[side.ordinal()];
	}

	public boolean isActive() {
		return active;
	}

	public long getStartTime() {
		return startTime;
	}

	public void pause() {

		if (!paused) {
			time[turn.ordinal()] += System.currentTimeMillis() - startTime;
		} else {
			startTime = System.currentTimeMillis();
		}

	}

}
