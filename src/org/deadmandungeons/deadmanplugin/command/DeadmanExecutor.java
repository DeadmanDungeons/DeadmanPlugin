package org.deadmandungeons.deadmanplugin.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.deadmandungeons.deadmanplugin.DeadmanPlugin;

public abstract class DeadmanExecutor implements CommandExecutor {

	private static Map<String, Command> commands = new HashMap<String, Command>();
	
	private DeadmanPlugin plugin;
	
	public DeadmanExecutor(DeadmanPlugin plugin) {
		this.plugin = plugin;
		registerCommands();
	}
	
	
	@Override
	public boolean onCommand(CommandSender sender, org.bukkit.command.Command bukkitCommand, String label, String[] args) {
		if (args.length == 0) {
			plugin.getMessenger().sendPluginInfo(sender);
            return true;
        }
        if (args[0].equals("?") || args[0].equalsIgnoreCase("help")) {
        	plugin.getMessenger().sendHelpInfo(sender, commands);
            return true;
        } 
        
        List<Command> matches = getMatchingCommands(args[0]);
        if (matches.size() > 1) {
            for (Command cmd : matches) {
            	plugin.getMessenger().sendCommandInfo(cmd, sender);
            }
            return false;
        }
        else if (matches.size() == 0) {
        	plugin.getMessenger().sendMessage(sender, "failed.invalid-arguments");
            return false;
        }
        
        Command command = matches.get(0);
        CommandInfo info = command.getClass().getAnnotation(CommandInfo.class);
        
        if (!sender.hasPermission(info.permission())) {
        	plugin.getMessenger().sendMessage(sender, "failed.no-permission");
            return false;
        }
        if (info.inGameOnly()) {
        	if (sender instanceof Player == false) {
    			sender.sendMessage(ChatColor.RED + "This command can only be used in game.");
    			return false;
    		}
        }
        
        if (args[args.length-1].equals("?") || args[args.length-1].equals("help")) {
        	plugin.getMessenger().sendCommandInfo(command, sender);
        	return true;
        }
        
        String[] params = Arrays.copyOfRange(args, 1, args.length);
        Object[] argumentObjects = getArgumentObjects(sender, command, params);
        if (argumentObjects != null) {
        	return command.execute(sender, argumentObjects);
        }
		
		return false;
	}
	
	
	protected abstract void registerCommands();
	
	protected abstract Object[] getArgumentObjects(CommandSender sender, Command command, String[] args);
	
    
	protected void register(Class<? extends Command> command) {
        CommandInfo info = command.getAnnotation(CommandInfo.class);
        if (info != null) {
        	try {
        		commands.put(info.pattern(), command.newInstance());
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    private List<Command> getMatchingCommands(String arg) {
        List<Command> result = new ArrayList<Command>();
        
        for (Entry<String,Command> entry : commands.entrySet()) {
            if (arg.matches(entry.getKey())) {
                result.add(entry.getValue());
            }
        }
        
        return result;
    }
    
	public static Map<String, Command> getCommands() {
		return commands;
	}
	
	public static Command getCommand(Class<? extends Command> command) {
		CommandInfo info = command.getAnnotation(CommandInfo.class);
		return DeadmanExecutor.getCommands().get(info.pattern());
	}
	
	
}
