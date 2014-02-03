package org.deadmandungeons.deadmanplugin;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class DeadmanUtils {
	
	/**
	 * Do not instantiate this class. Public constructor must be provided to be extended from each plugin
	 * @throws AssertionError
	 */
	public DeadmanUtils() throws AssertionError {
		throw new AssertionError("Do not instantiate this class. Public constructor " 
				+ "must be provided to be extended from each plugin");
	}
	
	
	/**
	 * 
	 * @param millis - The amount of time in milliseconds
	 * @return - the amount of time passed in the format of: # days, # hours, # minutes, #seconds
	 */
	public static String getDurationString(long millis) {
		String formattedTimer = "";
		
		int days = (int)TimeUnit.MILLISECONDS.toDays(millis);        
		long hours = TimeUnit.MILLISECONDS.toHours(millis) - (days * 24);
		long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) - (TimeUnit.MILLISECONDS.toHours(millis) * 60);
		long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) - (TimeUnit.MILLISECONDS.toMinutes(millis) * 60);
		
		if (days != 0) {
			formattedTimer += days + " days";
		}
		if (hours != 0) {
			formattedTimer += (formattedTimer.length() != 0 ? ", " : "") + hours + " hours";
		}
		if (minutes != 0 && days == 0) {
			formattedTimer += (formattedTimer.length() != 0 ? ", " : "") + minutes + " minutes";
		}
		if (seconds != 0 && days == 0 && hours == 0) {
			formattedTimer += (formattedTimer.length() != 0 ? ", " : "") + seconds + " seconds";
		}

		return formattedTimer;
	}
	
	/**
	 * This method will check if the given string matches the timer sign format which is a colon delimited set of 
	 * time amounts. Supported time intervals are days, hours, and minutes in the format '#d:#h:#m'
	 * <br>Example time amounts:
	 * <ul><li>11d:12h:30m</li><li>5d:30m</li><li>5d:45m</li><li>45m</li></ul>
	 * 
	 * @param durationLine
	 * @return the duration of time in milliseconds from the sign formatted duration String
	 */
	public static long getDuration(String durationLine) {
		if (durationLine != null) {
			if (durationLine.matches("^\\d+[dD](:\\d+[hH](:\\d+[mM])?)?$|^\\d+[dD]:\\d+[mM]$|^\\d+[hH](:\\d+[mM])?$|^\\d+[mM]$")) {
				int days = 0;
				int hours = 0;
				int minutes = 0;
				
				Pattern pattern;
				Matcher matcher;
				String daysRegex = "\\d+[dD]";
				pattern = Pattern.compile(daysRegex); 
				matcher = pattern.matcher(durationLine);
				if (matcher.find()) {
					days = Integer.parseInt(matcher.group().replaceAll("[^\\d]", ""));
				}
				String hoursRegex = "\\d+[hH]";
				pattern = Pattern.compile(hoursRegex); 
				matcher = pattern.matcher(durationLine);
				if (matcher.find()) {
					hours = Integer.parseInt(matcher.group().replaceAll("[^\\d]", ""));
				}
				String minutesRegex = "\\d+[mM]";
				pattern = Pattern.compile(minutesRegex); 
				matcher = pattern.matcher(durationLine);
				if (matcher.find()) {
					minutes = Integer.parseInt(matcher.group().replaceAll("[^\\d]", ""));
				}
				
				return (days * 86400000L) + (hours * 3600000) + (minutes * 60000);
			}
		}
		return 0;
	}
	

	public static String getOrdinalSuffix(int value) {
		int remainder = value % 10;
		switch (remainder) {
		case 1:
			return value + "st";
		case 2:
			return value + "nd";
		case 3:
			return value + "rd";
		default:
			return value + "th";
		}
	}
	
	public static boolean isPlayerWithinRadius(Player player, Location loc, double radius)  {
		double x = (loc.getX() + .5) - player.getLocation().getX();
		double y = (loc.getY() + .5) - player.getLocation().getY();
		double z = (loc.getZ() + .5) - player.getLocation().getZ();
		double distance = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2)) - .6;
		if (distance <= radius) {
			return true;
		}
		return false;
	}
	
	public static boolean isInventoryFree(Player player) {
	    Inventory inv = player.getInventory();
        ItemStack[] items = inv.getContents();
        for (ItemStack is : items) {
            if (is == null) {
                return true;
            }
        }
		return false;
	}


	public static Sign getSignState(final Block block) {
		Sign sign = null;
		if(block.getType().equals(Material.SIGN_POST) || block.getType().equals(Material.WALL_SIGN)) {
			BlockState bs = block.getState();
			sign = (Sign) bs;
		}
		return sign;
	}
	
	
	public static void clearSign(Sign sign) {
		sign.setLine(0, "");
		sign.setLine(1, "");
		sign.setLine(2, "");
		sign.setLine(3, "");
		sign.update();
	}
	
	/**
	 * TODO remove deprecated. put this here to remind myself that the parameter is the colro string and not the config path
	 * @param color
	 * @return
	 */
	@Deprecated
	public static ChatColor getChatColor(String color) {
		ChatColor signColor = null;
		if (isColorValid(color)) {
			if (ChatColor.valueOf(color).isColor()) {
				signColor = ChatColor.valueOf(color);
			}
		}
		return signColor;
	}
	
	public static boolean isColorValid(String color) {
		for (ChatColor chatColor : ChatColor.values()) {
			if (chatColor.name().equals(color)) {
				return true;
			}
		}
		return false;
	}
	
	public static String formatList(List<String> list) {
		String result = "";
		if (list.size() > 0) {
			for (int n=0; n<list.size(); n++) {
				result += (n != 0) ? (n == list.size() -1 ? ChatColor.GRAY + " and " : ChatColor.GRAY + ", ") + ChatColor.GOLD + list.get(n) : ChatColor.GOLD + list.get(n);
			}
		} else {
			result = ChatColor.RED + "none";
		}
		
		return result;
	}
	
	public static String seperateElements(String[] args, String seperator) {
		String cmdStr = "";
		for (int i=0; i<args.length; i++) {
			cmdStr += (i > 0 ? seperator : "") + args[i];
		}
		return cmdStr;
	}
	
	
	public static boolean isInteger(String number) {
		try {
			Integer.parseInt(number);
		} catch(NumberFormatException e) {
			return false;
		}
		return true;
	}
	
	public static boolean isPositiveNumber(String str) {
		if (str == null || str.isEmpty()) {
			return false;
		}
	    for (char c : str.toCharArray()) {
	        if (!Character.isDigit(c)) {
	        	return false;
	        }
	    }
	    return true;
	}
	
	public static boolean isNumeric(String str) {
		return str.matches("-?\\d+(\\.\\d+)?");
	}
	
}
