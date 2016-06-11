package com.deadmandungeons.deadmanplugin.command;

import com.deadmandungeons.deadmanplugin.Result;

/**
 * This interface is used to convert a String command argument to its appropriate type,
 * which will then be used in the {@link Arguments} object for the appropriate {@link Command}.
 * @param <T> - The type to be converted to
 * @author Jon
 */
public interface ArgumentConverter<T> {
	
	/**
	 * @param argName - The name of the argument to convert
	 * @param arg - The String value of the argument
	 * @return the argument represented as type T
	 */
	public Result<T> convertCommandArg(String argName, String arg);
	
}
