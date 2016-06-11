package com.deadmandungeons.deadmanplugin.command;

import org.bukkit.command.CommandSender;

/**
 * A PseudoCommand is a no-arg command that is not executed as a normal command.
 * Thus, the CommandInfo annotation is not needed because a Pseudo command is not a
 * standard registered Command. <br />
 * Examples of a PseudoCommand are:
 * <ul>
 * <li>/&lt;prefix&gt; accept</li>
 * <li>/&lt;prefix&gt; cancel</li>
 * </ul>
 * @author Jon
 */
public interface PseudoCommand {
	
	/**
	 * Execute a PseudoCommand as the given {@link CommandSender}.
	 * @param sender - the CommandSender executing the command
	 * @return true if the command was executed successfully, and false otherwise.
	 */
	public boolean execute(CommandSender sender);
}
