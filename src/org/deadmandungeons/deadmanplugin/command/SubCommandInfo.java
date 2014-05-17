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
	 * Further permission checks may be made in the command execute method. <br />
	 * default = an empty String array
	 */
	public String[] permissions() default {};
	
}
