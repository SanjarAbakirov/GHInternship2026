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