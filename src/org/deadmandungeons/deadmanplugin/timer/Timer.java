package org.deadmandungeons.deadmanplugin.timer;

import org.apache.commons.lang.Validate;
import org.deadmandungeons.deadmanplugin.DeadmanUtils;


/**
 * A Timer is used to keep track of when a certain task should be executed.
 * @author Jon
 */
public abstract class Timer {
	
	public static final int ONE_MINUTE_IN_MILLIS = 60000;
	public static final int ONE_HOUR_IN_MILLIS = ONE_MINUTE_IN_MILLIS * 60;
	
	protected final long duration;
	
	protected Timer(long duration) {
		Validate.isTrue(duration > 0, "duration must be greater than 0");
		this.duration = duration;
	}
	
	/**
	 * @return the duration in milliseconds for this Timer
	 */
	public final long getDuration() {
		return duration;
	}
	
	/**
	 * This checks if the startTime has been set, and if {@link #getTimeLeft()} returns a number less than or equal to 0
	 * @return true if this timer has been started and is now ended, and false otherwise
	 */
	public final boolean isEnded() {
		return getStartTime() != -1 && getTimeLeft() <= 0;
	}
	
	/**
	 * @return the time left in milliseconds, or -1 if this timer has not yet been started.
	 */
	public abstract long getTimeLeft();
	
	/**
	 * @return the timestamp (in milliseconds since the epoch) when this Timer was started,
	 * or -1 if this Timer has not yet been started.
	 */
	public abstract long getStartTime();
	
	abstract void start();
	
	/**
	 * @return the amount of time left in a readable format short enough for use on signs
	 */
	@Override
	public String toString() {
		return DeadmanUtils.getDurationString(getTimeLeft());
	}
	
}