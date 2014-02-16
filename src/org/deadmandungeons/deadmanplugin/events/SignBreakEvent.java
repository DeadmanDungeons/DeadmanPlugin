package org.deadmandungeons.deadmanplugin.events;

import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockBreakEvent;
import org.deadmandungeons.deadmanplugin.DeadmanSign;

/**
 * This Event will be fired when a DeadmanSign of type T is broken
 * @param <T> - The DeadmanSign subclass that was involved in the SignBreakEvent
 * @author Jon
 */
public class SignBreakEvent<T extends DeadmanSign> extends BlockBreakEvent {
	
	private static final HandlerList handlers = new HandlerList();
	
	private T deadmanSign;

	public SignBreakEvent(BlockBreakEvent e, T deadmanSign) {
		super(e.getBlock(), e.getPlayer());
		this.deadmanSign = deadmanSign;
	}
	
	@Override
	public HandlerList getHandlers() {
        return handlers;
    }
	
    public static HandlerList getHandlerList() {
        return handlers;
    }
	
    /**
     * @return The DeadmanSign subclass object that was broken
     */
	public T getSign() {
		return deadmanSign;
	}
	
}
