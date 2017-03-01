package com.deadmandungeons.deadmanplugin.command;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.commons.lang.Validate;

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
	 * The actual name of the command, and the String to be used to match against ignoring case.
	 */
	public String name();
	
	/**
	 * A description of what the command does.
	 */
	public String description() default "";
	
	/**
	 * An array of aliases of alternatives to the command name.<br>
	 * default = an empty String array
	 */
	public String[] aliases() default {};
	
	/**
	 * An array of permission nodes to check before executing this command and any of its sub commands.
	 * Only one permission node is needed to execute.
	 * Further permission checks may be made for the executed subCommand and/or the commands execute method.<br>
	 * default = an empty String array
	 */
	public String[] permissions() default {};
	
	/**
	 * A flag stating whether or not the command can only be executed in-game.<br>
	 * default = false
	 */
	public boolean inGameOnly() default false;
	
	/**
	 * All of the possible inner sub-commands that can be executed.<br>
	 * default = an empty SubCommandInfo array
	 */
	public SubCommandInfo[] subCommands() default {};
	
	
	/**
	 * @author Jon
	 */
	public static class CommandInfoImpl implements Annotation, CommandInfo {
		
		private final String name;
		private final String description;
		private final String[] aliases;
		private final String[] permissions;
		private final boolean inGameOnly;
		private final SubCommandInfo[] subCommands;
		
		public CommandInfoImpl(String name, String description, String[] aliases, String[] permissions, Boolean inGameOnly,
				SubCommandInfo[] subCommands) {
			Validate.notNull(name);
			this.name = name;
			this.description = (description != null ? description : "");
			this.aliases = (aliases != null ? aliases : new String[0]);
			this.permissions = (permissions != null ? permissions : new String[0]);
			this.inGameOnly = (inGameOnly != null ? inGameOnly : false);
			this.subCommands = (subCommands != null ? subCommands : new SubCommandInfo[0]);
		}
		
		@Override
		public String name() {
			return name;
		}
		
		@Override
		public String description() {
			return description;
		}
		
		@Override
		public String[] aliases() {
			return aliases;
		}
		
		@Override
		public String[] permissions() {
			return permissions;
		}
		
		@Override
		public boolean inGameOnly() {
			return inGameOnly;
		}
		
		@Override
		public SubCommandInfo[] subCommands() {
			return subCommands;
		}
		
		@Override
		public Class<? extends Annotation> annotationType() {
			return CommandInfo.class;
		}
		
	}
}
