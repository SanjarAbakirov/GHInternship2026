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

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "telephone")
    private String telephone;

    //construction of getters and setters
    public UserEntity(String name, String email, String telephone) {
        this.name = name;
        this.email = email;
        this.telephone = telephone;
        // id generates automatically
    }

    public UserEntity() {

    }


    //getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}


//@Table(name = "users_table")
//public class UserEntity {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//    private String name;
//    private String email;

//  ======  Constructors ======
//    public UserEntity(String name, String email) {
//        this.name = name;
//        this.email = email;
//    }


    // ======   Getter and Setter
//    public String getName() { return name; }
//    public void setName(String name) { this.name = name; }
//    public String getEmail() { return email; }
//    public void setEmail(String email) { this.email = email; }
