package xyuguyn00.common;

public class Result {
    private final boolean success;
    private final String message;

    private Result(boolean success, String message)
    {
        this.success = success;
        this.message = message;
    }

    public static Result success() {
        return new Result(true, null);
    }

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