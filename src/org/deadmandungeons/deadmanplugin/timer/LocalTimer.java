package org.deadmandungeons.deadmanplugin.timer;

import org.apache.commons.lang.Validate;

/**
 * A LocalTimer will have time elapse based on server up time.
 * @author Jon
 */
public class LocalTimer extends Timer {
	
	private long elapsed = 0;
	private long startTime = -1;
	
	/**
	 * Construct a LocalTimer object to continue with the given amount of elapsed time.
	 * @param duration - The duration in milliseconds
	 * @param elapsed - The elapsed time in milliseconds
	 * @throws IllegalArgumentException if duration is less than or equal to 0. Or if elapsed is less than 0.
	 */
	public LocalTimer(long duration, long elapsed) {
		this(duration);
		Validate.isTrue(elapsed >= 0, "elapsed must be greater than or equal to 0");
		this.elapsed = elapsed;
	}
	
	/**
	 * Construct a LocalTimer object with no amount of elapsed time. The amount of elapsed time will be set to 0.
	 * @param duration - The duration in milliseconds
	 * @throws IllegalArgumentException if duration is less than or equal to 0.
	 */
	public LocalTimer(long duration) {
		super(duration);
	}
	
	@Override
	void start() {
		startTime = System.currentTimeMillis() - elapsed;
	}
	
	/**
	 * The time left is equal to the duration time minus the elapsed time.
	 */
	@Override
	public long getTimeLeft() {
		return startTime != -1 ? duration - (elapsed = System.currentTimeMillis() - startTime) : startTime;
	}
	
	@Override
	public long getStartTime() {
		return startTime;
	}
	
	/**
	 * @return the amount of time in milliseconds that this timer has elapsed.
	 */
	public long getElapsed() {
		return startTime != -1 ? elapsed = System.currentTimeMillis() - startTime : elapsed;
	}
	
	/**
	 * @return a new GlobalTimer object with an expire time based on the amount of time passed in this LocalTimer.
	 */
	public GlobalTimer toGlobalTimer() {
		long startTime = System.currentTimeMillis() - elapsed;
		long expire = startTime + duration;
		return new GlobalTimer(duration, expire);
	}
	
}
