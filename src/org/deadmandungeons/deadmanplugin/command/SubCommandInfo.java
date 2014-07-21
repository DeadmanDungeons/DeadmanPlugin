package org.deadmandungeons.deadmanplugin.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is to be used to define a sub-command for a {@link Command}.
 * A sub-command is just a variation in the provided arguments such as:
 * <ul>
 * <li>/&lt;prefix&gt; &lt;command&gt; &lt;sub-command1-arg1&gt; &lt;sub-command1-arg2&gt;</li>
 * <li>/&lt;prefix&gt; &lt;command&gt; &lt;sub-command2-arg1&gt;</li>
 * @author Jon
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = { ElementType.TYPE })
public @interface SubCommandInfo {
	
	/**
	 * The arguments for each sub-command, and how that command should be executed, and validated.
	 * If this is empty or not given, The subCommand will match when no arguments are given.
	 */
	public ArgumentInfo[] arguments() default {};
	
	/**
	 * The description of this sub-command
	 */
	public String description();
	
	/**
	 * An array of permission nodes to check before executing the command.
	 * Only one permission node is needed to execute.
	 * Further permission checks may be made in the command execute method. <br />
	 * default = an empty String array
	 */
	public String[] permissions() default {};
	
	/**
	 * A flag stating whether or not the SubCommand can only be executed in-game.
	 * This flag has a lower priority than the {@link CommandInfo#inGameOnly()} flag,
	 * meaning if inGameOnly is true at CommandInfo scope, this flag will not be evaluated
	 * if the command was not executed by a player.
	 */
	public boolean inGameOnly() default false;
	
}
