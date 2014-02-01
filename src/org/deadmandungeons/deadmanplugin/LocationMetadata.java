package org.deadmandungeons.deadmanplugin;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.metadata.MetadataValue;

public class LocationMetadata extends Location {
	
	private Map<Keys, MetadataValue> metaData;

	public LocationMetadata(World world, double x, double y, double z) { 
		super(world, x, y, z);
		this.metaData = new HashMap<Keys, MetadataValue>();
	}
	
	public LocationMetadata(World world, double x, double y, double z, float yaw, float pitch) {
		super(world, x, y, z, yaw, pitch);
		this.metaData = new HashMap<Keys, MetadataValue>();
	}

	public LocationMetadata(Location loc) {
		super(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
		this.metaData = new HashMap<Keys, MetadataValue>();
	}
	
	public LocationMetadata(Location loc, HashMap<Keys, MetadataValue> metaData) {
		super(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
		this.metaData = metaData;
	}

	public Map<Keys, MetadataValue> getMetaData() {
		return metaData;
	}

}
