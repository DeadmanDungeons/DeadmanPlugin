package com.deadmandungeons.deadmanplugin;

/**
 * This simple class is useful as a return type for methods that may return null,
 * but but want to provide a reason for the result being null. The {@link #success(Object)}
 * static method should be used to wrap a resulting object of a successful operation.
 * The {@link #fail(String)} static method should be used to provide the reason why
 * an operation was unsuccessful.
 * @param <T> - The type of the result object
 * @author Jon
 */
public class Result<T> {

    private final T result;
    private final String failReason;


    /**
     * @param result - The object that was a result of a successful operation
     * @return a Resultsuccessful Result for the given object
     * @throws IllegalArgumentException if result is null
     */
    public static <T> Result<T> success(T result) throws IllegalArgumentException {
        if (result == null) {
            throw new IllegalArgumentException("result cannot be null");
        }
        return new Result<>(result, null);
    }

    /**
     * @param failReason - The message indicating the reason why an operation was not successful
     * @return a Result indicating a failed operation
     * @throws IllegalArgumentException if failReason is null
     */
    public static <T> Result<T> fail(String failReason) {
        if (failReason == null) {
            throw new IllegalArgumentException("failReason cannot be null");
        }
        return new Result<>(null, failReason);
    }

    protected Result(T result, String failReason) {
        this.result = result;
        this.failReason = failReason;
    }

    /**
     * @return the successful result object, or null if the result was not successful
     */
    public T getResult() {
        return result;
    }

    /**
     * @return the message explaining the reason why the result failed, or null if the result was successful
     */
    public String getFailReason() {
        return failReason;
    }

    /**
     * Equivalent to null-checking the result object.
     * @return true if the result was successful, and false otherwise
     */
    public boolean isSuccess() {
        return result != null;
    }

}
