package org.deadmandungeons.deadmanplugin.filedata;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.material.MaterialData;
import org.deadmandungeons.deadmanplugin.DeadmanUtils;
import org.deadmandungeons.deadmanplugin.PlayerID;
import org.deadmandungeons.deadmanplugin.WorldCoord;
import org.deadmandungeons.deadmanplugin.timer.GlobalTimer;
import org.deadmandungeons.deadmanplugin.timer.LocalTimer;
import org.deadmandungeons.deadmanplugin.timer.Timer;

import com.google.common.collect.ImmutableMap;

/**
 * A container class for the Key/Value pairs in a single data entry in a YAML file.<br>
 * Additional formatting utility methods are provided to format specific objects as specified by {@link #toString()}<br>
 * Enum constants are used as the keys to a DataEntry value to enforce uniformity and validity in
 * the parsed Keys.
 * @see {@link #toString()} for information on the format of a DataEntry
 * @author Jon
 */
public class DataEntry implements Cloneable {
	
	// Matches a java enum constant for the key group, and any character that is not a comma as the value group, separated by a colon
	private static final Pattern VALUE_PATTERN = Pattern.compile("([a-zA-Z][a-zA-Z0-9$_]*?):([^,]+)");
	private static final String INVALID_MSG_1 = "The given value has a comma character. Commas are not allowed";
	private static final String INVALID_MSG_2 = "The value for key '%s' has a comma character. Commas are not allowed";
	
	private final Map<String, Object> values;
	
	/**
	 * Construct an empty DataEntry instance
	 */
	public DataEntry() {
		this((String) null);
	}
	
	/**
	 * Construct a DataEntry instance with the key/value pairs defined in the given entryStr.<br>
	 * The given String entry should be in the format as specified by {@link DataEntry#toString()}
	 * @param entry - The raw data entry String containing the key/value pairs to include in the returned DataEntry
	 */
	public DataEntry(String entryStr) {
		values = new HashMap<String, Object>();
		if (entryStr != null) {
			Matcher valueMatcher = VALUE_PATTERN.matcher(entryStr);
			while (valueMatcher.find()) {
				String key = valueMatcher.group(1);
				String value = valueMatcher.group(2);
				values.put(key.toUpperCase(), value);
			}
		}
	}
	
	/**
	 * This simply returns {@link #DataEntry(Block)}.<br>
	 * In some circumstances, the english-like nature of the code to invoke this method may
	 * be more understandable and readable than using the constructor.
	 * @param entryStr - The raw data entry String containing the key/value pairs to include in the returned DataEntry
	 * @return a new DataEntry instance with the key/value pairs defined in the given entryStr
	 */
	public static DataEntry of(String entryStr) {
		return new DataEntry(entryStr);
	}
	
	/**
	 * @return a new DataEntry.Builder instance
	 */
	public static Builder<?> builder() {
		return new BuilderImpl();
	}
	
	private static class BuilderImpl extends Builder<BuilderImpl> {
		
		@Override
		protected BuilderImpl self() {
			return this;
		}
	}
	
	/**
	 * The Builder for a DataEntry
	 * @see {@link DataEntry}
	 * @author Jon
	 */
	public static abstract class Builder<T extends Builder<T>> {
		
		private Map<String, Object> values = new HashMap<String, Object>();
		
		private Location location;
		private MaterialData materialData;
		private Timer timer;
		private PlayerID playerId;
		
		protected abstract T self();
		
		
		/**
		 * @deprecated for {@link #location(Location)}
		 */
		@Deprecated
		public final T withLocation(Location location) {
			return location(location);
		}
		
		/**
		 * @see {@link DataEntry#setLocation(Location)}
		 * @param location - The location to set in the built DataEntry
		 * @return this builder
		 */
		public final T location(Location location) {
			this.location = location;
			return self();
		}
		
		
		/**
		 * @deprecated for {@link #materialData(MaterialData)}
		 */
		@Deprecated
		public final T withMaterialData(MaterialData materialData) {
			return materialData(materialData);
		}
		
		/**
		 * @see {@link DataEntry#setMaterialData(MaterialData)}
		 * @param materialData - The MaterialData to set in the built DataEntry
		 * @return this builder
		 */
		public final T materialData(MaterialData materialData) {
			this.materialData = materialData;
			return self();
		}
		
		/**
		 * @deprecated for {@link #timer(Timer)}
		 */
		@Deprecated
		public final T withTimer(Timer timer) {
			return timer(timer);
		}
		
		/**
		 * @param timer - The Timer to set in the built DataEntry
		 * @return this builder
		 */
		public final T timer(Timer timer) {
			this.timer = timer;
			return self();
		}
		
		/**
		 * @deprecated for {@link #playerID(PlayerID)}
		 */
		@Deprecated
		public final T withPlayerID(PlayerID playerId) {
			return playerID(playerId);
		}
		
		/**
		 * @param playerId - The PlayerID to set in the built DataEntry
		 * @return this builder
		 */
		public final T playerID(PlayerID playerId) {
			this.playerId = playerId;
			return self();
		}
		
		/**
		 * @deprecated for {@link #value(Enum, Object)}
		 */
		@Deprecated
		public final T withValue(Enum<?> key, Object value) {
			return value(key, value);
		}
		
		/**
		 * <b>Note:</b> The given value cannot have a comma in the string returned by its toString invocation
		 * @param key - The Enum Key representing the value to set
		 * @param value - The value to set. Indexed by the given Key.<br>
		 * @throws IllegalArgumentException if the given value has a comma in its String representation
		 * @return this builder
		 */
		public final T value(Enum<?> key, Object value) {
			if (value.toString().contains(",")) {
				throw new IllegalArgumentException(String.format(INVALID_MSG_2, key));
			}
			values.put(key.name().toUpperCase(), value);
			return self();
		}
		
		/**
		 * @return a new DataEntry for the key/value pairs specified in this build
		 * @throws IllegalArgumentException - if one of the values has a comma in the string returned by its toString invocation
		 */
		public DataEntry build() throws IllegalArgumentException {
			return new DataEntry(this);
		}
	}
	
	
	protected DataEntry(Builder<?> builder) {
		values = builder.values;
		
		if (builder.location != null) {
			setLocation(builder.location);
		}
		if (builder.materialData != null) {
			setMaterialData(builder.materialData);
		}
		if (builder.timer != null) {
			setTimer(builder.timer);
		}
		if (builder.playerId != null) {
			setPlayerID(builder.playerId);
		}
	}
	
	
	/**
	 * @param key - The Enum Key representing the value to get
	 * @return the value that the given Enum key represents in this DataEntry, or null if no value exists for the given key
	 */
	public final Object getValue(Enum<?> key) {
		return getValue(key, null);
	}
	
	/**
	 * @param key - The Enum Key representing the value to get
	 * @param def - The default value to return if no value exists for the given key
	 * @return the value that the given Enum key represents in this DataEntry,
	 * or the given default if no value exists for the given key
	 */
	public final Object getValue(Enum<?> key, Object def) {
		Object value = values.get(key.name().toUpperCase());
		return value != null ? value : def;
	}
	
	/**
	 * <b>Note:</b> The given value cannot have a comma in the string returned by its toString invocation
	 * @param key - The Enum key representing the value to set
	 * @param value - The value to set. Indexed by the given Key.<br>
	 * If value is null, the key/value pair for the given key will be removed from this DataEntry.
	 */
	public final void setValue(Enum<?> key, Object value) {
		Validate.notNull(key, "key cannot be null");
		if (value != null) {
			Validate.isTrue(!value.toString().contains(","), INVALID_MSG_1);
			values.put(key.name().toUpperCase(), value);
		} else {
			values.remove(key.name().toUpperCase());
		}
	}
	
	
	/**
	 * @param key - The Enum Key representing the Number to get
	 * @return A Number object of the value indexed by the given Key,
	 * or null if a Number value did not exist at the given Key
	 */
	public final Number getNumber(Enum<?> key) {
		return getNumber(key, null);
	}
	
	/**
	 * @param key - The Enum Key representing the Number to get
	 * @param def - The default Number to return if no Number value exists for the given key
	 * @return A Number object of the value indexed by the given Key,
	 * or the default Number if a Number value did not exist at the given Key
	 */
	public final Number getNumber(Enum<?> key, Number def) {
		Object value = getValue(key);
		if (value != null) {
			if (value instanceof Number) {
				return (Number) value;
			} else if (NumberUtils.isNumber(value.toString())) {
				Number number = NumberUtils.createNumber(value.toString());
				setValue(key, number);
				return number;
			}
		}
		return def;
	}
	
	
	/**
	 * @return The {@link World} object indexed at key {@link Key#WORLD},
	 * or null if a World value does not exist for the respective Key
	 */
	public final World getWorld() {
		Object value = getValue(Key.WORLD);
		if (value != null) {
			World world = Bukkit.getWorld(value.toString());
			if (world != null) {
				setValue(Key.WORLD, world.getName());
			}
			return world;
		}
		return null;
	}
	
	/**
	 * @param world - The {@link World} object to set. Indexed by key {@link Key#WORLD}.<br>
	 * If world is null, the World key/value pair will be removed from this DataEntry.
	 */
	public final void setWorld(World world) {
		setValue(Key.WORLD, world.getName());
	}
	
	
	/**
	 * @return the WorldCoord defined by keys: {@link Key#WORLD}, {@link Key#X}, {@link Key#Y}, {@link Key#Z}.
	 * Or null if the minimum required values did not exist, or were invalid
	 */
	public final WorldCoord getWorldCoord() {
		World world = getWorld();
		Number x = getNumber(Key.X);
		Number y = getNumber(Key.Y);
		Number z = getNumber(Key.Z);
		if (world != null && x != null && y != null && z != null) {
			int yInt = y.intValue();
			if (yInt >= 0 && yInt <= world.getMaxHeight()) {
				return new WorldCoord(world, x.intValue(), yInt, z.intValue());
			}
		}
		return null;
	}
	
	/**
	 * @param coord - The {@link WorldCoord} object to set and be represented by the
	 * {@link Key#WORLD}, {@link Key#X}, {@link Key#Y}, and {@link Key#Z} keys.<br>
	 * If coord is null, all of the above key/value pairs will be removed from this DataEntry.
	 */
	public final void setWorldCoord(WorldCoord coord) {
		if (coord != null) {
			setWorld(coord.getWorld());
			setValue(Key.X, coord.getX());
			setValue(Key.Y, coord.getY());
			setValue(Key.Z, coord.getZ());
		} else {
			setWorld(null);
			setValue(Key.X, null);
			setValue(Key.Y, null);
			setValue(Key.Z, null);
		}
	}
	
	/**
	 * @param coord - The {@link WorldCoord} to be formatted
	 * @return the formatted String representation of the given WorldCoord with the keys:
	 * {@link Key#WORLD}, {@link Key#X}, {@link Key#Y}, {@link Key#Z}
	 */
	public static String formatWorldCoord(WorldCoord coord) {
		return format(ImmutableMap.<Enum<?>, Object> of(Key.WORLD, coord.getWorld().getName(), Key.X, coord.getX(), Key.Y, coord.getY(), Key.Z,
				coord.getZ()));
	}
	
	
	/**
	 * @return the Location defined by keys: {@link Key#WORLD}, {@link Key#X}, {@link Key#Y},
	 * {@link Key#Z}, and optionally {@link Key#YAW}, {@link Key#PITCH}.
	 * Or null if the minimum required values did not exist, or were invalid
	 */
	public final Location getLocation() {
		World world = getWorld();
		Number x = getNumber(Key.X);
		Number y = getNumber(Key.Y);
		Number z = getNumber(Key.Z);
		if (world != null && x != null && y != null && z != null) {
			Location loc = new Location(world, x.doubleValue(), y.doubleValue(), z.doubleValue());
			Number yaw = getNumber(Key.YAW);
			Number pitch = getNumber(Key.PITCH);
			if (yaw != null && pitch != null) {
				loc.setYaw(yaw.floatValue());
				loc.setPitch(pitch.floatValue());
			}
			return loc;
		}
		return null;
	}
	
	/**
	 * @param location - The {@link Location} object to set and be represented by the
	 * {@link Key#WORLD}, {@link Key#X}, {@link Key#Y}, and {@link Key#Z} keys. <br>
	 * If the yaw or pitch is not 0, they will also be represented by the {@link Key#YAW}, and {@link Key#PITCH} keys. <br>
	 * If location is null, all of the above key/value pairs will be removed from this DataEntry.
	 */
	public final void setLocation(Location location) {
		if (location != null) {
			setWorld(location.getWorld());
			setValue(Key.X, doubleOrInt(location.getX()));
			setValue(Key.Y, doubleOrInt(location.getY()));
			setValue(Key.Z, doubleOrInt(location.getZ()));
			setValue(Key.YAW, (location.getYaw() != 0 ? location.getYaw() : null));
			setValue(Key.PITCH, (location.getPitch() != 0 ? location.getPitch() : null));
		} else {
			setWorld(null);
			setValue(Key.X, null);
			setValue(Key.Y, null);
			setValue(Key.Z, null);
			setValue(Key.YAW, null);
			setValue(Key.PITCH, null);
		}
	}
	
	/**
	 * @param loc - the {@link Location} to be formatted
	 * @return the formatted String representation of the given Location with the keys:
	 * {@link Key#WORLD}, {@link Key#X}, {@link Key#Y}, {@link Key#Z}, and optionally {@link Key#YAW}, {@link Key#PITCH}
	 */
	public static String formatLocation(Location loc) {
		ImmutableMap.Builder<Enum<?>, Object> mapBuilder = ImmutableMap.builder();
		mapBuilder.put(Key.WORLD, loc.getWorld().getName()).put(Key.X, doubleOrInt(loc.getX())).put(Key.Y, doubleOrInt(loc.getY())).put(Key.Z,
				doubleOrInt(loc.getZ()));
		if (loc.getYaw() != 0) {
			mapBuilder.put(Key.YAW, loc.getYaw());
		}
		if (loc.getPitch() != 0) {
			mapBuilder.put(Key.PITCH, loc.getPitch());
		}
		return format(mapBuilder.build());
	}
	
	private static Object doubleOrInt(double coordValue) {
		if (coordValue % 1 == 0) {
			return (int) coordValue;
		}
		return coordValue;
	}
	
	
	/**
	 * @return the {@link MaterialData} that this DataEntry describes with keys {@link Key#ID} and {@link Key#DATA}.
	 * null will be returned is there was a missing or invalid key/value pair
	 */
	public final MaterialData getMaterialData() {
		Number id = getNumber(Key.ID);
		Number data = getNumber(Key.DATA);
		if (id != null && data != null) {
			return new MaterialData(id.intValue(), data.byteValue());
		}
		return null;
	}
	
	/**
	 * @param materialData - The {@link MaterialData} to set and be represented by the {@link Key#ID} and {@link Key#DATA} keys.
	 */
	public final void setMaterialData(MaterialData materialData) {
		if (materialData != null) {
			setValue(Key.ID, materialData.getItemTypeId());
			setValue(Key.DATA, materialData.getData());
		} else {
			setValue(Key.ID, null);
			setValue(Key.DATA, null);
		}
	}
	
	/**
	 * @param materialData - the {@link MaterialData} to be formatted
	 * @return the formatted String representation of the given MaterialData with the {@link Key#ID} and {@link Key#DATA} keys.
	 */
	public static String formatMaterialData(MaterialData materialData) {
		return format(ImmutableMap.<Enum<?>, Object> of(Key.ID, materialData.getItemTypeId(), Key.DATA, materialData.getData()));
	}
	
	
	/**
	 * This DataEntry must contain the key/value pairs for the {@link Key#DURATION} key,
	 * and either the {@link Key#EXPIRE} key or the {@link Key#ELAPSED} key.
	 * @return The LocalTimer that this DataEntry describes. null will be returned if there was a missing or invalid key/value pair
	 */
	public final LocalTimer getLocalTimer() {
		Timer timer = getTimer();
		return (timer instanceof GlobalTimer ? ((GlobalTimer) timer).toLocalTimer() : (LocalTimer) timer);
	}
	
	/**
	 * This DataEntry must contain the key/value pairs for the {@link Key#DURATION} key,
	 * and either the {@link Key#EXPIRE} key or the {@link Key#ELAPSED} key.
	 * @return The GlobalTimer that this DataEntry describes. null will be returned if there was a missing or invalid key/value pair
	 */
	public final GlobalTimer getGlobalTimer() {
		Timer timer = getTimer();
		return (timer instanceof LocalTimer ? ((LocalTimer) timer).toGlobalTimer() : (GlobalTimer) timer);
	}
	
	/**
	 * This DataEntry must contain the key/value pairs for the {@link Key#DURATION} key,
	 * and either the {@link Key#EXPIRE} key or the {@link Key#ELAPSED} key.
	 * @return The GlobalTimer or LocalTimer that this DataEntry describes.
	 * null will be returned if there was a missing or invalid key/value pair
	 */
	public Timer getTimer() {
		Number duration = getNumber(Key.DURATION);
		if (duration != null && duration.longValue() > 0) {
			Number expire = getNumber(Key.EXPIRE);
			Number elapsed = getNumber(Key.ELAPSED);
			if (expire != null && expire.longValue() > 0) {
				return new GlobalTimer(duration.longValue(), expire.longValue());
			} else if (elapsed != null && elapsed.longValue() >= 0) {
				return new LocalTimer(duration.longValue(), elapsed.longValue());
			}
		}
		return null;
	}
	
	/**
	 * @param timer - The {@link Timer} object to set and be represented by the {@link Key#DURATION} key,
	 * as well as the {@link Key#EXPIRE} key if the given Timer is a {@link GlobalTimer}, or
	 * the {@link Key#ELAPSED} key if the given Timer is a {@link LocalTimer}.<br>
	 * If timer is null, all of the above key/value pairs will be removed from this DataEntry.
	 */
	public final void setTimer(Timer timer) {
		if (timer != null) {
			if (timer instanceof GlobalTimer) {
				setValue(Key.DURATION, timer.getDuration());
				setValue(Key.EXPIRE, ((GlobalTimer) timer).getExpire());
			} else {
				setValue(Key.DURATION, timer.getDuration());
				setValue(Key.ELAPSED, ((LocalTimer) timer).getElapsed());
			}
		} else {
			setValue(Key.DURATION, null);
			setValue(Key.EXPIRE, null);
			setValue(Key.ELAPSED, null);
		}
	}
	
	/**
	 * @param timer - the {@link Timer} to be formatted
	 * @return the formatted String representation of the given Timer with the {@link Key#DURATION} key,
	 * as well as the {@link Key#EXPIRE} key if the given Timer is a {@link GlobalTimer}, or
	 * the {@link Key#ELAPSED} key if the given Timer is a {@link LocalTimer}
	 */
	public static String formatTimer(Timer timer) {
		if (timer instanceof GlobalTimer) {
			return format(ImmutableMap.<Enum<?>, Object> of(Key.DURATION, timer.getDuration(), Key.EXPIRE, ((GlobalTimer) timer).getExpire()));
		}
		return format(ImmutableMap.<Enum<?>, Object> of(Key.DURATION, timer.getDuration(), Key.ELAPSED, ((LocalTimer) timer).getElapsed()));
	}
	
	/**
	 * @return the {@link PlayerID} that this DataEntry describes with keys {@link Key#UUID} and {@link Key#USERNAME}.
	 * null will be returned is there was a missing or invalid key/value pair
	 */
	public PlayerID getPlayerID() {
		Object uuid = getValue(Key.UUID);
		Object usermane = getValue(Key.USERNAME);
		if (uuid != null && DeadmanUtils.isUUID(uuid.toString()) && usermane != null) {
			return new PlayerID(UUID.fromString(uuid.toString()), usermane.toString());
		}
		return null;
	}
	
	/**
	 * @param playerId - The {@link PlayerID} to set and be represented by the {@link Key#UUID} and {@link Key#USERNAME} keys.
	 */
	public void setPlayerID(PlayerID playerId) {
		if (playerId != null) {
			setValue(Key.UUID, playerId.getUUID().toString());
			setValue(Key.USERNAME, playerId.getUsername().toLowerCase());
		} else {
			setValue(Key.UUID, null);
			setValue(Key.USERNAME, null);
		}
	}
	
	
	/**
	 * Null will be returned if:
	 * <ul>
	 * <li>{@link #getLocation()} returns null</li>
	 * <li>There was no Sign block at the location and force was false</li>
	 * <li>There was no Sign block at the location and {@link #getMaterialData()} returned null</li>
	 * <li>There was still no Sign block at the location even after forcing the block materialData</li>
	 * </ul>
	 * @param force - The ID and data for the block at the location returned by {@link #getLocation()} will
	 * be set to the values as defined by {@link #getMaterialData()} if the block is not a Sign block
	 * @return the {@link Sign} at the location this DataEntry describes
	 */
	public Sign getSign(boolean force) {
		Location signLoc = getLocation();
		if (signLoc != null) {
			return DeadmanUtils.getSignState(signLoc.getBlock(), (force ? getMaterialData() : null));
		}
		return null;
	}
	
	
	/**
	 * Example DataEntry String format:<br>
	 * <code>WORLD:Cynelia, X:-491, Y:23, Z:285, ID:68, DATA:4, DURATION:101m, PRICE:30000</code>
	 * @return the formatted key/value pairs that this DataEntry represents in the format:<br>
	 * <code>KEY1:some-value, KEY2:another-value, KEY3:key3-value</code><br>
	 */
	@Override
	public final String toString() {
		StringBuilder entryBuiler = new StringBuilder();
		for (Map.Entry<String, Object> valueEntry : values.entrySet()) {
			format(entryBuiler, valueEntry.getKey().toUpperCase(), valueEntry.getValue());
		}
		return entryBuiler.toString();
	}
	
	@Override
	public DataEntry clone() {
		// clone by parsing this DataEntry serialized as String rather than cloning values map
		// because there is a chance that one of the set values is not immutable
		return new DataEntry(toString());
	}
	
	
	/**
	 * @param values - A map containing the pairs of Enum keys to values to be formatted
	 * @return A formatted String with the given key/value pairs as specified by {@link #toString()}
	 */
	public static String format(Map<Enum<?>, Object> values) {
		StringBuilder entryBuiler = new StringBuilder();
		for (Map.Entry<Enum<?>, Object> valueEntry : values.entrySet()) {
			format(entryBuiler, valueEntry.getKey().name().toUpperCase(), valueEntry.getValue());
		}
		return entryBuiler.toString();
	}
	
	private static void format(StringBuilder entry, String key, Object value) {
		if (entry.length() > 0) {
			entry.append(", ");
		}
		entry.append(key).append(":").append(value);
	}
	
	public static enum Key {
		/* Location related keys */
		WORLD,
		X,
		Y,
		Z,
		YAW,
		PITCH,
		
		/* BlockState related keys */
		ID,
		DATA,
		
		/* Timer related keys */
		DURATION,
		EXPIRE,
		ELAPSED,
		
		/* PlayerID related keys */
		UUID,
		USERNAME;
	}
	
}
