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

/**
 * This listener class is what invokes the DeadmanSign events for DeadmanSigns of type T.<br />
 * Call {@link org.deadmandungeons.deadmanplugin.DeadmanPlugin.registerSignEvent registerSignEvent(Class&lt;T&gt; signType, String signTag)}
 * for each DeadmanSign type that the plugin will be listening to.
 * @param <T> - The DeadmanSign sublcass involved in the DeadmanSign events
 * @author Jon
 */
@SuppressWarnings("unchecked")//Except it is checked
public class SignEventListener<T extends DeadmanSign> implements Listener {
	
	//TODO maybe the pluginSigns hashMap should be a member variable in this listener, and have a value type of T.
	
	private DeadmanPlugin plugin;
	private Class<T> signType;
	private String signTag;
	
	public SignEventListener(DeadmanPlugin instance, Class<T> signType, String signTag) {
		this.plugin = instance;
		this.signType = signType;
		this.signTag = signTag;
	}
	
	@EventHandler
	public void onSignCreate(SignChangeEvent event) {
		Sign sign = DeadmanUtils.getSignState(event.getBlock());
		if (sign != null && event.getLine(0).equals(signTag)) {
			SignCreateEvent<T> signCreateEvent = new SignCreateEvent<T>(event, sign);
			
			Bukkit.getServer().getPluginManager().callEvent(signCreateEvent);
		}
	}
	
	@EventHandler
	public void onSignBreak(BlockBreakEvent event) {
		Object deadmanSign = plugin.getPluginSigns().get(event.getBlock().getLocation());
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
