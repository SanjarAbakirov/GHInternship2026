package com.ghInternship.GHInternship2026.service;

import com.ghInternship.GHInternship2026.entity.User;
import com.ghInternship.GHInternship2026.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("findByUsername should return user when user exists")
    void shouldFindUserByUsername() {
        User user = new User();
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        Optional<User> result = userService.findByUsername("testuser");

        assertTrue(result.isPresent());
        assertEquals(user, result.get());
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    @DisplayName("registerUser should hash raw password and save user")
    void shouldRegisterUserWithHashedPassword() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("rawPassword123")).thenReturn("$2a$10$hashedPasswordSample");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User registered = userService.registerUser("newuser", "rawPassword123");

        assertEquals("newuser", registered.getUsername());
        assertEquals("$2a$10$hashedPasswordSample", registered.getPasswordHash());
        verify(passwordEncoder).encode("rawPassword123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("registerUser should throw exception when username already exists")
    void shouldThrowExceptionWhenRegisteringDuplicateUsername() {
        when(userRepository.existsByUsername("existingUser")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                userService.registerUser("existingUser", "anyPassword"));

        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("verifyPassword should delegate to PasswordEncoder.matches")
    void shouldVerifyPassword() {
        when(passwordEncoder.matches("rawPass", "encodedPass")).thenReturn(true);
        when(passwordEncoder.matches("wrongPass", "encodedPass")).thenReturn(false);

        assertTrue(userService.verifyPassword("rawPass", "encodedPass"));
        assertFalse(userService.verifyPassword("wrongPass", "encodedPass"));
    }
}

