package com.example.demo.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig {

    /**
     * Создает бин PasswordEncoder с использованием BCryptPasswordEncoder
     * Это сильный алгоритм хеширования паролей
     * Бин будет доступен для внедрения во всем приложении
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt использует соль (salt) и адаптивную сложность
        // Параметр 10 (default) - сила хеширования (2^10 итераций)
        return new BCryptPasswordEncoder();
    }
}