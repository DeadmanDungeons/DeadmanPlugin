package org.deadmandungeons.deadmanplugin;

import org.apache.commons.lang.Validate;

/**
 * This simple class is useful as a return type for methods that may return null,
 * but but want to provide a reason for the result being null. The {@link #Result(Object)} constructor
 * should be used when the result was successful to provide the resulting object.
 * The {@link #Result(String)} constructor should be used when the result was not successful for the given reason.
 * If the resulting object is of type String, use the {@link #Result(Object, String)} constructor to avoid ambiguity.
 * @param <T> - The type of the result object
 * @author Jon
 */
public class Result<T> {
	
	private final T result;
	private final String errorMessage;
	
	/**
	 * This constructor is only provided as a means to get around the ambiguity when the generic type is String.
	 * If the generic type is not String, the {@link #Result(Object)} and {@link #Result(String)} constructors should be used instead.
	 * @param result - The resulting Object of type T
	 * @param errorMessage - The error message explaining the reason why the result failed
	 * @throws IllegalArgumentException if result and errorMessageis is either both null or both not-null
	 */
	public Result(T result, String errorMessage) throws IllegalArgumentException {
		if ((result != null && errorMessage != null) || (result == null && errorMessage == null)) {
			throw new IllegalArgumentException("either result or errorMessage must be null and not both");
		}
		this.result = result;
		this.errorMessage = errorMessage;
	}
	
	/**
	 * errorMessage will be set to null indicating a successful result.
	 * @param result - The resulting Object of type T
	 * @throws IllegalArgumentException if result is null
	 */
	public Result(T result) throws IllegalArgumentException {
		Validate.notNull(result, "result cannot be null");
		this.result = result;
		this.errorMessage = null;
	}
	
	/**
	 * result will be set to null indicating a failed result.
	 * @param errorMessage - The error message explaining the reason why the result failed
	 * @throws IllegalArgumentException if errorMessage is null
	 */
	public Result(String errorMessage) throws IllegalArgumentException {
		Validate.notNull(errorMessage, "errorMessage cannot be null");
		this.errorMessage = errorMessage;
		this.result = null;
	}
	
	/**
	 * @return the successful result object or null if the result was not successful
	 */
	public T getResult() {
		return result;
	}
	
	/**
	 * @return the error message explaining the reason why the result failed, or null if the result was successful
	 */
	public String getErrorMessage() {
		return errorMessage;
	}
	
	/**
	 * @return true if the result failed (or if errorMessage is not null)
	 */
	public boolean isError() {
		return errorMessage != null;
	}
	
}
