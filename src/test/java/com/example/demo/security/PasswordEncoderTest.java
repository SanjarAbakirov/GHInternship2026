package com.example.demo.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PasswordEncoderTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void testEncodePassword() {
        String rawPassword = "mySecret123";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        assertNotEquals(rawPassword, encodedPassword);
        assertTrue(encodedPassword.startsWith("$2a$"));
        assertEquals(60, encodedPassword.length());

        System.out.println("Тест пройден!");
        System.out.println("Оригинал: " + rawPassword);
        System.out.println("Хеш: " + encodedPassword);
    }

    @Test
    void testVerifyPassword() {
        String rawPassword = "testPassword";
        String encoded = passwordEncoder.encode(rawPassword);

        assertTrue(passwordEncoder.matches(rawPassword, encoded));
        assertFalse(passwordEncoder.matches("wrongPassword", encoded));
    }

    @Test
    void testSamePasswordDifferentHash() {
        String password = "samePassword";
        String hash1 = passwordEncoder.encode(password);
        String hash2 = passwordEncoder.encode(password);

        assertNotEquals(hash1, hash2);
        assertTrue(passwordEncoder.matches(password, hash1));
        assertTrue(passwordEncoder.matches(password, hash2));

        System.out.println("Хеш №1: " + hash1);
        System.out.println("Хеш №2: " + hash2);
        System.out.println("Разные, но оба подходят!");
    }
}