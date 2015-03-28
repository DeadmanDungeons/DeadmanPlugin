package org.deadmandungeons.deadmanplugin.filedata;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.deadmandungeons.deadmanplugin.DeadmanPlugin;

public class PluginFile {
	
	private static final String WARNING_MSG = "\n %1$s %2$s file\n\n WARNING: THIS FILE IS AUTOMATICALLY GENERATED."
			+ " If you modify this file by\n hand, be aware that A SINGLE MISTYPED CHARACTER CAN CORRUPT THE FILE. If\n "
			+ "%1$s is unable to parse the file, the plugin may BREAK!\n\n REMEMBER TO KEEP PERIODICAL BACKUPS.\n ";
	
	private File configFile;
	private FileConfiguration fileConfig;
	private YamlConfiguration defaultConfig;
	private int reloadCount;
	
	private final DeadmanPlugin plugin;
	private final String filePath;
	private final String defaultFilePath;
	
	/**
	 * @param instance - An instance of the DeadmanPlugin this file belongs to
	 * @param filePath - The relative path to the file in the plugin's data folder.
	 * If a file at this path does not exist, a new file will be created.
	 * @param defaultFilePath - The path to the resource file bundled in the plugin's jar
	 * that should be saved as the file specified by filePath if it does not already exist.
	 * If this is null, a new file will be created if one does not yet exist at filePath.
	 * @throws IllegalStateException if the plugin has not yet been enabled
	 */
	public PluginFile(DeadmanPlugin instance, String filePath, String defaultFilePath) {
		if (!instance.isEnabled()) {
			throw new IllegalStateException("This plugin has not been enabled yet! Cannot create plugin file before plugin is enabled");
		}
		this.plugin = instance;
		this.filePath = plugin.getDataFolder().getPath() + File.separator + filePath;
		this.defaultFilePath = defaultFilePath;
		this.configFile = new File(this.filePath);
		
		if (!configFile.exists()) {
			try {
				if (configFile.getParentFile() != null) {
					configFile.getParentFile().mkdirs();
				}
				if (defaultFilePath != null) {
					plugin.getLogger().info("Saving default '" + defaultFilePath + "' config file as '" + filePath + "'");
					saveDefaultConfig(defaultFilePath);
				} else {
					plugin.getLogger().info("Creating '" + filePath + "' file");
					configFile.createNewFile();
					String fileName = filePath.replaceAll("(.*)\\" + File.separator, "");
					getConfig().options().header(String.format(WARNING_MSG, plugin.getName(), fileName));
					saveConfig();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Synonymous to calling <code>new PluginFile(plugin, filePath, null);</code>
	 * @param plugin - An instance of the DeadmanPlugin this file belongs to
	 * @param filePath - The relative path to the file in the plugin's data folder.
	 * If this file does not exist, a new one will be created.
	 */
	public PluginFile(DeadmanPlugin plugin, String filePath) {
		this(plugin, filePath, null);
	}
	
	/**
	 * reload the contents of this PluginFile to its {@link org.bukkit.configuration.file.FileConfiguration FileConfiguration},
	 * and if this PluginFile has a default resource, set the FileConfiguration defaults to the YamlConfiguration of the default resource.
	 */
	public void reloadConfig() {
		fileConfig = YamlConfiguration.loadConfiguration(configFile);
		reloadCount++;
		
		// Look for defaults in the jar
		if (defaultFilePath != null) {
			InputStream defConfigStream = plugin.getResource(defaultFilePath);
			if (defConfigStream != null) {
				defaultConfig = YamlConfiguration.loadConfiguration(defConfigStream);
				fileConfig.setDefaults(defaultConfig);
			}
		}
	}
	
	/**
	 * @return The {@link org.bukkit.configuration.file.FileConfiguration FileConfiguration} for this PluginFile
	 */
	public FileConfiguration getConfig() {
		if (fileConfig == null) {
			reloadConfig();
		}
		return fileConfig;
	}
	
	/**
	 * Save the {@link org.bukkit.configuration.file.FileConfiguration FileConfiguration} of this PluginFile to disk.
	 */
	public void saveConfig() {
		if (fileConfig != null && configFile != null) {
			try {
				getConfig().save(configFile);
			} catch (IOException ex) {
				Bukkit.getServer().getLogger().log(Level.SEVERE, "Could not save config to " + configFile, ex);
			}
		}
	}
	
	/**
	 * Save a default resource file packaged in the plugin's jar to this PluginFile file path.
	 * @param defaultFilePath - The path inside the jar to the desired resource
	 * @param replace - If true, the embedded resource will overwrite the contents of the existing PluginFile.
	 */
	public void saveDefaultConfig(String defaultFilePath, boolean replace) {
		reloadConfig();
		if (!configFile.exists()) {
			// save the default resource at the given path to the root plugin directory
			plugin.saveResource(defaultFilePath, replace);
		}
		// if the saved resource isn't at the intended configFile path
		if (!configFile.exists()) {
			File createdFile = new File(plugin.getDataFolder().getPath() + File.separator + defaultFilePath);
			// move the saved resource file to the appropriate directory
			if (!createdFile.renameTo(configFile)) {
				plugin.getLogger().severe(defaultFilePath + " failed to move!");
			}
		}
	}
	
	/**
	 * If this PluginFile does not exist, save a default resource file packaged in the plugin's jar to
	 * this PluginFile file path. Synonymous to calling <code>saveDefaultConfig(defaultFilePath, false);</code>
	 * @param defaultFilePath - The path inside the jar to the desired resource
	 */
	public void saveDefaultConfig(String defaultFilePath) {
		saveDefaultConfig(defaultFilePath, false);
	}
	
	/**
	 * This is useful to check if the pluginFile has been reloaded and that any cached values may not be valid.
	 * @return the amount of times this PluginFile has been reloaded.
	 */
	public int getReloadCount() {
		return reloadCount;
	}
	
}
