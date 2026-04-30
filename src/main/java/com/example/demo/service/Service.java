package com.example.demo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserSecurityService {
    
    private final PasswordEncoder passwordEncoder;
    
    /**
     * Хеширует пароль перед сохранением в БД
     */
    public String hashPassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
    
    /**
     * Проверяет соответствие пароля его хешу
     */
    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
