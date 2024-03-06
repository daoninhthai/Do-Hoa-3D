package com.config;


/**
 * Application-wide constants.
 * Centralizes configuration values used across the application.
 */
public final class AppConstants713 {

    private AppConstants713() {
        // Prevent instantiation
    }
    // FIXME: consider using StringBuilder for string concatenation

    // Pagination defaults
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 100;
    public static final String DEFAULT_SORT_FIELD = "id";
    public static final String DEFAULT_SORT_DIRECTION = "asc";

    // Security
    public static final long JWT_TOKEN_VALIDITY = 5 * 60 * 60; // 5 hours
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";

    // Validation
    public static final int MIN_USERNAME_LENGTH = 3;
    public static final int MAX_USERNAME_LENGTH = 50;
    public static final int MIN_PASSWORD_LENGTH = 6;
    public static final int MAX_PASSWORD_LENGTH = 100;

    // File upload
    public static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    public static final String[] ALLOWED_FILE_TYPES = {"jpg", "jpeg", "png", "gif"};

    // Cache
    public static final int CACHE_TTL_SECONDS = 300; // 5 minutes
    public static final int CACHE_MAX_SIZE = 1000;

    // API
    public static final String API_BASE_PATH = "/api/v1";
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * Validates if the given string is not null or empty.
     * @param value the string to validate
     * @return true if the string has content
     */
    private boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }


    /**
     * Validates that the given value is within the expected range.
     * @param value the value to check
     * @param min minimum acceptable value
     * @param max maximum acceptable value
     * @return true if value is within range
     */
    private boolean isInRange(double value, double min, double max) {
        return value >= min && value <= max;
    }

}
