package com.deadmandungeons.deadmanplugin;

import com.deadmandungeons.deadmanplugin.filedata.DataEntry;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.UUID;


/**
 * A simple class to identify a player by their {@link UUID} and username so that a
 * plugin can easily rely on the UUID for unique identification, and the username for display.
 * @author Jon
 */
public class PlayerId {

    private final UUID uuid;
    private String username;

    /**
     * @param player - The {@link OfflinePlayer} to create a PlayerId from
     * @throws IllegalArgumentException if player is null
     */
    public PlayerId(OfflinePlayer player) throws IllegalArgumentException {
        Validate.notNull(player, "player cannot be null");
        this.uuid = player.getUniqueId();
        this.username = player.getName();
        Validate.notNull(username, "player name cannot be null");
    }

    /**
     * @param uuid - The UUID of the player
     * @param username - The players username
     * @throws IllegalArgumentException if uuid or username are null
     */
    public PlayerId(UUID uuid, String username) throws IllegalArgumentException {
        Validate.notNull(uuid, "uuid cannot be null");
        Validate.notNull(username, "username cannot be null");
        this.uuid = uuid;
        this.username = username;
    }

    /**
     * @return the UUID of the Player
     */
    public UUID getId() {
        return uuid;
    }

    /**
     * This will check if {@link #getOfflinePlayer()}.{@link OfflinePlayer#getName() getName()} is different from
     * the currently defined username in this PlayerId and update it accordingly.
     * @return the most recent username for the player identified by this PlayerId
     */
    public String getUsername() {
        String lastUsername = getOfflinePlayer().getName();
        if (lastUsername != null && !username.equals(lastUsername)) {
            username = lastUsername;
        }
        return username;
    }

    /**
     * This is synonymous to calling {@link Bukkit#getOfflinePlayer(UUID)}
     * @return an OfflinePlayer instance for the player identified by the UUID in this PlayerId
     */
    public OfflinePlayer getOfflinePlayer() {
        return Bukkit.getOfflinePlayer(uuid);
    }

    /**
     * @param player - the player to check identity equality
     * @return true if the given player is not null and its uuid equals the uuid of this PlayerId
     */
    public boolean equalsPlayer(OfflinePlayer player) {
        return player != null && equals(player.getUniqueId());
    }

    /**
     * @param uuid - the uuid of the Player to check identity equality
     * @return true if the given uuid is not null and it equals the uuid of this PlayerId
     */
    public boolean equalsId(UUID uuid) {
        return this.uuid.equals(uuid);
    }

    /**
     * @param username - the username of the Player to check identity equality
     * @return true if the given username equals the most recent username of the player identified by this PlayerId ignoring case
     */
    public boolean equalsUsername(String username) {
        return getUsername().equalsIgnoreCase(username);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof PlayerId)) {
            return false;
        }
        PlayerId other = (PlayerId) obj;
        return uuid.equals(other.uuid);
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    /**
     * @return the DataEntry string value of this PlayerId as would be returned by {@link DataEntry#toString()} with
     * the {@link DataEntry.DataKey#UUID_KEY} and {@link DataEntry.DataKey#NAME_KEY} keys
     */
    @Override
    public String toString() {
        return DataEntry.formatPlayerId(this);
    }

}
