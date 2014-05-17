package org.deadmandungeons.deadmanplugin;

public class Result<T> {
	
	private final T result;
	private final String errorMessge;
	
	public Result(T result, String errorMessge) {
		this.result = result;
		this.errorMessge = errorMessge;
	}
	
	public T getResult() {
		return result;
	}
	
	public String getErrorMessge() {
		return errorMessge;
	}
	
}
