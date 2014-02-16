package org.deadmandungeons.deadmanplugin.events;

import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockBreakEvent;
import org.deadmandungeons.deadmanplugin.DeadmanSign;

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
	
	public T getSign() {
		return deadmanSign;
	}
	
}
