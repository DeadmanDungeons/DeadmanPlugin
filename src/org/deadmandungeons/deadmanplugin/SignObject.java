package org.deadmandungeons.deadmanplugin;

import org.bukkit.configuration.InvalidConfigurationException;

/**
 * @author Jon
 */
public interface SignObject extends ConfigObject {
	
	/**
	 * This method should be called to load any DeadmanSigns that represent this object using
	 * the data stored in its {@link org.bukkit.configuration.ConfigurationSection ConfigurationSection}.
	 * The signs location data should be stored in its respective SignEventListener sign Map.
	 * @throws InvalidConfigurationException when the provided data is invalid for this type
	 */
	public void loadSigns() throws InvalidConfigurationException;
	
}
