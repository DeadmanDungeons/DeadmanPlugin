package org.deadmandungeons.deadmanplugin;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.collect.ImmutableMap;

/**
 * The base abstract class to be extended by the main class for all Deadman plugins.
 * @author Jon
 */
public abstract class DeadmanPlugin extends JavaPlugin {
	
	public static final String LANG_DIRECTORY = "lang" + File.separator;
	public static final String DATA_DIRECTORY = "data" + File.separator;
	
	private static final Map<Class<? extends DeadmanPlugin>, DeadmanPlugin> plugins = new LinkedHashMap<Class<? extends DeadmanPlugin>, DeadmanPlugin>();
	
	private Economy economy;
	private Permission permissions;
	
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
		// this may be used in the future
		
		onPluginLoad();
	}
	
	@Override
	public final void onEnable() {
		// Add instance to plugin's datastore in case the plugin was disabled and then enabled again
		if (!plugins.containsKey(getClass())) {
			plugins.put(getClass(), this);
		}
		
		Bukkit.getScheduler().runTask(this, new Runnable() {
			
			@Override
			public void run() {
				if (isEnabled()) {
					System.out.println("onFirstServerTick(): " + getName());
					onFirstServerTick();
				}
			}
		});
		
		onPluginEnable();
	}
	
	@Override
	public final void onDisable() {
		// Free up memory
		plugins.remove(getClass());
		
		onPluginDisable();
	}
	
	/**
	 * @see {@link #onLoad()}
	 */
	public void onPluginLoad() {}
	
	/**
	 * @see {@link #onEnable()}
	 */
	public void onPluginEnable() {}
	
	/**
	 * @see {@link #onDisable()}
	 */
	public void onPluginDisable() {}
	
	/**
	 * This will be called on the first server tick after the plugin is enabled.
	 * This is useful when loading saved objects from file that contain World specific
	 * data because all of the worlds should be loaded at this point. For example,
	 * plugin's like MultiVerse may be enabled after this plugin is.
	 */
	protected void onFirstServerTick() {}
	
	
	public final boolean setupEconomy() {
		if (economy == null) {
			if (!getServer().getPluginManager().isPluginEnabled("Vault")) {
				getLogger().log(Level.SEVERE, "Vault is not enabled on this server and is a required dependendy!");
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
				getLogger().log(Level.SEVERE, "Vault is not enabled on this server and is a required dependendy!");
				return false;
			}
			RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(Permission.class);
			if (permissionProvider != null) {
				permissions = permissionProvider.getProvider();
			}
		}
		return (permissions != null);
	}
	
	public final <T extends Plugin> T getPluginDependency(String pluginName, Class<T> pluginType) {
		Plugin plugin = getServer().getPluginManager().getPlugin(pluginName);
		if (plugin != null && pluginType.isInstance(plugin)) {
			return pluginType.cast(plugin);
		}
		return null;
	}
	
	public final Economy getEconomy() {
		return economy;
	}
	
	public final Permission getPermissions() {
		return permissions;
	}
	
	
	/**
	 * @return an ImmutableMap of all the currently instantiated DeadmanPlugins
	 * with the plugin Class as the Key, and the DeadmanPlugin instance as the value
	 */
	public static final Map<Class<? extends DeadmanPlugin>, DeadmanPlugin> getDeadmanPlugins() {
		return ImmutableMap.copyOf(plugins);
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
