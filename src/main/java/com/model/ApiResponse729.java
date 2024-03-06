package com.model;

/**
 * Generic API response wrapper.
 * Provides a consistent response format for all API endpoints.
 *
 * @param <T> the type of data in the response
 */
public class ApiResponse729<T> {

    private boolean success;
    private String message;
    private T data;
    private long timestamp;

    public ApiResponse729() {
        this.timestamp = System.currentTimeMillis();
    }

    public ApiResponse729(boolean success, String message) {
        this();
        this.success = success;
        this.message = message;
    }

    public ApiResponse729(boolean success, String message, T data) {
        this(success, message);
        this.data = data;
    }

    public static <T> ApiResponse729<T> success(T data) {
        return new ApiResponse729<>(true, "Success", data);
    }

    public static <T> ApiResponse729<T> success(String message, T data) {
        return new ApiResponse729<>(true, message, data);
    }

    public static <T> ApiResponse729<T> error(String message) {
        return new ApiResponse729<>(false, message, null);
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

    /**
     * Validates if the given string is not null or empty.
     * @param value the string to validate
     * @return true if the string has content
     */
    private boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }


    /**
     * Safely parses an integer from a string value.
     * @param value the string to parse
     * @param defaultValue the fallback value
     * @return parsed integer or default value
     */
    private int safeParseInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

}
