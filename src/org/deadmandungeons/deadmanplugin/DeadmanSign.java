package org.deadmandungeons.deadmanplugin;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.material.MaterialData;
import org.bukkit.metadata.FixedMetadataValue;
import org.deadmandungeons.deadmanplugin.filedata.DataEntry;

import com.google.common.collect.ImmutableList;

/**
 * This abstract class is to be implemented to define a certain Sign type which represents objects of type T.
 * @param <T> - The type of the object that the DeadmanSign represents
 * @author Jon
 */
public abstract class DeadmanSign<T> {
	
	private static final String INVALID_SIGN_ENTRY = "The sign entry '%s' in the %2$s data list at path '%s.%2$s' is invalid! This entry will be removed from file.";
	
	// This is the most ridiculous Generic use ever
	private static final Map<Class<? extends DeadmanSign<?>>, DeadmanSignHandler<?, ? extends DeadmanSign<?>>> handlers = new HashMap<Class<? extends DeadmanSign<?>>, DeadmanSignHandler<?, ? extends DeadmanSign<?>>>();
	
	private final Sign sign;
	private final DataEntry dataEntry;
	private final T signObject;
	
	private final MaterialData materialData;
	
	public DeadmanSign(Sign sign, DataEntry dataEntry, T signObject) {
		if (!handlers.containsKey(getClass())) {
			throw new IllegalStateException("A DeadmanSignHandler has not been set for the " + getClass().getCanonicalName());
		}
		Validate.notNull(sign, "sign cannot be null");
		Validate.notNull(dataEntry, "dataEntry cannot be null");
		Validate.notNull(signObject, "signObject cannot be null");
		this.sign = sign;
		this.dataEntry = dataEntry;
		this.signObject = signObject;
		materialData = sign.getData();
	}
	
	/**
	 * @return The bukkit {@link Sign} object
	 */
	public Sign getSign() {
		// return the current state of the stored sign because BlockState is not persistent
		return DeadmanUtils.getSignState(sign.getBlock(), materialData);
	}
	
	/**
	 * @return The {@link DataEntry} object containing the information about this sign
	 */
	public DataEntry getDataEntry() {
		return dataEntry;
	}
	
	/**
	 * @return The Object of type T that this sign represents
	 */
	public T getSignObject() {
		return signObject;
	}
	
	/**
	 * This method should be used to update the lines on the bukkit {@link Sign Sign}
	 */
	public void update() {
		Sign sign = getSign();
		String[] lines = getLines();
		for (int i = 0; i < lines.length && i < 4; i++) {
			sign.setLine(i, lines[i]);
		}
		sign.update(true);
	}
	
	/**
	 * This method should be used to update the lines on the bukkit {@link Sign Sign} during a {@link SignChangeEvent}
	 */
	public void update(SignChangeEvent event) {
		String[] lines = getLines();
		for (int i = 0; i < lines.length && i < 4; i++) {
			event.setLine(i, lines[i]);
		}
	}
	
	/**
	 * This method should be called to set the lines for this DeadmanSign object
	 */
	public abstract String[] getLines();
	
	/**
	 * log a severe message stating that the given dataEntry was invalid and that it will be removed from file
	 * @param entry - The invalid DataEntry
	 * @param property - The name of the config property containing the invalid DataEntry
	 * @param configPath - The path to the given property
	 */
	public static void alertInvalidEntry(DeadmanPlugin plugin, DataEntry entry, String property, String configPath) {
		plugin.getLogger().log(Level.SEVERE, String.format(INVALID_SIGN_ENTRY, entry.toString(), property, configPath));
	}
	
	/**
	 * This will unregister the events of any {@link DeadmanSignHandler} that was previously set for the
	 * given {@link DeadmanSign} class if any, and register the events of the given DeadmanSignHandler
	 * @param signClass - The class of the DeadmanSign the DeadmanSignHandler is for
	 * @param handler - The instance of the DeadmanSignHandler to set as the handler for all stored instances of the given DeadmanSign type
	 */
	public static <V, T extends DeadmanSign<V>> void setHandler(Class<T> signClass, DeadmanSignHandler<V, T> handler) {
		Validate.notNull(signClass, "signClass cannot be null");
		Validate.notNull(handler, "handler cannot be null");
		
		DeadmanSignHandler<V, T> previous = getHandler(signClass);
		if (previous != null) {
			HandlerList.unregisterAll(previous);
		}
		Bukkit.getPluginManager().registerEvents(handler, handler.getPlugin());
		handlers.put(signClass, handler);
	}
	
	/**
	 * @param signClass - The class of the {@link DeadmanSign} the desired {@link DeadmanSignHandler} is for
	 * @return the DeadmanSignHandler instance that was set for the given DeadmanSign class or if null no DeadmanSignHandler was set
	 */
	public static <V, T extends DeadmanSign<V>> DeadmanSignHandler<V, T> getHandler(Class<T> signClass) {
		Validate.notNull(signClass, "signClass cannot be null");
		@SuppressWarnings("unchecked")
		DeadmanSignHandler<V, T> handler = (DeadmanSignHandler<V, T>) handlers.get(signClass);
		return handler;
	}
	
	
	/**
	 * This Listener class is provided to improve modularity with events relating to DeadmanSigns.
	 * Extend this class for each {@link DeadmanSign} of type T.<br />
	 * Construct this object for each DeadmanSign type that the plugin will be listening to.
	 * Call {@link DeadmanSign#setHandler(Class, DeadmanSignHandler)} to register the Sign events and
	 * bind a handler to the DeadmanSign of type T.<br>
	 * All DeadmanSigns of type T that are created should be stored in this class's deadmanSigns HashMap
	 * using {@link #getDeadmanSigns()}
	 * @param <V> - The type of the object that the signs of type T represent
	 * @param <T> - The {@link DeadmanSign} involved in the sign events
	 * @author Jon
	 */
	public static abstract class DeadmanSignHandler<V, T extends DeadmanSign<V>> implements Listener {
		
		private Map<Location, T> deadmanSigns = new HashMap<Location, T>();
		
		private final List<String> signTags;
		private final DeadmanPlugin plugin;
		private final int cooldown;
		
		public DeadmanSignHandler(DeadmanPlugin plugin, String signTag) {
			this(plugin, signTag, -1);
		}
		
		public DeadmanSignHandler(DeadmanPlugin plugin, String signTag, int cooldown) {
			this(plugin, ImmutableList.of(signTag), cooldown);
		}
		
		public DeadmanSignHandler(DeadmanPlugin plugin, List<String> signTags, int cooldown) {
			this.signTags = signTags;
			this.plugin = plugin;
			this.cooldown = cooldown;
		}
		
		/**
		 * @return the {@link DeadmanPlugin} this DeadmanSignHandler is for
		 */
		public final DeadmanPlugin getPlugin() {
			return plugin;
		}
		
		@EventHandler(ignoreCancelled = true)
		public void onSignCreate(SignChangeEvent event) {
			Sign sign = DeadmanUtils.getSignState(event.getBlock());
			if (sign != null && signTags.contains(event.getLine(0))) {
				onSignCreate(event, sign);
			}
		}
		
		@EventHandler(ignoreCancelled = true)
		public void onSignBreak(BlockBreakEvent event) {
			T deadmanSign = deadmanSigns.get(event.getBlock().getLocation());
			if (deadmanSign != null) {
				onSignBreak(event, deadmanSign);
			}
		}
		
		@EventHandler(ignoreCancelled = true)
		public void onPlayerInteract(PlayerInteractEvent event) {
			if (event.getClickedBlock() != null) {
				T deadmanSign = deadmanSigns.get(event.getClickedBlock().getLocation());
				if (deadmanSign != null) {
					// Handle sign cooldown
					if (cooldown > 0 && !event.getPlayer().isOp()) {
						String metadataKey = plugin.getName() + "-sign-cooldown";
						Long timestamp = DeadmanUtils.getMetadata(plugin, event.getPlayer(), metadataKey, Long.class);
						if (timestamp != null) {
							long timeLeft = ((timestamp) + (cooldown * 1000)) - System.currentTimeMillis();
							if (timeLeft > 0) {
								int secondsLeft = (int) Math.ceil(timeLeft / 1000);
								event.getPlayer().sendMessage(ChatColor.RED + "You can click this sign in " + secondsLeft + " seconds");
								return;
							}
						}
						
						event.getPlayer().setMetadata(metadataKey, new FixedMetadataValue(plugin, System.currentTimeMillis()));
					}
					
					onSignClick(event, deadmanSign);
				}
			}
		}
		
		// TODO maybe return UnmodifiableMap and provide add/remove methods
		/**
		 * @return The reference to all of the DeadmanSigns of type T for this plugin
		 */
		public final Map<Location, T> getDeadmanSigns() {
			return deadmanSigns;
		}
		
		public final Map<Location, T> getDeadmanSigns(V signObject) {
			Map<Location, T> deadmanSigns = new HashMap<Location, T>();
			for (T deadmanSign : this.deadmanSigns.values()) {
				if (deadmanSign.getSignObject().equals(signObject)) {
					deadmanSigns.put(deadmanSign.sign.getLocation(), deadmanSign);
				}
			}
			return deadmanSigns;
		}
		
		/**
		 * clear the text on the {@link Sign} object of all the stored DeadmanSigns of type T and remove them from the datastore
		 */
		public final void clearSigns() {
			for (T deadmanSign : deadmanSigns.values()) {
				DeadmanUtils.clearSign(deadmanSign.getSign());
			}
			deadmanSigns.clear();
		}
		
		/**
		 * clear the text on the {@link Sign} object of all the stored DeadmanSigns of type T
		 * that represent the given sign Object, and remove them from the datastore
		 * @param signObject - The sign Object of type V that the cleared signs should represent
		 */
		public final void clearSigns(V signObject) {
			Iterator<T> iter = deadmanSigns.values().iterator();
			while (iter.hasNext()) {
				T deadmanSign = iter.next();
				if (deadmanSign.getSignObject().equals(signObject)) {
					DeadmanUtils.clearSign(deadmanSign.getSign());
					iter.remove();
				}
			}
		}
		
		/**
		 * @param signObject - The sign Object of type V that the updated signs should represent
		 */
		public final void updateSigns(V signObject) {
			for (T deadmanSign : deadmanSigns.values()) {
				if (deadmanSign.getSignObject().equals(signObject)) {
					deadmanSign.update();
				}
			}
		}
		
		public final void loadSigns(V signObject) {
			Set<T> loadedSigns = load(signObject);
			// clear signs after load in case implementor manually adds the DeadmanSigns to the datastore
			clearSigns(signObject);
			if (loadedSigns != null && !loadedSigns.isEmpty()) {
				for (T deadmanSign : loadedSigns) {
					deadmanSigns.put(deadmanSign.sign.getLocation(), deadmanSign);
				}
			}
		}
		
		protected abstract void onSignCreate(SignChangeEvent event, Sign sign);
		
		protected abstract void onSignBreak(BlockBreakEvent event, T deadmanSign);
		
		protected abstract void onSignClick(PlayerInteractEvent event, T deadmanSign);
		
		protected abstract Set<T> load(V signObject);
		
	}
	
}
