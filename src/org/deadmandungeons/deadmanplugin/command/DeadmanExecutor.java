package org.deadmandungeons.deadmanplugin.command;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.deadmandungeons.deadmanplugin.DeadmanPlugin;
import org.deadmandungeons.deadmanplugin.DeadmanUtils;
import org.deadmandungeons.deadmanplugin.Result;
import org.deadmandungeons.deadmanplugin.command.Arguments.SubCommand;

//TODO use Messenger but check if message exists and fallback on hardcoded messages
/**
 * The base CommandExecutor for Deadman plugins.<br />
 * Any {@link Command}, {@link PseudoCommand}, {@link ArgumentConverter}, and HelpInfo need to be registered using the
 * respective register method. A default converter is already created for arguments of type: Integer, ChatColor, Long, and Boolean.
 * These can be overridden by supplying your own ArgumentConverter for that type
 * @author Jon
 */
public class DeadmanExecutor implements CommandExecutor {
	
	private static final String NOT_INT = "'%s' is not an integer";
	private static final String NOT_CHATCOLOR = "'%s' is not a valid Minecraft Color";
	private static final String NOT_DURATION = "The time duration match the format of #m:#h:#d and cannot be equal to zero minutes";
	private static final String NOT_BOOLEAN = "'%s' is not a boolean. Argument must be either 'true' or 'false'";
	
	private final Map<Class<?>, CommandWrapper<?>> commands = new LinkedHashMap<Class<?>, CommandWrapper<?>>();
	private final Map<String, PseudoCommand> pseudoCommands = new HashMap<String, PseudoCommand>();
	private final Map<String, String> helpInfo = new HashMap<String, String>();
	private final Map<Class<?>, ArgumentConverter<?>> converters = new HashMap<Class<?>, ArgumentConverter<?>>();
	
	private final DeadmanPlugin plugin;
	private final PluginCommand bukkitCmd;
	private final Integer coolDown;
	
	public DeadmanExecutor(DeadmanPlugin plugin, String baseCmd) {
		this(plugin, baseCmd, null);
	}
	
	public DeadmanExecutor(DeadmanPlugin plugin, String baseCmd, Integer coolDown) {
		if (!plugin.isJavaPluginLoaded()) {
			throw new IllegalStateException("This plugin has not been loaded yet! Cannot construct DeadmanExecutor before plugin is loaded");
		}
		Validate.notNull(plugin, "plugin cannot be null");
		Validate.notNull(baseCmd, "baseCmd cannot be null");
		Validate.notNull(bukkitCmd = plugin.getCommand(baseCmd), "There is no configured command for the string '" + baseCmd + "'");
		
		this.plugin = plugin;
		this.coolDown = (coolDown != null && coolDown < 1 ? null : coolDown);
		bukkitCmd.setExecutor(this);
		
		/*
		 * Default argument type converters. Assuming that all arguments of
		 * these types should be converted this way. These converters can be
		 * overridden by the implementing plugin
		 */
		registerConverter(Integer.class, new ArgumentConverter<Integer>() {
			
			@Override
			public Result<Integer> convertCommandArg(String argName, String arg) {
				Integer num = (DeadmanUtils.isInteger(arg) ? Integer.parseInt(arg) : null);
				return num != null ? new Result<Integer>(num) : new Result<Integer>(String.format(NOT_INT, arg));
			}
		});
		registerConverter(ChatColor.class, new ArgumentConverter<ChatColor>() {
			
			@Override
			public Result<ChatColor> convertCommandArg(String argName, String arg) {
				ChatColor color = DeadmanUtils.getChatColor(arg.toUpperCase());
				return color != null ? new Result<ChatColor>(color) : new Result<ChatColor>(String.format(NOT_CHATCOLOR, arg));
			}
		});
		registerConverter(Long.class, new ArgumentConverter<Long>() {
			
			@Override
			public Result<Long> convertCommandArg(String argName, String arg) {
				Long duration = DeadmanUtils.getDuration(arg);
				return duration > 0 ? new Result<Long>(duration) : new Result<Long>(NOT_DURATION);
			}
		});
		registerConverter(Boolean.class, new ArgumentConverter<Boolean>() {
			
			@Override
			public Result<Boolean> convertCommandArg(String argName, String arg) {
				Boolean bool = (arg.equalsIgnoreCase("true") || arg.equalsIgnoreCase("false") ? Boolean.parseBoolean(arg) : null);
				return bool != null ? new Result<Boolean>(bool) : new Result<Boolean>(String.format(NOT_BOOLEAN, arg));
			}
		});
	}
	
	public final PluginCommand getBukkitCmd() {
		return bukkitCmd;
	}
	
	public final Integer getCoolDown() {
		return coolDown;
	}
	
	@Override
	public final boolean onCommand(CommandSender sender, org.bukkit.command.Command bukkitCmd, String label, String[] args) {
		if (args.length == 0) {
			plugin.getMessenger().sendPluginInfo(bukkitCmd, sender);
			return true;
		}
		if (args[0].equals("?") || args[0].equalsIgnoreCase("help")) {
			if (args.length == 2) {
				if (StringUtils.isNumeric(args[1])) {
					plugin.getMessenger().sendHelpInfo(bukkitCmd, sender, commands, Integer.parseInt(args[1]));
					return true;
				} else if (helpInfo.containsKey(args[1].toLowerCase())) {
					plugin.getMessenger().sendMessage(sender, helpInfo.get(args[1].toLowerCase()));
					return true;
				}
			} else {
				plugin.getMessenger().sendHelpInfo(bukkitCmd, sender, commands, 1);
				return true;
			}
		}
		
		PseudoCommand pseudoCommand = getPseudoCommand(args[0]);
		if (pseudoCommand != null && args.length == 1) {
			if (pseudoCommand.execute(sender)) {
				return true;
			}
		}
		
		if (coolDown != null && sender instanceof Player && !((Player) sender).isOp()) {
			Player player = (Player) sender;
			String metadataKey = plugin.getName() + "-cmd-cooldown";
			Long timestamp = DeadmanUtils.getMetadata(plugin, player, metadataKey, Long.class);
			if (timestamp != null) {
				long timeLeft = ((timestamp) + (coolDown * 1000)) - System.currentTimeMillis();
				if (timeLeft > 0) {
					int secondsLeft = (int) Math.ceil(timeLeft / 1000);
					player.sendMessage(ChatColor.RED + "You can execute this command in " + secondsLeft + " seconds");
					return false;
				}
			}
			
			player.setMetadata(metadataKey, new FixedMetadataValue(plugin, System.currentTimeMillis()));
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
		if (cmdWrapper.info.inGameOnly() && !(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "This command can only be used in game.");
			return false;
		}
		
		if (args[args.length - 1].equals("?") || args[args.length - 1].equals("help")) {
			plugin.getMessenger().sendCommandInfo(bukkitCmd, cmdWrapper.info, sender);
			return true;
		}
		
		String[] params = Arrays.copyOfRange(args, 1, args.length);
		SubCommand subCmd = Arguments.matcher(this).forCommand(cmdWrapper).withStringArgs(params).findMatch();
		if (subCmd == null) {
			plugin.getMessenger().sendMessage(sender, "failed.invalid-args-alt");
			plugin.getMessenger().sendCommandUsage(bukkitCmd, cmdWrapper.info, sender);
			return false;
		}
		if (subCmd.info() != null) {
			if (subCmd.info().inGameOnly() && !(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "This command can only be used in game.");
				return false;
			}
			if (!hasCommandPerm(sender, subCmd.info().permissions())) {
				plugin.getMessenger().sendMessage(sender, "failed.no-permission");
				return false;
			}
		}
		
		Result<Arguments> conversionResult = subCmd.convert();
		if (conversionResult.isError()) {
			sender.sendMessage(ChatColor.RED + conversionResult.getErrorMessage());
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
	 * Register the given command.<br>
	 * This method will construct a new instance of the given Command class by calling<br>
	 * {@link java.lang.Class#newInstance() Class#newInstance()}. Thus the Command class must have a no-arg constructor.
	 * If there is not a public no-arg constructor, then register the Command using {@link #registerCommand(Command)}.
	 * @param command - The class of the command that should be registered
	 */
	public final <T extends Command> void registerCommand(Class<T> commandClass) {
		Validate.notNull(commandClass);
		CommandInfo info = getCommandInfo(commandClass);
		if (info != null) {
			try {
				commands.put(commandClass, new CommandWrapper<T>(info, commandClass.newInstance()));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Register the given command.<br>
	 * Use this register method over {@link #registerCommand(Class)} when the
	 * Command class does not have a public no-arg constructor.
	 * @param command - An instance of the command that should be registered
	 */
	public final <T extends Command> void registerCommand(T command) {
		Validate.notNull(command);
		CommandInfo info = getCommandInfo(command.getClass());
		if (info != null) {
			commands.put(command.getClass(), new CommandWrapper<T>(info, command));
		}
	}
	
	private <T extends Command> CommandInfo getCommandInfo(Class<T> commandClass) {
		CommandInfo info = commandClass.getAnnotation(CommandInfo.class);
		if (info == null || info.name() == null || info.pattern() == null) {
			String msg = "The '%s' command must be annotated with the CommandInfo annotation,"
					+ " and the name, and pattern cannot be null. This command will not be registered";
			plugin.getLogger().log(Level.SEVERE, String.format(msg, commandClass.getCanonicalName()));
		}
		return info;
	}
	
	/**
	 * @param command - The class of the desired Command
	 * @return an instance of the registered Command
	 * @throws IllegalStateException if the command has not been registered.
	 */
	public final <T extends Command> T getCommand(Class<T> command) {
		return getCommandWrapper(command).getCmd();
	}
	
	/**
	 * @param command - The class of the desired Command
	 * @return a {@link CommandWrapper} containing an instance of the registered Command and the commands CommandInfo annotation
	 * @throws IllegalStateException if the command has not been registered.
	 */
	@SuppressWarnings("unchecked")
	public final <T extends Command> CommandWrapper<T> getCommandWrapper(Class<T> command) throws IllegalStateException {
		Validate.notNull(command);
		if (!commands.containsKey(command)) {
			throw new IllegalArgumentException("A command for type '" + command.getCanonicalName() + "' has not been registered!");
		}
		return (CommandWrapper<T>) commands.get(command);
	}
	
	/**
	 * Any modification to the returned Map, will not have any effect on the registered commands.
	 * Use the {@link #register(Class command)} method to properly register a command.
	 * @return A new HashMap containing all of the registered commands, with the
	 * commands class by key, and the {@link CommandWrapper} by value.
	 */
	public final Map<Class<?>, CommandWrapper<?>> getCommands() {
		return new HashMap<Class<?>, CommandWrapper<?>>(commands);
	}
	
	/**
	 * A pseudo command is a command that will not be registered as a real command, but can be executed.
	 * Pseudo commands are no-arg commands, and will be executed as: /&lt;prefix&gt; &lt;pseudo-cmd-name&gt;<br />
	 * Where '&lt;prefix&gt;' is the main plugin command.<br />
	 * Register a pseudoCommand for commands
	 * like 'accept', 'cancel', and 'continue', so that these commands will not be shown in the plugin's help page.
	 * @param cmdName - The String name and syntax for the PseudoCommand. This must not match any other PseudoCommand or regular command name.
	 * @param pseudoCommand - The PseudoCommand object to be executed when this PseudoCommand is called
	 */
	public final void registerPseudoCommad(String cmdName, PseudoCommand pseudoCommand) {
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
	 * Any modification to the returned Map, will not have any effect on the registered PseudoCommands.
	 * Use the {@link #registerPseudoCommad(String cmdName, Command command)} method to properly register a PseudoCommand.
	 * @return A copied HashMap containing all of the commands pattern by key, and the registered commands by value.
	 */
	public final Map<String, PseudoCommand> getPseudoCommands() {
		return new HashMap<String, PseudoCommand>(pseudoCommands);
	}
	
	/**
	 * @param converter - The ArgumentConverter to register
	 */
	public final <T> void registerConverter(Class<? super T> type, ArgumentConverter<T> converter) {
		Validate.notNull(type);
		Validate.notNull(converter);
		
		converters.put(type, converter);
	}
	
	/**
	 * @param type - The type of converter
	 * @return the registered {@link ArgumentConverter} for the given type or
	 * null if an ArgumentConverter was not registered for the given
	 * type.
	 */
	@SuppressWarnings("unchecked")
	public final <T> ArgumentConverter<T> getConverter(Class<T> type) {
		return (ArgumentConverter<T>) converters.get(type);
	}
	
	/**
	 * Help Info is a message that will be sent to a CommandSender when the help
	 * command with a registered help info argument is executed. The syntax of a
	 * help info command is <br>
	 * <code>/&lt;base&gt; &lt;? or help&gt; &lt;help-arg&gt;<code><br>
	 * Example:<br>
	 * <code>/dd help plugin</code>
	 * @param helpArg - The help info argument
	 * @param messagePath - The path the the help info message in the plugins lang file
	 */
	public final void registerHelpInfo(String helpArg, String messagePath) {
		Validate.notNull(helpArg);
		Validate.notNull(messagePath);
		String arg = helpArg.trim().toLowerCase();
		if (StringUtils.isNumeric(arg)) {
			throw new IllegalArgumentException("helpArg must not be a number");
		}
		
		helpInfo.put(arg, messagePath);
	}
	
	public final <C extends Command> void sendCommandInfo(CommandSender sender, Class<C> cmd) {
		CommandInfo info = getCommandWrapper(cmd).info;
		plugin.getMessenger().sendCommandInfo(bukkitCmd, info, sender);
	}
	
	public final <C extends Command> void sendCommandUsage(CommandSender sender, Class<C> cmd) {
		CommandInfo info = getCommandWrapper(cmd).info;
		plugin.getMessenger().sendCommandUsage(bukkitCmd, info, sender);
	}
	
	/**
	 * @param sender - The CommandSender to check for permission
	 * @param perms - An array of permission nodes to check against the player
	 * @return true if the sender has at least 1 of the permission nodes, and
	 * false if they have none
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
	 * This wrapper class is used to combine a registered {@link Command} with
	 * its cached CommandInfo annotation
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
	
}
