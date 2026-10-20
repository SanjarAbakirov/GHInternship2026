package com.ghInternship.GHInternship2026.service;

import com.ghInternship.GHInternship2026.dto.UserRegistrationDto;
import com.ghInternship.GHInternship2026.entity.User;
import com.ghInternship.GHInternship2026.exception.UserAlreadyExistsException;
import com.ghInternship.GHInternship2026.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Реализация сервиса управления пользователями {@link UserService}.
 * Обеспечивает хэширование паролей, валидацию уникальности логина и сохранение в БД.
 */
@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User registerUser(UserRegistrationDto userData) {
        if (userData == null) {
            log.warn("Registration failed: userData is null");
            throw new IllegalArgumentException("User registration data must not be null");
        }
        return registerUser(userData.username(), userData.password());
    }

    @Override
    public User registerUser(String username, String rawPassword) {
        log.info("Attempting to register user: {}", username);

        if (username == null || username.isBlank()) {
            log.warn("Registration failed: username is blank or null");
            throw new IllegalArgumentException("Username must not be blank");
        }

        if (rawPassword == null || rawPassword.isBlank()) {
            log.warn("Registration failed for user '{}': rawPassword is blank or null", username);
            throw new IllegalArgumentException("Password must not be blank");
        }

        if (userRepository.existsByUsername(username)) {
            log.warn("Registration failed: username '{}' is already taken", username);
            throw new UserAlreadyExistsException("Username '" + username + "' is already taken");
        }

        String encodedPassword = passwordEncoder.encode(rawPassword);
        User user = new User(username, encodedPassword);
        User savedUser = userRepository.save(user);

        log.info("User '{}' successfully registered with id: {}", username, savedUser.getId());
        return savedUser;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        log.info("Searching for user with username: {}", username);
        return userRepository.findByUsername(username);
    }

    @Override
    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
