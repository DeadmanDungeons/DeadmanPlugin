package org.deadmandungeons.deadmanplugin;

public enum Keys {
	
	WORLD("World: "),
	BLOCKID("ID: "),
	BLOCKDATA("Data: "),
	XCOORD("X: "),
	YCOORD("Y: "),
	ZCOORD("Z: "),
	YAW("Yaw: "),
	PITCH("Pitch: "),
	OFFSETX("OffX: "),
	OFFSETY("OffY: "),
	OFFSETZ("OffZ: "),
	WIDTH("W: "),
	HEIGHT("H: "),
	LENGTH("L: "),
	AMOUNT("Amt: "),
	DURATION("Dur: "),
	EXPIRATION("Exp: "),
	PASSED("Psd: "),
	ENDS("Ends: "),
	PRICE("Price: ");
	
	private String value;
	
	Keys(String value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
        return value;
    }
		
}
