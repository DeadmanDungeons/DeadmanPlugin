package org.deadmandungeons.deadmanplugin.command;

import org.deadmandungeons.deadmanplugin.Result;

public interface ArgumentConverter<T> {
	
	public Result<T> convertCommandArg(String argName, String arg);
	
}
