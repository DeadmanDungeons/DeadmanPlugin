package org.deadmandungeons.deadmanplugin;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;
import org.deadmandungeons.deadmanplugin.command.Command;
import org.deadmandungeons.deadmanplugin.command.CommandInfo;
import org.deadmandungeons.deadmanplugin.command.SubCommandInfo;

/**
 * A Messaging utility class that makes messaging Players with configured messages easy.
 * All messages will be injected with appropriate configured colors, and variables.
 * @author Jon
 */
public class Messenger {
	
	//The Regex to find any variables in the language file
	private final String VARIABLE_REGEX = "<[^>]+>";
	private final Pattern VARIABLE_PATTERN = Pattern.compile(VARIABLE_REGEX);
	//The Regex to find any formatting codes in a message
	private final String FORMATTING_REGEX = "&[\\da-fk-or]";
	private final Pattern FORMAT_PATTERN = Pattern.compile(FORMATTING_REGEX);
	
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
	 * 
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
	 * 
	 * @param path - String path name to the desired message in the plugin's language file
	 * @param colorCode - boolean flag state weather message should returned with color injected or not
	 * @param vars - The variables to be injected in the message, given in the order that they occur
	 * @return the String message at the given path with the given variables injected along with any colors if colorCode is flagged as true
	 */
	public String getMessage(String path, boolean colorCode, Object... vars) {
		String message = getMessage(path, true);
		if (message != null && message.length() != 0) {
			Matcher matcher = VARIABLE_PATTERN.matcher(message);
			for (int i=0; i<vars.length && matcher.find(); i++) {
				message = message.replace(matcher.group(), vars[i].toString());
			}
			if (!colorCode) {
				message = message.replaceAll(FORMATTING_REGEX, "");
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
				sender.sendMessage(message);
			}
		}
	}

	/**
	 * @param cmd - The Command to send as its usage, and description
	 * @param sender - The Command Sender to send the command info to
	 */
	public void sendCommandInfo(Command cmd, CommandSender sender) {
        CommandInfo info = cmd.getClass().getAnnotation(CommandInfo.class);
        if (sender.hasPermission(info.permission())) {
        	sender.sendMessage(ChatColor.BOLD + "" + getPrimaryColor() + info.name() + " Command");
        	SubCommandInfo[] commands = info.subCommands();
        	if (commands.length == 0) {
        		sender.sendMessage(getSecondaryColor() + "  /" + getMainCmd() + " " + info.name());
        	}
        	if (info.description() != null && !info.description().trim().isEmpty()) {
				sender.sendMessage(getTertiaryColor() + "    - " + info.description());
			}
    		if (commands.length > 0) {
        		for (SubCommandInfo cmdInfo : commands) {
        			String arguments = "";
        			for (int i=0; i<cmdInfo.arguments().length; i++) {
        				arguments += (i > 0 ? " " : "") + cmdInfo.arguments()[i].argName();
        			}
        			sender.sendMessage(getSecondaryColor() + "  /" + getMainCmd() + " " + info.name() + " " + arguments);
        			if (cmdInfo.description() != null && !cmdInfo.description().trim().isEmpty()) {
        				sender.sendMessage(getTertiaryColor() + "    - " + cmdInfo.description());
        			}
        		}
    		}
        }
    }
	
	/**
	 * 
	 * @param sender - The CommandSender to send the command help page to
	 * @param commandMap - A Map containing all of the registered commands to be sent to the CommandSender
	 */
	public void sendHelpInfo(CommandSender sender, Map<String, Command> commandMap) {
		String helpTitle = getPrimaryColor() + plugin.getName() + " Commands" + getTertiaryColor();
		sender.sendMessage(getTertiaryColor() + "<============== " + helpTitle + " ==============>");
		for (Command cmd : commandMap.values()) {
			sendCommandInfo(cmd, sender);
			sender.sendMessage("");
        }
		sender.sendMessage(getTertiaryColor() + "<==================================================>");
	}
	
	/**
	 * 
	 * @param sender - The CommandSender to send this plugin's information to
	 */
	public void sendPluginInfo(CommandSender sender) {
		PluginDescriptionFile pdf = plugin.getDescription();
		sender.sendMessage(getSecondaryColor() + pdf.getName() + " Version: " + getPrimaryColor() + pdf.getVersion());
		String authors = DeadmanUtils.formatList(pdf.getAuthors(), getPrimaryColor(), getSecondaryColor());
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
				primaryColor = ChatColor.getByChar(colorCode.replace("&", ""));;
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
	 * Clear any cached messages forcing them to pull them from the config again
	 */
	public void clearCache() {
		cachedMessages.clear();
		primaryColor = null;
		secondaryColor = null;
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
	
	//using the raw section symbol in the message does not seem to work on some spigot builds, so inject the ChatColor
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