package org.deadmandungeons.deadmanplugin.command;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface CommandInfo {
    /**
     * The actual name of the command. Not really used anywhere.
     */
    public String name();
    
    /**
     * A regex pattern that allows minor oddities and alternatives to the command name.
     */
    public String pattern();
    
    /**
     * A description of what the command does.
     */
    public String description();
    
    /**
	 * An array of permission nodes to check before executing this command and any of its sub commands. 
	 * Only one permission node is needed to execute.
	 * Further permission checks may be made for the executed subCommand and/or the commands execute method
	 */
	public String[] permissions() default {};
    
    /**
     * A flag stating whether or not the command can only be executed in-game.
     */
    public boolean inGameOnly();
    
    /**
     * All of the possible inner sub-commands that can be executed
     */
    public SubCommandInfo[] subCommands();
    
}
