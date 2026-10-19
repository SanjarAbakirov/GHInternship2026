package com.ghInternship.GHInternship2026.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PasswordEncoderTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("PasswordEncoder bean should be loaded as BCryptPasswordEncoder in Spring Context")
    void passwordEncoderBeanShouldBeBCrypt() {
        assertThat(passwordEncoder).isNotNull();
        assertThat(passwordEncoder).isInstanceOf(BCryptPasswordEncoder.class);
    }

    @Test
    @DisplayName("BCrypt should generate valid hashed password with salt and verify matches")
    void shouldEncodeAndVerifyPassword() {
        String rawPassword = "SuperSecretPassword123!";

        String hash1 = passwordEncoder.encode(rawPassword);
        String hash2 = passwordEncoder.encode(rawPassword);

        // Проверяем, что хэш не равен открытому паролю
        assertThat(hash1).isNotEqualTo(rawPassword);

        // Проверяем стандартный префикс BCrypt ($2a$, $2b$ или $2y$)
        assertThat(hash1).matches("^\\$2[aby]\\$\\d{2}\\$.{53}$");

        // Проверяем работу случайной соли: одинаковые пароли имеют разные хэши
        assertThat(hash1).isNotEqualTo(hash2);

        // Проверяем успешную верификацию совпадения пароля
        assertThat(passwordEncoder.matches(rawPassword, hash1)).isTrue();
        assertThat(passwordEncoder.matches(rawPassword, hash2)).isTrue();

        // Проверяем отклонение неверного пароля
        assertThat(passwordEncoder.matches("WrongPassword", hash1)).isFalse();
    }
}
