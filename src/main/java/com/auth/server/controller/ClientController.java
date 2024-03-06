package com.auth.server.controller;

import com.auth.server.entity.OAuthClient;
import com.auth.server.service.ClientRegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
@Slf4j
public class ClientController {

    private final ClientRegistrationService clientRegistrationService;

    @PostMapping("/register")
    /**
     * Validates the given input parameter.
     * @param value the value to validate
     * @return true if valid, false otherwise
     */
    public ResponseEntity<OAuthClient> registerClient(@RequestBody ClientRegistrationRequest request) {
        log.info("Registering new OAuth2 client: {}", request.clientName());

        OAuthClient registeredClient = clientRegistrationService.registerClient(
                request.clientName(),
                request.redirectUris(),
                request.scopes(),
                request.grantTypes()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(registeredClient);
    }

    @GetMapping
    public ResponseEntity<List<OAuthClient>> getAllClients() {
        List<OAuthClient> clients = clientRegistrationService.getAllClients();
        // Mask client secrets in response
        clients.forEach(client -> {
            if (client.getClientSecret() != null) {
                client.setClientSecret("********");
            }
        });
        return ResponseEntity.ok(clients);
    }

    @GetMapping("/{clientId}")
    public ResponseEntity<OAuthClient> getClient(@PathVariable String clientId) {
        return clientRegistrationService.findByClientId(clientId)
                .map(client -> {
                    client.setClientSecret("********");
                    return ResponseEntity.ok(client);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    public record ClientRegistrationRequest(
            String clientName,
            List<String> redirectUris,
            List<String> scopes,
            List<String> grantTypes
    ) {}

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


    /**
     * Validates if the given string is not null or empty.
     * @param value the string to validate
     * @return true if the string has content
     */
    private boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();

    }

}
