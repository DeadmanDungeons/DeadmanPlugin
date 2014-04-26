package org.deadmandungeons.deadmanplugin;

/**
 * A Timer is used to keep track of when a certain task should be executed.
 * If a Timer is set as global, the time will elapse based on server up time.<br />
 * If a Timer is set as not global, the time will elapse based on global time regardless of server up-time.<br />
 * @author Jon
 */
public class Timer {
	
	private long expire;
	private long duration;
	private long elapsed;
	private boolean global;
	
	private long startTime = -1;
	
	/**
	 * @param duration - The duration in milliseconds
	 * @param elapsed - The elapsed time in milliseconds
	 * @param expire - The expire timestamp (time since the epoch in milliseconds)
	 * @param global - The boolean flag specifying whether this timer is global or not
	 */
	public Timer(long duration, long elapsed, long expire, boolean global) {
		// the expire date is taken in the constructor to allow for resuming previous global timers
		// since once the timer has started, the expire date will no longer equal currentTime - duration
		this.expire = expire;
		this.duration = duration;
		this.elapsed = elapsed;
		this.global = global;
	}
	
	/**
	 * @param duration - The duration in milliseconds
	 * @param elapsed - The elapsed time in milliseconds
	 * @param global - The boolean flag specifying whether this timer is global or not
	 */
	public Timer(long duration, long elapsed, boolean global) {
		this.duration = duration;
		this.elapsed = elapsed;
		this.global = global;
	}
	
	public long getDuration() {
		return duration;
	}
	
	public void setDuration(Long duration) {
		this.duration = duration;
	}
	
	public long getExpire() {
		return expire;
	}
	
	public void setExpire(Long expire) {
		this.expire = expire;
	}
	
	public long getElapsed() {
		return elapsed;
	}
	
	public void setElapsed(Long elapsed) {
		this.elapsed = elapsed;
	}
	
	/**
	 * Set this timers elapsed time to the current time minus the startTime
	 */
	public void updateElapsed() {
		elapsed = System.currentTimeMillis() - startTime;
	}
	
	/**
	 * @return the startTime in milliseconds
	 */
	public long getStartTime() {
		return startTime;
	}
	
	/**
	 * Start this timer.<br />
	 * This timers start values will be set to the current time.
	 */
	public void start() {
		if (expire == 0) {
			expire = System.currentTimeMillis() + duration;
		}
		startTime = System.currentTimeMillis() - elapsed;
	}
	
	/**
	 * @return true if this timer is based on global time, and false if it is based on server time
	 */
	public boolean isGlobal() {
		return global;
	}
	
	/**
	 * If global is false, the time will elapse based on server up time.<br />
	 * If global is true, the time will elapse based on global time regardless of server up-time
	 * @param global - The boolean flag specifying whether this timer is global or not
	 */
	public void setGlobal(boolean global) {
		this.global = global;
	}
	
	/**
	 * If global is set to true, the time left is equal to the expire time minus the current time</br />
	 * If global is set to false, the time left is equal to the duration time minus the elapsed time
	 * @return the time left in milliseconds
	 */
	public long getTimeLeft() {
		if (global) {
			return expire - System.currentTimeMillis();
		}
		return duration - elapsed;
	}
	
	/**
	 * This simply checks if the startTime has been set, and if {@link #getTimeLeft() getTimeLeft()} returns a number less than or equal to 0
	 * @return true if this timer has been started and is now ended, and false otherwise
	 */
	public boolean isEnded() {
		return startTime > -1 && getTimeLeft() <= 0;
	}
	
	/**
	 * @return the amount of time left in a readable format short enough for use on signs
	 */
	@Override
	public String toString() {
		return DeadmanUtils.getDurationString(getTimeLeft());
	}
	
}