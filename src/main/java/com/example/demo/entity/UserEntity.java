package com.example.demo.entity;
import jakarta.persistence.*;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
@Table(name = "users_entity")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

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

    //getters and setters are needed to read and add data in database
    //for id
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id; // establish new id
    }
    //for name
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    //for email
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    //for phone
    public String getTelephone() {
        return telephone;
    }
    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }
}