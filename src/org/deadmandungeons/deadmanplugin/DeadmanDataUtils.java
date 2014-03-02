package org.deadmandungeons.deadmanplugin;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * <b>A utility class to get variables from a formatted string used for YAML data storage.</b> <br />
 * Variables that are stored as config data <b>entries</b> are in the following format:<br />
 * <code>&lt;key&gt;: &lt;value&gt;, &lt;key&gt;: &lt;value&gt;, &lt;key&gt;: &lt;value&gt;</code><br />
 * Example: <code>World: empire, X: 140, Y: 98, Z: 354, ID: 135, Data: 2</code><br /><br />
 * Variables that are stored as config data <b>keys</b> are in the following format:<br />
 * <code>X&lt;x-coord&gt;Y&lt;y-coord&gt;Z&lt;z-coord&gt;W&lt;world&gt;</code><br />
 * Example: X351Y154Z1478Wempire
 * Use the {@link Keys} enum to map each value.
 * @author Jon
 */
public class DeadmanDataUtils {
	
	/**
	 * Do not instantiate this class. Public constructor must be provided to be extended from each plugin
	 * @throws AssertionError
	 */
	public DeadmanDataUtils() throws AssertionError {
		throw new AssertionError("Do not instantiate this class. Public constructor " 
				+ "must be provided to be extended from each plugin");
	}
	
	/**
	 * This is used to convert the given String entry into a Location object.<br />
	 * The given String must contain a minimum of the following Keys:<br />
	 * {@link Keys.WORLD}, {@link Keys.XCOORD}, {@link Keys.YCOORD}, {@link Keys.ZCOORD}
	 * @param entry - The String entry containing all of the necessary Location Keys and value pairs
	 * @return The Location that the given String described. null will be returned if the entry String
	 * was not formatted properly, or there was a missing key and value pair 
	 */
	public static Location getLocationFromEntry(String entry) {
		if (entry == null || entry.isEmpty()) {
			return null;
		}
		World world = getWorld(entry, Keys.WORLD);
		Double xCoord = getDouble(entry, Keys.XCOORD);
		Double yCoord = getDouble(entry, Keys.YCOORD);
		Double zCoord = getDouble(entry, Keys.ZCOORD);
		if (world != null && xCoord != null && yCoord != null && zCoord != null) {
			Double yaw = getDouble(entry, Keys.YAW);
			Double pitch = getDouble(entry, Keys.PITCH);
			if (yaw != null && pitch != null) {
				return new Location(world, xCoord.doubleValue(), yCoord.doubleValue(), zCoord.doubleValue(), yaw.floatValue(), pitch.floatValue());
			}
			return new Location(world, xCoord.doubleValue(), yCoord.doubleValue(), zCoord.doubleValue());
		}
		return null;
	}
	
	
	/**
	 * This is used to convert the given String config key into a Location object<br />
	 * The given String key must be in the format of:<br />
	 * <code>X&lt;x-coord&gt;Y&lt;y-coord&gt;Z&lt;z-coord&gt;W&lt;world&gt;</code><br />
	 * Example: X351Y154Z1478Wempire
	 * @param key - The String key int the format of a location to be converted into a Location
	 * @return The Location the given String key represents or null if the key is improperly formatted
	 */
	public static Location getLocationFromKey(String key) {
		if (key == null || key.isEmpty()) {
			return null;
		}
		Pattern coordPattern = Pattern.compile("((?<=[XYZ])-?\\d+)");
		Matcher matcher = coordPattern.matcher(key);
		Integer xCoord = null, yCoord = null, zCoord = null;
		if (matcher.find()) {
			xCoord = Integer.parseInt(matcher.group());
		}
		if (matcher.find()) {
			yCoord = Integer.parseInt(matcher.group());
		}
		if (matcher.find()) {
			zCoord = Integer.parseInt(matcher.group());
		}
		World world = Bukkit.getWorld(key.replaceAll("([XYZ]-?\\d+){3}W", ""));
		if (xCoord != null && yCoord != null && zCoord != null && world != null) {
			return new Location(world, xCoord.intValue(), yCoord.intValue(), zCoord.intValue());
		}
		return null;
	}

	/**
	 * @param loc - The Location object that the returned String should represent
	 * @param withDirection - a flag to specify if Yaw and Pitch should be included in the returned String
	 * @return A String representation of the given Location in the format used for config data entries
	 */
	public static String formatLocation(Location loc, boolean withDirection) {
		String formatted = "";
		if (!withDirection) {
			formatted = Keys.WORLD + loc.getWorld().getName() + ", " + Keys.XCOORD + loc.getBlockX() 
					+ ", " + Keys.YCOORD + loc.getBlockY() + ", " + Keys.ZCOORD + loc.getBlockZ();
		} else {
			formatted = Keys.WORLD + loc.getWorld().getName() + ", " + Keys.XCOORD + loc.getX() 
					+ ", " + Keys.YCOORD + loc.getY() + ", " + Keys.ZCOORD + loc.getZ() 
					+ ", " + Keys.YAW + loc.getYaw() + ", " + Keys.PITCH + loc.getPitch();
		}
		
		return formatted;
	}
	
	/**
	 * @param loc - The Location that the returned String should represent
	 * @return A String representation of the given Location in the format used for config data keys
	 */
	public static String formatLocationKey(Location loc) {
		return "X" + loc.getBlockX() + "Y" + loc.getBlockY() + "Z" + loc.getBlockZ() + "W" + loc.getWorld().getName();
	}

	/**
	 * A convenience method to format a List of Locations in the format used in config data entries
	 * @param locationList - The List of Locations that should be represented in the returned list of Strings
	 * @return A List Strings containing all of the given Locations in the format used for config data entries
	 */
	public static List<String> formatLocationList(List<Location> locationList) {
		List<String> stringList = new ArrayList<String>();
		for (Location loc : locationList) {
			stringList.add(formatLocation(loc, false));
		}
		return stringList;
	}
	
	/**
	 * A convenience method to format a List of Locations in the format used in config data keys
	 * @param locationList - The List of Locations that should be represented in the returned list of Strings
	 * @return A List Strings containing all of the given Locations in the format used for config data keys
	 */
	public static List<String> formatLocationKeyList(List<Location> locationList) {
		List<String> stringList = new ArrayList<String>();
		for (Location loc : locationList) {
			stringList.add(formatLocationKey(loc));
		}
		return stringList;
	}
	
	public static String formatLocationData(LocationMetadata locData, boolean withDirection) {
		String formatted = formatLocation(locData, withDirection);
		for (Keys key : locData.getMetaData().keySet()) {
			formatted += ", " + key + locData.getMetaData().get(key).asString();
		}
		return formatted;
	}
	
	public static List<String> formatLocationDataList(List<LocationMetadata> locationList) {
		List<String> stringList = new ArrayList<String>();
		for (LocationMetadata loc : locationList) {
			stringList.add(formatLocationData(loc, false));
		}
		return stringList;
	}
	
	public static List<Location> getLocationEntryList(List<String> stringList) {
		List<Location> locationList = new ArrayList<Location>();
		for (String stringLoc : stringList) {
			Location loc = getLocationFromEntry(stringLoc);
			if (loc != null) {
				locationList.add(loc);
			}
		}
		return locationList;
	}
	

	/**
	 * @param entry - The String entry containing the desired Long variable
	 * @param key - The Key of the desired Long variable
	 * @return A Long object of the desired long value, or null if a Long value did not exist at the given Key
	 */
	public static Long getLong(String entry, Keys key) {
		String regex = key + "-?\\d+";
		Pattern patern = Pattern.compile(regex); 
		Matcher matcher = patern.matcher(entry);
		if (matcher.find()) {
			return Long.parseLong(matcher.group().replace(key.toString(), ""));
		}
		return null;
	}
	
	/**
	 * @param entry - The String entry containing the desired Integer variable
	 * @param key - The Key of the desired Integer variable
	 * @return An Integer object of the desired int value, or null if a Integer value did not exist at the given Key
	 */
	public static Integer getInt(String entry, Keys key) {
		Long longResult = getLong(entry, key);
		if (longResult != null && longResult > Integer.MIN_VALUE && longResult < Integer.MAX_VALUE) {
			return (int)longResult.longValue();
		}
		return null;
	}
	
	/**
	 * @param entry - The String entry containing the desired Double variable
	 * @param key - The Key of the desired Double variable
	 * @return A Double object of the desired double value, or null if a Double value did not exist at the given Key
	 */
	public static Double getDouble(String entry, Keys key) {
		String regex = key + "-?\\d+(\\.\\d+)?";
		Pattern patern = Pattern.compile(regex); 
		Matcher matcher = patern.matcher(entry);
		if (matcher.find()) {
			return Double.parseDouble(matcher.group().replace(key.toString(), ""));
		}
		return null;
	}
	
	/**
	 * @param entry - The String entry containing the desired WorldName variable
	 * @param key - The Key of the desired WorldName variable
	 * @return The World defined at the given key, or null if a WorldName value did not exist at the given Key
	 */
	public static World getWorld(String entry, Keys key) {
		String regex = key + ".+?(?=,)";
		Pattern patern = Pattern.compile(regex); 
		Matcher matcher = patern.matcher(entry);
		if (matcher.find()) {
			return Bukkit.getWorld(matcher.group().replace(key.toString(), ""));
		}
		return null;
	}
	
	/**
	 * @param entry - The String entry containing the desired variable
	 * @param key - The Key of the desired variable
	 * @return The raw String defined at the given key, or null if a value did not exist at the given Key
	 */
	public static String getString(String entry, Keys key) {
		String regex = key + "([^,])+";
		Pattern patern = Pattern.compile(regex); 
		Matcher matcher = patern.matcher(entry);
		if (matcher.find()) {
			return matcher.group().replace(key.toString(), "");
		}
		return null;
	}
	
}
