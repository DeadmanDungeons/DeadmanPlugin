package com.deadmandungeons.deadmanplugin.timer;


/**
 * A GlobalTimer will have time elapse based on real time regardless of server up-time.
 * @author Jon
 */
public class GlobalTimer extends Timer {
	
	private long expire = 0;
	private long startTime = -1;
	
	/**
	 * Construct a GlobalTimer object for a known expire timestamp.
	 * @param duration - The duration in milliseconds
	 * @param expire - The expire timestamp of when the timer will expire or end (in milliseconds since the epoch)
	 * @throws IllegalArgumentException if duration is less than or equal to 0
	 */
	public GlobalTimer(long duration, long expire) {
		super(duration);
		this.expire = expire;
		startTime = expire - duration;
	}
	
	/**
	 * Construct a GlobalTimer object for an unknown expire timestamp. The expire timestamp will be set when this timer is started.
	 * @param duration - The duration in milliseconds
	 * @throws IllegalArgumentException if duration is less than or equal to 0
	 */
	public GlobalTimer(long duration) {
		super(duration);
	}
	
	@Override
	void start() {
		if (expire == 0) {
			expire = System.currentTimeMillis() + duration;
		}
		if (startTime == -1) {
			startTime = expire - duration;
		}
	}
	
	/**
	 * The time left is equal to the expire time minus the current time.
	 */
	@Override
	public long getTimeLeft() {
		return startTime != -1 ? expire - System.currentTimeMillis() : startTime;
	}
	
	@Override
	public long getStartTime() {
		return startTime;
	}
	
	/**
	 * @return the timestamp (in milliseconds since the epoch) that this timer will expire or end on,
	 * or 0 if this Timer has not yet been started
	 */
	public long getExpire() {
		return expire;
	}
	
	/**
	 * @return a new LocalTimer object with an amount of elapsed time based on the amount of time passed in this GlobalTimer.
	 */
	public LocalTimer toLocalTimer() {
		long elapsed = (this.expire > 0 ? System.currentTimeMillis() - (expire - duration) : 0);
		return new LocalTimer(duration, elapsed);
	}
	
}
