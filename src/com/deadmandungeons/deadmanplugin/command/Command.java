package com.deadmandungeons.deadmanplugin.command;

import org.bukkit.command.CommandSender;

/**
 * This interface defines a Command. Each Command must be registered through the plugin's {@link DeadmanExecutor} instance.
 * A standard Command must also define a {@link CommandInfo} annotation which tells the executor the requirements of how
 * the command should be executed. The {@link SubCommandInfo} annotation (a member of the CommandInfo annotation) defines the
 * separate sub commands and the required arguments for each.
 * @author Jon
 */
public interface Command {
	
	/**
	 * Execute a command as the given {@link CommandSender}. Each command has a specific
	 * set of argument Objects defined in the classes {@link CommandInfo} annotation. The
	 * provided Arguments parameter is guaranteed to have a valid set of argument objects
	 * (if the parameter is not null) based on the defined CommandInfo.
	 * @param sender - the CommandSender executing the command
	 * @param Arguments - An {@link Arguments} object containing a valid set of arguments based on the
	 * defined {@link CommandInfo}, and the index of the matching {@link SubCommandInfo} based on the array of arguments.
	 * If this Command does not define any SubCommandInfo, this will be null as there are no arguments to the command.
	 * @return true if the command was executed successfully, and false otherwise.
	 */
	public boolean execute(CommandSender sender, Arguments args);
	
}
