package com.deadmandungeons.deadmanplugin.command;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.EventExecutor;

import com.deadmandungeons.deadmanplugin.DeadmanPlugin;
import com.deadmandungeons.deadmanplugin.DeadmanUtils;
import com.deadmandungeons.deadmanplugin.Messenger;
import com.deadmandungeons.deadmanplugin.Result;
import com.deadmandungeons.deadmanplugin.command.Arguments.SubCommand;
import com.deadmandungeons.deadmanplugin.command.CommandInfo.CommandInfoImpl;
import com.google.common.primitives.Ints;

// TODO use Messenger but check if message exists and fallback on hardcoded messages
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
	private static final String NOT_DURATION = "%s must match the format of #d:#h:#m and cannot be equal to zero";
	private static final String NOT_BOOLEAN = "'%s' is not a boolean. %s must be either 'true' or 'false'";
	
	private final Map<Class<?>, CommandWrapper<?>> commands = new LinkedHashMap<Class<?>, CommandWrapper<?>>();
	private final Map<Class<?>, ConfirmationCommand<?>> confirmationCommands = new HashMap<Class<?>, ConfirmationCommand<?>>();
	private final Map<String, PseudoCommand> pseudoCommands = new HashMap<String, PseudoCommand>();
	private final Map<String, String> helpInfo = new HashMap<String, String>();
	private final Map<Class<?>, ArgumentConverter<?>> converters = new HashMap<Class<?>, ArgumentConverter<?>>();
	
	private final DeadmanPlugin plugin;
	private final Messenger messenger;
	private final Integer coolDown;
	
	/**
	 * Construct a DeadmanExecutor with no cooldown time
	 * @param plugin - The {@link DeadmanPlugin} this executor is for
	 * @param messenger - The {@link Messenger} to use
	 * @throws IllegalStateException if the given DeadmanPlugin has not been enabled yet
	 * @throws IllegalArgumentException if plugin or messenger is null
	 */
	public DeadmanExecutor(DeadmanPlugin plugin, Messenger messenger) {
		this(plugin, messenger, null);
	}
	
	/**
	 * @param plugin - The {@link DeadmanPlugin} this executor is for
	 * @param messenger - The {@link Messenger} to use
	 * @param coolDown - The cooldown time in seconds that a player must wait before sending another command
	 * @throws IllegalStateException if the given DeadmanPlugin has not been enabled yet
	 * @throws IllegalArgumentException if plugin or messenger is null
	 */
	public DeadmanExecutor(DeadmanPlugin plugin, Messenger messenger, Integer coolDown) {
		if (!plugin.isEnabled()) {
			throw new IllegalStateException("This plugin has not been enabled yet! Cannot construct DeadmanExecutor before plugin is enabled");
		}
		Validate.notNull(plugin, "plugin cannot be null");
		Validate.notNull(messenger, "messenger cannot be null");
		
		this.plugin = plugin;
		this.messenger = messenger;
		this.coolDown = (coolDown != null && coolDown < 1 ? null : coolDown);
		
		ExecutorListener listener = new ExecutorListener();
		Bukkit.getPluginManager().registerEvent(PlayerQuitEvent.class, listener, EventPriority.NORMAL, new EventExecutor() {
			
			@Override
			public void execute(Listener listener, Event event) throws EventException {
				((ExecutorListener) listener).onPlayerQuit((PlayerQuitEvent) event);
			}
		}, plugin);
		
		/*
		 * Default argument type converters. Assuming that all arguments of
		 * these types should be converted this way. These converters can be
		 * overridden by the implementing plugin
		 */
		registerConverter(Integer.class, new ArgumentConverter<Integer>() {
			
			@Override
			public Result<Integer> convertCommandArg(String argName, String arg) {
				Integer num = Ints.tryParse(arg);
				return num != null ? Result.success(num) : Result.<Integer> fail(String.format(NOT_INT, arg));
			}
		});
		registerConverter(ChatColor.class, new ArgumentConverter<ChatColor>() {
			
			@Override
			public Result<ChatColor> convertCommandArg(String argName, String arg) {
				ChatColor color = DeadmanExecutor.this.plugin.getConversion().toChatColor(arg);
				return color != null ? Result.success(color) : Result.<ChatColor> fail(String.format(NOT_CHATCOLOR, arg));
			}
		});
		registerConverter(Long.class, new ArgumentConverter<Long>() {
			
			@Override
			public Result<Long> convertCommandArg(String argName, String arg) {
				long duration = DeadmanUtils.parseDuration(arg);
				return duration > 0 ? Result.success(duration) : Result.<Long> fail(String.format(NOT_DURATION, argName));
			}
		});
		registerConverter(Boolean.class, new ArgumentConverter<Boolean>() {
			
			@Override
			public Result<Boolean> convertCommandArg(String argName, String arg) {
				Boolean bool = BooleanUtils.toBooleanObject(arg);
				return bool != null ? Result.success(bool) : Result.<Boolean> fail(String.format(NOT_BOOLEAN, arg, argName));
			}
		});
	}
	
	@Override
	public final boolean onCommand(CommandSender sender, org.bukkit.command.Command bukkitCmd, String label, String[] args) {
		if (args.length == 0) {
			messenger.sendPluginInfo(sender, bukkitCmd);
			return true;
		}
		if (args[0].equals("?") || args[0].equalsIgnoreCase("help")) {
			if (args.length == 2) {
				if (DeadmanUtils.isNumeric(args[1])) {
					messenger.sendHelpInfo(sender, bukkitCmd, commands.values(), Integer.parseInt(args[1]));
					return true;
				} else {
					String helpInfoPath = helpInfo.get(args[1].toLowerCase());
					if (helpInfoPath != null) {
						messenger.sendMessage(sender, helpInfoPath);
						return true;
					}
				}
			} else {
				messenger.sendHelpInfo(sender, bukkitCmd, commands.values(), 1);
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
			messenger.sendMessage(sender, "failed.invalid-args");
			return false;
		}
		
		if (cmdWrapper.info.permissions().length > 0 && !hasCommandPerm(sender, cmdWrapper.info.permissions())) {
			messenger.sendMessage(sender, "failed.no-permission");
			return false;
		}
		if (cmdWrapper.info.inGameOnly() && !(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "This command can only be used in game.");
			return false;
		}
		
		if (args[args.length - 1].equals("?") || args[args.length - 1].equals("help")) {
			messenger.sendCommandInfo(sender, bukkitCmd, cmdWrapper.info);
			return true;
		}
		
		String[] params = Arrays.copyOfRange(args, 1, args.length);
		SubCommand subCmd = Arguments.matcher(this).forCommand(cmdWrapper).withStringArgs(params).findMatch();
		if (subCmd == null) {
			messenger.sendMessage(sender, "failed.invalid-args-alt");
			messenger.sendCommandUsage(sender, bukkitCmd, cmdWrapper.info);
			return false;
		}
		if (subCmd.info() != null) {
			if (subCmd.info().inGameOnly() && !(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "This command can only be used in game.");
				return false;
			}
			if (!hasCommandPerm(sender, subCmd.info().permissions())) {
				messenger.sendMessage(sender, "failed.no-permission");
				return false;
			}
		}
		
		Result<Arguments> conversionResult = subCmd.convert();
		if (!conversionResult.isSuccess()) {
			sender.sendMessage(ChatColor.RED + conversionResult.getFailReason());
			return false;
		}
		
		CommandExecuteEvent event = new CommandExecuteEvent(sender, cmdWrapper.cmd, conversionResult.getResult());
		Bukkit.getPluginManager().callEvent(event);
		
		return !event.isCancelled() && cmdWrapper.cmd.execute(sender, conversionResult.getResult());
	}
	
	private CommandWrapper<?> getMatchingCommand(String arg) {
		for (CommandWrapper<?> cmdWrapper : commands.values()) {
			if (cmdWrapper.info.name().equalsIgnoreCase(arg) || cmdWrapper.aliasPattern.matcher(arg).matches()) {
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
	 * @return the {@link DeadmanPlugin} that this DeadmanExecutor is for
	 */
	public final DeadmanPlugin getPlugin() {
		return plugin;
	}
	
	/**
	 * @return the command cooldown that was specified in the constructor, or null if there is no cooldown
	 */
	public final Integer getCoolDown() {
		return coolDown;
	}
	
	
	/**
	 * Register the given command.<br>
	 * This method will construct a new instance of the given Command class by calling<br>
	 * {@link java.lang.Class#newInstance() Class#newInstance()}. Thus the Command class must have a no-arg constructor.
	 * If there is not a public no-arg constructor, then register the Command using {@link #registerCommand(Command)}.
	 * @param commandClass - The class of the command that should be registered
	 * @throws IllegalStateException if the class of the given command is not annotated with the {@link CommandInfo} annotation
	 * @throws IllegalArgumentException if command is null
	 */
	public final <C extends Command> void registerCommand(Class<C> commandClass) throws IllegalStateException, IllegalArgumentException {
		Validate.notNull(commandClass, "commandClass cannot be null");
		
		CommandInfo commandInfo = getCommandInfo(commandClass);
		registerCommand(commandClass, commandInfo);
	}
	
	/**
	 * Register the given command.<br>
	 * Use this register method when the CommandInfo cannot be static and is generated at runtime.
	 * Use {@link CommandInfoImpl} to construct a CommandInfo object at runtime.
	 * @param commandClass - The class of the command that should be registered
	 * @param commandInfo - The {@link CommandInfo} for the command
	 * @throws IllegalArgumentException if commandClass or commandInfo is null
	 */
	public final <C extends Command> void registerCommand(Class<C> commandClass, CommandInfo commandInfo) throws IllegalArgumentException {
		Validate.notNull(commandClass, "commandClass cannot be null");
		
		try {
			registerCommand(commandClass.newInstance(), commandInfo);
		} catch (Exception e) {
			plugin.getLogger().severe("An exception occured while registering command " + commandClass.getCanonicalName());
			e.printStackTrace();
		}
	}
	
	/**
	 * Register the given command.<br>
	 * Use this register method when the Command class does not have a public no-arg constructor.
	 * @param command - An instance of the command that should be registered
	 * @throws IllegalStateException if the class of the given command is not annotated with the {@link CommandInfo} annotation
	 * @throws IllegalArgumentException if command is null
	 */
	public final <C extends Command> void registerCommand(C command) throws IllegalStateException, IllegalArgumentException {
		Validate.notNull(command, "command cannot be null");
		
		CommandInfo commandInfo = getCommandInfo(command.getClass());
		registerCommand(command, commandInfo);
	}
	
	/**
	 * Register the given command.<br>
	 * Use this register method when the CommandInfo cannot be static and is generated at runtime,
	 * and when the Command class does not have a public no-arg constructor.
	 * Use {@link CommandInfoImpl} to construct a CommandInfo object at runtime.
	 * @param command - An instance of the command that should be registered
	 * @param commandInfo - The {@link CommandInfo} for the command
	 * @throws IllegalArgumentException if command or commandInfo is null
	 */
	public final <C extends Command> void registerCommand(C command, CommandInfo commandInfo) throws IllegalArgumentException {
		Validate.notNull(command, "command cannot be null");
		Validate.notNull(commandInfo, "commandInfo cannot be null");
		
		commands.put(command.getClass(), new CommandWrapper<C>(commandInfo, command));
	}
	
	private <C extends Command> CommandInfo getCommandInfo(Class<C> commandClass) throws IllegalStateException {
		CommandInfo info = commandClass.getAnnotation(CommandInfo.class);
		if (info == null || info.name() == null || !info.name().matches("^\\S*$")) {
			String msg = "The '%s' command must be annotated with the CommandInfo annotation, and the name cannot be null or contain whitespace";
			throw new IllegalStateException(String.format(msg, commandClass.getCanonicalName()));
		}
		return info;
	}
	
	/**
	 * Unregister or remove a command by the given command class
	 * @param commandClass - The class of the command that should be unregistered
	 * @return the CommandWrapper for the unregistered command
	 * @throws IllegalStateException if the command has not been registered
	 */
	public final <C extends Command> CommandWrapper<C> unregisterCommand(Class<C> commandClass) throws IllegalStateException {
		CommandWrapper<C> commandWrapper = getCommandWrapper(commandClass);
		commands.remove(commandClass);
		return commandWrapper;
	}
	
	
	/**
	 * @param command - The class of the desired Command
	 * @return an instance of the registered Command
	 * @throws IllegalStateException if the command has not been registered.
	 */
	public final <C extends Command> C getCommand(Class<C> command) throws IllegalStateException {
		return getCommandWrapper(command).getCmd();
	}
	
	/**
	 * @param command - The class of the desired Command
	 * @return a {@link CommandWrapper} containing an instance of the registered Command and the commands CommandInfo annotation
	 * @throws IllegalStateException if the command has not been registered.
	 */
	public final <C extends Command> CommandWrapper<C> getCommandWrapper(Class<C> command) throws IllegalStateException {
		@SuppressWarnings("unchecked")
		CommandWrapper<C> commandWrapper = (CommandWrapper<C>) commands.get(command);
		if (commandWrapper == null) {
			throw new IllegalStateException("A command for type '" + command.getCanonicalName() + "' has not been registered!");
		}
		return commandWrapper;
	}
	
	/**
	 * Use the {@link #register(Class command)} method to properly register a command.
	 * @return An UnmodifiableMap containing all of the registered commands, with the
	 * commands class by key, and the {@link CommandWrapper} by value.
	 */
	public final Map<Class<?>, CommandWrapper<?>> getCommands() {
		return Collections.unmodifiableMap(commands);
	}
	
	
	/**
	 * A pseudo command is a command that will not be registered as a real command, but can be executed.
	 * Pseudo commands are no-arg commands, and will be executed as: /&lt;prefix&gt; &lt;pseudo-cmd-name&gt;<br />
	 * Where '&lt;prefix&gt;' is the main plugin command.<br />
	 * Register a pseudoCommand for commands
	 * like 'accept', 'cancel', and 'continue', so that these commands will not be shown in the plugin's help page.
	 * @param cmdName - The String name and syntax for the PseudoCommand. This must not match any other PseudoCommand or regular command name.
	 * @param pseudoCommand - The PseudoCommand object to be executed when this PseudoCommand is called
	 * @throws IllegalArgumentException if cmdName or pseudoCommand is null
	 */
	public final void registerPseudoCommand(String cmdName, PseudoCommand pseudoCommand) throws IllegalArgumentException {
		Validate.notNull(cmdName, "cmdName cannot be null");
		Validate.notNull(pseudoCommand, "pseudoCommand cannot be null");
		
		pseudoCommands.put(cmdName.toLowerCase(), pseudoCommand);
	}
	
	/**
	 * Use the {@link #registerPseudoCommad(String cmdName, Command command)} method to properly register a PseudoCommand.
	 * @return An UnmodifiableMap containing all of the commands pattern by key, and the registered commands by value.
	 */
	public final Map<String, PseudoCommand> getPseudoCommands() {
		return Collections.unmodifiableMap(pseudoCommands);
	}
	
	
	/**
	 * Equivalent of calling <br>
	 * {@link #registerConfirmationCommand(ConfirmationCommand, String, String) registerConfirmationCommand(command, null, null)}
	 * @param command - The instance of the ConfirmationCommand to register
	 * @throws IllegalArgumentException if command is null
	 */
	public final void registerConfirmationCommand(ConfirmationCommand<?> command) throws IllegalArgumentException {
		registerConfirmationCommand(command, null, null);
	}
	
	/**
	 * @param command - The instance of the ConfirmationCommand to register
	 * @param acceptCmd - The String for the accept {@link PseudoCommand} to be registered. defaults to "accept".
	 * @param declineCmd - The String for the decline {@link PseudoCommand} to be registered. defaults to "cancel".
	 * @throws IllegalArgumentException if command is null
	 */
	public final void registerConfirmationCommand(ConfirmationCommand<?> command, String acceptCmd, String declineCmd)
			throws IllegalArgumentException {
		Validate.notNull(command, "command cannot be null");
		
		acceptCmd = (acceptCmd == null ? "accept" : acceptCmd.toLowerCase());
		declineCmd = (declineCmd == null ? "cancel" : declineCmd.toLowerCase());
		registerPseudoCommand(acceptCmd.toLowerCase(), ConfirmationCommand.getAcceptCommand());
		registerPseudoCommand(declineCmd.toLowerCase(), ConfirmationCommand.getDeclineCommand());
		confirmationCommands.put(command.getClass(), command);
	}
	
	/**
	 * @param cmdClass - The class of the ConfirmationCommand to get
	 * @return the registered instance of the given ConfirmationCommand class
	 * @throws IllegalStateException if no instance of the given ConfirmationCommand class has been registered
	 * @throws IllegalArgumentException if cmdClass is null
	 */
	public final <T, C extends ConfirmationCommand<T>> C getConfirmationCommand(Class<C> cmdClass)
			throws IllegalStateException, IllegalArgumentException {
		Validate.notNull(cmdClass, "cmdClass cannot be null");
		C cmd = cmdClass.cast(confirmationCommands.get(cmdClass));
		if (cmd == null) {
			throw new IllegalStateException("A ConfirmationCommand for type '" + cmdClass.getCanonicalName() + "' has not been registered!");
		}
		return cmd;
	}
	
	/**
	 * @return an unmodifiable Map of all the registered {@link ConfirmationCommand}s
	 */
	public final Map<Class<?>, ConfirmationCommand<?>> getConfirmationCommands() {
		return Collections.unmodifiableMap(confirmationCommands);
	}
	
	/**
	 * @param converter - The ArgumentConverter to register
	 * @throws IllegalArgumentException if type or converter is null
	 */
	public final <C> void registerConverter(Class<? super C> type, ArgumentConverter<C> converter) throws IllegalArgumentException {
		Validate.notNull(type, "type cannot be null");
		Validate.notNull(converter, "converter cannot be null");
		
		converters.put(type, converter);
	}
	
	/**
	 * @param type - The type of converter
	 * @return the registered {@link ArgumentConverter} for the given type or
	 * null if an ArgumentConverter was not registered for the given type.
	 */
	@SuppressWarnings("unchecked")
	public final <C> ArgumentConverter<C> getConverter(Class<C> type) {
		return (ArgumentConverter<C>) converters.get(type);
	}
	
	
	/**
	 * Help Info is a message that will be sent to a CommandSender when the help command with a
	 * registered help info argument is executed. The syntax of a help info command is <br>
	 * <code>/&lt;base&gt; &lt;? or help&gt; &lt;help-arg&gt;<code><br>
	 * Example:<br><code>/dd help plugin</code>
	 * @param helpArg - The help info argument
	 * @param messagePath - The path the the help info message in the plugins lang file
	 * @throws IllegalStateException if helpArg is a number
	 * @throws IllegalArgumentException if helpArg or messagePath is null
	 */
	public final void registerHelpInfo(String helpArg, String messagePath) throws IllegalStateException, IllegalArgumentException {
		Validate.notNull(helpArg, "helpArg cannot be null");
		Validate.notNull(messagePath, "messagePath cannot be null");
		String arg = helpArg.trim().toLowerCase();
		if (DeadmanUtils.isNumeric(arg)) {
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
		Validate.notNull(sender, "sender cannot be null");
		if (perms == null || perms.length == 0) {
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
	public static final class CommandWrapper<C extends Command> {
		
		private final CommandInfo info;
		private final C cmd;
		private final Pattern aliasPattern;
		
		private CommandWrapper(CommandInfo info, C cmd) {
			this.info = info;
			this.cmd = cmd;
			aliasPattern = Pattern.compile(String.format("(?i:%s)", StringUtils.join(info.aliases(), '|')));
		}
		
		public CommandInfo getInfo() {
			return info;
		}
		
		public C getCmd() {
			return cmd;
		}
	}
	
	private class ExecutorListener implements Listener {
		
		public void onPlayerQuit(PlayerQuitEvent event) {
			ConfirmationCommand.removePlayer(event.getPlayer().getUniqueId());
		}
	}
	
}
