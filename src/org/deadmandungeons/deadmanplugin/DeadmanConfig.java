package org.deadmandungeons.deadmanplugin;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;


public abstract class DeadmanConfig {
	
	private static final String MISSING_STRING = "The String config value at path '%s' is missing. Defaulting to value '%s'";
	private static final String UNMATCHED_STRING = "The String config value at path '%s' is invalid. "
			+ "It should match the regex: '%s'. Defaulting to value '%s'";
	private static final String MISSING_INTERGER = "The integer config value at path '%s' is missing. Defaulting to value '%s'";
	private static final String INVALID_INTERGER = "The integer config value at path '%s' is not a number. Defaulting to value '%s'";
	private static final String MISSING_BOOLEAN = "The boolean config value at path '%s' is missing. Defaulting to value '%s'";
	private static final String INVALID_BOOLEAN = "The boolean config value at path '%s' is not 'true' or 'false'. Defaulting to value '%s'";
	private static final String INVALID_CHATCOLOR = "The Color config value at path '%s' is not a valid Minecraft color. Defaulting to value '%s'";
	private static final String INVALID_MATERIAL = "The material config value at path '%s' is invalid. "
			+ "It should be a block name/id. Defaulting to value '%s'";
	
	private static final String ITEMSTACK_REGEX = "[A-Za-z0-9_]+(:\\d+)?(:\\d+)?";
	
	private Map<Class<?>, ValueLoader<?>> defaultLoaders = new HashMap<Class<?>, ValueLoader<?>>();
	
	private DeadmanPlugin plugin;
	
	protected DeadmanConfig(DeadmanPlugin plugin) {
		this.plugin = plugin;
		
		registerLoader(String.class, new ValueLoader<String>() {
			
			@Override
			public String loadValue(String path) {
				// TODO
				return getString(path, null);
			}
		});
		registerLoader(Integer.class, new ValueLoader<Integer>() {
			
			@Override
			public Integer loadValue(String path) {
				return getInt(path);
			}
		});
		registerLoader(Boolean.class, new ValueLoader<Boolean>() {
			
			@Override
			public Boolean loadValue(String path) {
				return getBoolean(path);
			}
		});
		registerLoader(ChatColor.class, new ValueLoader<ChatColor>() {
			
			@Override
			public ChatColor loadValue(String path) {
				return getChatColor(path);
			}
		});
		registerLoader(ItemStack.class, new ValueLoader<ItemStack>() {
			
			@Override
			public ItemStack loadValue(String path) {
				return getItemStack(path);
			}
		});
		registerLoader(Map.class, new ValueLoader<Map<String, String>>() {
			
			@Override
			public Map<String, String> loadValue(String path) {
				return getMap(path);
			}
		});
	}
	
	
	protected interface ValueLoader<T> {
		
		T loadValue(String path);
	}
	
	protected interface ConfigEnum<E extends Enum<E> & ConfigEnum<E, T>, T> {
		
		String getPath();
		
		T value();
		
		EnumMap<E, T> getValues();
	}
	
	@SuppressWarnings("unchecked")
	protected <E extends Enum<E> & ConfigEnum<E, T>, T> void loadValues(Class<E> enumClass, Class<? super T> valueClass) {
		Validate.notNull(valueClass);
		ValueLoader<T> loader = (ValueLoader<T>) defaultLoaders.get(valueClass);
		if (loader == null) {
			String msg = "A ValueLoader for config values of type '%s' is not supported! use loadValues(enumClass, loader) "
					+ "to load config values for types that are not supported by default.";
			throw new IllegalStateException(String.format(msg, valueClass.getCanonicalName()));
		}
		loadValues(enumClass, loader);
	}
	
	protected <E extends Enum<E> & ConfigEnum<E, T>, T> void loadValues(Class<E> enumClass, ValueLoader<T> loader) {
		Validate.notNull(enumClass);
		Validate.notNull(loader);
		
		for (E configEnum : EnumSet.allOf(enumClass)) {
			configEnum.getValues().put(configEnum, loader.loadValue(configEnum.getPath()));
		}
	}
	
	
	private <T> void registerLoader(Class<? super T> type, ValueLoader<T> loader) {
		Validate.notNull(type);
		Validate.notNull(loader);
		
		defaultLoaders.put(type, loader);
	}
	
	private String getString(String path, String regex) {
		String val = plugin.getConfig().getString(path);
		if (!plugin.getConfig().isSet(path)) {
			plugin.getLogger().log(Level.SEVERE, String.format(MISSING_STRING, path, val));
		} else if (regex != null && !val.matches(regex)) {
			val = plugin.getConfig().getDefaults().getString(path);
			plugin.getLogger().log(Level.SEVERE, String.format(UNMATCHED_STRING, path, regex, val));
		}
		return val;
	}
	
	private int getInt(String path) {
		int val = plugin.getConfig().getInt(path);
		if (!plugin.getConfig().isSet(path)) {
			plugin.getLogger().log(Level.SEVERE, String.format(MISSING_INTERGER, path, val));
		}
		if (!plugin.getConfig().isInt(path)) {
			plugin.getLogger().log(Level.SEVERE, String.format(INVALID_INTERGER, path, val));
		}
		return val;
	}
	
	private boolean getBoolean(String path) {
		boolean val = plugin.getConfig().getBoolean(path);
		if (!plugin.getConfig().isSet(path)) {
			plugin.getLogger().log(Level.SEVERE, String.format(MISSING_BOOLEAN, path, val));
		}
		if (!plugin.getConfig().isBoolean(path)) {
			plugin.getLogger().log(Level.SEVERE, String.format(INVALID_BOOLEAN, path, val));
		}
		return val;
	}
	
	private ChatColor getChatColor(String path) {
		ChatColor color = DeadmanUtils.getChatColor(getString(path, null));
		if (color == null) {
			color = DeadmanUtils.getChatColor(plugin.getConfig().getDefaults().getString(path));
			plugin.getLogger().log(Level.SEVERE, String.format(INVALID_CHATCOLOR, path, color.name()));
		}
		return color;
	}
	
	private ItemStack getItemStack(String path) {
		ItemStack itemStack = DeadmanUtils.getItemStack(getString(path, ITEMSTACK_REGEX));
		if (itemStack == null) {
			itemStack = DeadmanUtils.getItemStack(plugin.getConfig().getDefaults().getString(path));
			plugin.getLogger().log(Level.SEVERE, String.format(INVALID_MATERIAL, path, itemStack.getType()));
		}
		return itemStack;
	}
	
	private Map<String, String> getMap(String path) {
		Map<String, String> map = new HashMap<String, String>();
		ConfigurationSection conf = plugin.getConfig().getConfigurationSection(path);
		Map<String, Object> vals = conf.getValues(false);
		for (String key : vals.keySet()) {
			if (vals.get(key) instanceof String) {
				map.put(key, (String) vals.get(key));
			}
		}
		
		return map;
	}
	
}
