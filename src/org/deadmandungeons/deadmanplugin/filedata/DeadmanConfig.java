package org.deadmandungeons.deadmanplugin.filedata;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.deadmandungeons.deadmanplugin.DeadmanPlugin;
import org.deadmandungeons.deadmanplugin.DeadmanUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * This class is used to handle plugin configuration. The {@link ConfigEnum ConfigEnum<E, T>} interface
 * is to be used with an enumeration containing the config path Strings for a certain value type.
 * The defined config values for a ConfigEnum are loaded by this classes {@link #loadValues(Class, Class)} method.
 * The loaded values of a ConfigEnum can be obtained with the {@link #getValue(Enum)}, {@link #getList(Enum)},
 * and {@link #getMap(Enum)} methods. A Converter will need to be registered for config values that are of a type
 * other than String, Integer, Boolean, ChatColor, or ItemStack. Use {@link #registerConverter(Class, Converter)} to
 * register a new Converter, or to override a default converter.
 * @author Jon
 */
public class DeadmanConfig {
	
	// All of the unchecked casts are guaranteed to be type safe.
	
	private static final String MISSING_VALUE = "The %s config value at path '%s' is missing. Defaulting to value '%s'";
	private static final String INVALID_VALUE = "The $s config value at path '%s' is invalid. Defaulting to value '%s'";
	
	private static final String NON_LOADED_ENUM = "The values for the '%s' ConfigEnum have not been loaded!";
	private static final String MISSING_CONVERTER = "A Converter for config values of type '%s' is not registered! "
			+ "use registerConverter(typeClass, converter) to register a config Converter for this type.";
	private static final String FAILED_TO_LOAD = "A '%s' value for the config value at path '%s' in the default configuration file "
			+ "was either missing or invalid! The default configuration must contain valid values.";
	
	private Map<Class<?>, Converter<?>> converters = new HashMap<Class<?>, Converter<?>>();
	
	// TODO maybe make a simple composite pattern object to handle this 3 level tree structure
	// so many wildcards! The conversions are type safe though. Each inner collection will never be empty once loaded
	private Map<ConfigEnum<?, ?>, Class<?>> types = new HashMap<ConfigEnum<?, ?>, Class<?>>();
	private Map<Class<?>, Map<ConfigEnum<?, ?>, ?>> values = new HashMap<Class<?>, Map<ConfigEnum<?, ?>, ?>>();
	private Map<Class<?>, Map<ConfigEnum<?, ?>, List<?>>> lists = new HashMap<Class<?>, Map<ConfigEnum<?, ?>, List<?>>>();
	private Map<Class<?>, Map<ConfigEnum<?, ?>, Map<String, ?>>> maps = new HashMap<Class<?>, Map<ConfigEnum<?, ?>, Map<String, ?>>>();
	
	private DeadmanPlugin plugin;
	
	public DeadmanConfig(DeadmanPlugin plugin) {
		this.plugin = plugin;
		
		registerDefaultConverters();
	}
	
	/**
	 * A Converter is used to convert an Object obtained from the plugins Config to the appropriate type
	 * @param <T> - The type to be converted to
	 * @author Jon
	 */
	public static interface Converter<T> {
		
		T convert(Object obj);
	}
	
	/**
	 * @param <E> - The Enum of this ConfigEnum
	 * @param <T> - The type of the config values for this ConfigEnum
	 * @author Jon
	 */
	public static interface ConfigEnum<E extends Enum<E> & ConfigEnum<E, T>, T> {
		
		/**
		 * @return the String path of the config value
		 */
		String getPath();
		
		/**
		 * @return true if this config value is in list format, and false otherwise
		 */
		ValueType getValueType();
	}
	
	public static enum ValueType {
		/**
		 * Represents a single configuration value
		 */
		SINGLE,
		/**
		 * Represents a list of configuration values
		 */
		LIST,
		/**
		 * Represents a map of configuration keys the configuration values
		 */
		MAP
	}
	
	/**
	 * This will load both list and non-list config values
	 * @param enumClass - The class of the ConfigEnum to be loaded
	 * @param type - The type of config value that loaded ConfigEnum is for
	 * @throws IllegalStateException if a converter for the specified type has not been registered,
	 * or if a config value is either missing from the default configuration file or invalid based of the specified type
	 */
	public <E extends Enum<E> & ConfigEnum<E, T>, T> void loadValues(Class<E> enumClass, Class<T> type) throws IllegalStateException {
		Validate.notNull(enumClass);
		Validate.notNull(type);
		Validate.notNull(values);
		
		@SuppressWarnings("unchecked")
		Converter<T> converter = (Converter<T>) converters.get(type);
		if (converter != null) {
			Map<ConfigEnum<?, ?>, T> tValues = new HashMap<ConfigEnum<?, ?>, T>();
			Map<ConfigEnum<?, ?>, List<?>> tLists = new HashMap<ConfigEnum<?, ?>, List<?>>();
			Map<ConfigEnum<?, ?>, Map<String, ?>> tMaps = new HashMap<ConfigEnum<?, ?>, Map<String, ?>>();
			for (E configEnum : EnumSet.allOf(enumClass)) {
				if (configEnum.getValueType() == ValueType.SINGLE) {
					T value = getValue(type, converter, configEnum.getPath());
					if (value != null) {
						tValues.put(configEnum, value);
					} else {
						throw new IllegalStateException(String.format(FAILED_TO_LOAD, type.getName(), configEnum.getPath()));
					}
				} else if (configEnum.getValueType() == ValueType.LIST) {
					List<T> list = getList(type, converter, configEnum.getPath());
					if (list != null) {
						tLists.put(configEnum, list);
					} else {
						throw new IllegalStateException(String.format(FAILED_TO_LOAD, type.getName() + " List", configEnum.getPath()));
					}
				} else {
					Map<String, T> map = getMap(type, converter, configEnum.getPath());
					if (map != null) {
						tMaps.put(configEnum, map);
					} else {
						throw new IllegalStateException(String.format(FAILED_TO_LOAD, type.getName() + " List", configEnum.getPath()));
					}
				}
				types.put(configEnum, type);
			}
			if (!tValues.isEmpty()) {
				values.put(type, tValues);
			}
			if (!tLists.isEmpty()) {
				lists.put(type, tLists);
			}
			if (!tMaps.isEmpty()) {
				maps.put(type, tMaps);
			}
		} else {
			throw new IllegalStateException(String.format(MISSING_CONVERTER, type.getCanonicalName()));
		}
	}
	
	
	/**
	 * @param configEnum - The ConfigEnum to get the type of loaded values from
	 * @return the type of the loaded config values for the given ConfigEnum
	 */
	public <E extends Enum<E> & ConfigEnum<E, T>, T> Class<T> getType(E configEnum) {
		Validate.notNull(configEnum);
		
		@SuppressWarnings("unchecked")
		Class<T> type = (Class<T>) types.get(configEnum);
		if (type == null) {
			throw new IllegalStateException(String.format(NON_LOADED_ENUM, configEnum.name()));
		}
		return type;
	}
	
	/**
	 * If the given ConfigEnum is of {@link ValueType#SINGLE}, the single value will be returned. <br>
	 * If the given ConfigEnum is of {@link ValueType#LIST}, the first value in the list will be returned.<br>
	 * If the given ConfigEnum is of {@link ValueType#MAP}, the first iterated value of the map will be returned.<br>
	 * This is to prevent null pointers. Call the appropriate getter method for the ConfigEnum's ValueType.
	 * @param configEnum - The ConfigEnum to get the loaded config value for
	 * @return the loaded config value for the given ConfigEnum
	 */
	public <E extends Enum<E> & ConfigEnum<E, T>, T> T getValue(E configEnum) {
		Validate.notNull(configEnum);
		
		if (configEnum.getValueType() == ValueType.SINGLE) {
			// Will throw exception if the configEnum was not loaded
			Class<T> type = getType(configEnum);
			Map<ConfigEnum<?, ?>, ?> tValues = values.get(type);
			return type.cast(tValues.get(configEnum));
		} else if (configEnum.getValueType() == ValueType.LIST) {
			return getList(configEnum).get(0);
		}
		return getMap(configEnum).values().iterator().next();
	}
	
	/**
	 * If the given ConfigEnum is of {@link ValueType#LIST}, the list of values will be returned. <br>
	 * If the given ConfigEnum is of {@link ValueType#MAP}, a list of the map values will be returned.<br>
	 * If the given ConfigEnum is of {@link ValueType#SINGLE}, a list containing the single value will be returned.<br>
	 * This is to prevent null pointers. Call the appropriate getter method for the ConfigEnum's ValueType.
	 * @param configEnum - The ConfigEnum to get the loaded config value list for
	 * @return the loaded config value list for the given ConfigEnum
	 */
	public <E extends Enum<E> & ConfigEnum<E, T>, T> List<T> getList(E configEnum) {
		Validate.notNull(configEnum);
		
		if (configEnum.getValueType() == ValueType.LIST) {
			Class<T> type = getType(configEnum);
			Map<ConfigEnum<?, ?>, List<?>> tLists = lists.get(type);
			@SuppressWarnings("unchecked")
			List<T> list = (List<T>) tLists.get(configEnum);
			return list;
		} else if (configEnum.getValueType() == ValueType.MAP) {
			return ImmutableList.<T> builder().addAll(getMap(configEnum).values()).build();
		}
		return ImmutableList.<T> builder().add(getValue(configEnum)).build();
	}
	
	/**
	 * If the given ConfigEnum is of {@link ValueType#MAP}, the map of keys to values will be returned. <br>
	 * If the given ConfigEnum is of {@link ValueType#LIST}, a map of the list index to list value will be returned.<br>
	 * If the given ConfigEnum is of {@link ValueType#SINGLE}, a map of index 0 to the single value will be returned.<br>
	 * This is to prevent null pointers. Call the appropriate getter method for the ConfigEnum's ValueType.
	 * @param configEnum - The ConfigEnum to get the loaded config map for
	 * @return the loaded config map for the given ConfigEnum
	 */
	public <E extends Enum<E> & ConfigEnum<E, T>, T> Map<String, T> getMap(E configEnum) {
		Validate.notNull(configEnum);
		
		if (configEnum.getValueType() == ValueType.MAP) {
			Class<T> type = getType(configEnum);
			Map<ConfigEnum<?, ?>, Map<String, ?>> tMaps = maps.get(type);
			@SuppressWarnings("unchecked")
			Map<String, T> map = (Map<String, T>) tMaps.get(configEnum);
			return map;
		} else if (configEnum.getValueType() == ValueType.LIST) {
			ImmutableMap.Builder<String, T> builder = ImmutableMap.<String, T> builder();
			int index = 0;
			for (T val : getList(configEnum)) {
				builder.put(String.valueOf(index++), val);
			}
			return builder.build();
		}
		return ImmutableMap.<String, T> builder().put("0", getValue(configEnum)).build();
	}
	
	/**
	 * If a config value is of a type other than String, Integer, Boolean, ChatColor, or ItemStack,
	 * a new Converter will need to be registered for config values of that type.
	 * @param type - The type the converter converts to
	 * @param converter - The converter instance to register for objects of the given type
	 */
	public <T> void registerConverter(Class<? super T> type, Converter<T> converter) {
		Validate.notNull(type);
		Validate.notNull(converter);
		
		converters.put(type, converter);
	}
	
	
	private <T> T getValue(Class<T> type, Converter<T> converter, String path) {
		T value = null;
		Object val = plugin.getConfig().get(path);
		if (val != null) {
			if (!plugin.getConfig().isSet(path)) {
				plugin.getLogger().log(Level.SEVERE, String.format(MISSING_VALUE, type.getName(), path, val));
			}
			value = converter.convert(val);
			if (value == null) {
				value = converter.convert(plugin.getConfig().getDefaults().get(path));
				plugin.getLogger().log(Level.SEVERE, String.format(INVALID_VALUE, type.getName(), path, val));
			}
		}
		return value;
	}
	
	private <T> List<T> getList(Class<T> type, Converter<T> converter, String path) {
		List<T> list = null;
		List<?> vals = plugin.getConfig().getList(path);
		if (vals != null) {
			if (!plugin.getConfig().isSet(path)) {
				plugin.getLogger().log(Level.SEVERE, String.format(MISSING_VALUE, type.getName() + " List", path, Arrays.toString(vals.toArray())));
			}
			list = convertList(converter, vals);
			if (list == null) {
				list = convertList(converter, plugin.getConfig().getDefaults().getList(path));
				plugin.getLogger().log(Level.SEVERE, String.format(INVALID_VALUE, type.getName(), path, Arrays.toString(vals.toArray())));
			}
		}
		return list;
	}
	
	private <T> List<T> convertList(Converter<T> converter, List<?> vals) {
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
	
	private <T> Map<String, T> getMap(Class<T> type, Converter<T> converter, String path) {
		Map<String, T> map = null;
		ConfigurationSection section = plugin.getConfig().getConfigurationSection(path);
		if (section != null) {
			Map<String, ?> vals = section.getValues(false);
			if (!plugin.getConfig().isSet(path)) {
				plugin.getLogger().log(Level.SEVERE, String.format(MISSING_VALUE, type.getName() + " List", path, vals.toString()));
			}
			map = convertMap(converter, vals);
			if (map == null) {
				ConfigurationSection defaultSection = plugin.getConfig().getDefaults().getConfigurationSection(path);
				if (defaultSection != null) {
					map = convertMap(converter, defaultSection.getValues(false));
					plugin.getLogger().log(Level.SEVERE, String.format(INVALID_VALUE, type.getName(), path, vals.toString()));
				}
			}
		}
		return map;
	}
	
	private <T> Map<String, T> convertMap(Converter<T> converter, Map<String, ?> vals) {
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
	
	private void registerDefaultConverters() {
		registerConverter(String.class, new Converter<String>() {
			
			@Override
			public String convert(Object obj) {
				return obj.toString();
			}
		});
		registerConverter(Integer.class, new Converter<Integer>() {
			
			@Override
			public Integer convert(Object obj) {
				if (obj instanceof Number) {
					return ((Number) obj).intValue();
				}
				try {
					return Integer.valueOf(obj.toString());
				} catch (NumberFormatException e) {} catch (NullPointerException e) {}
				return null;
			}
		});
		registerConverter(Boolean.class, new Converter<Boolean>() {
			
			@Override
			public Boolean convert(Object obj) {
				if (obj instanceof Boolean) {
					return (Boolean) obj;
				}
				if (obj != null) {
					return Boolean.valueOf(obj.toString().toLowerCase());
				}
				return null;
			}
		});
		registerConverter(ChatColor.class, new Converter<ChatColor>() {
			
			@Override
			public ChatColor convert(Object obj) {
				if (obj instanceof ChatColor) {
					return (ChatColor) obj;
				}
				if (obj != null) {
					return DeadmanUtils.getChatColor(obj.toString());
				}
				return null;
			}
		});
		registerConverter(ItemStack.class, new Converter<ItemStack>() {
			
			@Override
			public ItemStack convert(Object obj) {
				if (obj instanceof ItemStack) {
					return (ItemStack) obj;
				}
				if (obj != null) {
					return DeadmanUtils.getItemStack(obj.toString());
				}
				return null;
			}
		});
	}
	
}
