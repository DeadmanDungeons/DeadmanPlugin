package org.deadmandungeons.deadmanplugin;

import org.bukkit.configuration.ConfigurationSection;

/**
 * This interface should be implemented by plugin Objects that are references to data that is
 * stored in the plugin's Data YAML file. Data such as sign locations and states is an example of such data
 * that might be referenced and maintained by a ConfigObject class.
 * @author Jon
 */
public interface ConfigObject {
	
	/**
	 * This method is used to get the {@link ConfigurationSection ConfigurationSection} that this object represents
	 * @return the ConfigurationSection that this object represents
	 */
	public ConfigurationSection getConfSection();
	
	/**
	 * Delete this objects {@link ConfigurationSection ConfigurationSection} in the plugin's data YAML file,
	 * and all the references to this instance as well as any other objects that may linked.
	 */
	public void delete();
	
	/**
	 * update all of this objects properties in the plugin's data YAML file
	 */
	public void updateConfig();
	
}
