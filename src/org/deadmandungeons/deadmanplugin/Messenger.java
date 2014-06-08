package org.deadmandungeons.deadmanplugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.deadmandungeons.deadmanplugin.command.CommandInfo;
import org.deadmandungeons.deadmanplugin.command.DeadmanExecutor;
import org.deadmandungeons.deadmanplugin.command.DeadmanExecutor.CommandWrapper;
import org.deadmandungeons.deadmanplugin.command.SubCommandInfo;

//TODO maybe make the implementing Plugin specify the main command
/**
 * A Messaging utility class that makes messaging Players with configured messages easy.
 * All messages will be injected with appropriate configured colors, and variables.
 * @author Jon
 */
public class Messenger {
	
	// The Regex to find any variables in the language file
	private static final String VARIABLE_REGEX = "<[^>]+>";
	private static final Pattern VARIABLE_PATTERN = Pattern.compile(VARIABLE_REGEX);
	// The Regex to find any formatting codes in a message
	private static final String FORMATTING_REGEX = "&[\\da-fk-or]";
	private static final Pattern FORMAT_PATTERN = Pattern.compile(FORMATTING_REGEX);
	
	private Map<String, String> cachedMessages = new HashMap<String, String>();
	
	private String mainCmd;
	private ChatColor primaryColor;
	private ChatColor secondaryColor;
	private ChatColor tertiaryColor;
	
	private DeadmanPlugin plugin;
	
	public Messenger(DeadmanPlugin plugin) {
		this.plugin = plugin;
	}
	
	/**
	 * @param path - String path name to the desired message in the plugin's language file
	 * @param colorCode - boolean flag state weather message should returned with color injected or not
	 * @return the configured message at the given path, with colors injected if colorCode is true
	 */
	public String getMessage(String path, boolean colorCode) {
		String message = null;
		String rawMessage = getRawMessage(path);
		if (rawMessage != null) {
			if (colorCode) {
				message = injectColors(rawMessage);
			} else {
				message = rawMessage.replaceAll(FORMATTING_REGEX, "");
			}
		}
		return message;
	}
	
	/**
	 * @param path - String path name to the desired message in the plugin's language file
	 * @param colorCode - boolean flag state weather message should returned with color injected or not
	 * @param vars - The variables to be injected in the message, given in the order that they occur
	 * @return the String message at the given path with the given variables injected along with any colors if colorCode is flagged as true
	 */
	public String getMessage(String path, boolean colorCode, Object... vars) {
		String message = getMessage(path, colorCode);
		if (message != null && message.length() != 0) {
			Matcher matcher = VARIABLE_PATTERN.matcher(message);
			for (int i = 0; i < vars.length && matcher.find(); i++) {
				message = message.replace(matcher.group(), vars[i].toString());
			}
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
				for (String line : message.split("\\\\n")) {
					sender.sendMessage(line);
				}
			}
		}
	}
	
	/**
	 * This method calls {@link #sendMessage(CommandSender, String, Object...) sendMessage()} but with a error sound played to the CommandSender if
	 * the sender is a player
	 */
	public void sendErrorMessage(CommandSender sender, String path, Object... vars) {
		sendMessage(sender, path, vars);
		if (sender instanceof Player) {
			Player player = (Player) sender;
			player.playSound(player.getLocation(), Sound.NOTE_BASS_GUITAR, 1, .8F);
		}
	}
	
	/**
	 * This method calls {@link #sendMessage(CommandSender, String, Object...) sendMessage()} but with a success sound played to the CommandSender if
	 * the sender is a player
	 */
	public void sendSuccessMessage(CommandSender sender, String path, Object... vars) {
		sendMessage(sender, path, vars);
		if (sender instanceof Player) {
			final Player player = (Player) sender;
			player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1, .5F);
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				
				@Override
				public void run() {
					player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1, .5F);
				}
			}, 4L);
		}
	}
	
	/**
	 * @param info - The CommandInfo that should be sent as the Command name, usage, and description
	 * @param sender - The Command Sender to send the command info to
	 */
	public void sendCommandInfo(CommandInfo info, CommandSender sender) {
		sender.sendMessage(ChatColor.BOLD + "" + getPrimaryColor() + info.name() + " Command");
		sendCommandUsage(info, sender);
	}
	
	/**
	 * @param info - The CommandInfo that should be sent as the Command usage and description
	 * @param sender - The Command Sender to send the command info to
	 */
	public void sendCommandUsage(CommandInfo info, CommandSender sender) {
		SubCommandInfo[] commands = info.subCommands();
		if (commands.length == 0) {
			sender.sendMessage(getSecondaryColor() + "  /" + getMainCmd() + " " + info.name());
		}
		if (info.description() != null && !info.description().trim().isEmpty()) {
			sender.sendMessage(getTertiaryColor() + "    - " + info.description());
		}
		for (SubCommandInfo cmdInfo : commands) {
			String arguments = "";
			for (int i = 0; i < cmdInfo.arguments().length; i++) {
				arguments += (i > 0 ? " " : "") + cmdInfo.arguments()[i].argName();
			}
			sender.sendMessage(getSecondaryColor() + "  /" + getMainCmd() + " " + info.name() + " " + arguments);
			if (cmdInfo.description() != null && !cmdInfo.description().trim().isEmpty()) {
				sender.sendMessage(getTertiaryColor() + "    - " + cmdInfo.description());
			}
		}
	}
	
	/**
	 * Only commands that the CommandSender has permissions for will be displayed. 5 commands
	 * are displayed per page.
	 * @param sender - The CommandSender to send the command help page to
	 * @param commandMap - A Map containing all of the registered commands to be sent to the CommandSender
	 * @param pageNum - The number of the page to send. Each page lists 5 commands
	 */
	public void sendHelpInfo(CommandSender sender, Map<Class<?>, CommandWrapper<?>> commandMap, int pageNum) {
		List<CommandInfo> cmdInfos = new ArrayList<CommandInfo>();
		for (CommandWrapper<?> cmdWrapper : commandMap.values()) {
			if (DeadmanExecutor.hasCommandPerm(sender, cmdWrapper.getInfo().permissions())) {
				cmdInfos.add(cmdWrapper.getInfo());
			}
		}
		CommandInfo[] infos = cmdInfos.toArray(new CommandInfo[cmdInfos.size()]);
		
		int maxPage = infos.length / 5 + (infos.length % 5 > 0 ? 1 : 0);
		if (pageNum * 5 > infos.length + 4) {
			pageNum = maxPage;
		}
		
		String paging = (infos.length > 5 ? " [pg. " + pageNum + "/" + maxPage + "]" : "");
		String helpTitle = getPrimaryColor() + plugin.getName() + " Commands" + paging + getTertiaryColor();
		sender.sendMessage("");
		sender.sendMessage(getTertiaryColor() + "<========= " + helpTitle + " =========>");
		sender.sendMessage(getSecondaryColor() + "KEY: " + getTertiaryColor() + "'non-variable' '<variable>' '[optional-variable]'");
		for (int i = 0; i < infos.length && i < (pageNum * 5); i++) {
			if (i >= (pageNum - 1) * 5) {
				sendCommandInfo(infos[i], sender);
				if (i + 1 != infos.length && i + 1 != pageNum * 5) {
					sender.sendMessage("");
				}
			}
		}
		sender.sendMessage(getTertiaryColor() + "<==================================================>");
		sender.sendMessage("");
	}
	
	/**
	 * @param sender - The CommandSender to send this plugin's information to
	 */
	public void sendPluginInfo(CommandSender sender) {
		PluginDescriptionFile pdf = plugin.getDescription();
		sender.sendMessage(getSecondaryColor() + pdf.getName() + " Version: " + getPrimaryColor() + pdf.getVersion());
		String authors = formatList(pdf.getAuthors());
		sender.sendMessage(getSecondaryColor() + "Created By: " + getPrimaryColor() + authors);
		sender.sendMessage(getSecondaryColor() + "Contact at: " + getPrimaryColor() + pdf.getWebsite());
		if (getMainCmd() != null) {
			sender.sendMessage(getSecondaryColor() + "Type '/" + getMainCmd() + " help' for a list of commands you can use");
		}
	}
	
	/**
	 * @return The primary ChatColor configured in the plugins lang file, or ChatColor.GOLD by default if
	 * no color is configured
	 */
	public ChatColor getPrimaryColor() {
		if (primaryColor == null) {
			String colorCode = getRawMessage("primary-color");
			if (colorCode != null) {
				primaryColor = ChatColor.getByChar(colorCode.replace("&", ""));
				;
			}
		}
		return primaryColor == null ? ChatColor.GOLD : primaryColor;
	}
	
	/**
	 * @return The secondary ChatColor configured in the plugins lang file, or ChatColor.GRAY by default if
	 * no color is configured
	 */
	public ChatColor getSecondaryColor() {
		if (secondaryColor == null) {
			String colorCode = getRawMessage("secondary-color");
			if (colorCode != null) {
				secondaryColor = ChatColor.getByChar(colorCode.replace("&", ""));
			}
		}
		return secondaryColor == null ? ChatColor.GRAY : secondaryColor;
	}
	
	/**
	 * @return The third ChatColor configured in the plugins lang file, or the secondary color by default if
	 * no color is configured
	 */
	public ChatColor getTertiaryColor() {
		if (tertiaryColor == null) {
			String colorCode = getRawMessage("tertiary-color");
			if (colorCode != null) {
				tertiaryColor = ChatColor.getByChar(colorCode.replace("&", ""));
			}
		}
		return tertiaryColor == null ? getSecondaryColor() : tertiaryColor;
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
	 * Clear any cached messages forcing them to pull them from the config again
	 */
	public void clearCache() {
		cachedMessages.clear();
		primaryColor = null;
		secondaryColor = null;
		tertiaryColor = null;
	}
	
	private String getRawMessage(String path) {
		String rawMessage = null;
		if (cachedMessages.containsKey(path)) {
			rawMessage = cachedMessages.get(path);
		} else {
			rawMessage = plugin.getLangFile().getConfig().getString(path);
			cachedMessages.put(path, rawMessage);
		}
		if (rawMessage == null) {
			plugin.getLogger().log(Level.SEVERE, "Failed to retrieve message '" + path + "' from lang file!");
		}
		return rawMessage;
	}
	
	// using the raw section symbol in the message does not seem to work on some spigot builds, so inject the ChatColor
	private String injectColors(String message) {
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
	
	private String getMainCmd() {
		if (mainCmd == null) {
			PluginDescriptionFile pdf = plugin.getDescription();
			if (!pdf.getCommands().isEmpty()) {
				for (String cmd : pdf.getCommands().keySet()) {
					String description = (String) pdf.getCommands().get(cmd).get("description");
					if (description != null && description.contains(pdf.getName() + " command prefix")) {
						mainCmd = cmd;
						break;
					}
				}
			}
		}
		return mainCmd;
	}
	
}