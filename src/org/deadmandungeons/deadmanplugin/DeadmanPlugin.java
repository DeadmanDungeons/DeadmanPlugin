package org.deadmandungeons.deadmanplugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.Location;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.deadmandungeons.deadmanplugin.events.SignEventListener;

public abstract class DeadmanPlugin extends JavaPlugin {

	public static final String LANG_DIRECTORY = "lang" + File.separator;
	public static final String DATA_DIRECTORY = "data" + File.separator;
	
	private Map<Location, DeadmanSign> pluginSigns = new HashMap<Location, DeadmanSign>();
	
	private Economy economy;
	private Permission permissions;
	
	@Override
	public abstract void onEnable();
	
	@Override
	public abstract void onDisable();
	
	public abstract PluginFile getConfigFile();
	
	public abstract PluginFile getLangFile();
	
	public abstract Messenger getMessenger();
	
	public boolean setupEconomy() {
		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			getLogger().log(Level.SEVERE, "Vault is not enabled on this server and may be required!");
			return false;
		}
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
		}
		return (economy != null);
	}
	
	public boolean setupPermissions() {
		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			getLogger().log(Level.SEVERE, "Vault is not enabled on this server and may be required!");
			return false;
		}
        RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
        	permissions = permissionProvider.getProvider();
        }
        return (permissions != null);
    }
	
	/**
	 * A convenience method to register the DeadmanSign events for the given sign type
	 * @param signType - The DeadmanSign subclass that should be registered
	 * @param signTag - The sign tag (first line) that identifies a sign to be of this type
	 */
	public <T1 extends DeadmanSign> void registerSignEvents(Class<T1> signType, String signTag) {
		getServer().getPluginManager().registerEvents(new SignEventListener<T1>(this, signType, signTag), this);
	}
	
	public Economy getEconomy() {
		return economy;
	}
	
	public Permission getPermissions() {
		return permissions;
	}
	
	/**
	 * @return The reference to all of the DeadmanSigns for this plugin
	 */
	public Map<Location, DeadmanSign> getPluginSigns() {
		return pluginSigns;
	}
	
	/**
	 * @param signType - The subclass of the desired DeadmanSigns
	 * @return a new Map containing all of the DeadmanSigns of the given class.
	 */
	@SuppressWarnings("unchecked")//except it is checked
	public <T extends DeadmanSign> Map<Location, T> getPluginSigns(Class<T> signType) {
		Map<Location, T> signs = new HashMap<Location, T>();
		for (Location loc : pluginSigns.keySet()) {
			if (signType.isInstance(pluginSigns.get(loc))) {
				signs.put(loc, (T) pluginSigns.get(loc));
			}
		}
		return signs;
	}
	
	/**
	 * @param obj - The ConfigObject of the desired DeadmanSigns
	 * @return a new Map containing all of the DeadmanSigns whos ConfigObjects equal the given ConfigObject
	 */
	@SuppressWarnings("unchecked")//except it is checked
	public <T extends DeadmanSign> Map<Location, T> getPluginSigns(Class<T> signType, ConfigObject obj) {
		Map<Location, T> signs = new HashMap<Location, T>();
		for (Location loc : pluginSigns.keySet()) {
			if (signType.isInstance(pluginSigns.get(loc)) && pluginSigns.get(loc).getObj().equals(obj)) {
				signs.put(loc, (T) pluginSigns.get(loc));
			}
		}
		return signs;
	}
	
}
