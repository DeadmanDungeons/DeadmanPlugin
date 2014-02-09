package org.deadmandungeons.deadmanplugin.command;

import org.bukkit.command.CommandSender;

public interface ArgumentConverter {
	
	public Object convertCommandArg(CommandSender sender, String argName, String arg);
	
}
