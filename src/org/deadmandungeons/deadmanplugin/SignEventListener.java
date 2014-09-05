package org.deadmandungeons.deadmanplugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.FixedMetadataValue;

import com.google.common.collect.ImmutableList;

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
	
	private final List<String> signTags;
	private final DeadmanPlugin plugin;
	private final int cooldown;
	
	public SignEventListener(String signTag, DeadmanPlugin plugin) {
		this(signTag, plugin, -1);
	}
	
	public SignEventListener(String signTag, DeadmanPlugin plugin, int cooldown) {
		this(ImmutableList.of(signTag), plugin, cooldown);
	}
	
	public SignEventListener(List<String> signTags, DeadmanPlugin plugin, int cooldown) {
		this.signTags = signTags;
		this.plugin = plugin;
		this.cooldown = cooldown;
		Bukkit.getPluginManager().registerEvents(this, plugin);
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
				//Handle sign cooldown
				if (cooldown > 0 && !event.getPlayer().isOp()) {
					String metadataKey = plugin.getName() + "-sign-cooldown";
					Long timestamp = DeadmanUtils.getMetadata(plugin, event.getPlayer(), metadataKey, Long.class);
					if (timestamp != null) {
						long timeLeft = ((timestamp) + (cooldown * 1000)) - System.currentTimeMillis();
						if (timeLeft > 0) {
							int secondsLeft = (int) Math.ceil(timeLeft / 1000);
							event.getPlayer().sendMessage(ChatColor.RED + "You can click this sign in " + secondsLeft + " seconds");
							return;
						}
					}
					
					event.getPlayer().setMetadata(metadataKey, new FixedMetadataValue(plugin, System.currentTimeMillis()));
				}
				
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
