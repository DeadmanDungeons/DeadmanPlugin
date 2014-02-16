package org.deadmandungeons.deadmanplugin.events;

import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerInteractEvent;
import org.deadmandungeons.deadmanplugin.DeadmanSign;

public class SignClickEvent<T extends DeadmanSign> extends PlayerInteractEvent {
	
	private static final HandlerList handlers = new HandlerList();
	
	private T deadmanSign;
	
	public SignClickEvent(PlayerInteractEvent e, T deadmanSign) {
		super(e.getPlayer(), e.getAction(), e.getItem(), e.getClickedBlock(), e.getBlockFace());
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
