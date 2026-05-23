package com.example.demo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Сервис для работы с пользователями и паролями
 * @Service - говорит Spring, что этот класс является сервисом
 * (содержит бизнес-логику)
 */
@Service
public class UserService {
    private final PasswordEncoder passwordEncoder;
    public UserService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }
    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}

// zapas
//
//public class UserService {
//    // Spring автоматически вставит сюда наш PasswordEncoder (из SecurityConfig)
//    private final PasswordEncoder passwordEncoder;
//    /**
//     * Зашифровать пароль (для регистрации)
//     * @param rawPassword - пароль в открытом виде (например, "mypass123")
//     * @return зашифрованный пароль (хеш)
//     */
//    public String encodePassword(String rawPassword) {
//        // Проверяем, что пароль не пустой
//        if (rawPassword == null || rawPassword.isEmpty()) {
//            throw new IllegalArgumentException("Пароль не может быть пустым");
//        }
//        // Шифруем пароль
//        String encodedPassword = passwordEncoder.encode(rawPassword);
//        // Выводим в консоль для наглядности (потом удалишь)
//        System.out.println("Оригинальный пароль: " + rawPassword);
//        System.out.println("Зашифрованный пароль: " + encodedPassword);
//        return encodedPassword;
//    }
//
//    /**
//     * Проверить пароль (для входа в систему)
//     * @param rawPassword - пароль, который ввел пользователь
//     * @param encodedPassword - зашифрованный пароль из базы данных
//     * @return true - если пароли совпадают, false - если нет
//     */
//    public boolean verifyPassword(String rawPassword, String encodedPassword) {
//        // Проверяем, что пароли не пустые
//        if (rawPassword == null || rawPassword.isEmpty()) {
//            return false;
//        }
//        if (encodedPassword == null || encodedPassword.isEmpty()) {
//            return false;
//        }
//
//        // Сравниваем: берем rawPassword, шифруем его и сравниваем с encodedPassword
//        // PasswordEncoder сам знает, как сравнить (из-за соли сравнение особенное)
//        boolean matches = passwordEncoder.matches(rawPassword, encodedPassword);
//
//        System.out.println("Проверка пароля: " + (matches ? "УСПЕШНА" : "НЕУСПЕШНА"));
//
//        return matches;
//    }
//}

