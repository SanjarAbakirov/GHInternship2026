package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User registerUser(String username, String email, String password) {
        if (userRepository.existsByUsername(username)) {
            log.warn("Registration failed: username already exists - {}", username);
            throw new RuntimeException("Username already exists!");
        }
        if (userRepository.existsByEmail(email)) {
            log.warn("Registration failed: email already exists - {}", email);
            throw new RuntimeException("Email already exists!");
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
            throw new RuntimeException("User not found!");
        }
        User user = userOptional.get();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("Authentication failed: invalid password for user {}", username);
            throw new RuntimeException("Invalid password!");
        }
        log.info("User authenticated: {}", username);
        return user;
    }
}