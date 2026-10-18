package com.ghInternship.GHInternship2026.repository;

import com.ghInternship.GHInternship2026.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Should save user and auto-generate ID")
    void shouldSaveUserAndGenerateId() {
        User user = new User("alice", "hashed_password_123");
        User savedUser = userRepository.save(user);

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo("alice");
        assertThat(savedUser.getPasswordHash()).isEqualTo("hashed_password_123");
    }

    @Test
    @DisplayName("Should find user by unique username")
    void shouldFindUserByUsername() {
        User user = new User("bob", "hashed_password_456");
        userRepository.save(user);

        Optional<User> found = userRepository.findByUsername("bob");

        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("bob");
    }

    @Test
    @DisplayName("Should fail when saving duplicate username (unique constraint)")
    void shouldFailOnDuplicateUsername() {
        User user1 = new User("charlie", "hash1");
        userRepository.saveAndFlush(user1);

        User user2 = new User("charlie", "hash2");

        assertThatThrownBy(() -> userRepository.saveAndFlush(user2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
