package org.deadmandungeons.deadmanplugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * This Listener class is provided to improve modularity with events relating to DeadmanSigns.
 * Extend this class for each {@link DeadmanSign} of type T that represents a {@link SignObject} of type V.<br />
 * Construct this object for each DeadmanSign type that the plugin will be listening to.
 * There is no need to register this listener because it will register itself upon construction.
 * All DeadmanSigns of type T that are created should be stored in this class's deadmanSigns HashMap
 * using {@link #getDeadmanSigns()}
 * @param <V> - The {@link SignObject} that the signs of type T represent
 * @param <T> - The {@link DeadmanSign} involved in the sign events
 * @author Jon
 */
public abstract class SignEventListener<V extends SignObject, T extends DeadmanSign<V>> implements Listener {
	
	private Map<Location, T> deadmanSigns = new HashMap<Location, T>();
	
	private List<String> signTags;
	
	public <U extends DeadmanPlugin> SignEventListener(String signTag, U plugin) {
		signTags = new ArrayList<String>();
		signTags.add(signTag);
		Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	public <U extends DeadmanPlugin> SignEventListener(List<String> signTags, U plugin) {
		this.signTags = signTags;
		Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onSignCreate(SignChangeEvent event) {
		Sign sign = DeadmanUtils.getSignState(event.getBlock());
		if (sign != null && signTags.contains(event.getLine(0))) {
			onSignCreate(event, sign);
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onSignBreak(BlockBreakEvent event) {
		T deadmanSign = deadmanSigns.get(event.getBlock().getLocation());
		if (deadmanSign != null) {
			onSignBreak(event, deadmanSign);
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.getClickedBlock() != null) {
			T deadmanSign = deadmanSigns.get(event.getClickedBlock().getLocation());
			if (deadmanSign != null) {
				onSignClick(event, deadmanSign);
			}
		}
	}
	
	/**
	 * @return The reference to all of the DeadmanSigns of type T for this plugin
	 */
	public Map<Location, T> getDeadmanSigns() {
		return deadmanSigns;
	}
	
	public abstract void onSignCreate(SignChangeEvent event, Sign sign);
	
	public abstract void onSignBreak(BlockBreakEvent event, T deadmanSign);
	
	public abstract void onSignClick(PlayerInteractEvent event, T deadmanSign);
	
}
