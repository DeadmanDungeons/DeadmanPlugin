package org.deadmandungeons.deadmanplugin.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A {@link Command} class must define this annotation to specify exactly how the Command should be executed.
 * This annotation will then be used to build a valid {@link Arguments} object for that Command to guarantee that
 * the provided arguments match the argument types that are specified by {@link #subCommands()}
 * @author Jon
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = { ElementType.TYPE })
public @interface CommandInfo {
	
	/**
	 * The actual name of the command.
	 */
	public String name();
	
	/**
	 * A regex pattern that allows minor oddities and alternatives to the command name.
	 */
	public String pattern();
	
	/**
	 * A description of what the command does.
	 */
	public String description() default "";
	
	/**
	 * An array of permission nodes to check before executing this command and any of its sub commands.
	 * Only one permission node is needed to execute.
	 * Further permission checks may be made for the executed subCommand and/or the commands execute method.<br />
	 * default = an empty String array
	 */
	public String[] permissions() default {};
	
	/**
	 * A flag stating whether or not the command can only be executed in-game.
	 */
	public boolean inGameOnly();
	
	/**
	 * All of the possible inner sub-commands that can be executed.<br />
	 * default = an empty SubCommandInfo array
	 */
	public SubCommandInfo[] subCommands() default {};
	
}
