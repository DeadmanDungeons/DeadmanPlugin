package org.deadmandungeons.deadmanplugin.command;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface SubCommandInfo {

	/**
     * The arguments for each sub-command, and how that command should be executed, and validated
     */
	public ArgumentInfo[] arguments();
	
	/**
	 * The description of this sub-command
	 */
	public String description();
	
	/**
	 * An array of permission nodes to check before executing the command. 
	 * Only one permission node is needed to execute.
	 * Further permission checks may be made in the command execute method
	 */
	public String[] permissions() default {};
	
}
