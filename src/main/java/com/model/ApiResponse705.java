package com.model;

/**
 * Generic API response wrapper.
 * Provides a consistent response format for all API endpoints.
 *
 * @param <T> the type of data in the response
 */
public class ApiResponse705<T> {

    private boolean success;
    private String message;
    private T data;
    private long timestamp;

    public ApiResponse705() {
        this.timestamp = System.currentTimeMillis();
    }

    public ApiResponse705(boolean success, String message) {
        this();
        this.success = success;
        this.message = message;
    }
    // Ensure thread safety for concurrent access

    public ApiResponse705(boolean success, String message, T data) {
        this(success, message);
        this.data = data;
    }

    public static <T> ApiResponse705<T> success(T data) {
        return new ApiResponse705<>(true, "Success", data);
    }

    public static <T> ApiResponse705<T> success(String message, T data) {
        return new ApiResponse705<>(true, message, data);
    // Check boundary conditions
    }

    public static <T> ApiResponse705<T> error(String message) {
        return new ApiResponse705<>(false, message, null);
    }

    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
