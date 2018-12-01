package com.deadmandungeons.deadmanplugin.filedata;

import com.deadmandungeons.deadmanplugin.Coord;
import com.deadmandungeons.deadmanplugin.DeadmanUtils;
import com.deadmandungeons.deadmanplugin.PlayerId;
import com.deadmandungeons.deadmanplugin.WorldCoord;
import com.deadmandungeons.deadmanplugin.timer.GlobalTimer;
import com.deadmandungeons.deadmanplugin.timer.LocalTimer;
import com.deadmandungeons.deadmanplugin.timer.Timer;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.material.MaterialData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TODO this entire class can be refactored to improve abstraction by defining an object's keys in the object itself

/**
 * A container class for the Key/Value pairs in a single data entry in a YAML file.<br>
 * Additional formatting utility methods are provided to format specific objects as specified by {@link #toString()}<br>
 * {@link DataKey} instances are used as the keys to a DataEntry value to enforce uniformity and validity in
 * the parsed Keys.
 * @author Jon
 * @see {@link #toString()} for information on the format of a DataEntry
 */
public class DataEntry implements Cloneable {

    // Matches a java enum constant for the key group, and any character that is not a comma as the value group, separated by a colon
    private static final String KEY_REGEX = "([a-zA-Z][a-zA-Z0-9$_]*?)";
    private static final Pattern KEY_PATTERN = Pattern.compile(KEY_REGEX);
    private static final Pattern VALUE_PATTERN = Pattern.compile(KEY_REGEX + ":([^,]+)");
    private static final String INVALID_MSG_1 = "The given value has a comma character. Commas are not allowed";
    private static final String INVALID_MSG_2 = "The value for key '%s' has a comma character. Commas are not allowed";

    public static final DataKey WORLD_KEY = new DataKey("WORLD");
    public static final DataKey X_KEY = new DataKey("X");
    public static final DataKey Y_KEY = new DataKey("Y");
    public static final DataKey Z_KEY = new DataKey("Z");
    public static final DataKey YAW_KEY = new DataKey("YAW");
    public static final DataKey PITCH_KEY = new DataKey("PITCH");

    /* BlockState related keys */
    public static final DataKey ID_KEY = new DataKey("ID");
    public static final DataKey DATA_KEY = new DataKey("DATA");

    /* Timer related keys */
    public static final DataKey DURATION_KEY = new DataKey("DURATION");
    public static final DataKey EXPIRE_KEY = new DataKey("EXPIRE");
    public static final DataKey ELAPSED_KEY = new DataKey("ELAPSED");

    /* PlayerId related keys */
    public static final DataKey UUID_KEY = new DataKey("UUID");
    public static final DataKey NAME_KEY = new DataKey("NAME");

    private static final Map<Integer, Material> MATERIALS_BY_ID;

    static {
        ImmutableMap.Builder<Integer, Material> materialsByIdBuilder = ImmutableMap.builder();
        for (Material material : Material.values()) {
            materialsByIdBuilder.put(material.getId(), material);
        }
        MATERIALS_BY_ID = materialsByIdBuilder.build();
    }

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
     * @param entryStr - The raw data entry String containing the key/value pairs to include in the returned DataEntry
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
     * This simply returns {@link #DataEntry(String)}.<br>
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
     * @author Jon
     * @see {@link DataEntry}
     */
    public static abstract class Builder<T extends Builder<T>> {

        private Map<String, Object> values = new HashMap<String, Object>();

        private Location location;
        private MaterialData materialData;
        private Timer timer;
        private PlayerId playerId;

        protected abstract T self();


        /**
         * @param location - The location to set in the built DataEntry
         * @return this builder
         * @see {@link DataEntry#setLocation(Location)}
         */
        public final T location(Location location) {
            this.location = location;
            return self();
        }

        /**
         * @param materialData - The MaterialData to set in the built DataEntry
         * @return this builder
         * @see {@link DataEntry#setMaterialData(MaterialData)}
         */
        public final T materialData(MaterialData materialData) {
            this.materialData = materialData;
            return self();
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
         * @param playerId - The PlayerId to set in the built DataEntry
         * @return this builder
         */
        public final T playerId(PlayerId playerId) {
            this.playerId = playerId;
            return self();
        }

        /**
         * <b>Note:</b> The given value cannot have a comma in the string returned by its toString invocation
         * @param key - The DataKey representing the value to set
         * @param value - The value to set. Indexed by the given Key.<br>
         * @return this builder
         * @throws IllegalArgumentException if the given value has a comma in its String representation
         */
        public final T value(DataKey key, Object value) {
            if (value.toString().contains(",")) {
                throw new IllegalArgumentException(String.format(INVALID_MSG_2, key));
            }
            values.put(key.name(), value);
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
            setPlayerId(builder.playerId);
        }
    }


    /**
     * @param key - The DataKey representing the value to get
     * @return the value that the given Datakey represents in this DataEntry, or null if no value exists for the given key
     */
    public final Object getValue(DataKey key) {
        return getValue(key, null);
    }

    /**
     * @param key - The Datakey representing the value to get
     * @param def - The default value to return if no value exists for the given key
     * @return the value that the given Datakey represents in this DataEntry,
     * or the given default if no value exists for the given key
     */
    public final Object getValue(DataKey key, Object def) {
        Object value = values.get(key.name());
        return value != null ? value : def;
    }

    /**
     * <b>Note:</b> The given value cannot have a comma in the string returned by its toString invocation
     * @param key - The Datakey representing the value to set
     * @param value - The value to set. Indexed by the given Key.<br>
     * If value is null, the key/value pair for the given key will be removed from this DataEntry.
     */
    public final void setValue(DataKey key, Object value) {
        Validate.notNull(key, "key cannot be null");
        if (value != null) {
            Validate.isTrue(!value.toString().contains(","), INVALID_MSG_1);
            values.put(key.name(), value);
        } else {
            values.remove(key.name());
        }
    }


    /**
     * @param key - The Datakey representing the Number to get
     * @return A Number object of the value indexed by the given Key,
     * or null if a Number value did not exist at the given Key
     */
    public final Number getNumber(DataKey key) {
        return getNumber(key, null);
    }

    /**
     * @param key - The Datakey representing the Number to get
     * @param def - The default Number to return if no Number value exists for the given key
     * @return A Number object of the value indexed by the given Key,
     * or the default Number if a Number value did not exist at the given Key
     */
    public final Number getNumber(DataKey key, Number def) {
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
     * @return The {@link World} object indexed at key {@link #WORLD_KEY},
     * or null if a World value does not exist for the respective Key
     */
    public final World getWorld() {
        Object value = getValue(WORLD_KEY);
        if (value != null) {
            World world = Bukkit.getWorld(value.toString());
            if (world != null) {
                setValue(WORLD_KEY, world.getName());
            }
            return world;
        }
        return null;
    }

    /**
     * @param world - The {@link World} object to set. Indexed by key {@link #WORLD_KEY}.<br>
     * If world is null, the World key/value pair will be removed from this DataEntry.
     */
    public final void setWorld(World world) {
        setValue(WORLD_KEY, world.getName());
    }


    /**
     * @return the Coord defined by keys: {@link #X_KEY}, {@link #Y_KEY}, {@link #Z_KEY}.
     * Or null if the minimum required values did not exist, or were invalid
     */
    public final Coord getCoord() {
        Number x = getNumber(X_KEY);
        Number y = getNumber(Y_KEY);
        Number z = getNumber(Z_KEY);
        if (x != null && y != null && z != null) {
            return new Coord(x.intValue(), y.intValue(), z.intValue());
        }
        return null;
    }

    /**
     * @param coord - The {@link Coord} object to set and be represented by the
     * {@link #X_KEY}, {@link #Y_KEY}, and {@link #Z_KEY} keys.<br>
     * If coord is null, all of the above key/value pairs will be removed from this DataEntry.
     */
    public final void setCoord(Coord coord) {
        if (coord != null) {
            setValue(X_KEY, coord.getX());
            setValue(Y_KEY, coord.getY());
            setValue(Z_KEY, coord.getZ());
        } else {
            setValue(X_KEY, null);
            setValue(Y_KEY, null);
            setValue(Z_KEY, null);
        }
    }

    /**
     * @param coord - The {@link Coord} to be formatted
     * @return the formatted String representation of the given Coord with the keys: {@link #X_KEY}, {@link #Y_KEY}, {@link #Z_KEY}
     */
    public static String formatCoord(Coord coord) {
        return format(ImmutableMap.<DataKey, Object>of(X_KEY, coord.getX(), Y_KEY, coord.getY(), Z_KEY, coord.getZ()));
    }

    /**
     * @return the WorldCoord defined by keys: {@link #WORLD_KEY}, {@link #X_KEY}, {@link #Y_KEY}, {@link #Z_KEY}.
     * Or null if the minimum required values did not exist, or were invalid
     */
    public final WorldCoord getWorldCoord() {
        World world = getWorld();
        Coord coord = getCoord();
        if (world != null && coord != null && coord.getY() >= 0 && coord.getY() <= world.getMaxHeight()) {
            return new WorldCoord(world, coord);
        }
        return null;
    }

    /**
     * @param coord - The {@link WorldCoord} object to set and be represented by the
     * {@link #WORLD_KEY}, {@link #X_KEY}, {@link #Y_KEY}, and {@link #Z_KEY} keys.<br>
     * If coord is null, all of the above key/value pairs will be removed from this DataEntry.
     */
    public final void setWorldCoord(WorldCoord coord) {
        if (coord != null) {
            setWorld(coord.getWorld());
            setCoord(coord);
        } else {
            setWorld(null);
            setCoord(null);
        }
    }

    /**
     * @param coord - The {@link WorldCoord} to be formatted
     * @return the formatted String representation of the given WorldCoord with the keys:
     * {@link #WORLD_KEY}, {@link #X_KEY}, {@link #Y_KEY}, {@link #Z_KEY}
     */
    public static String formatWorldCoord(WorldCoord coord) {
        return format(ImmutableMap.<DataKey, Object>of(WORLD_KEY, coord.getWorld().getName(), X_KEY, coord.getX(), Y_KEY, coord.getY(), Z_KEY,
                coord.getZ()));
    }


    /**
     * @return the Location defined by keys: {@link #WORLD_KEY}, {@link #X_KEY}, {@link #Y_KEY},
     * {@link #Z_KEY}, and optionally {@link #YAW_KEY}, {@link #PITCH_KEY}.
     * Or null if the minimum required values did not exist, or were invalid
     */
    public final Location getLocation() {
        World world = getWorld();
        Number x = getNumber(X_KEY);
        Number y = getNumber(Y_KEY);
        Number z = getNumber(Z_KEY);
        if (world != null && x != null && y != null && z != null) {
            Location loc = new Location(world, x.doubleValue(), y.doubleValue(), z.doubleValue());
            Number yaw = getNumber(YAW_KEY);
            if (yaw != null) {
                loc.setYaw(yaw.floatValue());
            }
            Number pitch = getNumber(PITCH_KEY);
            if (pitch != null) {
                loc.setPitch(pitch.floatValue());
            }
            return loc;
        }
        return null;
    }

    /**
     * @param location - The {@link Location} object to set and be represented by the
     * {@link #WORLD_KEY}, {@link #X_KEY}, {@link #Y_KEY}, and {@link #Z_KEY} keys. <br>
     * If the yaw or pitch is not 0, they will also be represented by the {@link #YAW_KEY}, and {@link #PITCH_KEY} keys. <br>
     * If location is null, all of the above key/value pairs will be removed from this DataEntry.
     */
    public final void setLocation(Location location) {
        if (location != null) {
            setWorld(location.getWorld());
            setValue(X_KEY, doubleOrInt(location.getX()));
            setValue(Y_KEY, doubleOrInt(location.getY()));
            setValue(Z_KEY, doubleOrInt(location.getZ()));
            setValue(YAW_KEY, (location.getYaw() != 0 ? location.getYaw() : null));
            setValue(PITCH_KEY, (location.getPitch() != 0 ? location.getPitch() : null));
        } else {
            setWorld(null);
            setValue(X_KEY, null);
            setValue(Y_KEY, null);
            setValue(Z_KEY, null);
            setValue(YAW_KEY, null);
            setValue(PITCH_KEY, null);
        }
    }

    /**
     * @param loc - the {@link Location} to be formatted
     * @return the formatted String representation of the given Location with the keys:
     * {@link #WORLD_KEY}, {@link #X_KEY}, {@link #Y_KEY}, {@link #Z_KEY}, and optionally {@link #YAW_KEY}, {@link #PITCH_KEY}
     */
    public static String formatLocation(Location loc) {
        ImmutableMap.Builder<DataKey, Object> mapBuilder = ImmutableMap.builder();
        mapBuilder.put(WORLD_KEY, loc.getWorld().getName()).put(X_KEY, doubleOrInt(loc.getX())).put(Y_KEY, doubleOrInt(loc.getY()))
                .put(Z_KEY, doubleOrInt(loc.getZ()));
        if (loc.getYaw() != 0) {
            mapBuilder.put(YAW_KEY, loc.getYaw());
        }
        if (loc.getPitch() != 0) {
            mapBuilder.put(PITCH_KEY, loc.getPitch());
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
     * @return the {@link MaterialData} that this DataEntry describes with keys {@link #ID_KEY} and {@link #DATA_KEY}.
     * null will be returned is there was a missing or invalid key/value pair
     */
    public final MaterialData getMaterialData() {
        Number id = getNumber(ID_KEY);
        Number data = getNumber(DATA_KEY);
        if (id != null && data != null) {
            return new MaterialData(MATERIALS_BY_ID.get(id.intValue()), data.byteValue());
        }
        return null;
    }

    /**
     * @param materialData - The {@link MaterialData} to set and be represented by the {@link #ID_KEY} and {@link #DATA_KEY} keys.
     */
    public final void setMaterialData(MaterialData materialData) {
        if (materialData != null) {
            setValue(ID_KEY, materialData.getItemType().getId());
            setValue(DATA_KEY, materialData.getData());
        } else {
            setValue(ID_KEY, null);
            setValue(DATA_KEY, null);
        }
    }

    /**
     * @param materialData - the {@link MaterialData} to be formatted
     * @return the formatted String representation of the given MaterialData with the {@link #ID_KEY} and {@link #DATA_KEY} keys.
     */
    public static String formatMaterialData(MaterialData materialData) {
        return format(ImmutableMap.<DataKey, Object>of(ID_KEY, materialData.getItemType().getId(), DATA_KEY, materialData.getData()));
    }


    /**
     * This DataEntry must contain the key/value pairs for the {@link #DURATION_KEY} key,
     * and either the {@link #EXPIRE_KEY} key or the {@link #ELAPSED_KEY} key.
     * @return The LocalTimer that this DataEntry describes. null will be returned if there was a missing or invalid key/value pair
     */
    public final LocalTimer getLocalTimer() {
        Timer timer = getTimer();
        return (timer instanceof GlobalTimer ? ((GlobalTimer) timer).toLocalTimer() : (LocalTimer) timer);
    }

    /**
     * This DataEntry must contain the key/value pairs for the {@link #DURATION_KEY} key,
     * and either the {@link #EXPIRE_KEY} key or the {@link #ELAPSED_KEY} key.
     * @return The GlobalTimer that this DataEntry describes. null will be returned if there was a missing or invalid key/value pair
     */
    public final GlobalTimer getGlobalTimer() {
        Timer timer = getTimer();
        return (timer instanceof LocalTimer ? ((LocalTimer) timer).toGlobalTimer() : (GlobalTimer) timer);
    }

    /**
     * This DataEntry must contain the key/value pairs for the {@link #DURATION_KEY} key,
     * and either the {@link #EXPIRE_KEY} key or the {@link #ELAPSED_KEY} key.
     * @return The GlobalTimer or LocalTimer that this DataEntry describes.
     * null will be returned if there was a missing or invalid key/value pair
     */
    public Timer getTimer() {
        Number duration = getNumber(DURATION_KEY);
        if (duration != null && duration.longValue() > 0) {
            Number expire = getNumber(EXPIRE_KEY);
            Number elapsed = getNumber(ELAPSED_KEY);
            if (expire != null && expire.longValue() > 0) {
                return new GlobalTimer(duration.longValue(), expire.longValue());
            } else if (elapsed != null && elapsed.longValue() >= 0) {
                return new LocalTimer(duration.longValue(), elapsed.longValue());
            }
        }
        return null;
    }

    /**
     * @param timer - The {@link Timer} object to set and be represented by the {@link #DURATION_KEY} key,
     * as well as the {@link #EXPIRE_KEY} key if the given Timer is a {@link GlobalTimer}, or
     * the {@link #ELAPSED_KEY} key if the given Timer is a {@link LocalTimer}.<br>
     * If timer is null, all of the above key/value pairs will be removed from this DataEntry.
     */
    public final void setTimer(Timer timer) {
        if (timer != null) {
            if (timer instanceof GlobalTimer) {
                setValue(DURATION_KEY, timer.getDuration());
                setValue(EXPIRE_KEY, ((GlobalTimer) timer).getExpire());
            } else {
                setValue(DURATION_KEY, timer.getDuration());
                setValue(ELAPSED_KEY, ((LocalTimer) timer).getElapsed());
            }
        } else {
            setValue(DURATION_KEY, null);
            setValue(EXPIRE_KEY, null);
            setValue(ELAPSED_KEY, null);
        }
    }

    /**
     * @param timer - the {@link Timer} to be formatted
     * @return the formatted String representation of the given Timer with the {@link #DURATION_KEY} key,
     * as well as the {@link #EXPIRE_KEY} key if the given Timer is a {@link GlobalTimer}, or
     * the {@link #ELAPSED_KEY} key if the given Timer is a {@link LocalTimer}
     */
    public static String formatTimer(Timer timer) {
        if (timer instanceof GlobalTimer) {
            return format(ImmutableMap.<DataKey, Object>of(DURATION_KEY, timer.getDuration(), EXPIRE_KEY, ((GlobalTimer) timer).getExpire()));
        }
        return format(ImmutableMap.<DataKey, Object>of(DURATION_KEY, timer.getDuration(), ELAPSED_KEY, ((LocalTimer) timer).getElapsed()));
    }

    /**
     * @return the {@link PlayerId} that this DataEntry describes with keys {@link #UUID_KEY} and {@link #NAME_KEY}.
     * null will be returned is there was a missing or invalid key/value pair
     */
    public PlayerId getPlayerId() {
        Object uuid = getValue(UUID_KEY);
        Object usermane = getValue(NAME_KEY);
        if (uuid != null && DeadmanUtils.isUUID(uuid.toString()) && usermane != null) {
            return new PlayerId(UUID.fromString(uuid.toString()), usermane.toString());
        }
        return null;
    }

    /**
     * @param playerId - The {@link PlayerId} to set and be represented by the {@link #UUID_KEY} and {@link #NAME_KEY} keys.
     */
    public void setPlayerId(PlayerId playerId) {
        if (playerId != null) {
            setValue(UUID_KEY, playerId.getId().toString());
            setValue(NAME_KEY, playerId.getUsername().toLowerCase());
        } else {
            setValue(UUID_KEY, null);
            setValue(NAME_KEY, null);
        }
    }

    /**
     * @param playerId - The {@link PlayerId} to be formatted;
     * @return the formatted String representation of the given PlayerId with the {@link #UUID_KEY} and {@link #NAME_KEY} keys.
     */
    public static String formatPlayerId(PlayerId playerId) {
        return format(ImmutableMap.<DataKey, Object>of(UUID_KEY, playerId.getId(), NAME_KEY, playerId.getUsername()));
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
            format(entryBuiler, valueEntry.getKey(), valueEntry.getValue());
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
     * @param values - A map containing the pairs of Datakey to values to be formatted
     * @return A formatted String with the given key/value pairs as specified by {@link #toString()}
     */
    public static String format(Map<DataKey, Object> values) {
        StringBuilder entryBuiler = new StringBuilder();
        for (Map.Entry<DataKey, Object> valueEntry : values.entrySet()) {
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


    public static class DataKey {

        private final String key;

        public DataKey(String name) {
            if (!KEY_PATTERN.matcher(name).matches()) {
                throw new IllegalArgumentException("Invalid key name syntax.  Key name must match " + KEY_REGEX);
            }
            this.key = name.toUpperCase();
        }

        protected String name() {
            return key;
        }

        @Override
        public String toString() {
            return name();
        }

    }

}
