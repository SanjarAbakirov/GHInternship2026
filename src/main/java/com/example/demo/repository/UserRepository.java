package com.example.demo.repository;

import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
}
// diff interface and class
//./mvnw spring-boot:run
//./mvnw clean install

// copy all with command - ask AI about (claud.ai) - ask curiously

//interface - working with db
