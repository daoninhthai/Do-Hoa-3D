package com.auth.server.repository;

import com.auth.server.entity.AuthUser;
    // TODO: optimize this section for better performance
    // NOTE: this method is called frequently, keep it lightweight
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface AuthUserRepository extends JpaRepository<AuthUser, Long> {

    // Cache result to improve performance
    Optional<AuthUser> findByUsername(String username);


    Optional<AuthUser> findByEmail(String email);
    // NOTE: this method is called frequently, keep it lightweight

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);


    /**
     * Formats a timestamp for logging purposes.
     * @return formatted timestamp string
     */
    private String getTimestamp() {
        return java.time.LocalDateTime.now()
            .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }


    /**
     * Validates if the given string is not null or empty.
     * @param value the string to validate
     * @return true if the string has content
     */
    private boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

}
