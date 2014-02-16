package org.deadmandungeons.deadmanplugin.events;

import org.bukkit.Bukkit;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.deadmandungeons.deadmanplugin.DeadmanPlugin;
import org.deadmandungeons.deadmanplugin.DeadmanSign;
import org.deadmandungeons.deadmanplugin.DeadmanUtils;

/*
 * There is a compilation warning when casting to T, but it will only cast to T if the object is an instance of T
 */
@SuppressWarnings("unchecked")
public class SignEventListener<T extends DeadmanSign> implements Listener {
	
	private DeadmanPlugin plugin;
	private Class<T> signType;
	private String signTag;
	
	public SignEventListener(DeadmanPlugin instance, Class<T> signType, String signTag) {
		this.plugin = instance;
		this.signType = signType;
		this.signTag = signTag;
		
		System.out.println("constructed: " + signTag);
	}
	
	@EventHandler
	public void onSignCreate(SignChangeEvent event) {
		Sign sign = DeadmanUtils.getSignState(event.getBlock());
		System.out.println("onSignCreate: " + (sign != null) + " - " + event.getBlock().getType() + " - " + signTag);
		if (sign != null && event.getLine(0).equals(signTag)) {
			SignCreateEvent<T> signCreateEvent = new SignCreateEvent<T>(event, sign);
			
			Bukkit.getServer().getPluginManager().callEvent(signCreateEvent);
		}
	}
	
	@EventHandler
	public void onSignBreak(BlockBreakEvent event) {
		Object deadmanSign = plugin.getPluginSigns().get(event.getBlock().getLocation());
		System.out.println("onSignBreak: " + (deadmanSign != null) + " - " + event.getBlock().getType() + " - " + signTag);
		if (deadmanSign != null && signType.isInstance(deadmanSign)) {
			T sign = (T) deadmanSign;
			SignBreakEvent<T> signBreakEvent = new SignBreakEvent<T>(event, sign);
			
			Bukkit.getServer().getPluginManager().callEvent(signBreakEvent);
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Object deadmanSign = plugin.getPluginSigns().get(event.getClickedBlock().getLocation());
		if (deadmanSign != null && signType.isInstance(deadmanSign)) {
			T sign = (T) deadmanSign;
			SignClickEvent<T> signClickEvent = new SignClickEvent<T>(event, sign);
			
			Bukkit.getServer().getPluginManager().callEvent(signClickEvent);
		}
	}
	
}
