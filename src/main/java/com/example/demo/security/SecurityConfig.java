package com.example.demo.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;s
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Конфигурационный класс для безопасности приложения
 * @Configuration - говорит Spring, что этот класс содержит настройки
 */
@Configuration
public class SecurityConfig {

    /**
     * Создаем бин (объект) PasswordEncoder, который будет использовать BCrypt
     * @Bean - говорит Spring: "создай этот объект и храни его в контексте"
     *
     * Что такое бин? Это объект, которым управляет Spring.
     * Мы сможем использовать этот PasswordEncoder в любом месте приложения
     * через аннотацию @Autowired
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCryptPasswordEncoder - это реализация PasswordEncoder
        // Сила = 10 (чем выше, тем дольше шифруется, но безопаснее)
        // Можно написать new BCryptPasswordEncoder(10) - но 10 это значение по умолчанию
        return new BCryptPasswordEncoder();
    }
}