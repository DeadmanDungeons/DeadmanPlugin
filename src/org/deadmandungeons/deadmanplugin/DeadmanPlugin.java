package org.deadmandungeons.deadmanplugin;

import java.io.File;
import java.util.logging.Level;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class DeadmanPlugin extends JavaPlugin {

	public static final String LANG_DIRECTORY = "lang" + File.separator;
	public static final String DATA_DIRECTORY = "data" + File.separator;
	
	private Economy economy;
	private Permission permissions;
	
	@Override
	public abstract void onEnable();
	
	@Override
	public abstract void onDisable();
	
	public abstract PluginFile getConfigFile();
	
	public abstract PluginFile getLangFile();
	
	public abstract Messenger getMessenger();
	
	public boolean setupEconomy() {
		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			getLogger().log(Level.SEVERE, "Vault is not enabled on this server and may be required!");
			return false;
		}
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
		}
		return (economy != null);
	}
	
	public boolean setupPermissions() {
		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			getLogger().log(Level.SEVERE, "Vault is not enabled on this server and may be required!");
			return false;
		}
        RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
        	permissions = permissionProvider.getProvider();
        }
        return (permissions != null);
    }
	
	public Economy getEconomy() {
		return economy;
	}
	
	public Permission getPermissions() {
		return permissions;
	}
	
	
}
