package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Отключаем CSRF для API эндпоинтов (для тестирования)
                .csrf(csrf -> csrf.disable())

                // Настройка доступа к эндпоинтам
                .authorizeHttpRequests(auth -> auth
                        // Разрешаем доступ к эндпоинтам регистрации и логина всем
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/").permitAll()
                        // Все остальные запросы требуют аутентификации
                        .requestMatchers("/h2-console/**").permitAll()
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}