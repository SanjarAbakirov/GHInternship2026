package com.ghInternship.GHInternship2026.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Модульные тесты для {@link JwtService}.
 * Проверяют генерацию токенов, извлечение subject/claims, проверку подписи,
 * обработку просроченных или подделанных токенов и валидацию длины секрета.
 */
class JwtServiceTest {

    private static final String TEST_SECRET =
            "Y2hhbmdlLXRoaXMtZGV2LWp3dC1zZWNyZXQta2V5LXRvLWEtMjU2LWJpdC1zZWNyZXQ=";
    private static final long EXPIRATION_MS = 3_600_000L;

    private JwtService jwtService;
    private UserDetails aliceUserDetails;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(TEST_SECRET, EXPIRATION_MS);
        aliceUserDetails = User.builder()
                .username("alice")
                .password("$2a$10$hash")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                .build();
    }

    @Test
    @DisplayName("Should generate valid HS256 JWT token with 3 parts and correct username")
    void shouldGenerateValidJwtToken() {
        String token = jwtService.generateToken("alice");

        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3);
        assertThat(jwtService.extractUsername(token)).isEqualTo("alice");
        assertThat(jwtService.isTokenValid(token, aliceUserDetails)).isTrue();
        assertThat(jwtService.isTokenValid(token)).isTrue();
    }

    @Test
    @DisplayName("Should generate token with extra claims and extract expiration")
    void shouldGenerateTokenWithExtraClaims() {
        String token = jwtService.generateToken(Map.of("role", "USER"), "alice");

        assertThat(jwtService.extractUsername(token)).isEqualTo("alice");
        assertThat(jwtService.extractExpiration(token)).isInTheFuture();
        String role = jwtService.extractClaim(token, claims -> claims.get("role", String.class));
        assertThat(role).isEqualTo("USER");
    }

    @Test
    @DisplayName("Should return false when token username does not match UserDetails")
    void shouldReturnFalseWhenUsernameDoesNotMatch() {
        String token = jwtService.generateToken("alice");
        UserDetails bobUserDetails = User.builder()
                .username("bob")
                .password("$2a$10$hash")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                .build();

        assertThat(jwtService.isTokenValid(token, bobUserDetails)).isFalse();
    }

    @Test
    @DisplayName("Should return false when JWT signature is tampered or malformed")
    void shouldRejectTamperedOrMalformedTokens() {
        String validToken = jwtService.generateToken("alice");
        String tamperedToken = validToken.substring(0, validToken.length() - 4) + "XXXX";

        assertThat(jwtService.isTokenValid(tamperedToken, aliceUserDetails)).isFalse();
        assertThat(jwtService.isTokenValid(tamperedToken)).isFalse();
        assertThat(jwtService.isTokenValid("not.a.valid.jwt", aliceUserDetails)).isFalse();
        assertThat(jwtService.isTokenValid("", aliceUserDetails)).isFalse();
        assertThat(jwtService.isTokenValid(null, aliceUserDetails)).isFalse();
    }

    @Test
    @DisplayName("Should return false when JWT token is signed with a different secret key")
    void shouldRejectTokenSignedWithDifferentSecret() {
        JwtService otherJwtService = new JwtService(
                "YW5vdGhlci1jb21wbGV0ZWx5LWRpZmZlcmVudC0yNTYtYml0LXNlY3JldC1rZXk=",
                EXPIRATION_MS
        );
        String foreignToken = otherJwtService.generateToken("alice");

        assertThat(jwtService.isTokenValid(foreignToken, aliceUserDetails)).isFalse();
        assertThat(jwtService.isTokenValid(foreignToken)).isFalse();
    }

    @Test
    @DisplayName("Should reject expired JWT token")
    void shouldRejectExpiredToken() throws InterruptedException {
        JwtService shortLivedJwtService = new JwtService(TEST_SECRET, 1L);
        String token = shortLivedJwtService.generateToken("alice");

        Thread.sleep(15);

        assertThat(shortLivedJwtService.isTokenValid(token, aliceUserDetails)).isFalse();
        assertThat(shortLivedJwtService.isTokenValid(token)).isFalse();
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when JWT secret key is shorter than 256 bits")
    void shouldThrowExceptionWhenSecretIsTooShort() {
        assertThatThrownBy(() -> new JwtService("short-secret", EXPIRATION_MS))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least 256 bits");
    }
}
