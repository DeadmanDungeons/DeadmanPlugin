package org.deadmandungeons.deadmanplugin;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.util.BlockIterator;

/**
 * Do not instantiate this class. Public constructor must be provided to be extended from each plugin
 * @author Jon
 */
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
			String unit = (days != 0 || (days == 0 && minutes > 0) ? " hr" : "hour") + (hours > 1 ? "s" : "");
			formattedTimer += (formattedTimer.length() != 0 ? ", " : "") + hours + unit;
		}
		if (minutes != 0 && days == 0) {
			String unit = (hours != 0) ? " min" : "minute" + (minutes > 1 ? "s" : "");
			formattedTimer += (formattedTimer.length() != 0 ? ", " : "") + minutes + unit;
		}
		if (seconds != 0 && days == 0 && hours == 0 && minutes == 0) {
			String unit = "second" + (seconds > 1 ? "s" : "");
			formattedTimer += (formattedTimer.length() != 0 ? ", " : "") + seconds + unit;
		}

		return formattedTimer;
	}
	
	/**
	 * This method will check if the given string matches the timer sign format which is a colon delimited set of 
	 * time amounts. Supported time intervals are days, hours, and minutes in the format '#d:#h:#m'
	 * <br>Example time amounts:
	 * <ul><li>11d:12h:30m</li><li>5d:30m</li><li>5d:45m</li><li>45m</li></ul>
	 * 
	 * @param duration - The amount of time given in the format stated above
	 * @return the duration of time in milliseconds from the sign formatted duration String
	 */
	public static long getDuration(String duration) {
		if (duration != null) {
			if (duration.matches("^\\d+[dD](:\\d+[hH](:\\d+[mM])?)?$|^\\d+[dD]:\\d+[mM]$|^\\d+[hH](:\\d+[mM])?$|^\\d+[mM]$")) {
				int days = 0;
				int hours = 0;
				int minutes = 0;
				
				Pattern pattern;
				Matcher matcher;
				String daysRegex = "\\d+[dD]";
				pattern = Pattern.compile(daysRegex); 
				matcher = pattern.matcher(duration);
				if (matcher.find()) {
					days = Integer.parseInt(matcher.group().replaceAll("[^\\d]", ""));
				}
				String hoursRegex = "\\d+[hH]";
				pattern = Pattern.compile(hoursRegex); 
				matcher = pattern.matcher(duration);
				if (matcher.find()) {
					hours = Integer.parseInt(matcher.group().replaceAll("[^\\d]", ""));
				}
				String minutesRegex = "\\d+[mM]";
				pattern = Pattern.compile(minutesRegex); 
				matcher = pattern.matcher(duration);
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
	
	public static Block getTargetBlock(Player player, Integer range) {
		Block block = null;
        BlockIterator iter = new BlockIterator(player, range);
        while (iter.hasNext()) {
        	block = iter.next();
            if (!block.getType().equals(Material.AIR)) {
            	return block;
            }
        }
        return null;
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
	
	/**
	 * Check if a players inventory has 1 or more open spaces
	 * @param player - The player to check
	 * @return true if the players inventory has 1 or more open inventory slots, and false otherwise
	 */
	public static boolean isInventoryFree(Player player) {
	    return isInventoryFree(player, 1);
	}
	
	/**
	 * Check if a players inventory has 'amount' open spaces
	 * @param player - The player to check
	 * @param amount - The number of needed open invenotry slots to be considered free
	 * @return true if the players inventory has 'amount' or more open inventory slots, and false otherwise
	 */
    public static boolean isInventoryFree(Player player, int amount) {
        Inventory inv = player.getInventory();
        ItemStack[] items = inv.getContents();
        int emptySlots = 0;
        for (ItemStack is : items) {
            if (is == null) {
                emptySlots++;
            }
        }
        if (emptySlots >= amount) {
            return true;
        }
        return false;
    }

	public static Sign getSignState(final Block block) {
		Sign sign = null;
		if (block != null) {
			if(block.getType().equals(Material.SIGN_POST) || block.getType().equals(Material.WALL_SIGN)) {
	            sign = (Sign) block.getState();
	        }
		}
        return sign;
	}
	
	public static void resetSign(LocationMetadata signLoc) {
		MetadataValue blockIdVal = signLoc.getMetaData().get(Keys.BLOCKID);
		MetadataValue blockDataVal = signLoc.getMetaData().get(Keys.BLOCKDATA);
		if (blockIdVal != null && blockDataVal != null) {
			int blockId = blockIdVal.asInt();
			byte blockData = blockDataVal.asByte();
			if (blockId == 68 || blockId == 63) {
				signLoc.getBlock().setTypeIdAndData(blockId, blockData, true);
			}
		}
	}
	
	public static void clearSign(Sign sign) {
		if (sign != null) {
			sign.setLine(0, "");
			sign.setLine(1, "");
			sign.setLine(2, "");
			sign.setLine(3, "");
			sign.update();
		}
	}
	
	public static ChatColor getChatColor(String color) {
		if (color != null) {
			if (color.length() == 1) {
				return ChatColor.getByChar(color.charAt(0));
			}
			else if (color.length() == 2 && color.startsWith("&")) {
				return ChatColor.getByChar(color.charAt(1));
			}
			for (ChatColor chatColor : ChatColor.values()) {
				if (chatColor.name().equalsIgnoreCase(color)) {
					return chatColor;
				}
			}
		}
		return null;
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
