package org.deadmandungeons.deadmanplugin;

import java.io.File;
import java.util.logging.Level;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public abstract class DeadmanPlugin extends JavaPlugin {
	
	public static final String LANG_DIRECTORY = "lang" + File.separator;
	public static final String DATA_DIRECTORY = "data" + File.separator;
	
	private Economy economy;
	private Permission permissions;
	private WorldGuardPlugin worldGuard;
	private WorldEditPlugin worldEdit;
	
	private boolean loaded;
	
	@Override
	public final void onLoad() {
		loaded = true;
		onPluginLoad();
	}
	
	public void onPluginLoad() {}
	
	@Override
	public abstract void onEnable();
	
	@Override
	public abstract void onDisable();
	
	public abstract PluginFile getLangFile();
	
	public abstract Messenger getMessenger();
	
	public final boolean setupEconomy() {
		if (!getServer().getPluginManager().isPluginEnabled("Vault")) {
			getLogger().log(Level.SEVERE, "Vault is not enabled on this server and is a required dependendy!");
			return false;
		}
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
		}
		return (economy != null);
	}
	
	public final boolean setupPermissions() {
		if (!getServer().getPluginManager().isPluginEnabled("Vault")) {
			getLogger().log(Level.SEVERE, "Vault is not enabled on this server and is a required dependendy!");
			return false;
		}
		RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(Permission.class);
		if (permissionProvider != null) {
			permissions = permissionProvider.getProvider();
		}
		return (permissions != null);
	}
	
	public final boolean setupWorldGuard() {
		Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");
		if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
			getLogger().log(Level.SEVERE, "WorldGuard is not enabled on this server and is a required dependendy!");
			return false;
		}
		worldGuard = (WorldGuardPlugin) plugin;
		return true;
	}
	
	public final boolean setupWorldEdit() {
		Plugin plugin = getServer().getPluginManager().getPlugin("WorldEdit");
		if (plugin == null || !(plugin instanceof WorldEditPlugin)) {
			getLogger().log(Level.SEVERE, "WorldEdit is not enabled on this server and is a required dependendy!");
			return false;
		}
		worldEdit = (WorldEditPlugin) plugin;
		return true;
	}
	
	public final Economy getEconomy() {
		return economy;
	}
	
	public final Permission getPermissions() {
		return permissions;
	}
	
	public final WorldGuardPlugin getWorldGuard() {
		return worldGuard;
	}
	
	public final WorldEditPlugin getWorldEdit() {
		return worldEdit;
	}
	
	public final boolean isJavaPluginLoaded() {
		return loaded;
	}
	
}
