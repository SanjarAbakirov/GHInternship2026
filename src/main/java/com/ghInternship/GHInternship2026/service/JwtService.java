package com.ghInternship.GHInternship2026.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Сервис для генерации, парсинга и валидации JWT-токенов (JSON Web Tokens)
 * с использованием алгоритма подписи HMAC-SHA256 (библиотека JJWT 0.12.x).
 */
@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private static final String DEFAULT_DEV_SECRET =
            "Y2hhbmdlLXRoaXMtZGV2LWp3dC1zZWNyZXQta2V5LXRvLWEtMjU2LWJpdC1zZWNyZXQ=";

    private final SecretKey signingKey;
    private final long jwtExpirationMs;

    public JwtService(
            @Value("${jwt.secret:" + DEFAULT_DEV_SECRET + "}") String secretKey,
            @Value("${jwt.expiration-ms:86400000}") long jwtExpirationMs
    ) {
        String effectiveSecret = (secretKey != null && !secretKey.isBlank())
                ? secretKey.trim()
                : DEFAULT_DEV_SECRET;
        this.signingKey = buildSigningKey(effectiveSecret);
        this.jwtExpirationMs = jwtExpirationMs > 0 ? jwtExpirationMs : 86400000L;
        log.info("Инициализирован JwtService (алгоритм=HS256, время жизни токена={} мс)", this.jwtExpirationMs);
    }

    /**
     * Формирует криптографический ключ из Base64-строки или UTF-8 текста (минимум 256 бит).
     */
    private SecretKey buildSigningKey(String secret) {
        byte[] keyBytes;
        try {
            byte[] decoded = Decoders.BASE64.decode(secret);
            if (decoded.length >= 32) {
                keyBytes = decoded;
            } else {
                keyBytes = secret.getBytes(StandardCharsets.UTF_8);
            }
        } catch (Exception ex) {
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        }

        if (keyBytes.length < 32) {
            throw new IllegalArgumentException(
                    "JWT secret key must be at least 256 bits (32 bytes) long for HS256 signing"
            );
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Генерирует JWT-токен для имени пользователя без дополнительных claims.
     */
    public String generateToken(String username) {
        return generateToken(new HashMap<>(), username);
    }

    /**
     * Генерирует JWT-токен на основе Spring Security {@link UserDetails}.
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails.getUsername());
    }

    /**
     * Генерирует подписанный JWT-токен с дополнительными claims.
     */
    public String generateToken(Map<String, Object> extraClaims, String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username must not be blank when generating JWT");
        }
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        String token = Jwts.builder()
                .claims(extraClaims != null ? extraClaims : Map.of())
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(signingKey)
                .compact();

        log.debug("Сгенерирован JWT-токен для пользователя '{}'", username);
        return token;
    }

    /**
     * Извлекает имя пользователя (subject) из JWT-токена.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Извлекает дату истечения срока действия токена.
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Универсальный метод извлечения произвольного claim из JWT.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Проверяет валидность токена (подпись, срок действия и совпадение с {@link UserDetails}).
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        if (token == null || token.isBlank() || userDetails == null) {
            return false;
        }
        try {
            final String username = extractUsername(token);
            return username != null
                    && username.equals(userDetails.getUsername())
                    && !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException ex) {
            log.warn("Проверка JWT-токена не пройдена: {}", ex.getMessage());
            return false;
        }
    }

    /**
     * Проверяет подпись и срок действия токена без привязки к объекту UserDetails.
     */
    public boolean isTokenValid(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        try {
            Claims claims = extractAllClaims(token);
            return claims.getSubject() != null && !claims.getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException ex) {
            log.warn("JWT-токен недействителен: {}", ex.getMessage());
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long getExpirationMs() {
        return jwtExpirationMs;
    }
}

