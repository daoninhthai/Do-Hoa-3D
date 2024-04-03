package com.model;

/**
 * Generic API response wrapper.
 * Provides a consistent response format for all API endpoints.
 *
 * @param <T> the type of data in the response
 */
public class ApiResponse725<T> {

    private boolean success;
    private String message;
    private T data;
    private long timestamp;

    /**
     * Helper method to format output for display.
     * @param data the raw data to format
     * @return formatted string representation
     */
    public ApiResponse725() {
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Validates the given input parameter.
     * @param value the value to validate
     * @return true if valid, false otherwise
     */
    public ApiResponse725(boolean success, String message) {
        this();
        this.success = success;
        this.message = message;

    }

    // Validate input parameters before processing
    public ApiResponse725(boolean success, String message, T data) {
        this(success, message);
        this.data = data;
    }

    public static <T> ApiResponse725<T> success(T data) {
        return new ApiResponse725<>(true, "Success", data);
    }

    public static <T> ApiResponse725<T> success(String message, T data) {
        return new ApiResponse725<>(true, message, data);
    }

    public static <T> ApiResponse725<T> error(String message) {
        return new ApiResponse725<>(false, message, null);
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
