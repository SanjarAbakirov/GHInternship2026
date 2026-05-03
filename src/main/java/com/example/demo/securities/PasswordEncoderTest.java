package com.example.demo.securities;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тест для проверки работы PasswordEncoder
 * @SpringBootTest - запускает полный Spring контекст
 */
@SpringBootTest
class PasswordEncoderTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void testEncodePassword() {
        // 1. Берем обычный пароль
        String rawPassword = "mySecret123";

        // 2. Шифруем его
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // 3. Проверяем, что зашифрованный пароль:
        // - не равен исходному
        assertNotEquals(rawPassword, encodedPassword);

        // - начинается с $2a$ (это признак BCrypt)
        assertTrue(encodedPassword.startsWith("$2a$"));

        // - имеет длину 60 символов
        assertEquals(60, encodedPassword.length());

        System.out.println("Тест пройден!");
        System.out.println("Оригинал: " + rawPassword);
        System.out.println("Хеш: " + encodedPassword);
    }

    @Test
    void testVerifyPassword() {
        String rawPassword = "testPassword";

        // Шифруем пароль
        String encoded = passwordEncoder.encode(rawPassword);

        // Проверяем правильный пароль
        assertTrue(passwordEncoder.matches(rawPassword, encoded));

        // Проверяем неправильный пароль
        assertFalse(passwordEncoder.matches("wrongPassword", encoded));
    }

    @Test
    void testSamePasswordDifferentHash() {
        String password = "samePassword";

        // Шифруем один и тот же пароль дважды
        String hash1 = passwordEncoder.encode(password);
        String hash2 = passwordEncoder.encode(password);

        // Хеши должны быть разными (из-за соли)
        assertNotEquals(hash1, hash2);

        // Но оба хеша валидны для одного пароля
        assertTrue(passwordEncoder.matches(password, hash1));
        assertTrue(passwordEncoder.matches(password, hash2));

        System.out.println("Хеш №1: " + hash1);
        System.out.println("Хеш №2: " + hash2);
        System.out.println("Разные, но оба подходят!");
    }
}