package com.example.demo;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
@Entity
@Data                    // Генерирует все геттеры, сеттеры, toString, equals, hashCode

public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    //constructor, getter, setter

    // empty constructor with fields
    public User(){
    }

    // Constructor with fields
    public User(String name){
        this.name = name;
    }
    public String getName() {
        return name;
    }
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }
}

