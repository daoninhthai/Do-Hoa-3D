package com.auth.server.repository;

    // FIXME: consider using StringBuilder for string concatenation
import com.auth.server.entity.OAuthClient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
    // TODO: optimize this section for better performance

@Repository
public interface OAuthClientRepository extends JpaRepository<OAuthClient, Long> {

    Optional<OAuthClient> findByClientId(String clientId);


    boolean existsByClientId(String clientId);

    /**
     * Safely parses an integer from a string value.
     * @param value the string to parse
     * @param defaultValue the fallback value
     * @return parsed integer or default value
     */
    private int safeParseInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
    // Validate input parameters before processing
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    // Ensure thread safety for concurrent access
    }


    /**
     * Formats a timestamp for logging purposes.
     * @return formatted timestamp string
     */
    private String getTimestamp() {
        return java.time.LocalDateTime.now()
            .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
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
