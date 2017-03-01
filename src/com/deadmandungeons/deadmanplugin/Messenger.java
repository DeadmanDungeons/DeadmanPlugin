package com.deadmandungeons.deadmanplugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;

import com.deadmandungeons.deadmanplugin.command.ArgumentInfo;
import com.deadmandungeons.deadmanplugin.command.CommandInfo;
import com.deadmandungeons.deadmanplugin.command.DeadmanExecutor;
import com.deadmandungeons.deadmanplugin.command.DeadmanExecutor.CommandWrapper;
import com.deadmandungeons.deadmanplugin.command.SubCommandInfo;
import com.deadmandungeons.deadmanplugin.filedata.PluginFile;

/**
 * A Messaging utility class that makes messaging Players with configured messages easy.
 * All messages will be injected with appropriate configured colors, and variables.
 * @author Jon
 */
public class Messenger {
	
	// The Regex to find any variables in the language file
	private static final String VARIABLE_REGEX = "(?<!\\\\)<[^>]+[^\\\\]>";
	private static final Pattern VARIABLE_PATTERN = Pattern.compile(VARIABLE_REGEX);
	// The Regex to find any formatting codes in a message
	private static final String FORMATTING_REGEX = "&[\\da-fk-or]";
	private static final Pattern FORMAT_PATTERN = Pattern.compile(FORMATTING_REGEX);
	
	private static final String BAD_COLOR = "The '%s' property is either missing from the lang file or an invalid value. Defaulting to %s.";
	
	private final Map<String, String> cachedMessages = new HashMap<String, String>();
	
	private ChatColor primaryColor;
	private ChatColor secondaryColor;
	private ChatColor tertiaryColor;
	
	private final DeadmanPlugin plugin;
	private PluginFile langFile;
	
	public Messenger(DeadmanPlugin plugin, PluginFile langFile) {
		Validate.notNull(plugin, "plugin cannot be null");
		Validate.notNull(langFile, "langFile cannot be null");
		this.plugin = plugin;
		this.langFile = langFile;
	}
	
	/**
	 * @return the {@link DeadmanPlugin} that this Messenger is for
	 */
	public final DeadmanPlugin getPlugin() {
		return plugin;
	}
	
	/**
	 * @return the language {@link PluginFile} that this Messenger is using
	 */
	public final PluginFile getLangFile() {
		return langFile;
	}
	
	
	/**
	 * @param langFile - the language {@link PluginFile} file to use
	 */
	public void setLangFile(PluginFile langFile) {
		Validate.notNull(langFile, "langFile cannot be null");
		this.langFile = langFile;
		reload();
	}
	
	/**
	 * The provided variables will be represented in the returned message by the result of
	 * their {@link #toString()}. <br>
	 * If a provided variable is an instances of {@link Identifiable}, the variable will
	 * be set as the result of {@link Identifiable#getManagedId()}
	 * @param path - String path name to the desired message in the plugin's language file
	 * @param colorCode - boolean flag state weather message should returned with color injected or not
	 * @param vars - The variables to be injected in the message, given in the order that they occur
	 * @return the String message at the given path with the given variables injected along with any colors if colorCode is flagged as true
	 */
	public String getMessage(String path, boolean colorCode, Object... vars) {
		String message = getRawMessage(path);
		if (message != null && message.length() > 0) {
			boolean autoColor = false;
			if (colorCode) {
				if (FORMAT_PATTERN.matcher(message).find()) {
					message = injectColors(message);
				} else {
					message = getSecondaryColor() + message;
					autoColor = true;
				}
			} else {
				message = message.replaceAll(FORMATTING_REGEX, "");
			}
			if (vars.length > 0) {
				Matcher matcher = VARIABLE_PATTERN.matcher(message);
				for (int i = 0; i < vars.length && matcher.find(); i++) {
					Object var = vars[i];
					if (var instanceof Identifiable) {
						var = ((Identifiable<?>) var).getManagedId();
					}
					String value = (autoColor ? getPrimaryColor() + var.toString() + getSecondaryColor() : var.toString());
					message = message.replace(matcher.group(), value);
				}
			}
			return message.replace("\\<", "<").replace("\\>", ">");
		}
		return message;
	}
	
	/**
	 * @param sender - The CommandSender to send the message to
	 * @param path - The path of the configured message in the plugin's language file
	 * @param vars - The variables to be injected in the message, given in the order that they occur
	 */
	public void sendMessage(CommandSender sender, String path, Object... vars) {
		if (sender != null) {
			String message = getMessage(path, true, vars);
			if (message != null && message.length() != 0) {
				sender.sendMessage(message);
			}
		}
	}
	
	/**
	 * This method calls {@link #sendMessage(CommandSender, String, Object...) sendMessage()} but
	 * with a error sound played to the CommandSender if the sender is a player
	 */
	public void sendErrorMessage(CommandSender sender, String path, Object... vars) {
		sendMessage(sender, path, vars);
		if (sender instanceof Player) {
			Player player = (Player) sender;
			player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BASS, 1, .8F);
		}
	}
	
	/**
	 * This method calls {@link #sendMessage(CommandSender, String, Object...) sendMessage()} but
	 * with a 2 successive 'ding' sounds played to the CommandSender if the sender is a player
	 * that will get their attention
	 */
	public void sendImportantMessage(CommandSender sender, String path, Object... vars) {
		sendMessage(sender, path, vars);
		if (sender instanceof Player) {
			final Player player = (Player) sender;
			player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, .5F);
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				
				@Override
				public void run() {
					player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, .5F);
				}
			}, 4L);
		}
	}
	
	/**
	 * <b>Note:</b> If the CommandSender does not have any of the permissions specified by the given cmdInfo,
	 * or any of its sub-commands, this will do nothing.
	 * @param sender - The Command Sender to send the command info to
	 * @param bukkitCmd - The Bukkit Command used as the base command prefix for the given cmdInfo
	 * @param cmdInfo - The CommandInfo that should be sent as the Command name, usage, and description
	 */
	public void sendCommandInfo(CommandSender sender, Command bukkitCmd, CommandInfo cmdInfo) {
		List<Integer> permittedSubCmdIndexes = getPermittedSubCmdIndexes(sender, cmdInfo);
		if (permittedSubCmdIndexes == null) {
			return;
		}
		
		sender.sendMessage(getPrimaryColor() + cmdInfo.name() + " Command");
		sendPermittedCommandUsage(sender, bukkitCmd.getName(), cmdInfo, permittedSubCmdIndexes);
	}
	
	/**
	 * <b>Note:</b> If the CommandSender does not have any of the permissions specified by the given cmdInfo,
	 * or any of its sub-commands, this will do nothing.
	 * @param sender - The CommandSender to send the command usage to
	 * @param bukkitCmd - The Bukkit Command used as the base command prefix for the given cmdInfo
	 * @param cmdInfo - The CommandInfo that should be sent as the Command usage and description
	 */
	public void sendCommandUsage(CommandSender sender, Command bukkitCmd, CommandInfo cmdInfo) {
		List<Integer> permittedSubCmdIndexes = getPermittedSubCmdIndexes(sender, cmdInfo);
		if (permittedSubCmdIndexes == null) {
			return;
		}
		
		sendPermittedCommandUsage(sender, bukkitCmd.getName(), cmdInfo, permittedSubCmdIndexes);
	}
	
	/**
	 * Only commands and sub-commands that the CommandSender has permissions for will be displayed.
	 * @param sender - The CommandSender to send the command help page to
	 * @param bukkitCmd - The Bukkit Command used as the base command prefix for the given commands
	 * @param commands - A Collection containing all of the registered commands to be used in the sent help info
	 * @param pageNum - The number of the page to send. Each page lists at least 10 command parts.
	 * A command part is the base command info, and any sub-command info
	 */
	public void sendHelpInfo(CommandSender sender, Command bukkitCmd, Collection<CommandWrapper<?>> commands, int pageNum) {
		Map<CommandInfo, List<Integer>> pageCmds = new LinkedHashMap<>();
		int cmdPartsPerPage = 10, pageCmdParts = 0, page = 1, maxPage = 1;
		for (CommandWrapper<?> cmdWrapper : commands) {
			CommandInfo cmdInfo = cmdWrapper.getInfo();
			List<Integer> permittedSubCmdIndexes = getPermittedSubCmdIndexes(sender, cmdInfo);
			if (permittedSubCmdIndexes != null) {
				if (maxPage <= pageNum) {
					// If we are on a new page (and there are more cmdParts), clear the previous page
					if (pageCmdParts == 0 && maxPage > 1) {
						pageCmds.clear();
					}
					pageCmds.put(cmdInfo, permittedSubCmdIndexes);
				}
				
				// Add 1 for each sub command info and 1 for the base command info
				pageCmdParts += permittedSubCmdIndexes.size() + 1;
				
				if (pageCmdParts >= cmdPartsPerPage) {
					pageCmdParts = 0;
					maxPage++;
					if (page < pageNum) {
						page++;
					}
				}
			}
		}
		
		String paging = (maxPage > 1 ? " [pg. " + page + "/" + maxPage + "]" : "");
		String helpTitle = getPrimaryColor() + plugin.getName() + " Commands" + paging + getTertiaryColor();
		sender.sendMessage("");
		sender.sendMessage(getTertiaryColor() + "<========= " + helpTitle + " =========>");
		sender.sendMessage(getSecondaryColor() + "KEY: " + getTertiaryColor() + "\"non-variable\", \"<variable>\", \"[optional-variable]\"");
		
		Iterator<Map.Entry<CommandInfo, List<Integer>>> iter = pageCmds.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<CommandInfo, List<Integer>> entry = iter.next();
			sender.sendMessage(getPrimaryColor() + entry.getKey().name() + " Command");
			sendPermittedCommandUsage(sender, bukkitCmd.getName(), entry.getKey(), entry.getValue());
			
			if (iter.hasNext()) {
				sender.sendMessage("");
			}
		}
		
		sender.sendMessage(getTertiaryColor() + "<==================================================>");
		sender.sendMessage("");
	}
	
	private List<Integer> getPermittedSubCmdIndexes(CommandSender sender, CommandInfo cmdInfo) {
		if (!DeadmanExecutor.hasCommandPerm(sender, cmdInfo.permissions())) {
			return null;
		}
		if (cmdInfo.subCommands().length == 0) {
			return Collections.emptyList();
		}
		List<Integer> permittedSubCmdIndexes = new ArrayList<>();
		for (int i = 0; i < cmdInfo.subCommands().length; i++) {
			SubCommandInfo subCmdInfo = cmdInfo.subCommands()[i];
			if (DeadmanExecutor.hasCommandPerm(sender, subCmdInfo.permissions())) {
				permittedSubCmdIndexes.add(i);
			}
		}
		return (!permittedSubCmdIndexes.isEmpty() ? permittedSubCmdIndexes : null);
	}
	
	private void sendPermittedCommandUsage(CommandSender sender, String cmdName, CommandInfo cmdInfo, List<Integer> subCmdIndexes) {
		if (cmdInfo.aliases().length > 0) {
			sender.sendMessage(getTertiaryColor() + "  ALIASES: " + StringUtils.join(cmdInfo.aliases(), ", "));
		}
		String baseCmd = getSecondaryColor() + "  /" + cmdName + " " + cmdInfo.name().toLowerCase();
		if (subCmdIndexes.isEmpty()) {
			sender.sendMessage(baseCmd);
		}
		if (!StringUtils.isBlank(cmdInfo.description())) {
			sender.sendMessage(getTertiaryColor() + "  - " + injectConfigVariables(cmdInfo.description()).trim());
		}
		for (Integer subCmdIndex : subCmdIndexes) {
			SubCommandInfo subCmdInfo = cmdInfo.subCommands()[subCmdIndex];
			String arguments = "";
			for (int i = 0; i < subCmdInfo.arguments().length; i++) {
				ArgumentInfo argInfo = subCmdInfo.arguments()[i];
				arguments += (i > 0 ? " " : "") + String.format(argInfo.argType().getWrap(), argInfo.argName());
			}
			sender.sendMessage(baseCmd + " " + arguments);
			if (!StringUtils.isBlank(subCmdInfo.description())) {
				sender.sendMessage(getTertiaryColor() + "    - " + injectConfigVariables(subCmdInfo.description()).trim());
			}
		}
	}
	
	private String injectConfigVariables(String message) {
		Matcher matcher = VARIABLE_PATTERN.matcher(message);
		while (matcher.find()) {
			String configPath = matcher.group().replaceAll("<|>", "");
			Object value = plugin.getConfig().get(configPath);
			if (value != null) {
				message = message.replace(matcher.group(), value.toString());
			}
		}
		return message;
	}
	
	/**
	 * @param sender - The CommandSender to send this plugin's information to
	 */
	public void sendPluginInfo(CommandSender sender, Command bukkitCmd) {
		PluginDescriptionFile pdf = plugin.getDescription();
		sender.sendMessage(getSecondaryColor() + pdf.getName() + " Version: " + getPrimaryColor() + pdf.getVersion());
		sender.sendMessage(getSecondaryColor() + "Created By: " + getPrimaryColor() + formatList(pdf.getAuthors()));
		sender.sendMessage(getSecondaryColor() + "Contact at: " + getPrimaryColor() + pdf.getWebsite());
		sender.sendMessage(getSecondaryColor() + "Type '/" + bukkitCmd.getName() + " help' for a list of commands you can use");
	}
	
	/**
	 * @return The primary ChatColor configured in the plugins lang file, or ChatColor.GOLD by default if
	 * no color is configured
	 */
	public ChatColor getPrimaryColor() {
		if (primaryColor == null) {
			primaryColor = getChatColor("primary-color", ChatColor.GOLD);
		}
		return primaryColor;
	}
	
	/**
	 * @return The secondary ChatColor configured in the plugins lang file, or ChatColor.GRAY by default if
	 * no color is configured
	 */
	public ChatColor getSecondaryColor() {
		if (secondaryColor == null) {
			secondaryColor = getChatColor("secondary-color", ChatColor.GRAY);
		}
		return secondaryColor;
	}
	
	/**
	 * @return The third ChatColor configured in the plugins lang file, or ChatColor.DARK_GRAY by default if
	 * no color is configured
	 */
	public ChatColor getTertiaryColor() {
		if (tertiaryColor == null) {
			tertiaryColor = getChatColor("tertiary-color", ChatColor.DARK_GRAY);
		}
		return tertiaryColor;
	}
	
	/**
	 * This method is used to format a List into a human readable String
	 * using the primary and secondary colors defined in the plugins lang file.
	 * The list will be formatted as: <list-val>, <list-val>, <list-val> and <list-val>
	 * @param list - A list of objects to be formatted calling {@link java.lang.Object#toString toString()}
	 * @return A comma/'and' delimited String of all the values in the provided list
	 */
	public String formatList(Collection<?> list) {
		String result = "";
		if (list.size() > 0) {
			ChatColor secondary = getSecondaryColor();
			ChatColor primary = getPrimaryColor();
			int i = 0;
			for (Object obj : list) {
				String val = primary + obj.toString();
				result += (i++ != 0 ? secondary + (i == list.size() ? " and " : ", ") + val : val);
			}
		} else {
			result = ChatColor.RED + "none";
		}
		
		return result;
	}
	
	/**
	 * Clear any cached messages and reload the language file
	 */
	public void reload() {
		cachedMessages.clear();
		primaryColor = null;
		secondaryColor = null;
		tertiaryColor = null;
		
		langFile.reloadConfig();
	}
	
	/**
	 * This will replace any substring matching &[\da-fk-or] with the appropriate ChatColor
	 * @param message - The message string to inject colors in
	 * @return the given message with any ChatColors injected
	 */
	public static String injectColors(String message) {
		StringBuffer sb = new StringBuffer();
		if (message != null) {
			Matcher matcher = FORMAT_PATTERN.matcher(message);
			while (matcher.find()) {
				matcher.appendReplacement(sb, ChatColor.getByChar(matcher.group().replace("&", "")).toString());
			}
			matcher.appendTail(sb);
		}
		return sb.toString();
	}
	
	private String getRawMessage(String path) {
		String rawMessage = null;
		if (cachedMessages.containsKey(path)) {
			rawMessage = cachedMessages.get(path);
		} else {
			rawMessage = langFile.getConfig().getString(path);
			if (rawMessage == null) {
				rawMessage = langFile.getConfig().getDefaults().getString(path);
				if (rawMessage != null) {
					plugin.getLogger().warning("Missing message at path '" + path + "' from lang file! default message will be used");
				}
			}
			cachedMessages.put(path, rawMessage);
		}
		if (rawMessage == null) {
			plugin.getLogger().severe("Failed to retrieve message '" + path + "' from lang file!");
		}
		return rawMessage;
	}
	
	private ChatColor getChatColor(String property, ChatColor defaultColor) {
		String colorCode = getRawMessage(property);
		ChatColor color = plugin.getConversion().toChatColor(colorCode);
		if (color == null) {
			plugin.getLogger().warning(String.format(BAD_COLOR, property, defaultColor));
			color = defaultColor;
		}
		return color;
	}
	
	
	/**
	 * An identifiable object is one that can identified by a user, and indexed/managed by the
	 * implementing plugin with the identifier returned by {@link #getManagedId()}.
	 * It is up to the implementing plugin to handle how the identifiable object is managed,
	 * so there are no implementation requirements for this interface.
	 * @param <T> - The type of the managed ID for the Identifiable object
	 * @author Jon
	 */
	public static interface Identifiable<T> {
		
		/**
		 * When an Identifiable object is passed to a {@link Messenger} as a message variable, this
		 * returned value will be represented in the message by the result of its {@link #toString()}
		 * @return the ID that can be identified by a user, and indexed/managed by the implementing plugin.
		 */
		T getManagedId();
		
	}
	
}