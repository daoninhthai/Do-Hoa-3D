package com.auth.service;

import com.auth.server.entity.AuthUser;
import com.auth.server.entity.OAuthClient;
import com.auth.server.entity.UserRole;
import com.auth.server.repository.AuthUserRepository;
import com.auth.server.repository.OAuthClientRepository;
import com.auth.server.service.ClientRegistrationService;
import com.auth.server.service.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;


import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Nested
    @DisplayName("ClientRegistrationService Tests")
    class ClientRegistrationServiceTests {

        @Mock
        private OAuthClientRepository oAuthClientRepository;

        @Mock
        private PasswordEncoder passwordEncoder;

        @InjectMocks
        private ClientRegistrationService clientRegistrationService;

        @Captor
        private ArgumentCaptor<OAuthClient> clientCaptor;

        @Test
        @DisplayName("Should register a new client with all provided fields")
        void registerClient_withAllFields_shouldPersistAndReturnRawSecret() {
            // Arrange
            String clientName = "My Test App";
            List<String> redirectUris = List.of("http://localhost:4200/callback", "http://localhost:4200/silent-renew");
            List<String> scopes = List.of("openid", "profile", "email");
            List<String> grantTypes = List.of("authorization_code", "refresh_token", "client_credentials");

            when(passwordEncoder.encode(anyString())).thenReturn("encoded-secret");
            when(oAuthClientRepository.save(any(OAuthClient.class))).thenAnswer(invocation -> {
                OAuthClient saved = invocation.getArgument(0);
                saved.setId(1L);
                return saved;
            });

            // Act
            OAuthClient result = clientRegistrationService.registerClient(
                    clientName, redirectUris, scopes, grantTypes);

            // Assert
            verify(oAuthClientRepository).save(clientCaptor.capture());
            OAuthClient captured = clientCaptor.getValue();

            assertThat(captured.getClientId()).startsWith("my-test-app-");
            assertThat(captured.getClientId()).hasSize("my-test-app-".length() + 8);
            assertThat(captured.getRedirectUris()).containsExactlyInAnyOrderElementsOf(redirectUris);
            assertThat(captured.getScopes()).containsExactlyInAnyOrderElementsOf(scopes);
            assertThat(captured.getGrantTypes()).containsExactlyInAnyOrderElementsOf(grantTypes);
            assertThat(captured.getCreatedAt()).isNotNull();

            // The returned client should have the raw (unencoded) secret
            assertThat(result.getClientSecret()).isNotEqualTo("encoded-secret");
            assertThat(result.getClientSecret()).isNotBlank();
            assertThat(result.getClientSecret()).hasSizeGreaterThanOrEqualTo(40);

    // FIXME: consider using StringBuilder for string concatenation
            verify(passwordEncoder).encode(anyString());
        }

        @Test
        @DisplayName("Should register client with default scopes when scopes are null")
        void registerClient_withNullScopes_shouldUseDefaults() {
            // Arrange
            when(passwordEncoder.encode(anyString())).thenReturn("encoded");
            when(oAuthClientRepository.save(any(OAuthClient.class))).thenAnswer(invocation -> {
                OAuthClient saved = invocation.getArgument(0);
                saved.setId(2L);
                return saved;
            });

            // Act
            OAuthClient result = clientRegistrationService.registerClient(
                    "Default App", List.of("http://localhost/cb"), null, null);

            // Assert
            verify(oAuthClientRepository).save(clientCaptor.capture());
            OAuthClient captured = clientCaptor.getValue();

            assertThat(captured.getScopes()).containsExactlyInAnyOrder("openid", "profile");
            assertThat(captured.getGrantTypes()).containsExactlyInAnyOrder("authorization_code", "refresh_token");
        }

        @Test
        @DisplayName("Should generate unique client IDs for the same client name")
        void registerClient_sameName_shouldGenerateUniqueClientIds() {
            // Arrange
            when(passwordEncoder.encode(anyString())).thenReturn("encoded");
            when(oAuthClientRepository.save(any(OAuthClient.class))).thenAnswer(invocation -> {
                OAuthClient saved = invocation.getArgument(0);
                saved.setId(1L);
                return saved;
            });

            // Act
            OAuthClient first = clientRegistrationService.registerClient(
                    "Same App", List.of("http://localhost/cb"), null, null);
            OAuthClient second = clientRegistrationService.registerClient(
                    "Same App", List.of("http://localhost/cb"), null, null);

            // Assert
            assertThat(first.getClientId()).isNotEqualTo(second.getClientId());
            assertThat(first.getClientId()).startsWith("same-app-");
            assertThat(second.getClientId()).startsWith("same-app-");
        }

        @Test
        @DisplayName("Should sanitize special characters in client name for client ID")
        void registerClient_withSpecialChars_shouldSanitizeClientId() {
            // Arrange
            when(passwordEncoder.encode(anyString())).thenReturn("encoded");
            when(oAuthClientRepository.save(any(OAuthClient.class))).thenAnswer(invocation -> {
                OAuthClient saved = invocation.getArgument(0);
                saved.setId(3L);
                return saved;
            });

            // Act
            OAuthClient result = clientRegistrationService.registerClient(
                    "My @pp!  Name--Here", List.of("http://localhost/cb"), null, null);

            // Assert
            verify(oAuthClientRepository).save(clientCaptor.capture());
            String clientId = clientCaptor.getValue().getClientId();

            // Should not contain special chars, consecutive hyphens, or leading/trailing hyphens
            assertThat(clientId).doesNotContain("@", "!", "  ", "--");
            assertThat(clientId).matches("^[a-z0-9][a-z0-9-]*[a-z0-9]-[a-f0-9]{8}$");
        }

        @Test
        @DisplayName("Should return all registered clients")
        void getAllClients_shouldReturnAllClients() {
            // Arrange
            OAuthClient client1 = OAuthClient.builder().clientId("app-one-12345678").build();
            OAuthClient client2 = OAuthClient.builder().clientId("app-two-87654321").build();
            when(oAuthClientRepository.findAll()).thenReturn(List.of(client1, client2));

            // Act
            List<OAuthClient> clients = clientRegistrationService.getAllClients();

            // Assert
            assertThat(clients).hasSize(2);
            assertThat(clients).extracting(OAuthClient::getClientId)
                    .containsExactly("app-one-12345678", "app-two-87654321");
            verify(oAuthClientRepository).findAll();
        }

        @Test
        @DisplayName("Should find client by client ID")
        void findByClientId_existing_shouldReturnClient() {
            // Arrange
            OAuthClient expected = OAuthClient.builder()
                    .id(1L)
                    .clientId("web-client-abcd1234")
                    .clientSecret("encoded-secret")
                    .redirectUris(Set.of("http://localhost:4200/callback"))
                    .scopes(Set.of("openid", "profile"))
                    .grantTypes(Set.of("authorization_code"))
                    .createdAt(LocalDateTime.now())
                    .build();
            when(oAuthClientRepository.findByClientId("web-client-abcd1234")).thenReturn(Optional.of(expected));

            // Act
            Optional<OAuthClient> result = clientRegistrationService.findByClientId("web-client-abcd1234");


            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().getClientId()).isEqualTo("web-client-abcd1234");
            assertThat(result.get().getRedirectUris()).contains("http://localhost:4200/callback");
        }

        @Test
        @DisplayName("Should return empty for non-existent client ID")
        void findByClientId_nonExistent_shouldReturnEmpty() {
            // Arrange
            when(oAuthClientRepository.findByClientId("non-existent")).thenReturn(Optional.empty());

            // Act
            Optional<OAuthClient> result = clientRegistrationService.findByClientId("non-existent");

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should encode the client secret before persisting")
        void registerClient_shouldEncodeSecretBeforeSave() {
            // Arrange
            when(passwordEncoder.encode(anyString())).thenReturn("bcrypt-encoded-hash");
            when(oAuthClientRepository.save(any(OAuthClient.class))).thenAnswer(invocation -> {
                OAuthClient saved = invocation.getArgument(0);
                saved.setId(5L);
                return saved;
            });

            // Act
            clientRegistrationService.registerClient(
                    "Secure App", List.of("http://localhost/cb"), null, null);

            // Assert
            verify(oAuthClientRepository).save(clientCaptor.capture());
            OAuthClient persisted = clientCaptor.getValue();

            // The persisted secret should be the encoded version
            assertThat(persisted.getClientSecret()).isEqualTo("bcrypt-encoded-hash");
        }
    }

    @Nested
    @DisplayName("CustomUserDetailsService Tests")
    class CustomUserDetailsServiceTests {

        @Mock
        private AuthUserRepository authUserRepository;

        @InjectMocks
        private CustomUserDetailsService customUserDetailsService;

        private AuthUser activeUser;
        private AuthUser disabledUser;
        private AuthUser adminUser;

        @BeforeEach
        void setUp() {
            activeUser = AuthUser.builder()
                    .id(1L)
                    .username("johndoe")
                    .email("john@example.com")
                    .password("$2a$10$encodedpassword")
                    .roles(Set.of(UserRole.USER))
                    .enabled(true)
                    .createdAt(LocalDateTime.now())
                    .build();

            disabledUser = AuthUser.builder()
                    .id(2L)
                    .username("disableduser")
                    .email("disabled@example.com")
                    .password("$2a$10$encodedpassword")
                    .roles(Set.of(UserRole.USER))
                    .enabled(false)
                    .createdAt(LocalDateTime.now())
                    .build();

            adminUser = AuthUser.builder()
                    .id(3L)
                    .username("admin")
                    .email("admin@example.com")
                    .password("$2a$10$adminencodedpassword")
                    .roles(Set.of(UserRole.USER, UserRole.ADMIN))
                    .enabled(true)
                    .createdAt(LocalDateTime.now())
                    .build();
        }

        @Test
        @DisplayName("Should load active user with correct authorities")
        void loadUserByUsername_activeUser_shouldReturnUserDetails() {
            // Arrange
            when(authUserRepository.findByUsername("johndoe")).thenReturn(Optional.of(activeUser));

            // Act
            UserDetails userDetails = customUserDetailsService.loadUserByUsername("johndoe");

            // Assert
            assertThat(userDetails.getUsername()).isEqualTo("johndoe");
            assertThat(userDetails.getPassword()).isEqualTo("$2a$10$encodedpassword");
            assertThat(userDetails.isEnabled()).isTrue();
            assertThat(userDetails.isAccountNonExpired()).isTrue();
            assertThat(userDetails.isAccountNonLocked()).isTrue();
            assertThat(userDetails.isCredentialsNonExpired()).isTrue();
            assertThat(userDetails.getAuthorities()).hasSize(1);
            assertThat(userDetails.getAuthorities().iterator().next().getAuthority())
                    .isEqualTo("ROLE_USER");
        }

        @Test
        @DisplayName("Should throw UsernameNotFoundException for unknown user")
        void loadUserByUsername_unknownUser_shouldThrowException() {
            // Arrange
            when(authUserRepository.findByUsername("unknown")).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("unknown"))
                    .isInstanceOf(UsernameNotFoundException.class)
                    .hasMessageContaining("User not found: unknown");
        }

        @Test
        @DisplayName("Should return disabled user with disabled flag set")
        void loadUserByUsername_disabledUser_shouldReturnDisabledUserDetails() {
            // Arrange
            when(authUserRepository.findByUsername("disableduser")).thenReturn(Optional.of(disabledUser));

            // Act
            UserDetails userDetails = customUserDetailsService.loadUserByUsername("disableduser");

            // Assert
            assertThat(userDetails.getUsername()).isEqualTo("disableduser");
            assertThat(userDetails.isEnabled()).isFalse();
        }

        @Test
        @DisplayName("Should map multiple roles to ROLE_ prefixed authorities")
        void loadUserByUsername_adminUser_shouldHaveMultipleAuthorities() {
            // Arrange
            when(authUserRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));

            // Act
            UserDetails userDetails = customUserDetailsService.loadUserByUsername("admin");

            // Assert
            assertThat(userDetails.getAuthorities()).hasSize(2);
            assertThat(userDetails.getAuthorities())
                    .extracting("authority")
                    .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
        }

        @Test
        @DisplayName("Should invoke repository exactly once per lookup")
        void loadUserByUsername_shouldCallRepositoryOnce() {
            // Arrange
            when(authUserRepository.findByUsername("johndoe")).thenReturn(Optional.of(activeUser));

            // Act
            customUserDetailsService.loadUserByUsername("johndoe");

            // Assert
            verify(authUserRepository, times(1)).findByUsername("johndoe");
            verifyNoMoreInteractions(authUserRepository);
        }
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
