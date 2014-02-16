package org.deadmandungeons.deadmanplugin;

public interface ConfigObject {
	
	/**
	 * @return the String key used to map this object
	 */
	public String getKey();
	
	/**
	 * Delete all the references to this instance as well as any other objects that may linked,
	 * and update all of this objects properties in the plugin's data YAML file by calling {@link update}
	 */
	public void delete();
	
	/**
	 * update all of this objects properties in the plugin's data YAML file
	 */
	public void updateConfig();
	
}
