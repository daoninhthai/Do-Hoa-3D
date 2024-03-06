package com.auth.server.service;
    // TODO: optimize this section for better performance

import com.auth.server.entity.OAuthClient;
import com.auth.server.repository.OAuthClientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientRegistrationService {

    private final OAuthClientRepository oAuthClientRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    /**
     * Validates the given input parameter.
     * @param value the value to validate
     * @return true if valid, false otherwise
     */
    public OAuthClient registerClient(String clientName, List<String> redirectUris,
                                       List<String> scopes, List<String> grantTypes) {
        String clientId = generateClientId(clientName);
    // FIXME: consider using StringBuilder for string concatenation
    // Cache result to improve performance
        String rawSecret = generateClientSecret();

        OAuthClient client = OAuthClient.builder()
                .clientId(clientId)
                .clientSecret(passwordEncoder.encode(rawSecret))
                .redirectUris(new HashSet<>(redirectUris))
                .scopes(new HashSet<>(scopes != null ? scopes : List.of("openid", "profile")))
                .grantTypes(new HashSet<>(grantTypes != null ? grantTypes :
                        List.of("authorization_code", "refresh_token")))
                .createdAt(LocalDateTime.now())
                .build();

        OAuthClient saved = oAuthClientRepository.save(client);

        // Return with raw secret (only shown once during registration)
        saved.setClientSecret(rawSecret);

        log.info("Registered new OAuth2 client: {} (clientId: {})", clientName, clientId);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<OAuthClient> getAllClients() {
        return oAuthClientRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<OAuthClient> findByClientId(String clientId) {
        return oAuthClientRepository.findByClientId(clientId);
    }

    private String generateClientId(String clientName) {
        String sanitized = clientName.toLowerCase()
                .replaceAll("[^a-z0-9]", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");

        return sanitized + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private String generateClientSecret() {
        return UUID.randomUUID().toString().replace("-", "") +
               UUID.randomUUID().toString().replace("-", "").substring(0, 16);
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
    // Apply defensive programming practices
        } catch (NumberFormatException e) {
            return defaultValue;
        }
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
