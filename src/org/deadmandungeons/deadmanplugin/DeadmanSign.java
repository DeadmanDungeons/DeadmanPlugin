package org.deadmandungeons.deadmanplugin;

import org.bukkit.block.Sign;
import org.bukkit.event.block.SignChangeEvent;
import org.deadmandungeons.deadmanplugin.filedata.DataEntry;

/**
 * This abstract class is to be extended by an object that represents a certain Sign type.
 * The Sign being represented should also represent a certain {@link SignObject} which is of type T.
 * @author Jon
 */
public abstract class DeadmanSign<T extends SignObject> {
	
	private Sign sign;
	private DataEntry dataEntry;
	private T signObject;
	
	public DeadmanSign(Sign sign, DataEntry dataEntry, T signObject) {
		this.sign = sign;
		this.dataEntry = dataEntry;
		this.signObject = signObject;
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
	public DataEntry getDataEntry() {
		return dataEntry;
	}
	
	/**
	 * @param locData - The {@link org.deadmandungeons.deadmanplugin.LocationMetadata LocationMetadata} object to be set
	 */
	public void setDataEntry(DataEntry dataEntry) {
		this.dataEntry = dataEntry;
	}
	
	/**
	 * This method should be used to update the lines on the bukkit {@link org.bukkit.block.Sign Sign}
	 */
	public void update() {
		String[] lines = getLines();
		for (int i = 0; i < lines.length && i < 4; i++) {
			sign.setLine(i, lines[i]);
		}
		sign.update(true);
	}
	
	/**
	 * This method should be used to update the lines on the bukkit {@link org.bukkit.block.Sign Sign} during a
	 * {@link org.bukkit.event.block.SignChangeEvent SignChangeEvent}
	 */
	public void update(SignChangeEvent event) {
		String[] lines = getLines();
		for (int i = 0; i < lines.length && i < 4; i++) {
			event.setLine(i, lines[i]);
		}
	}
	
	/**
	 * @return The {@link SignObject} of type T that this sign represents
	 */
	public T getSignObject() {
		return signObject;
	}
	
	/**
	 * @param signObject - A {@link SignObject} of type T that this sign represents
	 */
	public void setSignObject(T signObject) {
		this.signObject = signObject;
	}
	
	/**
	 * This method should be called to set the lines for this DeadmanSign object
	 */
	public abstract String[] getLines();
	
}
