package org.deadmandungeons.deadmanplugin;

import java.util.UUID;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.deadmandungeons.deadmanplugin.filedata.DataEntry;
import org.deadmandungeons.deadmanplugin.filedata.DataEntry.Key;


/**
 * A simple immutable class to identify a player by their {@link UUID} and username so that a
 * plugin can easily rely on the UUID for unique identification, and the username for display.
 * @author Jon
 */
public class PlayerID {
	
	private final UUID uuid;
	private final String username;
	
	private final String asString;
	
	
	/**
	 * @param player - The {@link OfflinePlayer} to create a PlayerID from
	 * @throws IllegalArgumentException if player is null
	 */
	public PlayerID(OfflinePlayer player) throws IllegalArgumentException {
		Validate.notNull(player, "player cannot be null");
		this.uuid = player.getUniqueId();
		this.username = player.getName();
		asString = DataEntry.builder().playerID(this).build().toString();
	}
	
	/**
	 * @param uuid - The UUID of the player
	 * @param username - The players username
	 * @throws IllegalArgumentException if uuid or username are null
	 */
	public PlayerID(UUID uuid, String username) throws IllegalArgumentException {
		Validate.notNull(uuid, "uuid cannot be null");
		Validate.notNull(username, "username cannot be null");
		this.uuid = uuid;
		this.username = username;
		asString = DataEntry.builder().playerID(this).build().toString();
	}
	
	
	public UUID getUUID() {
		return uuid;
	}
	
	public String getUsername() {
		return username;
	}
	
	public OfflinePlayer getOfflinePlayer() {
		return Bukkit.getOfflinePlayer(uuid);
	}
	
	/**
	 * @param player - the player to check identity equality
	 * @return true if the given player is not null and its uuid equals the uuid of this PlayerID
	 */
	public boolean equalsPlayer(OfflinePlayer player) {
		return player != null && equals(player.getUniqueId());
	}
	
	/**
	 * @param uuid - the uuid of the Player to check identity equality
	 * @return true if the given uuid is not null and it equals the uuid of this PlayerID
	 */
	public boolean equalsUUID(UUID uuid) {
		return uuid != null && this.uuid.equals(uuid);
	}
	
	/**
	 * @param username - the username of the Player to check identity equality
	 * @return true if the given username is not null and it equals the username of this PlayerID ignoring case
	 */
	public boolean equalsUsername(String username) {
		return this.username.equalsIgnoreCase(username);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof PlayerID)) {
			return false;
		}
		PlayerID other = (PlayerID) obj;
		return uuid.equals(other.uuid) && username.equalsIgnoreCase(other.username);
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(uuid).append(username).toHashCode();
	}
	
	/**
	 * @return the DataEntry string value of this PlayerID as would be returned by {@link DataEntry#toString()} with
	 * the {@link Key#UUID} and {@link Key#USERNAME} keys
	 */
	@Override
	public String toString() {
		return asString;
	}
	
}
