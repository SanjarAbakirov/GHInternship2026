package com.example.demo.service;

import com.example.demo.dto.UserDTO;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.ArrayList;

@Service
public class UserService {

    public List<UserDTO> getAllUsers() {
        return new ArrayList<>(); // временная заглушка
    }

    public UserDTO getUserById(Long id) {
        UserDTO user = new UserDTO();
        user.setId(id);
//        user.setName("Test User");
        return user; // временная заглушка
    }

    public UserDTO createUser(UserDTO userDTO) {
        return userDTO; // временная заглушка
    }
}


// demonstrational class
//package com.example.demo.service;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//public class UserSecurityService {
//
//    private final PasswordEncoder passwordEncoder;
//
//    /**
//     * Хеширует пароль перед сохранением в БД
//     */
//    public String hashPassword(String rawPassword) {
//        return passwordEncoder.encode(rawPassword);
//    }
//
//    /**
//     * Проверяет соответствие пароля его хешу
//     */
//    public boolean verifyPassword(String rawPassword, String encodedPassword) {
//        return passwordEncoder.matches(rawPassword, encodedPassword);
//    }
//}
