package org.deadmandungeons.deadmanplugin;

import org.bukkit.block.Sign;
import org.bukkit.event.block.SignChangeEvent;

/**
 * This interface should be implemented by any objects representing a certain Sign type, and that
 * reference a certain ConfigObject.
 * @author Jon
 */
public abstract class DeadmanSign {

	private Sign sign;
	private LocationMetadata locData;
	
	public DeadmanSign(Sign sign, LocationMetadata locData) {
		this.sign = sign;
		this.locData = locData;
	}
	
	/**
	 * @return The bukkit {@link org.bukkit.block.Sign Sign} object 
	 */
	public Sign getSign() {
		return sign;
	}

	/**
	 * @param sign - The bukkit {@link org.bukkit.block.Sign Sign} object to be set
	 */
	public void setSign(Sign sign) {
		this.sign = sign;
	}

	/**
	 * @return The {@link org.deadmandungeons.deadmanplugin.LocationMetadata LocationMetadata} object
	 * containing the metadata for this sign such as block ID and block data
	 */
	public LocationMetadata getLocData() {
		return locData;
	}

	/**
	 * @param locData - The {@link org.deadmandungeons.deadmanplugin.LocationMetadata LocationMetadata} object to be set
	 */
	public void setLocData(LocationMetadata locData) {
		this.locData = locData;
	}
	
	/**
	 * This method should be used to update the lines on the bukkit {@link org.bukkit.block.Sign Sign}
	 */
	public void update() {
		String[] lines = getLines();
		for (int i=0; i<lines.length && i < 4; i++) {
			sign.setLine(i, lines[i]);
		}
		sign.update(true);
	}
	
	/**
	 * This method should be used to update the lines on the bukkit {@link org.bukkit.block.Sign Sign} 
	 * during a {@link org.bukkit.event.block.SignChangeEvent SignChangeEvent}
	 */
	public void update(SignChangeEvent event) {
		String[] lines = getLines();
		for (int i=0; i<lines.length && i < 4; i++) {
			event.setLine(i, lines[i]);
		}
	}
	
	/**
	 * This method should be called to set the lines for this DeadmanSign object
	 */
	public abstract String[] getLines();

}
