package org.deadmandungeons.deadmanplugin;

import org.bukkit.command.CommandSender;


public interface Command {
	
	/**
	 * Execute a RealEstateMarket command as the given sender. Each command has a specific 
	 * set of Objects that must be passed as arguments to execute the command successfully. 
	 * @param plugin - an instance of the main class
	 * @param sender - the CommandSender executing the command
	 * @param args - An array of objects to be passed as arguments for the command
	 * @return true if the command was executed successfully, and false otherwise. Will also 
	 *         return false if an invalid set of argument objects was passed.
	 */
	public boolean execute(CommandSender sender, Object... args);
	  
}
