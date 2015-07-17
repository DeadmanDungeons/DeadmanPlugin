package org.deadmandungeons.deadmanplugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
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
import org.deadmandungeons.deadmanplugin.filedata.DataEntry;
import org.deadmandungeons.deadmanplugin.filedata.DataEntry.Key;

/**
 * A utility class containing various useful methods which are commonly used throughout Deadman plugins
 * @author Jon
 */
public class DeadmanUtils {
	
	private static final String DURATION_REGEX = "^\\d+[dD](:\\d+[hH](:\\d+[mM])?)?$|^\\d+[dD]:\\d+[mM]$|^\\d+[hH](:\\d+[mM])?$|^\\d+[mM]$";
	private static final Pattern DURATION_PATTERN = Pattern.compile(DURATION_REGEX);
	private static final Pattern LOCATION_PATTERN = Pattern.compile("X(-?\\d+)Y(-?\\d+)Z(-?\\d+)W(.+)");
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
	public static String getDurationString(long millis) {
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
	 * This method will check if the given string matches the timer sign format which is a colon delimited set of
	 * time amounts. Supported time intervals are days, hours, and minutes in the format '#d:#h:#m' <br>
	 * Example time amounts:
	 * <ul>
	 * <li>11d:12h:30m</li>
	 * <li>5d:30m</li>
	 * <li>5d:45m</li>
	 * <li>45m</li>
	 * </ul>
	 * @param duration - The amount of time given in the format stated above
	 * @return the duration of time in milliseconds from the sign formatted duration String
	 */
	public static long getDuration(String duration) {
		if (duration != null && DURATION_PATTERN.matcher(duration).matches()) {
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
		return 0;
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
	
	public static Sign getSignState(Block block) {
		return getSignState(block, null);
	}
	
	public static Sign getSignState(Block block, MaterialData data) {
		Sign sign = null;
		if (block != null) {
			if (block.getType() == Material.SIGN_POST || block.getType() == Material.WALL_SIGN) {
				sign = (Sign) block.getState();
			}
			if (sign == null && data != null) {
				boolean success = block.setTypeIdAndData(data.getItemTypeId(), data.getData(), true);
				if (success && (block.getType() == Material.SIGN_POST || block.getType() == Material.WALL_SIGN)) {
					sign = (Sign) block.getState();
				}
			}
		}
		return sign;
	}
	
	public static boolean resetBlock(Block block, DataEntry dataEntry) {
		if (block != null && dataEntry != null) {
			Number blockId = dataEntry.getNumber(Key.ID);
			Number blockData = dataEntry.getNumber(Key.DATA);
			if (blockId != null && blockData != null) {
				return block.setTypeIdAndData(blockId.intValue(), blockData.byteValue(), true);
			}
		}
		return false;
	}
	
	public static void clearSign(Sign sign) {
		if (sign != null) {
			sign.setLine(0, "");
			sign.setLine(1, "");
			sign.setLine(2, "");
			sign.setLine(3, "");
			sign.update(true);
		}
	}
	
	/**
	 * This is used to convert the given String config key into a Location object<br />
	 * The given String key must be in the format of:<br />
	 * <code>X&lt;x-coord&gt;Y&lt;y-coord&gt;Z&lt;z-coord&gt;W&lt;world&gt;</code><br />
	 * Example: X351Y154Z-1478Wempire
	 * @param key - The String key in the format of a location to be converted into a Location
	 * @return The Location the given String key represents or null if the key is improperly formatted
	 */
	public static Location getLocationFromKey(String key) {
		if (key != null && !key.isEmpty()) {
			Matcher matcher = LOCATION_PATTERN.matcher(key);
			if (matcher.find()) {
				int xCoord = Integer.parseInt(matcher.group(1));
				int yCoord = Integer.parseInt(matcher.group(2));
				int zCoord = Integer.parseInt(matcher.group(3));
				World world = Bukkit.getWorld(matcher.group(4));
				if (world != null) {
					return new Location(world, xCoord, yCoord, zCoord);
				}
			}
		}
		return null;
	}
	
	/**
	 * @param loc - The Location that the returned String should represent
	 * @return A String representation of the given Location in the format used for config data keys
	 */
	public static String formatLocationKey(Location loc) {
		return "X" + loc.getBlockX() + "Y" + loc.getBlockY() + "Z" + loc.getBlockZ() + "W" + loc.getWorld().getName();
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
	
	public static boolean isUUID(String uuid) {
		return UUID_PATTERN.matcher(uuid).matches();
	}
	
}
