package com.deadmandungeons.deadmanplugin.signs;

import com.deadmandungeons.deadmanplugin.DeadmanPlugin;
import com.deadmandungeons.deadmanplugin.DeadmanUtils;
import com.deadmandungeons.deadmanplugin.WorldCoord;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class is provided to improve modularity with events relating to DeadmanSigns.
 * Extend this class for each {@link DeadmanSign} of type T that the plugin will be listening to.<br />
 * Call {@link DeadmanSign#setHandler(Class, DeadmanSignHandler)} to register the Sign events and
 * bind a handler to the DeadmanSign of type T.<br>
 * All DeadmanSigns of type T are created or loaded are stored in this class's deadmanSigns HashMap datastore.
 * @param <T> - The type of the object that the signs of type T represent
 * @param <S> - The {@link DeadmanSign} involved in the sign events
 * @author Jon
 */
public abstract class DeadmanSignHandler<T, S extends DeadmanSign<T>> {

    private final Map<WorldCoord, S> deadmanSigns = new HashMap<>();
    private final DeadmanSignListener listener = new DeadmanSignListener();

    private final String signTag;
    private final DeadmanPlugin plugin;
    private final int cooldown;

    public DeadmanSignHandler(DeadmanPlugin plugin, String signTag) {
        this(plugin, signTag, -1);
    }

    public DeadmanSignHandler(DeadmanPlugin plugin, String signTag, int cooldown) {
        this.signTag = signTag;
        this.plugin = plugin;
        this.cooldown = cooldown;
    }

    /**
     * @return the {@link DeadmanPlugin} this DeadmanSignHandler is for
     */
    public final DeadmanPlugin getPlugin() {
        return plugin;
    }

    /**
     * @return an unmodifiable map of the DeadmanSigns of type S for this plugin
     */
    public Map<WorldCoord, S> getSigns() {
        return Collections.unmodifiableMap(deadmanSigns);
    }

    /**
     * @param signObject - The object of type T that the signs should represent as a filter for the returned Map
     * @return a new HashMap of the DeadmanSigns of type T that represent the given signObject for this plugin
     */
    public Map<WorldCoord, S> getSigns(T signObject) {
        Map<WorldCoord, S> signs = new HashMap<>();
        for (S deadmanSign : deadmanSigns.values()) {
            if (deadmanSign.getSignObject().equals(signObject)) {
                signs.put(deadmanSign.getCoord(), deadmanSign);
            }
        }
        return signs;
    }

    /**
     * @param deadmanSign - The DeadmanSign of type S to add to the this handler's DeadmanSign datastore
     * @return the previously stored DeadmanSign instance at the given sign's location, or null if no previous sign
     */
    public S addSign(S deadmanSign) {
        deadmanSign.update();
        return add(deadmanSign);
    }

    /**
     * Remove the given DeadmanSign of type S from this handler's DeadmanSign datastore,
     * and clear the text on the Sign block if the DeadmanSign exists in the datastore.
     * @param sign - The DeadmanSign of type S to remove from this handler's DeadmanSign datastore
     * @return true if the given sign object was removed from the deadmanSigns datastore and false if it wasn't being stored
     */
    public final boolean removeSign(S sign) {
        return removeSign(sign.getCoord()) != null;
    }

    /**
     * Remove the DeadmanSign of type S located at the given signCoord from this handler's DeadmanSign datastore,
     * and clear the text on the Sign block if a DeadmanSign exists.
     * @param signCoord - The world coordinate of the DeadmanSign to remove from the deadmanSigns datastore
     * @return the DeadmanSign object of type S at the given world coordinate that was removed from this
     * handler's DeadmanSign datastore. or null if there is no sign at the coordinate
     */
    public S removeSign(WorldCoord signCoord) {
        S deadmanSign = remove(signCoord);
        if (deadmanSign != null) {
            clearSign(deadmanSign);
        }
        return deadmanSign;
    }

    /**
     * Remove all DeadmanSigns of type S from the datastore,
     * and clear the text on the Sign block for each removed DeadmanSign if the current block state is a Sign.
     */
    public final void removeSigns() {
        removeSigns(null);
    }

    /**
     * Remove all DeadmanSigns of type T that represent the given sign Object from the datastore,
     * and clear the text on the Sign block for each removed DeadmanSign if the current block state is a Sign.
     * @param signObject - The sign Object of type T that the removed signs should represent
     */
    public void removeSigns(T signObject) {
        Set<S> toRemove = null;
        for (S deadmanSign : deadmanSigns.values()) {
            if (signObject == null || deadmanSign.getSignObject().equals(signObject)) {
                (toRemove == null ? toRemove = new HashSet<>() : toRemove).add(deadmanSign);
            }
        }
        if (toRemove != null) {
            for (S deadmanSign : toRemove) {
                clearSign(deadmanSign);
                remove(deadmanSign.getCoord());
            }
        }
    }

    /**
     * Clear the text on the given DeadmanSign in the world.<br>
     * If the current block state for the given DeadmanSign is not a Sign block, this will do nothing.
     * @param deadmanSign - The DeadmanSign of type S to clear
     */
    public final void clearSign(S deadmanSign) {
        Sign sign = DeadmanUtils.getSignState(deadmanSign.getCoord().getBlock());
        DeadmanUtils.clearSign(sign);
    }

    /**
     * Update the Sign block state for each DeadmanSign of type S that represent the given sign Object.
     * @param signObject - The sign Object of type T that the updated signs should represent
     */
    public void updateSigns(T signObject) {
        for (S deadmanSign : deadmanSigns.values()) {
            if (deadmanSign.getSignObject().equals(signObject)) {
                deadmanSign.update();
            }
        }
    }

    /**
     * Load all DeadmanSigns of type S that represent the given sign Object from file, and store them in the datastore.
     * @param signObject - The sign Object of type T that the loaded signs should represent
     */
    public final void loadSigns(T signObject) {
        Set<S> loadedSigns = load(signObject);
        // remove signs after load in case implementor manually adds the DeadmanSigns to the datastore
        removeSigns(signObject);
        if (loadedSigns != null && !loadedSigns.isEmpty()) {
            for (S deadmanSign : loadedSigns) {
                add(deadmanSign);
            }
        }
    }


    protected final Map<WorldCoord, S> getDatastore() {
        return deadmanSigns;
    }

    protected S add(S deadmanSign) {
        return deadmanSigns.put(deadmanSign.getCoord(), deadmanSign);
    }

    protected S remove(WorldCoord signCoord) {
        return deadmanSigns.remove(signCoord);
    }

    DeadmanSignListener getListener() {
        return listener;
    }


    /**
     * Invoked when a player creates a Sign block where the first line text is equal to
     * one of the sign tags provided in the constructor of this handler.<br>
     * Use {@link #setCreated(DeadmanSign) event.setCreated(createdDeadmanSign);} a DeadmanSign of type S was successfully created.<br>
     * Use {@link #setCanceled(boolean) event.setCanceled(true);} if a DeadmanSign could not be created.
     * @param event - The SignCreateEvent for the created sign
     */
    protected abstract void onSignCreate(SignCreateEvent event);

    /**
     * Invoked when a player breaks the block of a DeadmanSign stored in this handler's datastore.<br>
     * <b>Note:</b> The DeadmanSign will have already been removed from the datastore prior to the invocation of this method.
     * Use <code>event.setCanceled(true);</code> to cancel the breaking of the block and to add the DeadmanSign back to the datastore.
     * @param event - The BlockBreakEvent for the broken DeadmanSign
     * @param deadmanSign - The DeadmanSign of type S that was broken
     */
    protected abstract void onSignBreak(BlockBreakEvent event, S deadmanSign);

    /**
     * Invoked when a player clicks the Sign block of a DeadmanSign stored in this handler's datastore.
     * @param event - The PlayerInteractEvent for this clicked DeadmanSign
     * @param deadmanSign - The DeadmanSign of type S that was clicked
     */
    protected abstract void onSignClick(PlayerInteractEvent event, S deadmanSign);

    /**
     * Invoked when {@link #loadSigns(Object)} is called.<br>
     * The returned Set of DeadmanSigns of type S will be added to this handler's datastore.
     * @param signObject - The sign Object of type T that the loaded signs should represent
     * @return the loaded DeadmanSigns of type S that represent signObject
     */
    protected abstract Set<S> load(T signObject);


    /**
     * Called when a player creates a Sign block where the first line text is equal to
     * one of the sign tags provided in the constructor of the handler.<br>
     * Use {@link #setCreated(DeadmanSign) event.setCreated(createdDeadmanSign);} a DeadmanSign of type S was successfully created.<br>
     * Use {@link #setCanceled(boolean) event.setCanceled(true);} if a DeadmanSign could not be created.
     * @author Jon
     */
    public class SignCreateEvent implements Cancellable {

        private final SignChangeEvent original;
        private final Sign sign;
        private final WorldCoord signCoord;

        private S created;

        private SignCreateEvent(SignChangeEvent original, Sign sign) {
            this.original = original;
            this.sign = sign;
            signCoord = WorldCoord.at(sign.getBlock());
        }

        /**
         * Store the created DeadmanSign in the handler's DeadmanSign datastore and update the sign block accordingly.<br>
         * If this is not set, the original SignChangeEvent will be canceled and the sign block will be naturally broken.
         * @param created - The newly created DeadmanSign of type S as a result of this event
         * @throws IllegalArgumentException if created is null or if the location of the
         * created DeadmanSign is not the same as the sign location in this event.
         */
        public void setCreated(S created) throws IllegalArgumentException {
            if (created == null) {
                throw new IllegalArgumentException("created cannot be null");
            }
            if (!created.getCoord().equals(signCoord)) {
                throw new IllegalArgumentException("The created DeadmanSign must be in the same location as the sign in the event");
            }

            add(created);
            this.created = created;
        }

        /**
         * @param cancel - If <code>true</code>, the original SignChangeEvent will be canceled and the placed Sign block will be
         * naturally broken. If <code>false</code>, the event will be executed normally.
         */
        @Override
        public void setCancelled(boolean cancel) {
            original.setCancelled(cancel);
        }

        @Override
        public boolean isCancelled() {
            return original.isCancelled();
        }

        public Player getPlayer() {
            return original.getPlayer();
        }

        public Sign getSign() {
            return sign;
        }

        public WorldCoord getSignCoord() {
            return signCoord;
        }

        public String[] getLines() {
            return original.getLines();
        }

        public String getLine(int index) throws IndexOutOfBoundsException {
            return original.getLine(index);
        }

    }


    class DeadmanSignListener implements Listener {

        private final DeadmanSignHandler<T, S> handler = DeadmanSignHandler.this;

        @EventHandler(ignoreCancelled = true)
        private void onSignCreate(SignChangeEvent event) {
            Sign sign = DeadmanUtils.getSignState(event.getBlock());
            if (sign != null && signTag.equals(event.getLine(0))) {
                SignCreateEvent createEvent = new SignCreateEvent(event, sign);
                handler.onSignCreate(createEvent);

                if (event.isCancelled()) {
                    // Since the event was canceled, ensure that no sign was stored
                    handler.remove(createEvent.signCoord);
                    event.getBlock().breakNaturally();
                } else if (createEvent.created != null) {
                    String[] lines = createEvent.created.getLines();
                    for (int i = 0; i < lines.length && i < 4; i++) {
                        event.setLine(i, lines[i]);
                    }
                }
            }
        }

        @EventHandler(ignoreCancelled = true)
        private void onSignBreak(BlockBreakEvent event) {
            WorldCoord coord = WorldCoord.at(event.getBlock());
            S deadmanSign = remove(coord);
            if (deadmanSign != null) {
                handler.onSignBreak(event, deadmanSign);

                if (event.isCancelled()) {
                    add(deadmanSign);
                }
            }
        }

        @EventHandler(ignoreCancelled = true)
        private void onSignClick(PlayerInteractEvent event) {
            if (DeadmanUtils.getSignState(event.getClickedBlock()) != null) {
                S deadmanSign = deadmanSigns.get(WorldCoord.at(event.getClickedBlock()));
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

                    handler.onSignClick(event, deadmanSign);
                }
            }
        }
    }

}
