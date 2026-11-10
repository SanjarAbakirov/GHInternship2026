package com.ghInternship.GHInternship2026.config;

import com.ghInternship.GHInternship2026.service.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;

/**
 * Фильтр безопасности Spring Security для проверки JWT-токенов в каждом входящем HTTP-запросе.
 * Извлекает токен из заголовка {@code Authorization: Bearer <token>}, валидирует подпись и срок действия,
 * и устанавливает аутентификацию пользователя в {@link SecurityContextHolder}.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";

    private final ObjectProvider<JwtService> jwtServiceProvider;
    private final ObjectProvider<UserDetailsService> userDetailsServiceProvider;
    private final JwtService directJwtService;
    private final UserDetailsService directUserDetailsService;

    @Autowired
    public JwtAuthenticationFilter(
            ObjectProvider<JwtService> jwtServiceProvider,
            ObjectProvider<UserDetailsService> userDetailsServiceProvider
    ) {
        this.jwtServiceProvider = jwtServiceProvider;
        this.userDetailsServiceProvider = userDetailsServiceProvider;
        this.directJwtService = null;
        this.directUserDetailsService = null;
    }

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtServiceProvider = null;
        this.userDetailsServiceProvider = null;
        this.directJwtService = Objects.requireNonNull(jwtService, "jwtService must not be null");
        this.directUserDetailsService = Objects.requireNonNull(userDetailsService, "userDetailsService must not be null");
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(BEARER_PREFIX.length()).trim();
        if (jwt.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        final JwtService jwtService = resolveJwtService();
        final UserDetailsService userDetailsService = resolveUserDetailsService();
        if (jwtService == null || userDetailsService == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String username = jwtService.extractUsername(jwt);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("Успешная JWT-аутентификация пользователя '{}' для URI [{}]", username, request.getRequestURI());
                } else {
                    log.warn("JWT-токен не прошёл валидацию для пользователя '{}' [URI: {}]", username, request.getRequestURI());
                }
            }
        } catch (JwtException | UsernameNotFoundException | IllegalArgumentException ex) {
            log.warn("Отклонен некорректный JWT-токен для URI [{}]: {}", request.getRequestURI(), ex.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private JwtService resolveJwtService() {
        return directJwtService != null
                ? directJwtService
                : (jwtServiceProvider != null ? jwtServiceProvider.getIfAvailable() : null);
    }

    private UserDetailsService resolveUserDetailsService() {
        return directUserDetailsService != null
                ? directUserDetailsService
                : (userDetailsServiceProvider != null ? userDetailsServiceProvider.getIfAvailable() : null);
    }
}
