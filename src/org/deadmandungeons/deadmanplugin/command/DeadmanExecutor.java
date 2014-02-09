package org.deadmandungeons.deadmanplugin.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.deadmandungeons.deadmanplugin.DeadmanPlugin;
import org.deadmandungeons.deadmanplugin.DeadmanUtils;

/**
 * The base CommandExecutor for Deadman plugins.<br />
 * When inherited, all commands need to be registered, and all ArgumentConverters must be put in the
 * converters HashMap. A default converter is already created for arguments of type: Integer, ChatColor,
 * Long, and Boolean. These can be overridden by supplying your own ArgumentConverter for that type
 * @author Jon
 */
public abstract class DeadmanExecutor implements CommandExecutor {
	
	/**
	 * The IllegalArgumentException message to be used when an invalid set of argument objects are provided when calling the execute method
	 */
	public static final String ARG_EXCEPTIION_MESSAGE = "This command must be executed with the corrent set of argument " 
			+ "objects defined in this class's CommandInfo annotation";
	
	private static final String VARIABLE_REGEX = "[<\\[][^>]+[>\\]]";
	private static final String OPT_VARIABLE_REGEX = "\\[[^>]+\\]";

	private Map<String, Command> commands = new HashMap<String, Command>();
	private Map<String, Command> pseudoCommands = new HashMap<String, Command>();
	
	protected Map<Class<?>, ArgumentConverter> converters = new HashMap<Class<?>, ArgumentConverter>();
	
	private DeadmanPlugin plugin;
	
	
	public DeadmanExecutor(DeadmanPlugin plugin) {
		this.plugin = plugin;
		registerCommands();
		
		/*
		 * Default argument type converters.
		 * Assuming that all arguments of these types should be converted this way. 
		 * These converters can be overridden by the implementing plugin
		 */
		converters.put(Integer.class, new ArgumentConverter() {
			@Override
			public Object convertCommandArg(CommandSender sender, String argName, String arg) {
				if (!DeadmanUtils.isInteger(arg)) {
					sender.sendMessage(ChatColor.RED + "'" + arg + "' is not an integer");
					return null;
				}
				return Integer.parseInt(arg);
			}
		});
		converters.put(ChatColor.class, new ArgumentConverter() {
			@Override
			public Object convertCommandArg(CommandSender sender, String argName, String arg) {
				ChatColor color = DeadmanUtils.getChatColor(arg.toUpperCase());
				if (color == null) {
					sender.sendMessage(ChatColor.RED + "'" + arg + "' is not a valid Minecraft Color");
				}
				return color;
			}
		});
		converters.put(Long.class, new ArgumentConverter() {
			@Override
			public Object convertCommandArg(CommandSender sender, String argName, String arg) {
				long duration = DeadmanUtils.getDuration(arg);
				if (duration == 0) {
					sender.sendMessage(ChatColor.RED + "The time duration match the format of #m:#h:#d and cannot be equal to zero minutes");
					return null;
				}
				return duration;
			}
		});
		converters.put(Boolean.class, new ArgumentConverter() {
			@Override
			public Object convertCommandArg(CommandSender sender, String argName, String arg) {
				if (!arg.equalsIgnoreCase("true") && !arg.equalsIgnoreCase("false")) {
					sender.sendMessage(ChatColor.RED + "'" + arg + "' is not a boolean. Argument must be either 'true' or 'false'");
					return null;
				}
				return Boolean.parseBoolean(arg);
			}
		});
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
        Command pseudoCommand = getPseudoCommand(args[0]);
        if (pseudoCommand != null && args.length == 1) {
        	if (pseudoCommand.execute(sender, new Object[0])) {
        		return true;
        	}
        }
        
        List<Command> matches = getMatchingCommands(args[0]);
        if (matches.size() > 1) {
            for (Command cmd : matches) {
            	plugin.getMessenger().sendCommandInfo(cmd, sender);
            }
            return false;
        }
        else if (matches.size() == 0) {
        	plugin.getMessenger().sendMessage(sender, "failed.invalid-args");
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
        ArgumentInfo[] executedArgs = getExecutedArgs(command, params);
        if (executedArgs == null) {
        	plugin.getMessenger().sendMessage(sender, "failed.invalid-args-alt");
        	plugin.getMessenger().sendCommandInfo(command, sender);
        	return false;
        }
        Object[] argumentObjects = convertArguments(sender, params, executedArgs);
        if (argumentObjects != null) {
        	return command.execute(sender, argumentObjects);
        }
        
        return false;
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
	
	private Command getPseudoCommand(String cmdName) {
		for (String name : pseudoCommands.keySet()) {
			if (name.equalsIgnoreCase(cmdName)) {
				return pseudoCommands.get(name);
			}
		}
		return null;
	}
	
	private ArgumentInfo[] getExecutedArgs(Command command, String[] args) {
		ArgumentInfo[] cmdArgs = null;
		SubCommandInfo[] subCommands = command.getClass().getAnnotation(CommandInfo.class).subCommands();
		
		//determine which command syntax the sender was using by matching the general syntax
		List<ArgumentInfo> matchingArgs = new ArrayList<ArgumentInfo>();
		for (int i=0; i<subCommands.length; i++) {
			ArgumentInfo[] arguments = subCommands[i].arguments();
			boolean isExecutedCmd = true;
			if (args.length <= arguments.length) {
				for (int n=0; n<arguments.length; n++) {
					//if the amount of given arguments is greater than the current index
					if (args.length > n) {
						//if the argType is a String, and the given argument isn't a variable. else if the argument is a variable
						if (arguments[n].argType().equals(String.class) && !arguments[n].argName().matches(VARIABLE_REGEX)) {
							if (arguments[n].argName().equalsIgnoreCase(args[n])) {
								matchingArgs.add(arguments[n]);
							} else {
								matchingArgs.clear();
								isExecutedCmd = false;
								break;
							}
						}
						else if (arguments[n].argName().matches(VARIABLE_REGEX)) {
							matchingArgs.add(arguments[n]);
						} else {
							matchingArgs.clear();
							isExecutedCmd = false;
							break;
						}
					} else {
						//if the argType is not an optional variable
						if (!arguments[n].argName().matches(OPT_VARIABLE_REGEX)) {
							matchingArgs.clear();
							isExecutedCmd = false;
							break;
						}
					}
				}
			}
			if (isExecutedCmd) {
				cmdArgs = matchingArgs.toArray(new ArgumentInfo[matchingArgs.size()]);
			}
		}
		return cmdArgs;
	}
	
	private Object[] convertArguments(CommandSender sender, String[] fromArgs, ArgumentInfo[] toArgs) {
		Object[] args = new Object[toArgs.length];
		if (fromArgs != null && toArgs != null && fromArgs.length == toArgs.length) {
			for (int i=0; i<toArgs.length; i++) {
				Object convertedArg = fromArgs[i];
				if (converters.containsKey(toArgs[i].argType())) {
					convertedArg = converters.get(toArgs[i].argType()).convertCommandArg(sender, toArgs[i].argName(), fromArgs[i]);
					if (convertedArg == null) {
						return null;
					}
				}
				else if (!toArgs[i].argType().equals(String.class)) {
					plugin.getLogger().warning("An ArgumentConverter was not found for arguments of type '" + toArgs[i].argType().getCanonicalName() + "'");
				}
				args[i] = convertedArg;
			}
		}
		
		return args;
	}
	
    /**
     * Register the given command
     * @param command - The class of the command that should be registered
     */
	protected void registerCommand(Class<? extends Command> command) {
		if (command == null) {
        	throw new IllegalArgumentException("command must not be null!");
        }
        CommandInfo info = command.getAnnotation(CommandInfo.class);
        if (info != null && info.name() != null && info.pattern() != null) {
        	try {
        		commands.put(info.pattern(), command.newInstance());
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        } else {
        	plugin.getLogger().log(Level.SEVERE, "The '" + command.getCanonicalName() + "' command must be annotated with the " 
        			+ "CommandInfo annotation, and the name, and pattern cannot be null. This command willl not be registered");
        }
    }
	
	/**
	 * A pseudo command is a command that will not be registered as a real command, but can be executed.
	 * Pseudo commands are no-arg commands, and will be executed as: /&lt;base&gt; &lt;pseudo-cmd-name&gt;<br />
	 * Where '&lt;base&gt;' is the main plugin command.<br />
	 * Register a pseudoCommand for commands like 'accept', 'cancel', and 'continue', so that these commands will not
	 * be shown in the plugin's help page. 
	 * @param cmdName - The String name and syntax for the PseudoCommand. This must not match any other PseudoCommand or
	 * regular command name.
	 * @param command - The Command object to be executed when this PseudoCommand is called
	 */
	protected void registerPseudoCommad(String cmdName, Command command) {
        if (cmdName == null) {
        	throw new IllegalArgumentException("cmdName must not be null!");
        }
        if (command == null) {
        	throw new IllegalArgumentException("command must not be null!");
        }
        if (getPseudoCommand(cmdName) == null) {
        	if (command.getClass().getAnnotation(CommandInfo.class) != null) {
            	plugin.getLogger().warning("The registered Pseudo Command named '" + cmdName + "' has the CommandInfo annotaion, " 
            			+ "but this annotation is useless as it will be ignored");
            }
        	pseudoCommands.put(cmdName, command);
        } else {
        	plugin.getLogger().warning("A Pseudo Command named '" + cmdName + "' (ignoring case) has already been registered! " 
        			+ "This Pseudo Command will not be registered");
        }
    }
    
	/**
	 * Any modification to the returned Map, will not have any effect on the registered commands. Use the
	 * {@link #register(Class command)} method to properly register a command.
	 * @return A copied HashMap containing all of the commands pattern by key, and the registered commands by value.
	 */
	public Map<String, Command> getCommands() {
		return new HashMap<String, Command>(commands);
	}
	
	/**
	 * Any modification to the returned Map, will not have any effect on the registered PseudoCommands. Use the
	 * {@link #registerPseudoCommad(String cmdName, Command command)} method to properly register a PseudoCommand.
	 * @return A copied HashMap containing all of the commands pattern by key, and the registered commands by value.
	 */
	public Map<String, Command> getPseudoCommands() {
		return new HashMap<String, Command>(pseudoCommands);
	}
	
	/**
	 * @param command - The class of the desired Command
	 * @return an instance of the command for the given Command class
	 */
	public Command getCommand(Class<? extends Command> command) {
		if (command == null) {
        	throw new IllegalArgumentException("command must not be null!");
        }
		CommandInfo info = command.getAnnotation(CommandInfo.class);
		return getCommands().get(info.pattern());
	}
	
	
	/**
	 * call {@link #register(Class command)} for each command that should be registered,
	 * and {@link #registerPseudoCommad(String cmdName, Command command)} for each PseudoCommand
	 * that should be registered.
	 */
	protected abstract void registerCommands();
	
	
}
