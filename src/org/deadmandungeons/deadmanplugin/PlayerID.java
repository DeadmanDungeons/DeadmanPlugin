package org.deadmandungeons.deadmanplugin;

import java.util.UUID;

import org.apache.commons.lang.builder.HashCodeBuilder;
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
	
	public PlayerID(UUID uuid, String username) {
		this.uuid = uuid;
		this.username = username;
		asString = DataEntry.builder().withPlayerID(this).build().toString();
	}
	
	public PlayerID(OfflinePlayer player) {
		this.uuid = player.getUniqueId();
		this.username = player.getName();
		asString = DataEntry.builder().withPlayerID(this).build().toString();
	}
	
	public UUID getUUID() {
		return uuid;
	}
	
	public String getUsername() {
		return username;
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
