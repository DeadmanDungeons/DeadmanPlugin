package org.deadmandungeons.deadmanplugin;

/**
 * This enum contains the keys for key/value pairs used to store data for {@link ConfigObject}
 * @author Jon
 */
public enum Keys {
	
	WORLD("World:"),
	BLOCKID("ID:"),
	BLOCKDATA("Data:"),
	XCOORD("X:"),
	YCOORD("Y:"),
	ZCOORD("Z:"),
	YAW("Yaw:"),
	PITCH("Pitch:"),
	OFFSETX("OffX:"),
	OFFSETY("OffY:"),
	OFFSETZ("OffZ:"),
	WIDTH("W:"),
	HEIGHT("H:"),
	LENGTH("L:"),
	AMOUNT("Amt:"),
	DURATION("Dur:"),
	EXPIRATION("Exp:"),
	PASSED("Psd:"),
	ENDS("Ends:"),
	PRICE("Price:"),
	TEXT("Text:"),
	COLOR("Color:");
	
	private String value;
	
	Keys(String value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		return value;
	}
	
}
