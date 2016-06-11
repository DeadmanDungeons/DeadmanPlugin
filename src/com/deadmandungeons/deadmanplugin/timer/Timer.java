package com.deadmandungeons.deadmanplugin.timer;

import org.apache.commons.lang.Validate;

import com.deadmandungeons.deadmanplugin.DeadmanUtils;


/**
 * A Timer is used to keep track of when a certain task should be executed.
 * @author Jon
 */
public abstract class Timer {
	
	public static final int ONE_MINUTE_IN_MILLIS = 60000;
	public static final int ONE_HOUR_IN_MILLIS = ONE_MINUTE_IN_MILLIS * 60;
	
	protected final long duration;
	
	Timer(long duration) {
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
	 * This checks if the startTime has been set, and if {@link #getTimeLeft()} returns a number greater than 0
	 * @return true if this timer has been started and is still alive, and false otherwise
	 */
	public final boolean isAlive() {
		return getStartTime() > -1 && getTimeLeft() > 0;
	}
	
	/**
	 * This checks if the startTime has not been set (or startTime == -1), and if {@link #getTimeLeft()} is 0 or less
	 * @return true if this timer has not yet been started
	 */
	public final boolean isPending() {
		return getStartTime() == -1 && getTimeLeft() <= 0;
	}
	
	/**
	 * This checks if {@link #isAlive()} and {@link #isPending()} both return false
	 * @return true if this timer has started and is now finished
	 */
	public final boolean isEnded() {
		return !isAlive() && !isPending();
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
		return DeadmanUtils.formatDuration(getTimeLeft());
	}
	
	public static boolean isAlive(Timer timer) {
		return timer != null && timer.isAlive();
	}
	
	public static boolean isPending(Timer timer) {
		return timer != null && timer.isPending();
	}
	
	public static boolean isEnded(Timer timer) {
		return timer == null || timer.isEnded();
	}
	
}