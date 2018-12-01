package com.deadmandungeons.deadmanplugin.signs;

import com.deadmandungeons.deadmanplugin.DeadmanPlugin;
import com.deadmandungeons.deadmanplugin.DeadmanUtils;
import com.deadmandungeons.deadmanplugin.WorldCoord;
import com.deadmandungeons.deadmanplugin.filedata.DataEntry;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.block.Sign;
import org.bukkit.event.HandlerList;
import org.bukkit.material.MaterialData;

import java.util.HashMap;
import java.util.Map;

/**
 * Minecraft Sign blocks can be used as user interfaces since they can display text, and can be interacted with.
 * This abstract class is a template that provides the base functionality for binding an object to a sign block
 * which can be interacted with. An implementation of this class should represent a certain Sign type which can
 * bind objects of type T to an actual Sign block in a world.
 * @param <T> - The type of the object that the DeadmanSign represents
 * @author Jon
 */
public abstract class DeadmanSign<T> {

    private static final String INVALID_SIGN_ENTRY = "The sign entry '%1$s' in the %2$s data list at path '%3$s.%2$s' is invalid! This sign cannot be loaded.";

    private static final Map<Class<? extends DeadmanSign<?>>, DeadmanSignHandler<?, ?>> handlers = new HashMap<>();

    private final Sign sign;
    private final DataEntry dataEntry;
    private final T signObject;

    private final WorldCoord coord;
    private final MaterialData materialData;

    public DeadmanSign(Sign sign, DataEntry dataEntry, T signObject) {
        Validate.notNull(sign, "sign cannot be null");
        Validate.notNull(dataEntry, "dataEntry cannot be null");
        Validate.notNull(signObject, "signObject cannot be null");
        this.sign = sign;
        this.dataEntry = dataEntry;
        this.signObject = signObject;
        coord = WorldCoord.at(sign.getLocation());
        materialData = sign.getData();
    }

    /**
     * NOTE: This will force reset the block to a Sign if it is not one
     * @return The bukkit {@link Sign} BlockState object
     */
    public Sign getSignState() {
        // return the current state of the stored sign because BlockState is not persistent
        return DeadmanUtils.getSignState(sign.getBlock(), materialData);
    }

    /**
     * @return the WorldCoord that this DeadmanSign is located at
     */
    public WorldCoord getCoord() {
        return coord;
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
        Sign sign = getSignState();
        String[] lines = getLines();
        for (int i = 0; i < lines.length && i < 4; i++) {
            sign.setLine(i, lines[i]);
        }
        sign.update(true);
    }

    /**
     * This method should be called to set the lines for this DeadmanSign object
     */
    public abstract String[] getLines();

    /**
     * log a warning message stating that the given dataEntry was invalid and that the sign cannot be loaded
     * @param entry - The invalid DataEntry
     * @param property - The name of the config property containing the invalid DataEntry
     * @param configPath - The path to the given property
     */
    public static void alertInvalidEntry(DeadmanPlugin plugin, DataEntry entry, String property, String configPath) {
        plugin.getLogger().warning(String.format(INVALID_SIGN_ENTRY, entry.toString(), property, configPath));
    }

    /**
     * This will unregister the events of any {@link DeadmanSignHandler} that was previously set for the
     * given {@link DeadmanSign} class if any, and register the events of the given DeadmanSignHandler
     * @param signClass - The class of the DeadmanSign the handler should be bound to
     * @param handler - The instance of the DeadmanSignHandler to set as the handler for all stored instances of the given DeadmanSign type
     */
    public static <V, T extends DeadmanSign<V>> void setHandler(Class<T> signClass, DeadmanSignHandler<V, T> handler) {
        Validate.notNull(signClass, "signClass cannot be null");
        Validate.notNull(handler, "handler cannot be null");

        DeadmanSignHandler<V, T> previous = getHandler(signClass);
        if (previous != null) {
            HandlerList.unregisterAll(previous.getListener());
        }
        Bukkit.getPluginManager().registerEvents(handler.getListener(), handler.getPlugin());
        handlers.put(signClass, handler);
    }

    /**
     * @param signClass - The class of the {@link DeadmanSign} the desired {@link DeadmanSignHandler} is bound to
     * @return the DeadmanSignHandler instance that was bound to the given DeadmanSign class or if null no DeadmanSignHandler was set
     */
    public static <V, T extends DeadmanSign<V>> DeadmanSignHandler<V, T> getHandler(Class<T> signClass) {
        Validate.notNull(signClass, "signClass cannot be null");
        @SuppressWarnings("unchecked")
        DeadmanSignHandler<V, T> handler = (DeadmanSignHandler<V, T>) handlers.get(signClass);
        return handler;
    }

    /**
     * clear the sign text and datastore of all instances of all DeadmanSign types for the given plugin.
     * @param plugin - The DeadmanPlugin that the signs to clear are for
     */
    public static void removeSigns(DeadmanPlugin plugin) {
        for (DeadmanSignHandler<?, ?> signHandler : handlers.values()) {
            if (signHandler.getPlugin().equals(plugin)) {
                signHandler.removeSigns();
            }
        }
    }

}
