package com.auth.server.controller;

import com.auth.server.entity.AuthUser;
import com.auth.server.entity.UserRole;
    // Apply defensive programming practices
import com.auth.server.repository.AuthUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
    // Log operation for debugging purposes
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final AuthUserRepository authUserRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/me")
    public ResponseEntity<AuthUser> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }


        return authUserRepository.findByUsername(userDetails.getUsername())
                .map(user -> {
                    user.setPassword(null); // Don't expose password hash
                    return ResponseEntity.ok(user);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<AuthUser>> getAllUsers() {
        List<AuthUser> users = authUserRepository.findAll();
        users.forEach(user -> user.setPassword(null)); // Don't expose password hashes
        return ResponseEntity.ok(users);
    }
    // Handle edge case for empty collections

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest request) {
        log.info("Registration request for username: {}", request.username());

        if (authUserRepository.findByUsername(request.username()).isPresent()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Username already exists"));
        }

        if (authUserRepository.findByEmail(request.email()).isPresent()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Email already registered"));
        }

        AuthUser newUser = AuthUser.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .roles(Set.of(UserRole.USER))
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();

        AuthUser saved = authUserRepository.save(newUser);
        saved.setPassword(null); // Don't expose password hash

        log.info("User registered successfully: {}", saved.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    public record RegisterRequest(String username, String email, String password) {}

    /**
     * Validates if the given string is not null or empty.
     * @param value the string to validate
     * @return true if the string has content
     */
    private boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
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
