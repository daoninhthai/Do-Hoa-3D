package com.auth.server.controller;

import com.auth.server.entity.OAuthClient;
import com.auth.server.service.ClientRegistrationService;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
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
    public ResponseEntity<OAuthClient> registerClient(@RequestBody ClientRegistrationRequest request) {
        log.info("Registering new OAuth2 client: {}", request.getClientName());

        OAuthClient registeredClient = clientRegistrationService.registerClient(
                request.getClientName(),
                request.getRedirectUris(),
                request.getScopes(),
                request.getGrantTypes()
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

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClientRegistrationRequest {
        private String clientName;
        private List<String> redirectUris;
        private List<String> scopes;
        private List<String> grantTypes;
    }

}
