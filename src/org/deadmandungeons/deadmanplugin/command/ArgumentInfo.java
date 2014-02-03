package org.deadmandungeons.deadmanplugin.command;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ArgumentInfo {
	
	/**
	 * The name of the argument in format: 
	 * <ul><li>'&lt;name&gt;' if it is a required variable</li>
	 * <li>'[name]' if it is an optional variable</li>
	 * <li>'name' if it is a required non-vriable</li></ul>
	 * Used to display the commands usage
	 */
	public String argName();
	
	/**
	 * The class of the argument. Used for argument conversion, and command validation
	 */
	public Class<?> argType();
	
}
