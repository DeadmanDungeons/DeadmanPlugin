package com.deadmandungeons.deadmanplugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.BlockIterator;

import com.deadmandungeons.deadmanplugin.filedata.DataEntry;

/**
 * A utility class containing various useful methods which are commonly used throughout Deadman plugins
 * @author Jon
 */
public class DeadmanUtils {
	
	private static final Pattern UUID_PATTERN = Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$");
	
	// protected constructor to allow this util class to be extended
	protected DeadmanUtils() {}
	
	/**
	 * This will return a rounded String representation of the given duration in milliseconds
	 * with a minimum of 1 time unit and a maximum of 2 time units.<br>
	 * Examples:
	 * <ul>
	 * <li>3785300: 1 hr, 3 min</li>
	 * <li>81074000: 22 hrs, 31 min</li>
	 * <li>97440000: 1 day, 3 hrs</li>
	 * <li>478200: 7 minutes</li>
	 * <li>18056000: 5 hours</li>
	 * </ul>
	 * @param millis - The amount of time in milliseconds
	 * @return - the amount of time passed in the format of:<br>
	 * # day(s), # (hour|hr)(s), # (minute|min)(s), # second(s)
	 */
	public static String formatDuration(long millis) {
		String formattedTimer = "";
		
		int days = (int) TimeUnit.MILLISECONDS.toDays(millis);
		long hours = TimeUnit.MILLISECONDS.toHours(millis) - (days * 24);
		long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) - (TimeUnit.MILLISECONDS.toHours(millis) * 60);
		long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) - (TimeUnit.MILLISECONDS.toMinutes(millis) * 60);
		
		if (days != 0) {
			formattedTimer += days + " day" + (days > 1 ? "s" : "");
		}
		if (hours != 0) {
			String unit = (days > 0 || (days == 0 && minutes > 0) ? " hr" : " hour") + (hours > 1 ? "s" : "");
			formattedTimer += (formattedTimer.length() != 0 ? ", " : "") + hours + unit;
		}
		if (minutes != 0 && days == 0) {
			String unit = (hours > 0) ? " min" : " minute" + (minutes > 1 ? "s" : "");
			formattedTimer += (formattedTimer.length() != 0 ? ", " : "") + minutes + unit;
		}
		if (seconds != 0 && days == 0 && hours == 0 && minutes == 0) {
			String unit = " second" + (seconds > 1 ? "s" : "");
			formattedTimer += (formattedTimer.length() != 0 ? ", " : "") + seconds + unit;
		}
		
		return !formattedTimer.isEmpty() ? formattedTimer : "0 seconds";
	}
	
	/**
	 * This method will parse a colon delimited set of time amounts and calculate the duration in milliseconds.
	 * Supported time intervals are days, hours, and minutes in the format '#d:#h:#m' <br>
	 * Example time amounts:
	 * <ul>
	 * <li>11d:12h:30m</li>
	 * <li>5d:30m</li>
	 * <li>5d:45m</li>
	 * <li>45m</li>
	 * </ul>
	 * @param durationStr - The String representation of a duration of time given in the format stated above
	 * @return the duration of time in milliseconds from the formatted duration String. 0 will be returned if durationStr is invalid.
	 */
	public static long parseDuration(String durationStr) {
		long duration = 0;
		if (durationStr != null) {
			for (String durationPartStr : durationStr.split(":")) {
				long durationPart = parseDurationPart(durationPartStr.trim());
				if (durationPart == -1) {
					return 0;
				}
				duration += durationPart;
			}
		}
		return duration;
	}
	
	private static long parseDurationPart(String durationPart) {
		if (durationPart == null || durationPart.length() < 2) {
			return -1;
		}
		String duration = durationPart.substring(0, durationPart.length() - 1);
		if (!isNumeric(duration)) {
			return -1;
		}
		
		char measure = durationPart.charAt(durationPart.length() - 1);
		if (measure == 'd' || measure == 'D') {
			return TimeUnit.DAYS.toMillis(Integer.parseInt(duration));
		} else if (measure == 'h' || measure == 'H') {
			return TimeUnit.HOURS.toMillis(Integer.parseInt(duration));
		} else if (measure == 'm' || measure == 'M') {
			return TimeUnit.MINUTES.toMillis(Integer.parseInt(duration));
		}
		return -1;
	}
	
	public static String getOrdinalSuffix(int value) {
		switch (value % 10) {
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
	
	public static boolean isPlayerWithinRadius(Player player, Location loc, double radius) {
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
	
	/**
	 * @param block - The block to check for a Sign block state
	 * @return the Sign block state the given block has or null if the given block is not a sign
	 */
	public static Sign getSignState(Block block) {
		return getSignState(block, (MaterialData) null);
	}
	
	/**
	 * Synonymous to {@link #getSignState(Block, MaterialData) getSignState(block, dataEntry.getMaterialData());}
	 * @param block - The block to check for a Sign block state
	 * @param dataEntry - The DataEntry containing the Sign {@link MaterialData} that will be used to force reset the
	 * state of the given block if it is not a sign. If this is null, the block will not be force reset to a sign block state.
	 * @return the Sign block state the given block had (or has if its block state was reset). Or null if the given block
	 * is not a sign and it was not forcefully reset as one.
	 */
	public static Sign getSignState(Block block, DataEntry dataEntry) {
		return getSignState(block, dataEntry.getMaterialData());
	}
	
	/**
	 * @param block - The block to check for a Sign block state
	 * @param data - The Sign {@link MaterialData} that will be used to force reset the state of the given block if it is not a sign.
	 * If this is null, the block will not be force reset to a sign block state.
	 * @return the Sign block state the given block had (or has if its block state was reset). Or null if the given block
	 * is not a sign and it was not forcefully reset as one.
	 */
	public static Sign getSignState(Block block, MaterialData data) {
		Sign sign = null;
		if (block != null) {
			if (isSign(block.getType())) {
				sign = (Sign) block.getState();
			}
			if (sign == null && data != null && isSign(data.getItemType())) {
				boolean success = block.setTypeIdAndData(data.getItemTypeId(), data.getData(), true);
				sign = (success ? getSignState(block) : null);
			}
		}
		return sign;
	}
	
	public static boolean isSign(Material type) {
		return type == Material.SIGN_POST || type == Material.WALL_SIGN;
	}
	
	/**
	 * @param block - The block to reset
	 * @param dataEntry - The DataEntry containing the {@link MaterialData} of what the given block should be reset to.
	 * @return true if the given block was successfully reset using the MaterialData specified by the given DataEntry.
	 * Or false if the given block or dataEntry is null or if dataEntry does not specify MaterialData.
	 */
	public static boolean resetBlock(Block block, DataEntry dataEntry) {
		if (block != null && dataEntry != null) {
			MaterialData data = dataEntry.getMaterialData();
			if (data != null) {
				return block.setTypeIdAndData(data.getItemTypeId(), data.getData(), true);
			}
		}
		return false;
	}
	
	public static void clearSign(Sign sign) {
		if (sign != null && sign.getBlock().getState().equals(sign)) {
			sign.setLine(0, "");
			sign.setLine(1, "");
			sign.setLine(2, "");
			sign.setLine(3, "");
			sign.update(true);
		}
	}
	
	public static List<String> toStringList(Collection<?> list) {
		List<String> strList = new ArrayList<String>();
		for (Object value : list) {
			strList.add(value.toString());
		}
		return strList;
	}
	
	
	public static <T> T getMetadata(Plugin plugin, Metadatable metadatable, String key, Class<T> type) {
		for (MetadataValue value : metadatable.getMetadata(key)) {
			if (value.getOwningPlugin().equals(plugin)) {
				if (value.value() != null && type.isAssignableFrom(value.value().getClass())) {
					return type.cast(value.value());
				}
			}
		}
		return null;
	}
	
	public static boolean isInteger(String number) {
		try {
			Integer.parseInt(number);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}
	
	public static boolean isNumeric(String number) {
		return number != null && !number.isEmpty() && StringUtils.isNumeric(number);
	}
	
	public static boolean isUUID(String uuid) {
		return UUID_PATTERN.matcher(uuid).matches();
	}
	
}
