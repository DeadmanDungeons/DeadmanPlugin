package org.deadmandungeons.deadmanplugin;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;

/**
 * This abstract class should be extended by plugin Objects that are references to data that is
 * stored in the plugin's Data YAML file. Data such as sign locations and states is an example of such data
 * that might be referenced and maintained by a ConfigObject class.
 * @author Jon
 */
public abstract class ConfigObject {
	
	/**
	 * No-Args constructor needed so inherited classes constructors can call super()
	 */
	public ConfigObject() {}
	
	/**
	 * @param data - A map containing the ConfigurationSection keys and values for this ConfigObject
	 * @return A new instance of this ConfigObject with the values of this map
	 */
	public ConfigObject(ConfigurationSection data) throws InvalidConfigurationException {
		load(data);
	}
	
	/**
	 * @return the String key used to map this object
	 */
	public abstract String getKey();
	
	/**
	 * Delete all the references to this instance as well as any other objects that may linked,
	 * and update all of this objects properties in the plugin's data YAML file by calling {@link update}
	 */
	public abstract void delete();
	
	/**
	 * update all of this objects properties in the plugin's data YAML file
	 */
	public abstract void updateConfig();
	
	/**
	 * This method should be called to load any DeadmanSigns that represent this object and to store
	 * the signs location data in its respective SignEventListener sign Map
	 * @param data - The ConfigurationSection containing the DeadmanSign data for this object type
	 * @throws InvalidConfigurationException when the provided data is invalid for this type
	 */
	public abstract void loadSigns(ConfigurationSection data) throws InvalidConfigurationException;
	
	/**
	 * This method will be called by the super constructor, and is meant to load the properties from the config
	 * to the objects properties. Throw InvalidConfigurationException when the provided data is invalid
	 * @param data - The ConfigurationSection containing the data for this object
	 * @throws InvalidConfigurationException when the provided data is invalid for this type
	 */
	public abstract void load(ConfigurationSection data) throws InvalidConfigurationException;
	
	
}
