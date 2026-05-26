package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User registerUser(String username, String email, String password) {
        // Проверка на существующего пользователя
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists!");
        }

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists!");
        }

        // Хеширование пароля
        String encodedPassword = passwordEncoder.encode(password);

        // Создание нового пользователя
        User user = new User(username, email, encodedPassword);

        // Сохранение в базу данных
        return userRepository.save(user);
    }

    public User authenticateUser(String username, String password) {
        // Поиск пользователя
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isEmpty()) {
            throw new RuntimeException("User not found!");
        }

        User user = userOptional.get();

        // Проверка пароля
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid password!");
        }

        return user;
    }
}