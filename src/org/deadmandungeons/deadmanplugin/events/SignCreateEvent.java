package org.deadmandungeons.deadmanplugin.events;

import org.bukkit.block.Sign;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.SignChangeEvent;
import org.deadmandungeons.deadmanplugin.DeadmanSign;

public class SignCreateEvent<T extends DeadmanSign> extends SignChangeEvent {

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
	
	public Sign getSign() {
		return sign;
	}

}
