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

    @PostMapping("/add")
    public UserEntity addData(@RequestParam String name) {
        UserEntity entity = new UserEntity(name);
        return userRepository.save(entity);
    }

    @GetMapping("/all")
    public List<UserEntity>getAll(){
        return userRepository.findAll();
    }
}