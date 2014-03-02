package org.deadmandungeons.deadmanplugin;

import org.bukkit.block.Sign;

/**
 * This interface should be implemented by any objects representing a certain Sign type, and that
 * reference a certain ConfigObject.
 * @author Jon
 */
public interface DeadmanSign {
	
	public Sign getSign();

	public void setSign(Sign sign);
	
	public LocationMetadata getLocData();

	public void setLocData(LocationMetadata locData);

}
