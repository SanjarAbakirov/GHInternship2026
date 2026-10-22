package com.ghInternship.GHInternship2026.service;

import com.ghInternship.GHInternship2026.dto.UserRegistrationDto;
import com.ghInternship.GHInternship2026.entity.User;
import com.ghInternship.GHInternship2026.exception.UserAlreadyExistsException;
import com.ghInternship.GHInternship2026.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Модульные тесты для класса {@link UserServiceImpl}.
 * Используют чистый JUnit 5 и Mockito (@Mock, @InjectMocks) без поднятия контекста Spring.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    // --- registerUser(UserRegistrationDto) tests ---

    @Test
    @DisplayName("registerUser with DTO should hash password and save new user when username is unique")
    void registerUser_ShouldSuccessfullyRegisterUser_WhenUsernameIsAvailable() {
        // Arrange
        UserRegistrationDto registrationDto = new UserRegistrationDto("john_doe", "PlainPassword123!");
        String encodedHash = "$2a$10$hashedPasswordSample1234567890";

        when(userRepository.existsByUsername("john_doe")).thenReturn(false);
        when(passwordEncoder.encode("PlainPassword123!")).thenReturn(encodedHash);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        // Act
        User createdUser = userService.registerUser(registrationDto);

        // Assert
        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getId()).isEqualTo(1L);
        assertThat(createdUser.getUsername()).isEqualTo("john_doe");
        assertThat(createdUser.getPasswordHash()).isEqualTo(encodedHash);

        // Проверяем аргументы сохранения через ArgumentCaptor
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getUsername()).isEqualTo("john_doe");
        assertThat(capturedUser.getPasswordHash()).isEqualTo(encodedHash);

        verify(userRepository).existsByUsername("john_doe");
        verify(passwordEncoder).encode("PlainPassword123!");
    }

    @Test
    @DisplayName("registerUser with DTO should throw UserAlreadyExistsException when username already taken")
    void registerUser_ShouldThrowUserAlreadyExistsException_WhenUsernameAlreadyExists() {
        // Arrange
        UserRegistrationDto registrationDto = new UserRegistrationDto("existing_user", "Password123!");
        when(userRepository.existsByUsername("existing_user")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.registerUser(registrationDto))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("Username 'existing_user' is already taken");

        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("registerUser with null DTO should throw IllegalArgumentException")
    void registerUser_ShouldThrowIllegalArgumentException_WhenDtoIsNull() {
        assertThatThrownBy(() -> userService.registerUser((UserRegistrationDto) null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User registration data must not be null");

        verify(userRepository, never()).existsByUsername(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("registerUser should throw IllegalArgumentException when username or password is blank")
    void registerUser_ShouldThrowIllegalArgumentException_WhenCredentialsAreBlank() {
        UserRegistrationDto blankUsernameDto = new UserRegistrationDto("   ", "ValidPassword123!");
        assertThatThrownBy(() -> userService.registerUser(blankUsernameDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Username must not be blank");

        UserRegistrationDto blankPasswordDto = new UserRegistrationDto("valid_user", "");
        assertThatThrownBy(() -> userService.registerUser(blankPasswordDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Password must not be blank");

        verify(userRepository, never()).save(any());
    }

    // --- findByUsername(String) tests ---

    @Test
    @DisplayName("findByUsername should return Optional with user when user exists in repository")
    void findByUsername_ShouldReturnUser_WhenUserExists() {
        // Arrange
        User expectedUser = new User(10L, "existing_user", "$2a$10$someHashedPassword");
        when(userRepository.findByUsername("existing_user")).thenReturn(Optional.of(expectedUser));

        // Act
        Optional<User> result = userService.findByUsername("existing_user");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(expectedUser);
        assertThat(result.get().getId()).isEqualTo(10L);
        assertThat(result.get().getUsername()).isEqualTo("existing_user");
        verify(userRepository).findByUsername("existing_user");
    }

    @Test
    @DisplayName("findByUsername should return Optional.empty when user does not exist in repository")
    void findByUsername_ShouldReturnEmptyOptional_WhenUserDoesNotExist() {
        // Arrange
        when(userRepository.findByUsername("nonexistent_user")).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userService.findByUsername("nonexistent_user");

        // Assert
        assertThat(result).isEmpty();
        verify(userRepository).findByUsername("nonexistent_user");
    }

    // --- verifyPassword(String, String) tests ---

    @Test
    @DisplayName("verifyPassword should return true when plain password matches encoded hash")
    void verifyPassword_ShouldReturnTrue_WhenPasswordsMatch() {
        when(passwordEncoder.matches("myPassword", "$2a$10$encodedHash")).thenReturn(true);

        boolean matches = userService.verifyPassword("myPassword", "$2a$10$encodedHash");

        assertThat(matches).isTrue();
        verify(passwordEncoder).matches("myPassword", "$2a$10$encodedHash");
    }

    @Test
    @DisplayName("verifyPassword should return false when plain password does not match encoded hash")
    void verifyPassword_ShouldReturnFalse_WhenPasswordsDoNotMatch() {
        when(passwordEncoder.matches("wrongPassword", "$2a$10$encodedHash")).thenReturn(false);

        boolean matches = userService.verifyPassword("wrongPassword", "$2a$10$encodedHash");

        assertThat(matches).isFalse();
        verify(passwordEncoder).matches("wrongPassword", "$2a$10$encodedHash");
    }
}
