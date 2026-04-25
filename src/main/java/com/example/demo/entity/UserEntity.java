package com.example.demo.entity;
import jakarta.persistence.*;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "users_entity")
public class UserEntity {
    // establish new id
    //getters and setters are needed to read and add data in database
    //for id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    //for name
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    //for email
    @Column(name = "email", unique = true, nullable = false)
    private String email;

    //for phone
    @Column(name = "phone")
    private String telephone;

    public UserEntity() { // empty constructor for JPA
    }


    //construction with parameters
    public UserEntity(String name, String email, String telephone) {
        this.name = name;
        this.email = email;
        this.telephone = telephone;
        // id generates automatically, no need to write
    }

}