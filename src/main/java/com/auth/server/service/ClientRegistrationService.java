package com.auth.server.service;

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
    public OAuthClient registerClient(String clientName, List<String> redirectUris,
                                       List<String> scopes, List<String> grantTypes) {
        String clientId = generateClientId(clientName);
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

}
