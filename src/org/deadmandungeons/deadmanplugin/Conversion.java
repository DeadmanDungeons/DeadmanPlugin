package org.deadmandungeons.deadmanplugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * This utility class provides methods for converting scalar values to objects of the specified Class.
 * A {@link Converter} is created and registered using {@link #registerConverter(Class, Converter)},
 * and obtained using {@link #getConverter(Class)}. A set of default converters are created which
 * are shown by the various conversion methods in this class. The default converters can be overriden,
 * in the registration datastore. However, the conversion methods will still use the default converters
 * unless the methods are overridden.
 * @author Jon
 */
public class Conversion {
	
	private static final Map<Class<? extends DeadmanPlugin>, Conversion> instances = new HashMap<Class<? extends DeadmanPlugin>, Conversion>();
	
	private final Map<Class<?>, Converter<?>> converters = new HashMap<Class<?>, Converter<?>>();
	
	protected Conversion(Class<? extends DeadmanPlugin> pluginClass) {
		Conversion previous = instances.get(pluginClass);
		if (previous != null) {
			converters.putAll(previous.converters);
		} else {
			registerDefaultConverters();
		}
		
		instances.put(pluginClass, this);
	}
	
	/**
	 * @param pluginClass - The DeadmanPlugin class of the desired Conversion utility class
	 * @return the Conversion instance for the given DeadmanPlugin class
	 */
	public static Conversion get(Class<? extends DeadmanPlugin> pluginClass) {
		Conversion convert = instances.get(pluginClass);
		return (convert != null ? convert : new Conversion(pluginClass));
	}
	
	/**
	 * Register a new Converter, or replace an existing Converter of the given type
	 * @param type - The data type of which this value should be converted
	 * @param converter - The converter instance to register for objects of the given type
	 */
	public <T> void registerConverter(Class<? super T> type, Converter<T> converter) {
		Validate.notNull(type);
		Validate.notNull(converter);
		
		converters.put(type, converter);
	}
	
	/**
	 * @param type - The data type of the desired Converter
	 * @return the Converter for the given type class if one has been registered, or null otherwise
	 */
	public <T> Converter<T> getConverter(Class<? super T> type) {
		@SuppressWarnings("unchecked")
		Converter<T> converter = (Converter<T>) converters.get(type);
		return converter;
	}
	
	/**
	 * This will cast value as String if it's an instanceof String. Otherwise,
	 * if value is not null, the result of <code>value.toString()</code> will be returned.
	 * @param value - The input value to be converted
	 * @return value as a String, or null if value is null
	 */
	public String toString(Object value) {
		return STRING_CONVERTER.convert(value);
	}
	
	/**
	 * This will cast value as Number if it's an instanceof Number. Otherwise,
	 * if value is not null, the result of <code>value.toString()</code> will
	 * be parsed using {@link NumberUtils#createNumber(String)}
	 * @param value - The input value to be converted
	 * @return value as a Number, or null if value is not a Number
	 */
	public Number toNumber(Object value) {
		return NUMBER_CONVERTER.convert(value);
	}
	
	/**
	 * This will cast value as a Boolean if it's an instanceof Boolean. Otherwise,
	 * if value is not null, the result of <code>value.toString()</code> will
	 * be parsed using {@link Boolean#valueOf(String)}
	 * @param value - The input value to be converted
	 * @return value as a Boolean, or null if value is null
	 */
	public Boolean toBoolean(Object value) {
		return BOOLEAN_CONVERTER.convert(value);
	}
	
	/**
	 * This will cast value as ChatColor if it's an instanceof ChatColor. Otherwise,
	 * if value is not null, the result of <code>value.toString()</code> will
	 * be compared to the ChatColor names. If no name was matched, and the String
	 * representation is in color code format, {@link ChatColor#getByChar(char)} will be used.
	 * @param value - The input value to be converted
	 * @return value as ChatColor, or null if value is not a ChatColor
	 */
	public ChatColor toChatColor(Object value) {
		return CHAT_COLOR_CONVERTER.convert(value);
	}
	
	/**
	 * This will cast value as ItemStack if it's an instanceof ItemStack. Otherwise,
	 * if value is not null, the result of <code>value.toString()</code> will
	 * be parsed using the format <code>&lt;material&gt;:&lt;data&gt;:&lt;amount&gt;</code><br>
	 * The data and amount are optional, and data and amount will always be the 2nd and 3rd parts. <br>
	 * Examples:
	 * <ul>
	 * <li>bone:0:5</li>
	 * <li>log:2</li>
	 * <li>diamond_sword</li>
	 * </ul>
	 * @param value - The input value to be converted
	 * @return value as ItemStack, or null if value is not an ItemStack
	 */
	public ItemStack toItemStack(Object value) {
		return ITEM_STACK_CONVERTER.convert(value);
	}
	
	/**
	 * This will cast value as Collection if it's an instanceof Collection. Otherwise,
	 * null will be returned.
	 * @param value - The input value to be converted
	 * @return value as a Collection, or null if value is not a Collection
	 */
	public Collection<?> toCollection(Object value) {
		return COLLECTION_CONVERTER.convert(value);
	}
	
	/**
	 * This will call {@link #toCollection(Object)} on value, and return a new ArrayList
	 * containing each element in the collection as a String using {@link #toString(Object)}.
	 * @param value - The input value to be converted
	 * @return value as a List of String values, or null if value is not a Collection
	 */
	public List<String> toStringList(Object value) {
		return STRING_LIST_CONVERTER.convert(value);
	}
	
	
	/**
	 * A General purpose data type converter that can be registered and used with the {@link Conversion} class
	 * to manage the conversion of objects from one type to another
	 * @param <T> - The type to be converted to
	 * @author Jon
	 */
	public static interface Converter<T> {
		
		/**
		 * Convert the specified input object into an output object of the specified type
		 * @param type - The data type to which this value should be converted
		 * @param value - The input value to be converted
		 * @return the converted value
		 */
		T convert(Object value);
	}
	
	private static final Converter<String> STRING_CONVERTER = new Converter<String>() {
		
		@Override
		public String convert(Object value) {
			if (value instanceof String) {
				return (String) value;
			} else if (value != null) {
				return value.toString();
			}
			return null;
		}
	};
	private static final Converter<Number> NUMBER_CONVERTER = new Converter<Number>() {
		
		@Override
		public Number convert(Object value) {
			if (value instanceof Number) {
				return (Number) value;
			} else if (value != null && NumberUtils.isNumber(value.toString())) {
				return NumberUtils.createNumber(value.toString());
			}
			return null;
		}
	};
	private static final Converter<Boolean> BOOLEAN_CONVERTER = new Converter<Boolean>() {
		
		@Override
		public Boolean convert(Object value) {
			if (value instanceof Boolean) {
				return (Boolean) value;
			} else if (value != null) {
				return Boolean.valueOf(value.toString().toLowerCase());
			}
			return null;
		}
	};
	private static final Converter<ChatColor> CHAT_COLOR_CONVERTER = new Converter<ChatColor>() {
		
		@Override
		public ChatColor convert(Object value) {
			if (value instanceof ChatColor) {
				return (ChatColor) value;
			}
			if (value != null) {
				String color = value.toString();
				for (ChatColor chatColor : ChatColor.values()) {
					if (chatColor.name().equalsIgnoreCase(color)) {
						return chatColor;
					}
				}
				if (color.length() == 1) {
					return ChatColor.getByChar(color.charAt(0));
				} else if (color.length() == 2 && (color.startsWith("&") || color.startsWith("§"))) {
					return ChatColor.getByChar(color.charAt(1));
				}
			}
			return null;
		}
	};
	private static final Converter<ItemStack> ITEM_STACK_CONVERTER = new Converter<ItemStack>() {
		
		@Override
		public ItemStack convert(Object value) {
			if (value instanceof ItemStack) {
				return (ItemStack) value;
			}
			if (value != null) {
				String[] parts = value.toString().split(":");
				Material type = Material.matchMaterial(parts[0]);
				if (type != null) {
					byte data = 0;
					if (parts.length >= 2) {
						data = Byte.parseByte(parts[1]);
					}
					int amount = 1;
					if (parts.length == 3) {
						amount = Integer.parseInt(parts[2]);
					}
					return new ItemStack(type, amount, (short) 0, data);
				}
			}
			return null;
		}
	};
	private static final Converter<Collection<?>> COLLECTION_CONVERTER = new Converter<Collection<?>>() {
		
		@Override
		public Collection<?> convert(Object value) {
			return (value instanceof Collection<?> ? (Collection<?>) value : null);
		}
	};
	private static final Converter<List<String>> STRING_LIST_CONVERTER = new Converter<List<String>>() {
		
		@Override
		public List<String> convert(Object value) {
			Collection<?> collection = COLLECTION_CONVERTER.convert(value);
			if (collection != null) {
				List<String> result = new ArrayList<String>();
				for (Object object : collection) {
					result.add(STRING_CONVERTER.convert(object));
				}
				return result;
			}
			return null;
		}
	};
	
	private void registerDefaultConverters() {
		registerConverter(String.class, STRING_CONVERTER);
		registerConverter(Number.class, NUMBER_CONVERTER);
		registerConverter(Boolean.class, BOOLEAN_CONVERTER);
		registerConverter(ChatColor.class, CHAT_COLOR_CONVERTER);
		registerConverter(ItemStack.class, ITEM_STACK_CONVERTER);
		registerConverter(Collection.class, COLLECTION_CONVERTER);
		registerConverter(List.class, STRING_LIST_CONVERTER);
	}
}
