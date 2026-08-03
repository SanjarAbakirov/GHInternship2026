package com.example.demo.service;

import com.example.demo.exception.AuthenticationException;
import com.example.demo.exception.DuplicateUserException;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    /** Generic message for both "user not found" and "wrong password" — avoids leaking which one it was. */
    private static final String INVALID_CREDENTIALS_MESSAGE = "Invalid username or password";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public User registerUser(String username, String email, String password) {
        if (userRepository.existsByUsername(username)) {
            log.warn("Registration failed: username already exists - {}", username);
            throw new DuplicateUserException("Username already exists!");
        }
        if (userRepository.existsByEmail(email)) {
            log.warn("Registration failed: email already exists - {}", email);
            throw new DuplicateUserException("Email already exists!");
        }
        String encodedPassword = passwordEncoder.encode(password);
        User user = new User(username, email, encodedPassword);
        User savedUser = userRepository.save(user);
        log.info("New user registered: {}", username);
        return savedUser;
    }

    public User authenticateUser(String username, String password) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            log.warn("Authentication failed: user not found - {}", username);
            throw new AuthenticationException(INVALID_CREDENTIALS_MESSAGE);
        }
        User user = userOptional.get();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("Authentication failed: invalid password for user {}", username);
            throw new AuthenticationException(INVALID_CREDENTIALS_MESSAGE);
        }
        log.info("User authenticated: {}", username);
        return user;
    }
}