package org.deadmandungeons.deadmanplugin;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;

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
		if (cachedMessages.containsKey(path)) {
			message = cachedMessages.get(path);
		} else {
			message = injectColors(plugin.getLangFile().getConfig().getString(path));
			cachedMessages.put(path, message);
		}
		if (message != null) {
			if (!colorCode) {
				message = message.replaceAll(FORMATTING_REGEX, "");
			}
		} else {
			plugin.getLogger().log(Level.SEVERE, "Failed to retreive message '" + path + "' from lang file!");
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
	 * 
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
	 * 
	 * @param cmd - The Command to send as its usage, and description
	 * @param sender - The Command Sender to send the command info to
	 */
	public void sendCommandInfo(Command cmd, CommandSender sender) {
        CommandInfo info = cmd.getClass().getAnnotation(CommandInfo.class);
        if (sender.hasPermission(info.permission())) {
    		String[] commands = info.arguments().split("\\|");
    		String[] descriptions = info.description().split("\\|");
    		for (int i=0; i<commands.length; i++) {
    			sender.sendMessage(getPrimaryColor() + "/" + getMainCmd() + " " + info.name() + " " + commands[i].replace("%", ""));
    			if (descriptions.length == commands.length) {
    				sender.sendMessage(getSecondaryColor() + "  -" + descriptions[i]);
    			} else {
    				if (i == 0) {
    					sender.sendMessage(getSecondaryColor() + "  -" + info.description());
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
		sender.sendMessage(getPrimaryColor() + "<============== " + getSecondaryColor() + "QuestControl Commands" + getPrimaryColor() + " ==============>");
		for (Command cmd : commandMap.values()) {
			sendCommandInfo(cmd, sender);
			sender.sendMessage("");
        }
		sender.sendMessage(getPrimaryColor() + "<==================================================>");
	}
	
	/**
	 * 
	 * @param sender - The CommandSender to send this plugin's information to
	 */
	public void sendPluginInfo(DeadmanPlugin plugin, CommandSender sender) {
		PluginDescriptionFile pdf = plugin.getDescription();
		sender.sendMessage(getPrimaryColor() + pdf.getName() + " Version: " + getSecondaryColor() + pdf.getVersion());
		sender.sendMessage(getPrimaryColor() + "Created By: " + getSecondaryColor() + CoreUtils.formatList(pdf.getAuthors()));
		sender.sendMessage(getPrimaryColor() + "Contact at: " + getSecondaryColor() + pdf.getWebsite());
		if (getMainCmd() != null) {
			sender.sendMessage(getPrimaryColor() + "Type '/" + getMainCmd() + " help' for a list of commands you can use");
		}
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
	
	private ChatColor getPrimaryColor() {
		if (primaryColor == null) {
			String colorCode = getMessage("primary-color", true);
			if (colorCode != null) {
				primaryColor = ChatColor.getByChar(colorCode.replace("&", ""));
			}
		}
		return primaryColor;
	}
	
	private ChatColor getSecondaryColor() {
		if (secondaryColor == null) {
			String colorCode = getMessage("secondary-color", true);
			if (colorCode != null) {
				secondaryColor = ChatColor.getByChar(colorCode.replace("&", ""));
			}
		}
		return secondaryColor;
	}

}