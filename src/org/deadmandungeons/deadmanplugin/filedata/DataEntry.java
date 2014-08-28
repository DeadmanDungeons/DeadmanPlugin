package org.deadmandungeons.deadmanplugin.filedata;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.material.MaterialData;
import org.deadmandungeons.deadmanplugin.timer.GlobalTimer;
import org.deadmandungeons.deadmanplugin.timer.LocalTimer;
import org.deadmandungeons.deadmanplugin.timer.Timer;

/**
 * A container class for the Key/Value pairs in a single data entry in a YAML file.<br>
 * Use DataEntry.{@link #builder()}.{@link Builder#build() build()} to get an instance of this class.<br>
 * The DataEntry.Builder class can create a DataEntry from a raw String as from file, or be passed values
 * to add to the set of key/value pairs in the built DataEntry.<br>
 * Enum constants are used as the keys to a DataEntry value to enforce uniformity and validity in
 * the parsed Keys.
 * @see {@link #toString()} for information on the format of a DataEntry
 * @author Jon
 */
public class DataEntry {
	
	private static final Pattern VALUE_PATTERN = Pattern.compile("([a-zA-Z][a-zA-Z0-9$_]*?):([^,]+)");
	private static final String INVALID_MSG_1 = "The given value has a comma character. Commas are not allowed";
	private static final String INVALID_MSG_2 = "The value for key '%s' has a comma character. Commas are not allowed";
	
	private final Map<String, Object> values;
	
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
		
		protected abstract T self();
		
		/**
		 * The given String entry should be in the format as specified by {@link DataEntry#toString()}
		 * @param entry - The raw data entry String containing the key/value pairs to include in the built DataEntry
		 * @return this builder
		 */
		public T fromEntry(String entry) {
			Matcher valueMatcher = VALUE_PATTERN.matcher(entry);
			while (valueMatcher.find()) {
				String key = valueMatcher.group(1);
				String value = valueMatcher.group(2);
				values.put(key.toUpperCase(), value);
			}
			return self();
		}
		
		/**
		 * @see {@link DataEntry#setLocation(Location)}
		 * @param location - The location to set in the built DataEntry
		 * @return this builder
		 */
		public T withLocation(Location location) {
			this.location = location;
			return self();
		}
		
		/**
		 * @see {@link DataEntry#setMaterialData(MaterialData)}
		 * @param materialData - The MaterialData to set in the built DataEntry
		 * @return this builder
		 */
		public T withMaterialData(MaterialData materialData) {
			this.materialData = materialData;
			return self();
		}
		
		public T withTimer(Timer timer) {
			this.timer = timer;
			return self();
		}
		
		/**
		 * <b>Note:</b> The given value cannot have a comma in the string returned by its toString invocation
		 * @param key - The Enum Key representing the value to set
		 * @param value - The value to set. Indexed by the given Key.<br>
		 * @return this builder
		 */
		public T withValue(Enum<?> key, Object value) {
			values.put(key.name(), value);
			return self();
		}
		
		private T validate() {
			for (String key : values.keySet()) {
				if (values.get(key).toString().contains(",")) {
					throw new IllegalArgumentException(String.format(INVALID_MSG_2, key));
				}
			}
			return self();
		}
		
		/**
		 * @return a new DataEntry for the key/value pairs specified in this build
		 * @throws IllegalArgumentException - if one of the values has a comma in the string returned by its toString invocation
		 */
		public DataEntry build() throws IllegalArgumentException {
			validate();
			
			return new DataEntry(this);
		}
	}
	
	
	private DataEntry(Builder<?> builder) {
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
	}
	
	/**
	 * @param key - The Enum Key representing the value to get
	 * @return the value that the given Enum key represents in this DataEntry, or null if no value exists for the given key
	 */
	public final Object getValue(Enum<?> key) {
		return values.get(key.name());
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
			values.put(key.name(), value);
		} else {
			values.remove(key.name());
		}
	}
	
	
	/**
	 * @param key - The Enum Key representing the Number to get
	 * @return A Number object of the value indexed by the given Key, or null if a Number value did not exist at the given Key
	 */
	public final Number getNumber(Enum<?> key) {
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
		return null;
	}
	
	/**
	 * @param key - The Enum Key representing the Number to set
	 * @param number - The Number to set. Indexed by the given Key.<br>
	 * If number is null, the key/value pair for the given key will be removed from this DataEntry.
	 */
	public final void setNumber(Enum<?> key, Number number) {
		Validate.notNull(key, "key cannot be null");
		if (number != null) {
			setValue(key, number);
		} else {
			values.remove(key.name());
		}
	}
	
	/**
	 * @return The {@link World} object indexed at key {@link DefaultKey#WORLD}, or null if a World value does not exist for the respective Key
	 */
	public final World getWorld() {
		Object value = getValue(DefaultKey.WORLD);
		if (value != null) {
			if (value instanceof World) {
				return (World) value;
			}
			World world = Bukkit.getWorld(value.toString());
			if (world != null) {
				setValue(DefaultKey.WORLD, world);
			}
			return world;
		}
		return null;
	}
	
	/**
	 * @param world - The {@link World} object to set. Indexed by key {@link DefaultKey#WORLD}.<br>
	 * If world is null, the World key/value pair will be removed from this DataEntry.
	 */
	public final void setWorld(World world) {
		if (world != null) {
			setValue(DefaultKey.WORLD, world);
		} else {
			values.remove(DefaultKey.WORLD.name());
		}
	}
	
	/**
	 * @return the Location defined by keys: {@link DefaultKey#WORLD}, {@link DefaultKey#XCOORD}, {@link DefaultKey#YCOORD}, {@link DefaultKey#ZCOORD}
	 * , and optionally {@link DefaultKey#YAW}, {@link DefaultKey#PITCH}. Or null if the minimum required
	 * values did not exist, or were invalid
	 */
	public final Location getLocation() {
		World world = getWorld();
		Number xCoord = getNumber(DefaultKey.X);
		Number yCoord = getNumber(DefaultKey.Y);
		Number zCoord = getNumber(DefaultKey.Z);
		if (world != null && xCoord != null && yCoord != null && zCoord != null) {
			Location loc = new Location(world, xCoord.doubleValue(), yCoord.doubleValue(), zCoord.doubleValue());
			Number yaw = getNumber(DefaultKey.YAW);
			Number pitch = getNumber(DefaultKey.PITCH);
			if (yaw != null && pitch != null) {
				loc.setYaw(yaw.floatValue());
				loc.setPitch(pitch.floatValue());
			}
			return loc;
		}
		return null;
	}
	
	/**
	 * @param location - The {@link Location} object to set and be represented by the {@link DefaultKey#WORLD}, {@link DefaultKey#XCOORD},
	 * {@link DefaultKey#YCOORD}, and {@link DefaultKey#ZCOORD} keys. <br>
	 * If the yaw or pitch is not 0, they will also be represented by the {@link DefaultKey#YAW}, and {@link DefaultKey#PITCH} keys. <br>
	 * If location is null, all of the above key/value pairs will be removed from this DataEntry.
	 */
	public final void setLocation(Location location) {
		if (location != null) {
			setWorld(location.getWorld());
			setNumber(DefaultKey.X, location.getBlockX());
			setNumber(DefaultKey.Y, location.getBlockY());
			setNumber(DefaultKey.Z, location.getBlockZ());
			if (location.getYaw() != 0) {
				setNumber(DefaultKey.YAW, location.getYaw());
			} else {
				values.remove(DefaultKey.YAW);
			}
			if (location.getPitch() != 0) {
				setNumber(DefaultKey.PITCH, location.getPitch());
			} else {
				values.remove(DefaultKey.PITCH);
			}
		} else {
			values.remove(DefaultKey.WORLD.name());
			values.remove(DefaultKey.X.name());
			values.remove(DefaultKey.Y.name());
			values.remove(DefaultKey.Z.name());
			values.remove(DefaultKey.YAW.name());
			values.remove(DefaultKey.PITCH.name());
		}
	}
	
	/**
	 * @return
	 */
	public final MaterialData getMaterialData() {
		Number id = getNumber(DefaultKey.ID);
		Number data = getNumber(DefaultKey.DATA);
		if (id != null && data != null) {
			return new MaterialData(id.intValue(), data.byteValue());
		}
		return null;
	}
	
	/**
	 * @param materialData
	 */
	public final void setMaterialData(MaterialData materialData) {
		if (materialData != null) {
			setNumber(DefaultKey.ID, materialData.getItemTypeId());
			setNumber(DefaultKey.DATA, materialData.getData());
		} else {
			values.remove(DefaultKey.ID.name());
			values.remove(DefaultKey.DATA.name());
		}
	}
	
	/**
	 * Get the Timer object that this DataEntry represents<br />
	 * This DataEntry must contain the key/value pairs for the {@link Keys#DURATION} key,
	 * and either the {@link Keys#EXPIRE} key or the {@link Keys#ELAPSED} key.
	 * @param global - The boolean flag stating weather or not the timer is global (true) or local (false)
	 * @return The Timer that this DataEntry describes. null will be returned if there was a missing key and value pair
	 */
	public final Timer getTimer(boolean global) {
		if (values.isEmpty()) {
			return null;
		}
		Number duration = getNumber(DefaultKey.DURATION);
		Number expire = getNumber(DefaultKey.EXPIRE);
		Number elapsed = getNumber(DefaultKey.ELAPSED);
		
		if (duration != null && duration.longValue() > 0) {
			if (expire != null && expire.longValue() > 0) {
				GlobalTimer globalTimer = new GlobalTimer(duration.longValue(), expire.longValue());
				if (!global) {
					return globalTimer.toLocalTimer();
				}
				return globalTimer;
			} else if (elapsed != null && elapsed.longValue() >= 0) {
				LocalTimer localTimer = new LocalTimer(duration.longValue(), elapsed.longValue());
				if (global) {
					return localTimer.toGlobalTimer();
				}
				return localTimer;
			}
		}
		return null;
	}
	
	/**
	 * @param timer - The {@link Timer} object to set and be represented by the {@link DefaultKey#DURATION} key,
	 * as well as the {@link DefaultKey#EXPIRE} key if the given Timer is a {@link GlobalTimer}, or
	 * the {@link DefaultKey#ELAPSED} key if the given Timer is a {@link LocalTimer}.<br>
	 * If timer is null, all of the above key/value pairs will be removed from this DataEntry.
	 */
	public final void setTimer(Timer timer) {
		if (timer != null) {
			if (timer instanceof GlobalTimer) {
				setNumber(DefaultKey.DURATION, timer.getDuration());
				setNumber(DefaultKey.EXPIRE, ((GlobalTimer) timer).getExpire());
			} else {
				setNumber(DefaultKey.DURATION, timer.getDuration());
				setNumber(DefaultKey.ELAPSED, ((LocalTimer) timer).getElapsed());
			}
		} else {
			values.remove(DefaultKey.DURATION.name());
			values.remove(DefaultKey.EXPIRE.name());
			values.remove(DefaultKey.ELAPSED.name());
		}
	}
	
	/**
	 * @return the formatted key/value pairs that this DataEntry represents in the format:<br>
	 * <code>KEY1:some-value, KEY2:another-value, KEY3:key3-value</code><br>
	 * Example:<br>
	 * <code>WORLD:Cynelia, X:-491, Y:23, Z:285, ID:68, DATA:4, DURATION:101m, PRICE:30000</code>
	 */
	@Override
	public final String toString() {
		StringBuilder entry = new StringBuilder();
		for (String key : values.keySet()) {
			if (entry.length() > 0) {
				entry.append(", ");
			}
			entry.append(key).append(":").append(values.get(key)).append(values.size());
		}
		return entry.toString();
	}
	
	public static enum DefaultKey {
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
		
		OFFSET_X,
		OFFSET_Y,
		OFFSET_Z,
		W,
		H,
		L,
		
		TYPE,
		PRICE,
		TEXT,
		COLOR;
	}
	
}
