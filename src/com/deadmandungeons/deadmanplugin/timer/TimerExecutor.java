package com.deadmandungeons.deadmanplugin.timer;

/**
 * This interface provides the methods that are to be called by the {@link TimerScheduler} that the implementing class constructs.
 * @author Jon
 */
public interface TimerExecutor {
	
	/**
	 * This will be called once every repeat of the timer which will be every minute when there is
	 * more than 2 minutes remaining, and every second when there is less than 2 minutes remaining
	 * @param timer - The timer object
	 * @param isPerMinute - true if the timer is running every minute
	 */
	void onTimerTick(Timer timer, boolean isPerMinute);
	
	/**
	 * This will be called when the timer has ended. The timer will be null at this point
	 */
	void onTimerEnd();
	
}
