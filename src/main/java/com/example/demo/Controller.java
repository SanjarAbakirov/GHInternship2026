package com.example.demo;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class Controller {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/")
    public String sayHello() {
        return "Security is configured!";
    }

    // Временный эндпоинт для проверки пользователей в БД
    @GetMapping("/api/users")
    public List<String> getUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(user -> "ID: " + user.getId() +
                        ", Username: " + user.getUsername() +
                        ", Email: " + user.getEmail() +
                        ", Password (hashed): " + user.getPassword().substring(0, 20) + "...")
                .collect(Collectors.toList());
    }
}