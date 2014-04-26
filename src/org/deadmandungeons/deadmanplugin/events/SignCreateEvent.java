package org.deadmandungeons.deadmanplugin.events;

import org.bukkit.block.Sign;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.SignChangeEvent;
import org.deadmandungeons.deadmanplugin.DeadmanSign;
import org.deadmandungeons.deadmanplugin.SignObject;

/**
 * This Event will be fired when a DeadmanSign of type T is created (based on the first line of the sign)
 * @param <T> - The DeadmanSign subclass that was involved in the SignCreateEvent
 * @author Jon
 */
public class SignCreateEvent<V extends SignObject, T extends DeadmanSign<V>> extends SignChangeEvent {
	
	private static final HandlerList handlers = new HandlerList();
	
	private Sign sign;
	
	public SignCreateEvent(SignChangeEvent e, Sign sign) {
		super(e.getBlock(), e.getPlayer(), e.getLines());
		this.sign = sign;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
	
	/**
	 * @return The DeadmanSign subclass object that was created
	 */
	public Sign getSign() {
		return sign;
	}
	
}
