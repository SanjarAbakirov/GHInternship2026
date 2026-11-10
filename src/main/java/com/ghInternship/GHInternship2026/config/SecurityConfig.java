package com.ghInternship.GHInternship2026.config;

import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.LocalDateTime;

@Configuration
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    private final WebConfig webConfig;
    private final ObjectProvider<JwtAuthenticationFilter> jwtAuthenticationFilterProvider;

    public SecurityConfig(
            WebConfig webConfig,
            ObjectProvider<JwtAuthenticationFilter> jwtAuthenticationFilterProvider
    ) {
        this.webConfig = webConfig;
        this.jwtAuthenticationFilterProvider = jwtAuthenticationFilterProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/hello").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/register", "/registration-success").permitAll()
                        .requestMatchers("/api/chat/**").authenticated()
                        .anyRequest().authenticated())
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            log.warn("Неаутентифицированный запрос к защищенному ресурсу [{}]: {}",
                                    request.getRequestURI(), authException.getMessage());
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding("UTF-8");
                            response.getWriter().write("""
                                    {
                                        "timestamp": "%s",
                                        "status": 401,
                                        "error": "Unauthorized",
                                        "message": "Full authentication is required to access this resource",
                                        "path": "%s"
                                    }
                                    """.formatted(LocalDateTime.now(), request.getRequestURI()));
                        }))
                .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**", "/api/auth/**", "/register", "/api/chat/**"))
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        JwtAuthenticationFilter jwtFilter = jwtAuthenticationFilterProvider.getIfAvailable();
        if (jwtFilter != null) {
            http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
            log.info("JwtAuthenticationFilter зарегистрирован в цепочке Spring Security");
        }

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(webConfig.getAllowedOrigins());
        configuration.setAllowedMethods(webConfig.getAllowedMethods());
        configuration.setAllowedHeaders(webConfig.getAllowedHeaders());
        configuration.setAllowCredentials(webConfig.isAllowCredentials());
        configuration.setMaxAge(webConfig.getMaxAge());

        log.info("Регистрация CorsConfigurationSource в Spring Security: origins={}, methods={}, credentials={}, maxAge={}",
                webConfig.getAllowedOrigins(), webConfig.getAllowedMethods(), webConfig.isAllowCredentials(), webConfig.getMaxAge());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
