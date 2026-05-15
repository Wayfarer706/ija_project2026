/**
 * Project: Advance Wars Clone
 * Authors: Mariia Zhdaniuk
 * Description: Implements the 'Result Pattern' for error handling. 
 * Allows the validation and dispatch systems to gracefully return failure reasons 
 * to the UI without resorting to throwing expensive Java Exceptions for standard gameplay rules.
 */
package xyuguyn00.common;

public class Result {
    private final boolean success;
    
    // Contains the specific error string (e.g., "Not enough funds") if the action failed
    private final String message;

    private Result(boolean success, String message)
    {
        this.success = success;
        this.message = message;
    }

    // Static Factory Method for successful executions
    public static Result success() {
        return new Result(true, null);
    }

    // Static Factory Method for illegal or failed executions
    public static Result failure(String errorMessage) {
        return new Result(false, errorMessage);
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean isFailure() {
        return !success;
    }

    public String getMessage() {
        return message;
    }
}