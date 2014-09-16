package org.deadmandungeons.deadmanplugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.deadmandungeons.deadmanplugin.filedata.PluginFile;

import com.google.common.collect.ImmutableMap;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

/**
 * The base abstract class to be extended by the main class for all Deadman plugins.
 * @author Jon
 */
public abstract class DeadmanPlugin extends JavaPlugin {
	
	public static final String LANG_DIRECTORY = "lang" + File.separator;
	public static final String DATA_DIRECTORY = "data" + File.separator;
	
	private static final Map<Class<? extends DeadmanPlugin>, DeadmanPlugin> plugins = new HashMap<Class<? extends DeadmanPlugin>, DeadmanPlugin>();
	private static ImmutableMap<Class<? extends DeadmanPlugin>, DeadmanPlugin> immutablePlugins;
	
	private final Messenger messenger;
	
	private Economy economy;
	private Permission permissions;
	private WorldGuardPlugin worldGuard;
	private WorldEditPlugin worldEdit;
	
	private boolean loaded;
	
	/**
	 * no-arg constructor called by the Craftbukkit classloader
	 * @throws IllegalStateException if an instance of this DeadmanPlugin already exists.
	 * This would only happen if something other than the Craftbukkit classloader tried
	 * to create a new instance of the plugin.
	 */
	protected DeadmanPlugin() throws IllegalStateException {
		if (plugins.containsKey(getClass())) {
			String msg = "The " + getName() + " DeadmanPlugin has already been initialized and cannot be initialized again";
			throw new IllegalStateException(msg);
		}
		Bukkit.getLogger().info("Initialized DeadmanPlugin: " + getName());
		plugins.put(getClass(), this);
		
		messenger = new Messenger(this);
	}
	
	
	/**
	 * @return an ImmutableMap of all the currently instantiated DeadmanPlugins
	 * with the plugin Class as the Key, and the DeadmanPlugin instance as the value
	 */
	public static final Map<Class<? extends DeadmanPlugin>, DeadmanPlugin> getDeadmanPlugins() {
		if (immutablePlugins == null || plugins.size() > immutablePlugins.size()) {
			immutablePlugins = ImmutableMap.copyOf(plugins);
		}
		return immutablePlugins;
	}
	
	/**
	 * @param pluginClass - The extended DeadmanPlugin class of the desired instance
	 * @return the singleton instance for the given DeadmanPlugin class type
	 * @throws IllegalStateException if the given DeadmanPlugin type has not been initialized and no instance exists
	 */
	public static final <T extends DeadmanPlugin> T getDeadmanPlugin(Class<T> pluginClass) throws IllegalStateException {
		Validate.notNull(pluginClass, "pluginClass cannot be null");
		T plugin = pluginClass.cast(plugins.get(pluginClass));
		if (plugin == null) {
			String msg = pluginClass.getSimpleName() + "has not been initialized yet! Cannot get plugin instance before plugin is initialized";
			throw new IllegalStateException(msg);
		}
		return plugin;
	}
	
	@Override
	public final void onLoad() {
		loaded = true;
		onPluginLoad();
	}
	
	/**
	 * @see org.bukkit.plugin.Plugin#onLoad()
	 */
	public void onPluginLoad() {}
	
	@Override
	public abstract void onEnable();
	
	@Override
	public abstract void onDisable();
	
	/**
	 * @return the {@link PluginFile} designated for message Strings
	 */
	public abstract PluginFile getLangFile();
	
	public final boolean setupEconomy() {
		if (!getServer().getPluginManager().isPluginEnabled("Vault")) {
			getLogger().log(Level.SEVERE, "Vault is not enabled on this server and is a required dependendy!");
			return false;
		}
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
		}
		return (economy != null);
	}
	
	public final boolean setupPermissions() {
		if (!getServer().getPluginManager().isPluginEnabled("Vault")) {
			getLogger().log(Level.SEVERE, "Vault is not enabled on this server and is a required dependendy!");
			return false;
		}
		RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(Permission.class);
		if (permissionProvider != null) {
			permissions = permissionProvider.getProvider();
		}
		return (permissions != null);
	}
	
	public final boolean setupWorldGuard() {
		Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");
		if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
			getLogger().log(Level.SEVERE, "WorldGuard is not enabled on this server and is a required dependendy!");
			return false;
		}
		worldGuard = (WorldGuardPlugin) plugin;
		return true;
	}
	
	public final boolean setupWorldEdit() {
		Plugin plugin = getServer().getPluginManager().getPlugin("WorldEdit");
		if (plugin == null || !(plugin instanceof WorldEditPlugin)) {
			getLogger().log(Level.SEVERE, "WorldEdit is not enabled on this server and is a required dependendy!");
			return false;
		}
		worldEdit = (WorldEditPlugin) plugin;
		return true;
	}
	
	public final Economy getEconomy() {
		return economy;
	}
	
	public final Permission getPermissions() {
		return permissions;
	}
	
	public final WorldGuardPlugin getWorldGuard() {
		return worldGuard;
	}
	
	public final WorldEditPlugin getWorldEdit() {
		return worldEdit;
	}
	
	/**
	 * @return the {@link Messenger} for this DeadmanPlugin
	 */
	public Messenger getMessenger() {
		return messenger;
	}
	
	/**
	 * @return true if craftbukkit has loaded this plugin and false otherwise
	 */
	public final boolean isJavaPluginLoaded() {
		return loaded;
	}
	
}
