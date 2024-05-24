package com.auth.server.controller;

import com.auth.server.entity.AuthUser;
import com.auth.server.entity.UserRole;
import com.auth.server.repository.AuthUserRepository;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
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

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest request) {
        log.info("Registration request for username: {}", request.getUsername());

        if (authUserRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest()
                    .body(Collections.singletonMap("error", "Username already exists"));
        }

        if (authUserRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest()
                    .body(Collections.singletonMap("error", "Email already registered"));
        }

        Set<UserRole> roles = new HashSet<>();
        roles.add(UserRole.USER);

        AuthUser newUser = AuthUser.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(roles)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();

        AuthUser saved = authUserRepository.save(newUser);
        saved.setPassword(null); // Don't expose password hash

        log.info("User registered successfully: {}", saved.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegisterRequest {
        private String username;
        private String email;
        private String password;
    }

}
