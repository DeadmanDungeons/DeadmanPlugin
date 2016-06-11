package com.deadmandungeons.deadmanplugin.filedata;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;

import com.deadmandungeons.deadmanplugin.DeadmanPlugin;
import com.deadmandungeons.deadmanplugin.Conversion.Converter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * This class provides a means to easily load and cache any configuration entry in a DeadmanPlugin's config.yml file.
 * A configuration entry is represented as one of 3 implementations of {@link BaseConfigEntry}.
 * There are 3 methods that can be used to create a new ConfigEntry instance for each ConfigEntry implementation;
 * {@link #entry(Class, String)} for single value types, {@link #listEntry(Class, String)} for list value types,
 * and {@link #mapEntry(Class, String)} for map value types. Each BaseConfigEntry implementation has a
 * {@link BaseConfigEntry#value()} method that returns the appropriate value for the specific value type.<br>
 * <b>Example:</b>
 * 
 * <pre>
 * 
 * ListConfigEntry&lt;String&gt; entry = config.listValueEntry(String.class, "path.to.entry");
 * List&lt;String&gt; value = entry.value();
 * </pre>
 * 
 * The {@link #loadValues()} method should be used to load (or reload) the values from file.<br>
 * <b>NOTE:</b> A Converter will need to be registered for config values that are of a type other than String, Integer,
 * Boolean, ChatColor, or ItemStack. Use {@link DeadmanPlugin#getConversion()} to register a new Converter,
 * or to override a default converter.
 * @author Jon
 */
public class DeadmanConfig {
	
	// All of the unchecked casts are guaranteed to be type safe.
	
	// Logger messages
	private static final String MISSING_VALUE = "The %s config value at path '%s' is missing. Defaulting to value '%s'";
	private static final String INVALID_VALUE = "The $s config value at path '%s' is invalid. Defaulting to value '%s'";
	private static final String NONUNIQUE_VALUE = "The values for the '%s' config entry group are not unique. "
			+ "The default values will be used for this group";
			
	// Exception messages
	private static final String MISSING_CONVERTER = "A Converter for config values of type '%s' is not registered! "
			+ "use plugin.getConversion() to register a Converter for this type.";
	private static final String FAILED_TO_LOAD = "A '%s' value for the config entry at path '%s' in the default configuration file "
			+ "was either missing or invalid! The default configuration must contain valid values.";
	private static final String FAILED_TO_LOAD_GROUP = "The values for the '%s' config entry group in the default configuration file "
			+ "are not unique! The default configuraiton must contain unique values among config entry groups";
			
	private final Map<BaseConfigEntry<?, ?>, EntryValue> entryValues = new HashMap<>();
	private final Map<String, GroupOptions> entryGroups = new HashMap<>();
	
	
	/**
	 * This will create and store a single value config entry for this DeadmanConfig instance.<br>
	 * Example entry:
	 * 
	 * <pre>
	 * path-to:
	 *   entry: 'value'
	 * </pre>
	 * 
	 * Example usage for the above config example:
	 * 
	 * <pre>
	 * ConfigEntry&ltString&gt entry = entry(String.class, "path-to.entry");
	 * </pre>
	 * 
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
	 * 
	 * <pre>
	 * path-to:
	 *   list-entry:
	 *   - 'value'
	 *   - 'another-value'
	 * </pre>
	 * 
	 * Example usage for the above config example:
	 * 
	 * <pre>
	 * ListConfigEntry&ltString&gt listEntry = listEntry(String.class, "path-to.list-entry");
	 * </pre>
	 * 
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
	 * This will create and store a unique value list config entry for this DeadmanConfig instance.<br>
	 * <b>Note:</b> This is the same as {@link #listEntry(Class, String)} except that the list elements
	 * are stored in a {@link Set} which prevents duplicate elements and ensures uniqueness of values.
	 * @param type - The type of the config entry value
	 * @param path - The path to the list config entry
	 * @return
	 */
	public <T> SetConfigEntry<T> setEntry(Class<T> type, String path) {
		SetConfigEntry<T> entry = new SetConfigEntry<>(type, path);
		entryValues.put(entry, null);
		return entry;
	}
	
	/**
	 * This will create and store a value map config entry for this DeadmanConfig instance.<br>
	 * Example map entry:
	 * 
	 * <pre>
	 * path-to:
	 *   map-entry:
	 *     key1: 'value'
	 *     key2: 'another-value'
	 * </pre>
	 * 
	 * Example usage for the above config example:
	 * 
	 * <pre>
	 * MapConfigEntry&ltString&gt mapEntry = mapEntry(String.class, "path-to.map-entry");
	 * </pre>
	 * 
	 * @param type - The type of the config entry value
	 * @param path - The path to the map config entry
	 * @return a new MapConfigEntry instance for a value map config entry
	 */
	public <T> MapConfigEntry<T> mapEntry(Class<T> type, String path) {
		MapConfigEntry<T> entry = new MapConfigEntry<T>(type, path);
		entryValues.put(entry, null);
		return entry;
	}
	
	/**
	 * This will group together the given config entries of same type to ensure the
	 * uniqueness of values between them when loading. If the loaded values of two
	 * or more entries in the group are equal, all of the entries in the group will
	 * be loaded with the default values and a message will be printed to console.<br>
	 * A single config entry can be grouped more than once.
	 * @param groupName - The name of the group which can be anything
	 * @param entries - The Collection of config entries with same type to group together
	 * @return the GroupOptions which can optionally be used to configure how the created
	 * group should be handled
	 * @throws IllegalArgumentException if the given groupName or entries is null.<br>
	 * Or if the given entries collection contains less than 2 entries.<br>
	 * Or if any of the entries in the given collection are unknown and do not belong to this DeadmanConfig instance.
	 */
	public <T, V, E extends BaseConfigEntry<T, V>> GroupOptions groupEntries(String groupName, Collection<E> entries)
			throws IllegalArgumentException {
		if (groupName == null || entries == null) {
			throw new IllegalArgumentException("groupName or entries cannot be null");
		}
		if (entries.size() < 2) {
			throw new IllegalArgumentException("entries must contain at least 2 entries");
		}
		Set<BaseConfigEntry<?, ?>> group = new HashSet<>();
		for (E entry : entries) {
			if (!entryValues.containsKey(entry)) {
				throw new IllegalArgumentException("entries contains unknown config entry instance: " + entry);
			}
			group.add(entry);
		}
		
		for (E entry : entries) {
			entry.groups.add(groupName);
		}
		GroupOptions options = new GroupOptions();
		entryGroups.put(groupName, options);
		return options;
	}
	
	
	// TODO maybe restrict access to this method to only be used by DeadmanPlugin class
	public void loadEntries(DeadmanPlugin plugin) throws IllegalStateException {
		// First initialize validator for testing the uniqueness of loaded group values
		GroupValidator groupValidator = new GroupValidator();
		
		// Then load the config entries and validate the default configurations and the uniqueness of group values
		Set<String> defaultedGroups = new HashSet<>();
		Map<BaseConfigEntry<?, ?>, EntryValue> loadedValues = new HashMap<>();
		for (Map.Entry<BaseConfigEntry<?, ?>, EntryValue> mapEntry : entryValues.entrySet()) {
			BaseConfigEntry<?, ?> entry = mapEntry.getKey();
			EntryValue entryValue = entry.loadValue(plugin);
			// Check if the default configuration contains a missing or invalid value
			if (entryValue == null) {
				throw new IllegalStateException(String.format(FAILED_TO_LOAD, entry.getType().getName(), entry.getPath()));
			}
			
			for (String groupName : entry.groups) {
				// Check if the value of the grouped entry is not unique to other values in the group
				if (!defaultedGroups.contains(groupName) && !groupValidator.validateValue(groupName, entryValue.value)) {
					defaultedGroups.add(groupName);
					plugin.getLogger().warning(String.format(NONUNIQUE_VALUE, groupName));
				}
				// Check if the values or grouped entries in default configuration are not unique
				if (!groupValidator.validateDefaultValue(groupName, entryValue.defaultValue)) {
					throw new IllegalStateException(String.format(FAILED_TO_LOAD_GROUP, groupName));
				}
			}
			loadedValues.put(entry, entryValue);
		}
		
		// Then set the loaded config entry values
		for (Map.Entry<BaseConfigEntry<?, ?>, EntryValue> mapEntry : entryValues.entrySet()) {
			BaseConfigEntry<?, ?> entry = mapEntry.getKey();
			EntryValue entryValue = loadedValues.get(entry);
			// Check if this config entry is apart of a group that was defaulted and update the entry value accordingly
			if (!entryValue.valueDefault && !Collections.disjoint(entry.groups, defaultedGroups)) {
				entryValue = new EntryValue(entryValue.defaultValue, entryValue.defaultValue, true);
			}
			mapEntry.setValue(entryValue);
		}
	}
	
	
	public abstract class BaseConfigEntry<T, V> {
		
		// This class object is what ensures the type safety of all casts in DeadmanConfig
		protected final Class<T> type;
		protected final String path;
		protected final Set<String> groups = new HashSet<>();
		
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
		
		public V defaultValue() {
			@SuppressWarnings("unchecked")
			V defaultValue = (V) getEntryValue().defaultValue;
			return defaultValue;
		}
		
		/**
		 * @return true if the value as returned by {@link #value()} is the default configuration value and false otherwise
		 */
		public boolean isValueDefault() {
			return getEntryValue().valueDefault;
		}
		
		@Override
		public String toString() {
			return getClass().getSimpleName() + "[" + path + ": " + type.getName() + "]";
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
			Object val = plugin.getConfig().get(path);
			if (val == null) {
				return null;
			}
			Converter<T> converter = getConverter(plugin, type);
			T defaultValue = converter.convert(plugin.getConfig().getDefaults().get(path));
			if (defaultValue == null) {
				return null;
			}
			
			if (plugin.getConfig().isSet(path)) {
				T value = converter.convert(val);
				if (value != null) {
					return new EntryValue(value, defaultValue, false);
				} else {
					plugin.getLogger().warning(String.format(INVALID_VALUE, type.getName(), path, val));
				}
			} else {
				plugin.getLogger().warning(String.format(MISSING_VALUE, type.getName(), path, val));
				
			}
			return new EntryValue(defaultValue, defaultValue, true);
		}
		
	}
	
	public class ListConfigEntry<T> extends BaseConfigEntry<T, List<T>> {
		
		private ListConfigEntry(Class<T> type, String path) {
			super(type, path);
		}
		
		@Override
		protected EntryValue loadValue(DeadmanPlugin plugin) {
			List<?> val = plugin.getConfig().getList(path);
			if (val == null) {
				return null;
			}
			Converter<T> converter = getConverter(plugin, type);
			List<T> defaultValue = convertList(converter, plugin.getConfig().getDefaults().getList(path));
			if (defaultValue == null) {
				return null;
			}
			
			if (plugin.getConfig().isSet(path)) {
				List<T> value = convertList(converter, val);
				if (value != null) {
					return new EntryValue(value, defaultValue, false);
				} else {
					plugin.getLogger().warning(String.format(INVALID_VALUE, type.getName(), path, Arrays.toString(val.toArray())));
				}
			} else {
				plugin.getLogger().warning(String.format(MISSING_VALUE, type.getName() + " List", path, Arrays.toString(val.toArray())));
				
			}
			return new EntryValue(defaultValue, defaultValue, true);
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
	
	public class SetConfigEntry<T> extends BaseConfigEntry<T, Set<T>> {
		
		private final ListConfigEntry<T> listEntry;
		
		private SetConfigEntry(Class<T> type, String path) {
			super(type, path);
			listEntry = new ListConfigEntry<>(type, path);
		}
		
		@Override
		protected EntryValue loadValue(DeadmanPlugin plugin) {
			EntryValue entryValue = listEntry.loadValue(plugin);
			@SuppressWarnings("unchecked")
			List<T> list = (List<T>) entryValue.value;
			Set<T> set = new HashSet<>(list);
			Set<T> defaultSet = set;
			if (!entryValue.valueDefault) {
				@SuppressWarnings("unchecked")
				List<T> defaultList = (List<T>) entryValue.defaultValue;
				defaultSet = new HashSet<>(defaultList);
			}
			return new EntryValue(set, defaultSet, entryValue.valueDefault);
		}
		
	}
	
	public class MapConfigEntry<T> extends BaseConfigEntry<T, Map<String, T>> {
		
		private MapConfigEntry(Class<T> type, String path) {
			super(type, path);
		}
		
		@Override
		protected EntryValue loadValue(DeadmanPlugin plugin) {
			ConfigurationSection section = plugin.getConfig().getConfigurationSection(path);
			Map<String, ?> val = (section != null ? section.getValues(false) : null);
			if (val == null) {
				return null;
			}
			Converter<T> converter = getConverter(plugin, type);
			section = plugin.getConfig().getDefaults().getConfigurationSection(path);
			Map<String, T> defaultValue = convertMap(converter, section.getValues(false));
			if (defaultValue == null) {
				return null;
			}
			
			if (plugin.getConfig().isSet(path)) {
				Map<String, T> value = convertMap(converter, val);
				if (value != null) {
					return new EntryValue(value, defaultValue, false);
				} else {
					plugin.getLogger().warning(String.format(INVALID_VALUE, type.getName(), path, val.toString()));
				}
			} else {
				plugin.getLogger().warning(String.format(MISSING_VALUE, type.getName() + " Map", path, val.toString()));
				
			}
			return new EntryValue(defaultValue, defaultValue, true);
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
	
	
	public static class GroupOptions {
		
		private boolean uniqueElements;
		// More may be added
		
		/**
		 * Invoke this to enable the uniqueElements validation.
		 * If enabled, the corresponding entry group will validate uniqueness among the elements of entry values.
		 * This only applies to Collection based config entries ({@link ListConfigEntry}, {@link SetConfigEntry}).
		 * @return this GroupOptions instance
		 */
		public GroupOptions uniqueElements() {
			uniqueElements = true;
			return this;
		}
		
	}
	
	private class GroupValidator {
		
		private final Map<String, Set<Object>> groupValues = new HashMap<>(entryGroups.size());
		private final Map<String, Set<Object>> groupDefaultValues = new HashMap<>(entryGroups.size());
		
		private GroupValidator() {
			for (String groupName : entryGroups.keySet()) {
				groupValues.put(groupName, new HashSet<>());
				groupDefaultValues.put(groupName, new HashSet<>());
			}
		}
		
		private boolean validateValue(String groupName, Object value) {
			return validate(groupValues, groupName, value);
		}
		
		private boolean validateDefaultValue(String groupName, Object defaultValue) {
			return validate(groupDefaultValues, groupName, defaultValue);
		}
		
		private boolean validate(Map<String, Set<Object>> groupValues, String groupName, Object value) {
			Set<Object> values = groupValues.get(groupName);
			boolean uniqueValue = values.add(value);
			if (!uniqueValue) {
				return false;
			}
			GroupOptions options = entryGroups.get(groupName);
			if (options.uniqueElements && value instanceof Collection) {
				for (Object val : values) {
					if (val != value && !Collections.disjoint((Collection<?>) val, (Collection<?>) value)) {
						return false;
					}
				}
			}
			return true;
		}
		
	}
	
	private static class EntryValue {
		
		private final Object value;
		private final Object defaultValue;
		private final boolean valueDefault;
		
		private EntryValue(Object value, Object defaultValue, boolean valueDefault) {
			this.value = value;
			this.defaultValue = defaultValue;
			this.valueDefault = valueDefault;
		}
		
	}
	
}
