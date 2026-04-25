package com.example.demo.repository;

import com.example.demo.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    //JPA gives all basic methods: save(), findAll(), findById(), delete(), count()

    //add methods
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByName(String name);
}