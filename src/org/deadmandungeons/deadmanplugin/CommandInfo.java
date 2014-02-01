package org.deadmandungeons.deadmanplugin;

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
     * The arguments for the command, i.e. how the command should be used.
     */
    public String arguments();
    
    /**
     * A description of what the command does.
     */
    public String description();
    
    /**
     * The permission required to execute this command.
     */
    public String permission();
    
    /**
     * The whether or not the command can only be executed in-game.
     */
    public boolean inGameOnly();
    
}
