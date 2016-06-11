package com.deadmandungeons.deadmanplugin.command;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.commons.lang.Validate;

/**
 * This annotation is used to define the properties of an argument for a {@link SubCommandInfo} such as
 * the arguments name, {@link ArgType} (for argument matching), and varType class (for argument conversion and validation)
 * @author Jon
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = { ElementType.TYPE })
public @interface ArgumentInfo {
	
	/**
	 * The name of the argument. Used to display the commands usage
	 */
	public String argName();
	
	/**
	 * The type of argument of the argument. Used for argument conversion, and command validation
	 */
	public ArgType argType();
	
	/**
	 * The class of the argument. Used for argument conversion, and command validation. <br>
	 * default = String.class
	 */
	public Class<?> varType() default String.class;
	
	
	/**
	 * @author Jon
	 */
	public static class ArgumentInfoImpl implements Annotation, ArgumentInfo {
		
		private final String argName;
		private final ArgType argType;
		private final Class<?> varType;
		
		public ArgumentInfoImpl(String argName, ArgType argType, Class<?> varType) {
			Validate.notNull(argName);
			Validate.notNull(argType);
			this.argName = argName;
			this.argType = argType;
			this.varType = (varType != null ? varType : String.class);
		}
		
		@Override
		public String argName() {
			return argName;
		}
		
		@Override
		public ArgType argType() {
			return argType;
		}
		
		@Override
		public Class<?> varType() {
			return varType;
		}
		
		@Override
		public Class<? extends Annotation> annotationType() {
			return ArgumentInfo.class;
		}
		
	}
	
	public static enum ArgType {
		VARIABLE("<%s>"),
		OPT_VARIABLE("[%s]"),
		NON_VARIABLE("%s");
		
		private String wrap;
		
		private ArgType(String wrap) {
			this.wrap = wrap;
		}
		
		public String getWrap() {
			return wrap;
		}
	}
	
}
