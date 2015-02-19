package org.deadmandungeons.deadmanplugin.timer;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.deadmandungeons.deadmanplugin.DeadmanPlugin;

/**
 * This class is used to increment a {@link Timer} object as time passes, and to execute
 * the tasks of the provided {@link TimerExecutor} implementing class. The TimerExecutor
 * interface defines two methods that are to be called during the timers countdown.
 * The {@link TimerExecutor#onTimerTick(Timer, boolean)} method willbe called once every
 * repeat of the timer, and the {@link TimerExecutor#onTimerEnd()} method
 * will be called when the timer has ended.
 * @author Jon
 */
public class TimerScheduler implements Runnable {
	
	private final long TWO_MINUTES_IN_MILLIS = Timer.ONE_MINUTE_IN_MILLIS * 2;
	private final long MINUTE_IN_TICKS = 1200;
	private final long SECOND_IN_TICKS = 20;
	
	private long repeatTicks;
	private BukkitTask task;
	
	private DeadmanPlugin plugin;
	private TimerExecutor timerExecutor;
	private Timer timer;
	
	/**
	 * @param plugin - The DeadmanPlugin that this TimerScheduler is for
	 * @param timerExecutor - The {@link TimerExecutor} to handle the Timer events
	 * @param timer - The {@link Timer} to be used for this TimerScheduler
	 * @throws IllegalArgumentException if plugin or timerExecutor is null
	 */
	public TimerScheduler(DeadmanPlugin plugin, TimerExecutor timerExecutor, Timer timer) {
		Validate.notNull(plugin, "plugin cannot be null");
		Validate.notNull(timerExecutor, "timerExecutor cannot be null");
		this.plugin = plugin;
		this.timerExecutor = timerExecutor;
		this.timer = timer;
	}
	
	@Override
	public final void run() {
		if (timer == null) {
			return;
		}
		
		if (timer.isEnded()) {
			setTimer(null);
			timerExecutor.onTimerEnd();
			return;
		}
		
		// If there is more than 2 minutes left on the clock, make sure we are only running this task every minute. Otherwise run it every second
		if (repeatTicks == SECOND_IN_TICKS) {
			if (timer.getTimeLeft() >= TWO_MINUTES_IN_MILLIS) {
				stopTimer();
				runPerMinute();
				return;
			}
		} else if (repeatTicks == MINUTE_IN_TICKS) {
			if (timer.getTimeLeft() < TWO_MINUTES_IN_MILLIS) {
				stopTimer();
				runPerSecond();
				return;
			}
		}
		
		timerExecutor.onTimerTick(timer, repeatTicks == MINUTE_IN_TICKS);
	}
	
	/**
	 * Start this TimerScheduler task and stop any timer that was previously running
	 * @throws IllegalStateException if the {@link Timer} object has not been set
	 */
	public final void startTimer() throws IllegalStateException {
		if (timer == null) {
			throw new IllegalStateException("The timer has not been set! A the timer scheduler cannot be started without a timer");
		}
		stopTimer();
		timer.start();
		runPerMinute();
	}
	
	/**
	 * Cancel this task if this task has been scheduled
	 */
	public final void stopTimer() {
		if (isScheduled()) {
			Bukkit.getScheduler().cancelTask(task.getTaskId());
		}
	}
	
	/**
	 * @return the Timer object that this scheduler is running with, or null if the task has not been scheduled
	 */
	public final Timer getTimer() {
		return timer;
	}
	
	/**
	 * Set Timer for this scheduler. Any currently scheduled Timers will be canceled.
	 * If the given timer has ended ({@link Timer#isEnded()}), the timer will be set as null
	 * @param timer - The Timer object to use for this scheduler
	 */
	public final void setTimer(Timer timer) {
		// cancel any currently qeued tasks now that we are using a new Timer
		stopTimer();
		this.timer = (timer != null && !timer.isEnded() ? timer : null);
	}
	
	/**
	 * @return true if this task is currently scheduled, and false otherwise
	 */
	public final boolean isScheduled() {
		// don't need to check isCurrentlyRunning() since this task is only synchronous
		return task != null && Bukkit.getScheduler().isQueued(task.getTaskId());
	}
	
	private void runPerMinute() {
		this.repeatTicks = MINUTE_IN_TICKS;
		task = Bukkit.getScheduler().runTaskTimer(plugin, this, 0, repeatTicks);
	}
	
	private void runPerSecond() {
		this.repeatTicks = SECOND_IN_TICKS;
		task = Bukkit.getScheduler().runTaskTimer(plugin, this, 0, repeatTicks);
	}
	
}