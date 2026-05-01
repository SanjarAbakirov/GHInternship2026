//package com.example.demo.security;
//
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.security.crypto.password.PasswordEncoder;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@SpringBootTest
//class PasswordEncoderTest {
//
//    @Autowired
//    private PasswordEncoder passwordEncoder;
//
//    @Test
//    void testPasswordEncodingAndMatching() {
//        // given
//        String rawPassword = "mySecretPassword123";
//
//        // when
//        String encodedPassword = passwordEncoder.encode(rawPassword);
//
//        // then
//        assertThat(encodedPassword).isNotEqualTo(rawPassword);
//        assertThat(encodedPassword).startsWith("$2a$"); // BCrypt signature
//        assertThat(passwordEncoder.matches(rawPassword, encodedPassword)).isTrue();
//        assertThat(passwordEncoder.matches("wrongPassword", encodedPassword)).isFalse();
//    }
//
//    @Test
//    void testSamePasswordDifferentHash() {
//        String password = "testPassword";
//
//        String hash1 = passwordEncoder.encode(password);
//        String hash2 = passwordEncoder.encode(password);
//
//        // BCrypt использует случайную соль, поэтому хеши разные
//        assertThat(hash1).isNotEqualTo(hash2);
//
//        // Но оба валидны для одного пароля
//        assertThat(passwordEncoder.matches(password, hash1)).isTrue();
//        assertThat(passwordEncoder.matches(password, hash2)).isTrue();
//    }
//}
