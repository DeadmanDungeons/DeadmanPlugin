package org.deadmandungeons.deadmanplugin.filedata;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.deadmandungeons.deadmanplugin.Conversion.Converter;
import org.deadmandungeons.deadmanplugin.DeadmanPlugin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * This class provides a means to easily load and cache any configuration entry in a DeadmanPlugin's config.yml file.
 * A configuration entry is represented as one of 3 implementations of {@link BaseConfigEntry}.
 * There are 3 methods that can be used to create a new ConfigEntry instance for each ConfigEntry implementation;
 * {@link #entry(Class, String)} for single value types, {@link #listEntry(Class, String)} for list value types,
 * and {@link #mapEntry(Class, String)} for map value types. Each ConfigEntry implementation has a
 * {@link BaseConfigEntry#value()} method that returns the appropriate value for the specific value type.<br>
 * <b>Example:</b>
 * <code><pre>
 * ListConfigEntry&lt;String&gt; entry = config.listValueEntry(String.class, "path.to.entry");
 * List&lt;String&gt; value = entry.value();
 * </pre></code>
 * The {@link #loadValues()} method should be used to load (or reload) the values from file.<br>
 * <b>NOTE:</b> A Converter will need to be registered for config values that are of a type other than String, Integer,
 * Boolean, ChatColor, or ItemStack. Use {@link #registerConverter(Class, Converter)} to register a new Converter,
 * or to override a default converter.
 * @author Jon
 */
public class DeadmanConfig {
	
	// All of the unchecked casts are guaranteed to be type safe.
	
	private static final String MISSING_VALUE = "The %s config value at path '%s' is missing. Defaulting to value '%s'";
	private static final String INVALID_VALUE = "The $s config value at path '%s' is invalid. Defaulting to value '%s'";
	
	private static final String MISSING_CONVERTER = "A Converter for config values of type '%s' is not registered! "
			+ "use registerConverter(typeClass, converter) to register a config Converter for this type.";
	private static final String FAILED_TO_LOAD = "A '%s' value for the config value at path '%s' in the default configuration file "
			+ "was either missing or invalid! The default configuration must contain valid values.";
			
	private final Map<BaseConfigEntry<?, ?>, EntryValue> entryValues = new HashMap<>();
	
	
	/**
	 * This will create and store a single value config entry for this DeadmanConfig instance.<br>
	 * Example entry:
	 * <code><pre>
	 * path-to:
	 *   entry: 'value'
	 * </pre></code>
	 * Example usage for the above config example:
	 * <code><pre>ConfigEntry&ltString&gt entry = entry(String.class, "path-to.entry");</pre></code><br>
	 * @param type - The type of the config entry value
	 * @param path - The path to the single value config entry
	 * @return a new ConfigEntry instance for a single value config entry
	 */
	public <T> ConfigEntry<T> entry(Class<T> type, String path) {
		ConfigEntry<T> entry = new ConfigEntry<T>(type, path);
		entryValues.put(entry, null);
		return entry;
	}
	
	/**
	 * This will create and store a value list config entry for this DeadmanConfig instance.<br>
	 * Example list entry:
	 * <code><pre>
	 * path-to:
	 *   list-entry:
	 *   - 'value'
	 *   - 'another-value'
	 * </pre></code>
	 * Example usage for the above config example:
	 * <code><pre>ListConfigEntry&ltString&gt listEntry = listEntry(String.class, "path-to.list-entry");</pre></code><br>
	 * @param type - The type of the config entry value
	 * @param path - The path to the list config entry
	 * @return a new ListConfigEntry instance for a value list config entry
	 */
	public <T> ListConfigEntry<T> listEntry(Class<T> type, String path) {
		ListConfigEntry<T> entry = new ListConfigEntry<T>(type, path);
		entryValues.put(entry, null);
		return entry;
	}
	
	/**
	 * This will create and store a value map config entry for this DeadmanConfig instance.<br>
	 * Example map entry:
	 * <code><pre>
	 * path-to:
	 *   map-entry:
	 *     key1: 'value'
	 *     key2: 'another-value'
	 * </pre></code>
	 * Example usage for the above config example:
	 * <code><pre>MapConfigEntry&ltString&gt mapEntry = mapEntry(String.class, "path-to.map-entry");</pre></code><br>
	 * @param type - The type of the config entry value
	 * @param path - The path to the map config entry
	 * @return a new MapConfigEntry instance for a value map config entry
	 */
	public <T> MapConfigEntry<T> mapEntry(Class<T> type, String path) {
		MapConfigEntry<T> entry = new MapConfigEntry<T>(type, path);
		entryValues.put(entry, null);
		return entry;
	}
	
	// TODO maybe restrict access to this method to only be used by DeadmanPlugin class
	public void loadEntries(DeadmanPlugin plugin) {
		for (Map.Entry<BaseConfigEntry<?, ?>, EntryValue> mapEntry : entryValues.entrySet()) {
			BaseConfigEntry<?, ?> entry = mapEntry.getKey();
			EntryValue entryValue = entry.loadValue(plugin);
			if (entryValue != null) {
				mapEntry.setValue(entryValue);
			} else {
				throw new IllegalStateException(String.format(FAILED_TO_LOAD, entry.getType().getName(), entry.getPath()));
			}
		}
	}
	
	
	public abstract class BaseConfigEntry<T, V> {
		
		// This class object is what ensures the type safety of all casts in DeadmanConfig
		protected final Class<T> type;
		protected final String path;
		
		private BaseConfigEntry(Class<T> type, String path) {
			this.type = type;
			this.path = path;
		}
		
		/**
		 * @return the type of this config entry value
		 */
		public Class<T> getType() {
			return type;
		}
		
		/**
		 * @return the path to this config entry
		 */
		public String getPath() {
			return path;
		}
		
		/**
		 * @return the loaded value for this config entry
		 */
		public V value() {
			@SuppressWarnings("unchecked")
			V value = (V) getEntryValue().value;
			return value;
		}
		
		/**
		 * @return true if the value as returned by {@link #value()} is the default configuration value and false otherwise
		 */
		public boolean isDefault() {
			return getEntryValue().defaultValue;
		}
		
		protected EntryValue getEntryValue() {
			EntryValue entryValue = entryValues.get(this);
			if (entryValue == null) {
				throw new IllegalStateException("This ConfigEntry has not been loaded");
			}
			return entryValue;
		}
		
		protected Converter<T> getConverter(DeadmanPlugin plugin, Class<T> type) {
			Converter<T> converter = plugin.getConversion().getConverter(type);
			if (converter == null) {
				throw new IllegalStateException(String.format(MISSING_CONVERTER, type.getCanonicalName()));
			}
			return converter;
		}
		
		protected abstract EntryValue loadValue(DeadmanPlugin plugin);
		
	}
	
	public class ConfigEntry<T> extends BaseConfigEntry<T, T> {
		
		private ConfigEntry(Class<T> type, String path) {
			super(type, path);
		}
		
		@Override
		protected EntryValue loadValue(DeadmanPlugin plugin) {
			T value = null;
			boolean defaultValue = false;
			Object val = plugin.getConfig().get(path);
			if (val != null) {
				if (!plugin.getConfig().isSet(path)) {
					plugin.getLogger().severe(String.format(MISSING_VALUE, type.getName(), path, val));
					defaultValue = true;
				}
				Converter<T> converter = getConverter(plugin, type);
				value = converter.convert(val);
				if (value == null) {
					value = converter.convert(plugin.getConfig().getDefaults().get(path));
					plugin.getLogger().severe(String.format(INVALID_VALUE, type.getName(), path, val));
				}
			}
			return (value != null ? new EntryValue(value, defaultValue) : null);
		}
		
	}
	
	public class ListConfigEntry<T> extends BaseConfigEntry<T, List<T>> {
		
		private ListConfigEntry(Class<T> type, String path) {
			super(type, path);
		}
		
		@Override
		protected EntryValue loadValue(DeadmanPlugin plugin) {
			List<T> valueList = null;
			boolean defaultValue = false;
			List<?> vals = plugin.getConfig().getList(path);
			if (vals != null) {
				if (!plugin.getConfig().isSet(path)) {
					plugin.getLogger().severe(String.format(MISSING_VALUE, type.getName() + " List", path, Arrays.toString(vals.toArray())));
					defaultValue = true;
				}
				Converter<T> converter = getConverter(plugin, type);
				valueList = convertList(converter, vals);
				if (valueList == null) {
					valueList = convertList(converter, plugin.getConfig().getDefaults().getList(path));
					plugin.getLogger().severe(String.format(INVALID_VALUE, type.getName(), path, Arrays.toString(vals.toArray())));
				}
			}
			return (valueList != null ? new EntryValue(valueList, defaultValue) : null);
		}
		
		private List<T> convertList(Converter<T> converter, List<?> vals) {
			if (vals == null) {
				return null;
			}
			ImmutableList.Builder<T> listBuilder = ImmutableList.builder();
			for (Object val : vals) {
				T value = converter.convert(val);
				if (value == null) {
					return null;
				}
				listBuilder.add(value);
			}
			return listBuilder.build();
		}
		
	}
	
	public class MapConfigEntry<T> extends BaseConfigEntry<T, Map<String, T>> {
		
		private MapConfigEntry(Class<T> type, String path) {
			super(type, path);
		}
		
		@Override
		protected EntryValue loadValue(DeadmanPlugin plugin) {
			Map<String, T> valueMap = null;
			boolean defaultValue = false;
			ConfigurationSection section = plugin.getConfig().getConfigurationSection(path);
			if (section != null) {
				Map<String, ?> vals = section.getValues(false);
				if (!plugin.getConfig().isSet(path)) {
					plugin.getLogger().severe(String.format(MISSING_VALUE, type.getName() + " List", path, vals.toString()));
					defaultValue = true;
				}
				Converter<T> converter = getConverter(plugin, type);
				valueMap = convertMap(converter, vals);
				if (valueMap == null) {
					ConfigurationSection defaultSection = plugin.getConfig().getDefaults().getConfigurationSection(path);
					if (defaultSection != null) {
						valueMap = convertMap(converter, defaultSection.getValues(false));
						plugin.getLogger().severe(String.format(INVALID_VALUE, type.getName(), path, vals.toString()));
					}
				}
			}
			return (valueMap != null ? new EntryValue(valueMap, defaultValue) : null);
		}
		
		private Map<String, T> convertMap(Converter<T> converter, Map<String, ?> vals) {
			if (vals == null) {
				return null;
			}
			ImmutableMap.Builder<String, T> mapBuilder = ImmutableMap.builder();
			for (String key : vals.keySet()) {
				Object val = vals.get(key);
				T value = converter.convert(val);
				if (value == null) {
					return null;
				}
				mapBuilder.put(key, value);
			}
			return mapBuilder.build();
		}
		
		
	}
	
	
	private static class EntryValue {
		
		private final Object value;
		private final boolean defaultValue;
		
		private EntryValue(Object value, boolean defaultValue) {
			this.value = value;
			this.defaultValue = defaultValue;
		}
		
	}
	
}
