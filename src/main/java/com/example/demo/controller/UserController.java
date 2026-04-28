package com.example.demo.controller;

import com.example.demo.entity.UserEntity;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
public class UserController {

    @GetMapping("/list")      // → GET /api/list
    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }

    //adding new user
    //POST http://localhost:8080/api
    @PostMapping("/add")      // → POST /api/add
    public UserEntity createUser(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam(required = false) String telephone) {
        UserEntity user = new UserEntity(name, email, telephone);
        return userRepository.save(user);
    }


    //Delete user
    // Delete http://localhost:8080/api/users/1
    @DeleteMapping("/{id}")   // → DELETE /api/{id}
    public String deleteUser(@PathVariable Long id) {
        if (userRepository.existsById(id)){
            userRepository.deleteById(id);
            return "User deleted with id: " + id;
        } else {
            return "User with id " + id + " not found!";
        }
    }

    @Autowired
    private UserRepository userRepository;
}
