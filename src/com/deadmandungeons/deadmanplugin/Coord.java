package com.deadmandungeons.deadmanplugin;

import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A simple immutable class that represents coordinate in 3D space.<br>
 * This can be used as an immutable alternative to {@link org.bukkit.util.Vector} if the integer coordinates is all that is needed.
 * The {@link Coord#parse(String)} static method can parse the results of {@link #toString()} and {@link #toCompactString()}
 * @author Jon
 */
public class Coord {

    // Used for parsing the result of toString()
    private static final Pattern VERBOSE_PATTERN = Pattern.compile("X\\[(-?\\d+)\\],Y\\[(-?\\d+)\\],Z\\[(-?\\d+)\\]");
    // Used for parsing the result of toCompactString()
    private static final Pattern COMPACT_PATTERN = Pattern.compile("^X(-?\\d+)Y(-?\\d+)Z(-?\\d+)");

    protected final int x, y, z;

    public Coord(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * @return the x-coordinate
     */
    public int getX() {
        return x;
    }

    /**
     * @return the y-coordinate
     */
    public int getY() {
        return y;
    }

    /**
     * @return the z-coordinate
     */
    public int getZ() {
        return z;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Coord)) {
            return false;
        }
        Coord other = (Coord) obj;
        return other.x == x && other.y == y && other.z == z;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(x).append(y).append(z).toHashCode();
    }

    /**
     * Format: <code>Coord(X[&lt;x&gt;],Y[&lt;y&gt;],Z[&lt;z&gt;])</code><br>
     * The returned String can be parsed back into a Coord using {@link #parse(String)}
     * @return a readable String representation of this Coord
     */
    @Override
    public String toString() {
        return "Coord(X[" + x + "],Y[" + y + "],Z[" + z + "])";
    }

    /**
     * Format: <code>X&lt;x&gt;Y&lt;y&gt;Z&lt;z&gt;W&lt;world&gt;</code><br>
     * The returned String can be parsed back into a Coord using {@link #parse(String)}
     * @return a compact String representation of this Coord
     */
    public String toCompactString() {
        return "X" + x + "Y" + y + "Z" + z;
    }

    /**
     * This can parse a String representation of a Coord as formatted by
     * either the {@link #toString()} or {@link #toCompactString()} methods.
     * @param coordStr - The String representation of a Coord
     * @return the parsed WorldCoord or null if the given coordStr was not valid
     */
    public static Coord parse(String coordStr) {
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

    private static Coord createFromMatcher(Matcher matcher) {
        int y = Integer.parseInt(matcher.group(2));
        int x = Integer.parseInt(matcher.group(1));
        int z = Integer.parseInt(matcher.group(3));
        return new Coord(x, y, z);
    }

}
