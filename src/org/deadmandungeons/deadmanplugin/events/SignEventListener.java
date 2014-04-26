package org.deadmandungeons.deadmanplugin.events;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.deadmandungeons.deadmanplugin.DeadmanPlugin;
import org.deadmandungeons.deadmanplugin.DeadmanSign;
import org.deadmandungeons.deadmanplugin.DeadmanUtils;
import org.deadmandungeons.deadmanplugin.SignObject;

/**
 * This listener class is what invokes the DeadmanSign events for DeadmanSigns of type T.<br />
 * Construct this object for each DeadmanSign type that the plugin will be listening to.
 * There is no need to register this listener because it will register itself upon construction
 * @param <V> - The {@link SignObject} that the signs of type T represent
 * @param <T> - The {@link DeadmanSign} involved in the sign events
 * @author Jon
 */
public abstract class SignEventListener<V extends SignObject, T extends DeadmanSign<V>> implements Listener {
	
	private Map<Location, T> deadmanSigns = new HashMap<Location, T>();
	
	private String signTag;
	
	public <U extends DeadmanPlugin> SignEventListener(String signTag, U plugin) {
		this.signTag = signTag;
		Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onSignCreate(SignChangeEvent event) {
		Sign sign = DeadmanUtils.getSignState(event.getBlock());
		if (sign != null && event.getLine(0).equals(signTag)) {
			SignCreateEvent<V, T> signCreateEvent = new SignCreateEvent<V, T>(event, sign);
			
			onSignCreate(signCreateEvent);
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onSignBreak(BlockBreakEvent event) {
		T deadmanSign = deadmanSigns.get(event.getBlock().getLocation());
		if (deadmanSign != null) {
			SignBreakEvent<V, T> signBreakEvent = new SignBreakEvent<V, T>(event, deadmanSign);
			
			onSignBreak(signBreakEvent);
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.getClickedBlock() != null) {
			T deadmanSign = deadmanSigns.get(event.getClickedBlock().getLocation());
			if (deadmanSign != null) {
				SignClickEvent<V, T> signClickEvent = new SignClickEvent<V, T>(event, deadmanSign);
				
				onSignClick(signClickEvent);
			}
		}
	}
	
	/**
	 * @return The reference to all of the DeadmanSigns of type T for this plugin
	 */
	public Map<Location, T> getDeadmanSigns() {
		return deadmanSigns;
	}
	
	public abstract void onSignCreate(SignCreateEvent<V, T> event);
	
	public abstract void onSignBreak(SignBreakEvent<V, T> event);
	
	public abstract void onSignClick(SignClickEvent<V, T> event);
	
}
