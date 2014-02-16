package org.deadmandungeons.deadmanplugin;

/**
 * This interface should be implemented by plugin Objects that are references to data that is
 * stored in the plugin's Data YAML file. Data such as sign locations and states is an example of such data
 * that might be referenced and maintained by a ConfigObject class.
 * @author Jon
 */
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
