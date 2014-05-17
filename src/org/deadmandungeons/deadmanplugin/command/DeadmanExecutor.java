package org.deadmandungeons.deadmanplugin.command;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.deadmandungeons.deadmanplugin.DeadmanPlugin;
import org.deadmandungeons.deadmanplugin.DeadmanUtils;
import org.deadmandungeons.deadmanplugin.Result;
import org.deadmandungeons.deadmanplugin.command.Arguments.SubCommand;

/**
 * The base CommandExecutor for Deadman plugins.<br />
 * When inherited, all commands need to be registered, and all ArgumentConverters must be put in the
 * converters HashMap. A default converter is already created for arguments of type: Integer, ChatColor,
 * Long, and Boolean. These can be overridden by supplying your own ArgumentConverter for that type
 * @author Jon
 */
public abstract class DeadmanExecutor implements CommandExecutor {
	
	private static final String NOT_INT = "'%s' is not an integer";
	private static final String NOT_CHATCOLOR = "'%s' is not a valid Minecraft Color";
	private static final String NOT_DURATION = "The time duration match the format of #m:#h:#d and cannot be equal to zero minutes";
	private static final String NOT_BOOLEAN = "'%s' is not a boolean. Argument must be either 'true' or 'false'";
	
	private Map<Class<?>, CommandWrapper<?>> commands = new HashMap<Class<?>, CommandWrapper<?>>();
	private Map<String, PseudoCommand> pseudoCommands = new HashMap<String, PseudoCommand>();
	private Map<String, String> helpInfo = new HashMap<String, String>();
	
	private Map<Class<?>, ArgumentConverter<?>> converters = new HashMap<Class<?>, ArgumentConverter<?>>();
	
	private DeadmanPlugin plugin;
	
	public DeadmanExecutor(DeadmanPlugin plugin) {
		this.plugin = plugin;
		registerCommands();
		
		/*
		 * Default argument type converters.
		 * Assuming that all arguments of these types should be converted this way.
		 * These converters can be overridden by the implementing plugin
		 */
		registerConverter(Integer.class, new ArgumentConverter<Integer>() {
			
			@Override
			public Result<Integer> convertCommandArg(String argName, String arg) {
				Integer num = (DeadmanUtils.isInteger(arg) ? Integer.parseInt(arg) : null);
				return num != null ? new Result<Integer>(num, null) : new Result<Integer>(null, String.format(NOT_INT, arg));
			}
		});
		registerConverter(ChatColor.class, new ArgumentConverter<ChatColor>() {
			
			@Override
			public Result<ChatColor> convertCommandArg(String argName, String arg) {
				ChatColor color = DeadmanUtils.getChatColor(arg.toUpperCase());
				return new Result<ChatColor>(color, (color == null ? String.format(NOT_CHATCOLOR, arg) : null));
			}
		});
		registerConverter(Long.class, new ArgumentConverter<Long>() {
			
			@Override
			public Result<Long> convertCommandArg(String argName, String arg) {
				Long duration = DeadmanUtils.getDuration(arg);
				if (duration == 0) {
					duration = null;
				}
				return new Result<Long>(duration, (duration == null ? NOT_DURATION : null));
			}
		});
		registerConverter(Boolean.class, new ArgumentConverter<Boolean>() {
			
			@Override
			public Result<Boolean> convertCommandArg(String argName, String arg) {
				Boolean bool = (arg.equalsIgnoreCase("true") && !arg.equalsIgnoreCase("false") ? Boolean.parseBoolean(arg) : null);
				return new Result<Boolean>(bool, (bool == null ? String.format(NOT_BOOLEAN, arg) : null));
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
			if (args.length == 2) {
				if (StringUtils.isNumeric(args[1])) {
					plugin.getMessenger().sendHelpInfo(sender, commands, Integer.parseInt(args[1]));
					return true;
				} else if (helpInfo.containsKey(args[1].toLowerCase())) {
					plugin.getMessenger().sendMessage(sender, helpInfo.get(args[1].toLowerCase()));
					return true;
				}
			} else {
				plugin.getMessenger().sendHelpInfo(sender, commands, 1);
				return true;
			}
		}
		PseudoCommand pseudoCommand = getPseudoCommand(args[0]);
		if (pseudoCommand != null && args.length == 1) {
			if (pseudoCommand.execute(sender)) {
				return true;
			}
		}
		
		CommandWrapper<?> cmdWrapper = getMatchingCommand(args[0]);
		if (cmdWrapper == null) {
			plugin.getMessenger().sendMessage(sender, "failed.invalid-args");
			return false;
		}
		
		if (cmdWrapper.info.permissions().length > 0 && !hasCommandPerm(sender, cmdWrapper.info.permissions())) {
			plugin.getMessenger().sendMessage(sender, "failed.no-permission");
			return false;
		}
		if (cmdWrapper.info.inGameOnly()) {
			if (sender instanceof Player == false) {
				sender.sendMessage(ChatColor.RED + "This command can only be used in game.");
				return false;
			}
		}
		
		if (args[args.length - 1].equals("?") || args[args.length - 1].equals("help")) {
			plugin.getMessenger().sendCommandInfo(cmdWrapper.info, sender);
			return true;
		}
		
		String[] params = Arrays.copyOfRange(args, 1, args.length);
		SubCommand subCmd = new Arguments.Matcher(this).forCommand(cmdWrapper).withStringArgs(params).findMatch();
		if (subCmd == null) {
			plugin.getMessenger().sendMessage(sender, "failed.invalid-args-alt");
			plugin.getMessenger().sendCommandUsage(cmdWrapper.info, sender);
			return false;
		}
		if (subCmd.info() != null && !hasCommandPerm(sender, subCmd.info().permissions())) {
			plugin.getMessenger().sendMessage(sender, "failed.no-permission");
			return false;
		}
		
		Result<Arguments> conversionResult = new Arguments.Converter().forSubCommand(subCmd).convert();
		if (conversionResult.getErrorMessge() != null) {
			sender.sendMessage(ChatColor.RED + conversionResult.getErrorMessge());
			return false;
		}
		
		return cmdWrapper.cmd.execute(sender, conversionResult.getResult());
	}
	
	private CommandWrapper<?> getMatchingCommand(String arg) {
		for (CommandWrapper<?> cmdWrapper : commands.values()) {
			if (arg.matches(cmdWrapper.info.pattern())) {
				return cmdWrapper;
			}
		}
		
		return null;
	}
	
	private PseudoCommand getPseudoCommand(String cmdName) {
		for (String name : pseudoCommands.keySet()) {
			if (name.equalsIgnoreCase(cmdName)) {
				return pseudoCommands.get(name);
			}
		}
		return null;
	}
	
	/**
	 * Register the given command
	 * @param command - The class of the command that should be registered
	 */
	protected <T extends Command> void registerCommand(Class<T> command) {
		Validate.notNull(command);
		CommandInfo info = command.getAnnotation(CommandInfo.class);
		if (info != null && info.name() != null && info.pattern() != null) {
			try {
				commands.put(command, new CommandWrapper<T>(info, command.newInstance()));
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			String msg = "The '" + command.getCanonicalName() + "' command must be annotated with the CommandInfo annotation,"
					+ " and the name, and pattern cannot be null. This command willl not be registered";
			plugin.getLogger().log(Level.SEVERE, msg);
		}
	}
	
	/**
	 * @param command - The class of the desired Command
	 * @return an instance of the registered Command
	 * @throws IllegalStateException if the command has not been registered.
	 */
	public <T extends Command> T getCommand(Class<T> command) {
		return getCommandWrapper(command).getCmd();
	}
	
	/**
	 * @param command - The class of the desired Command
	 * @return a {@link CommandWrapper} containing an instance of the registered Command and the commands CommandInfo annotation
	 * @throws IllegalStateException if the command has not been registered.
	 */
	@SuppressWarnings("unchecked")
	public <T extends Command> CommandWrapper<T> getCommandWrapper(Class<T> command) throws IllegalStateException {
		Validate.notNull(command);
		if (!commands.containsKey(command)) {
			throw new IllegalStateException("A command for type '" + command.getCanonicalName() + "' has not been registered!");
		}
		return (CommandWrapper<T>) commands.get(command);
	}
	
	/**
	 * Any modification to the returned Map, will not have any effect on the registered commands.
	 * Use the {@link #register(Class command)} method to properly register a command.
	 * @return A new HashMap containing all of the registered commands, with the commands class by key,
	 * and the {@link CommandWrapper} by value.
	 */
	public Map<Class<?>, CommandWrapper<?>> getCommands() {
		return new HashMap<Class<?>, CommandWrapper<?>>(commands);
	}
	
	/**
	 * A pseudo command is a command that will not be registered as a real command, but can be executed.
	 * Pseudo commands are no-arg commands, and will be executed as: /&lt;prefix&gt; &lt;pseudo-cmd-name&gt;<br />
	 * Where '&lt;prefix&gt;' is the main plugin command.<br />
	 * Register a pseudoCommand for commands like 'accept', 'cancel', and 'continue', so that these commands will not
	 * be shown in the plugin's help page.
	 * @param cmdName - The String name and syntax for the PseudoCommand. This must not match any other PseudoCommand or
	 * regular command name.
	 * @param pseudoCommand - The PseudoCommand object to be executed when this PseudoCommand is called
	 */
	protected void registerPseudoCommad(String cmdName, PseudoCommand pseudoCommand) {
		Validate.notNull(cmdName);
		Validate.notNull(pseudoCommand);
		
		if (getPseudoCommand(cmdName) == null) {
			if (pseudoCommand.getClass().getAnnotation(CommandInfo.class) != null) {
				String msg = "The registered Pseudo Command named '" + cmdName + "' has the CommandInfo annotaion, "
						+ "but this annotation is useless as it will be ignored";
				plugin.getLogger().warning(msg);
			}
			pseudoCommands.put(cmdName, pseudoCommand);
		} else {
			String msg = "A Pseudo Command named '" + cmdName + "' (ignoring case) has already been registered! "
					+ "This Pseudo Command will not be registered";
			plugin.getLogger().warning(msg);
		}
	}
	
	/**
	 * Any modification to the returned Map, will not have any effect on the registered PseudoCommands. Use the
	 * {@link #registerPseudoCommad(String cmdName, Command command)} method to properly register a PseudoCommand.
	 * @return A copied HashMap containing all of the commands pattern by key, and the registered commands by value.
	 */
	public Map<String, PseudoCommand> getPseudoCommands() {
		return new HashMap<String, PseudoCommand>(pseudoCommands);
	}
	
	/**
	 * @param converter - The ArgumentConverter to register
	 */
	public void registerConverter(Class<?> type, ArgumentConverter<?> converter) {
		Validate.notNull(type);
		Validate.notNull(converter);
		
		converters.put(type, converter);
	}
	
	/**
	 * @param type - The type of converter
	 * @return the registered {@link ArgumentConverter} for the given type
	 * or null if an ArgumentConverter was not registered for the given type.
	 */
	public ArgumentConverter<?> getConverter(Class<?> type) {
		return converters.get(type);
	}
	
	/**
	 * Help Info is a message that will be sent to a CommandSender when the help command with a
	 * registered help info argument is executed. The syntax of a help info command is <br>
	 * <code>/&lt;base&gt; &lt;? or help&gt; &lt;help-arg&gt;<code><br>
	 * Example:<br>
	 * <code>/dd help plugin</code>
	 * @param helpArg - The help info argument
	 * @param messagePath - The path the the help info message in the plugins lang file
	 */
	public void registerHelpInfo(String helpArg, String messagePath) {
		Validate.notNull(helpArg);
		Validate.notNull(messagePath);
		String arg = helpArg.trim().toLowerCase();
		if (StringUtils.isNumeric(arg)) {
			throw new IllegalArgumentException("helpArg must not be a number");
		}
		
		helpInfo.put(arg, messagePath);
	}
	
	/**
	 * @param sender - The CommandSender to check for permission
	 * @param perms - An array of permission nodes to check against the player
	 * @return true if the sender has at least 1 of the permission nodes, and false if they have none
	 */
	public static boolean hasCommandPerm(CommandSender sender, String[] perms) {
		if (perms.length == 0) {
			return true;
		}
		for (String perm : perms) {
			if (sender.hasPermission(perm)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * This wrapper class is used to combine a registered {@link Command} with its cached CommandInfo annotation
	 * @param <T> - The Command that this CommandWrapper contains
	 * @author Jon
	 */
	public static final class CommandWrapper<T extends Command> {
		
		private final CommandInfo info;
		private final T cmd;
		
		private CommandWrapper(CommandInfo info, T cmd) {
			this.info = info;
			this.cmd = cmd;
		}
		
		public CommandInfo getInfo() {
			return info;
		}
		
		public T getCmd() {
			return cmd;
		}
	}
	
	/**
	 * call {@link #register(Class command)} for each command that should be registered,
	 * and {@link #registerPseudoCommad(String cmdName, Command command)} for each PseudoCommand
	 * that should be registered. This method will be invoked when the DeadmanExecutor is constructed.
	 */
	protected abstract void registerCommands();
	
}
