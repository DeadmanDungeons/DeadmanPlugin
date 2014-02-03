package org.deadmandungeons.deadmanplugin;

import org.bukkit.plugin.java.JavaPlugin;

public abstract class DeadmanPlugin extends JavaPlugin {

	
	@Override
	public abstract void onEnable();
	
	@Override
	public abstract void onDisable();
	
	public abstract PluginFile getConfigFile();
	
	public abstract PluginFile getLangFile();
	
	public abstract Messenger getMessenger();
	
	
}
