package com.example.demo.controller;

import com.example.demo.entity.UserEntity;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/test")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    //Get all users
    //Get http://localhost:8080/api/users

    @PostMapping
    public List<UserEntity> getAllUsers(){
        return userRepository.findAll();
    }

    //adding new user
    //POST http://localhost:8080/api/users?name=John&email=john@mail.com&phone=0444990099
    @PostMapping
    public UserEntity createUser(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam(required = false) String telephone){
        UserEntity user = new UserEntity(name, email, telephone);
        return userRepository.save(user);
    }





}