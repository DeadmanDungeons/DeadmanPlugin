package org.deadmandungeons.deadmanplugin;

import org.bukkit.block.Sign;

/**
 * This abstract class should be extended by any objects representing a certain Sign type, and that
 * reference a certain ConfigObject.
 * @author Jon
 */
public abstract class DeadmanSign {
	
	private Sign sign;
	private LocationMetadata locData;
	private ConfigObject obj;
	
	public DeadmanSign(Sign sign, LocationMetadata locData, ConfigObject obj) {
		this.sign = sign;
		this.locData = locData;
		this.obj = obj;
	}
	
	public Sign getSign() {
		return sign;
	}

	public void setSign(Sign sign) {
		this.sign = sign;
	}
	
	public LocationMetadata getLocData() {
		return locData;
	}

	public void setLocData(LocationMetadata locData) {
		this.locData = locData;
	}

	public ConfigObject getObj() {
		return obj;
	}

}
