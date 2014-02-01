package org.deadmandungeons.deadmanplugin;

import org.bukkit.plugin.java.JavaPlugin;

public abstract class DeadmanPlugin extends JavaPlugin {

	@Override
	public abstract void onLoad();
	
	@Override
	public abstract void onEnable();
	
	@Override
	public abstract void onDisable();
	
	public abstract DeadmanPlugin getInstance();
	
	public abstract PluginFile getConfigFile();
	
	public abstract PluginFile getLangFile();
	
	
	
}
