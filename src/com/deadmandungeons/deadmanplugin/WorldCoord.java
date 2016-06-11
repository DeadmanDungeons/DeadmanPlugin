package com.deadmandungeons.deadmanplugin;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

/**
 * A simple immutable class that represents a block coordinate in a world.<br>
 * This can be used as an immutable alternative to {@link Location} if the int block coordinate is all that is needed.
 * Additionally, a WorldCoord can easily be created from a Location using {@link #WorldCoord(Location)}, and converted
 * to a Location using {@link #toLocation()}.
 * @author Jon
 */
public class WorldCoord {
	
	// Used for parsing the result of toString()
	private static final Pattern VERBOSE_PATTERN = Pattern.compile("X\\[(-?\\d+)\\],Y\\[(-?\\d+)\\],Z\\[(-?\\d+)\\],W\\[([^\\]]+)\\]");
	// Used for parsing the result of toCompactString()
	private static final Pattern COMPACT_PATTERN = Pattern.compile("^X(-?\\d+)Y(-?\\d+)Z(-?\\d+)W(.+)");
	
	private final World world;
	private final int x, y, z;
	
	/**
	 * Synonymous to {@link #WorldCoord(Location) new WorldCoord(block.getLocation())}
	 * @param block - The Block to use for the constructed WorldCoord
	 */
	public WorldCoord(Block block) {
		this(block.getLocation());
	}
	
	/**
	 * Constructs a new WorldCoord for the world and block corrdinates defined by the given Location.
	 * @param loc - The Location to use for the constructed WorldCoord
	 * @throws IllegalArgumentException if loc.getBlockY() is less than 0 or greater than the world maxHeight
	 */
	public WorldCoord(Location loc) throws IllegalArgumentException {
		this(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
	}
	
	/**
	 * Constructs a new WorldCoord with the given World and block coordinates
	 * @param world - The world of this new WorldCoord
	 * @param x - The x-coordinate of this new WorldCoord
	 * @param y - The y-coordinate of this new WorldCoord
	 * @param z - The z-coordinate of this new WorldCoord
	 * @throws IllegalArgumentException if world is null or if y is less than 0 or greater than the world maxHeight
	 */
	public WorldCoord(World world, int x, int y, int z) throws IllegalArgumentException {
		if (world == null) {
			throw new IllegalArgumentException("world cannot be null");
		}
		if (y < 0 || y > world.getMaxHeight()) {
			throw new IllegalArgumentException("y cannot be less than 0 or greater than the world maxHeight");
		}
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	
	/**
	 * This simply returns {@link #WorldCoord(Block)}.<br>
	 * In some circumstances, the english-like nature of the code to invoke this method may
	 * be more understandable and readable than using the constructor.
	 * @param block
	 * @return
	 */
	public static WorldCoord at(Block block) {
		return new WorldCoord(block);
	}
	
	/**
	 * This simply returns {@link #WorldCoord(Location)}.<br>
	 * In some circumstances, the english-like nature of the code to invoke this method may
	 * be more understandable and readable than using the constructor.
	 * @param block
	 * @return
	 */
	public static WorldCoord at(Location loc) {
		return new WorldCoord(loc);
	}
	
	
	/**
	 * @return the World for this WorldCoord
	 */
	public World getWorld() {
		return world;
	}
	
	/**
	 * @return the x-coordinate for the block at this WorldCoord
	 */
	public int getX() {
		return x;
	}
	
	/**
	 * @return the y-coordinate for the block at this WorldCoord. This will not be less than 0 or greater than the world maxHeight.
	 */
	public int getY() {
		return y;
	}
	
	/**
	 * @return the z-coordinate for the block at this WorldCoord
	 */
	public int getZ() {
		return z;
	}
	
	/**
	 * @return the Chunk that contains this WorldCoord
	 */
	public Chunk getChunk() {
		return world.getChunkAt(x >> 4, z >> 4);
	}
	
	/**
	 * @return the Block at this WorldCoord
	 */
	public Block getBlock() {
		return world.getBlockAt(x, y, z);
	}
	
	/**
	 * @return a new Location with this WorldCoord world and block coordinates
	 */
	public Location toLocation() {
		return new Location(world, x, y, z);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof WorldCoord)) {
			return false;
		}
		WorldCoord other = (WorldCoord) obj;
		return other.world.equals(world) && other.x == x && other.y == y && other.z == z;
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(world).append(x).append(y).append(z).toHashCode();
	}
	
	/**
	 * Format: <code>WorldCoord(X[&lt;x&gt;],Y[&lt;y&gt;],Z[&lt;z&gt;],W[&lt;world&gt;])</code><br>
	 * The returned String can be parsed back into a WorldCoord using {@link #parse(String)}
	 * @return a readable String representation of this WorldCoord
	 */
	@Override
	public String toString() {
		return "WorldCoord(X[" + x + "],Y[" + y + "],Z[" + z + "],W[" + world.getName() + "])";
	}
	
	/**
	 * Format: <code>X&lt;x&gt;Y&lt;y&gt;Z&lt;z&gt;W&lt;world&gt;</code><br>
	 * The returned String can be parsed back into a WorldCoord using {@link #parse(String)}
	 * @return a compact String representation of this WorldCoord
	 */
	public String toCompactString() {
		return "X" + x + "Y" + y + "Z" + z + "W" + world.getName();
	}
	
	/**
	 * This can parse a String representation of a WorldCoord as formatted by
	 * either the {@link #toString()} or {@link #toCompactString()} methods.
	 * @param coordStr - The String represenation of a WorldCoord
	 * @return the parsed WorldCoord or null if the given coordStr was not valid
	 */
	public static WorldCoord parse(String coordStr) {
		if (coordStr != null && !coordStr.isEmpty()) {
			Matcher matcher = COMPACT_PATTERN.matcher(coordStr);
			if (matcher.find()) {
				return createFromMatcher(matcher);
			}
			matcher = VERBOSE_PATTERN.matcher(coordStr);
			if (matcher.find()) {
				return createFromMatcher(matcher);
			}
		}
		return null;
	}
	
	private static WorldCoord createFromMatcher(Matcher matcher) {
		World world = Bukkit.getWorld(matcher.group(4));
		int y = Integer.parseInt(matcher.group(2));
		if (world != null && y >= 0 && y <= world.getMaxHeight()) {
			int x = Integer.parseInt(matcher.group(1));
			int z = Integer.parseInt(matcher.group(3));
			
			return new WorldCoord(world, x, y, z);
		}
		return null;
	}
	
}
