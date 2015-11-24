package org.deadmandungeons.deadmanplugin;

import java.io.File;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.deadmandungeons.deadmanplugin.filedata.DeadmanConfig;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

/**
 * The base abstract class to be extended by the main class for all Deadman plugins.
 * @author Jon
 */
public abstract class DeadmanPlugin extends JavaPlugin {
	
	public static final String LANG_DIRECTORY = "lang" + File.separator;
	public static final String DATA_DIRECTORY = "data" + File.separator;
	
	private static final Map<Class<? extends DeadmanPlugin>, DeadmanPlugin> plugins = new LinkedHashMap<>();
	
	private boolean loaded;
	private Economy economy;
	private Permission permissions;
	private DeadmanConfig config;
	
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
	}
	
	@Override
	public final void onLoad() {
		loaded = true;
		try {
			onPluginLoad();
		} catch (Throwable t) {
			getLogger().log(Level.SEVERE, "An error occured while loading/initializing plugin", t);
			loaded = false;
		}
	}
	
	/**
	 * @see {@link #onLoad()}
	 */
	protected void onPluginLoad() throws Exception {}
	
	@Override
	public final void onEnable() {
		if (!loaded) {
			getLogger().severe("Plugin cannot be enabled due to an error that occurred during the plugin loading phase");
			setEnabled(false);
			return;
		}
		
		onPluginEnable();
		
		if (isEnabled()) {
			// Run after onPluginEnable to assure that it completed without unhandled errors
			Bukkit.getScheduler().runTask(this, new Runnable() {
				
				@Override
				public void run() {
					onFirstServerTick();
				}
			});
		}
	}
	
	/**
	 * @see {@link #onEnable()}
	 */
	protected void onPluginEnable() {}
	
	@Override
	public final void onDisable() {
		onPluginDisable();
		
		// Free up memory
		plugins.remove(getClass());
		loaded = false;
	}
	
	/**
	 * @see {@link #onDisable()}
	 */
	protected void onPluginDisable() {}
	
	/**
	 * This will be called on the first server tick after the plugin is enabled.
	 * This is useful when loading saved objects from file that contain World specific
	 * data because all of the worlds should be loaded at this point. For example,
	 * plugin's like MultiVerse may be enabled after this plugin is.
	 */
	protected void onFirstServerTick() {}
	
	
	@Override
	public void reloadConfig() {
		super.reloadConfig();
		if (config != null) {
			config.loadEntries(this);
		}
	}
	
	public final void setConfig(DeadmanConfig config) {
		if (config != null) {
			saveDefaultConfig();
			config.loadEntries(this);
		}
		this.config = config;
	}
	
	public final boolean isLoaded() {
		return loaded;
	}
	
	public final boolean setupEconomy() {
		if (economy == null) {
			if (!getServer().getPluginManager().isPluginEnabled("Vault")) {
				getLogger().severe("Vault is not enabled on this server and is a required dependendy!");
				return false;
			}
			RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
			if (economyProvider != null) {
				economy = economyProvider.getProvider();
			}
		}
		return (economy != null);
	}
	
	public final boolean setupPermissions() {
		if (permissions == null) {
			if (!getServer().getPluginManager().isPluginEnabled("Vault")) {
				getLogger().severe("Vault is not enabled on this server and is a required dependendy!");
				return false;
			}
			RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(Permission.class);
			if (permissionProvider != null) {
				permissions = permissionProvider.getProvider();
			}
		}
		return (permissions != null);
	}
	
	public final Economy getEconomy() {
		return economy;
	}
	
	public final Permission getPermissions() {
		return permissions;
	}
	
	public final Conversion getConversion() {
		return Conversion.get(getClass());
	}
	
	
	/**
	 * @return an u nmodifiable Map of all the currently instantiated DeadmanPlugins
	 * with the plugin Class as the Key, and the DeadmanPlugin instance as the value
	 */
	public static final Map<Class<? extends DeadmanPlugin>, DeadmanPlugin> getDeadmanPlugins() {
		return Collections.unmodifiableMap(plugins);
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
	
}
