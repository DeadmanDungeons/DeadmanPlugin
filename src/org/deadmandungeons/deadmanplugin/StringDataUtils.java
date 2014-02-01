package org.deadmandungeons.deadmanplugin;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;


public class StringDataUtils {
	
	private StringDataUtils(){}
	
	
	public static Location toLocation(String entry) {
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
	 * This is used to convert the config keys in the format of a location
	 * @param config key
	 * @return a Location from a String in the format of 'X#Y#Z#Wworld'
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

	public static List<String> formatLocationList(List<Location> locationList) {
		List<String> stringList = new ArrayList<String>();
		for (Location loc : locationList) {
			stringList.add(formatLocation(loc, false));
		}
		return stringList;
	}
	
	public static List<String> formatLocationKeyList(List<Location> locationList) {
		List<String> stringList = new ArrayList<String>();
		for (Location loc : locationList) {
			stringList.add(formatLocationKey(loc));
		}
		return stringList;
	}
	
	public static String formatLocationKey(Location loc) {
		return "X" + loc.getBlockX() + "Y" + loc.getBlockY() + "Z" + loc.getBlockZ() + "W" + loc.getWorld().getName();
	}

	
	public static Long getLong(String entry, Keys key) {
		String regex = key + "-?\\d+";
		Pattern patern = Pattern.compile(regex); 
		Matcher matcher = patern.matcher(entry);
		if (matcher.find()) {
			return Long.parseLong(matcher.group().replace(key.toString(), ""));
		}
		return null;
	}
	
	public static Integer getInt(String entry, Keys key) {
		Long longResult = getLong(entry, key);
		if (longResult != null && longResult > Integer.MIN_VALUE && longResult < Integer.MAX_VALUE) {
			return (int)longResult.longValue();
		}
		return null;
	}
	
	public static Double getDouble(String entry, Keys key) {
		String regex = key + "-?\\d+(\\.\\d+)?";
		Pattern patern = Pattern.compile(regex); 
		Matcher matcher = patern.matcher(entry);
		if (matcher.find()) {
			return Double.parseDouble(matcher.group().replace(key.toString(), ""));
		}
		return null;
	}
	
	public static World getWorld(String entry, Keys key) {
		String regex = key + ".+?(?=,)";
		Pattern patern = Pattern.compile(regex); 
		Matcher matcher = patern.matcher(entry);
		if (matcher.find()) {
			return Bukkit.getWorld(matcher.group().replace(key.toString(), ""));
		}
		return null;
	}
	
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
