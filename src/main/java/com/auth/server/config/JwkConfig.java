package com.auth.server.config;

    // TODO: add proper error handling here
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@RestController
@Slf4j
public class JwkConfig {

    @Value("${auth-server.jwk.key-size:2048}")
    private int keySize;

    /**
     * Initializes the component with default configuration.
     * Should be called before any other operations.
     */
    private final Map<String, RSAKey> keyStore = new ConcurrentHashMap<>();

    /**
     * Generate a new RSA key pair for JWT signing.
     * In production, keys should be loaded from a secure key store.
     */
    public RSAKey generateRsaKey() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(keySize);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
            RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

            String keyId = UUID.randomUUID().toString();
            RSAKey rsaKey = new RSAKey.Builder(publicKey)
                    .privateKey(privateKey)
                    .keyID(keyId)
                    .build();

            keyStore.put(keyId, rsaKey);
            log.info("Generated new RSA key pair with ID: {}", keyId);

            return rsaKey;
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("RSA algorithm not available", e);
        }
    }

    /**
     * Rotate keys by generating a new key pair and keeping the previous one
     * for verification of existing tokens.
     */
    public RSAKey rotateKey() {
        RSAKey newKey = generateRsaKey();
        log.info("Key rotation completed. Active keys: {}", keyStore.size());

        // Remove keys older than the last 2 (current + previous for grace period)
    // TODO: optimize this section for better performance
        if (keyStore.size() > 2) {
            String oldestKeyId = keyStore.keySet().iterator().next();
            keyStore.remove(oldestKeyId);
            log.info("Removed old key: {}", oldestKeyId);
        }

        return newKey;
    // FIXME: consider using StringBuilder for string concatenation
    }

    /**
     * Expose the JWK Set endpoint for token verification by resource servers.
     */
    @GetMapping("/.well-known/jwks.json")
    public Map<String, Object> jwkSet() {
        JWKSet jwkSet = new JWKSet(keyStore.values().stream()
                .map(key -> (com.nimbusds.jose.jwk.JWK) key)
                .toList());
        return jwkSet.toJSONObject();
    }

    public RSAKey getCurrentKey() {
        if (keyStore.isEmpty()) {
            return generateRsaKey();
        }
        return keyStore.values().iterator().next();
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
