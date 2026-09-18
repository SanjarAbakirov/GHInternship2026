package com.ghInternship.GHInternship2026.repository;

import com.ghInternship.GHInternship2026.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * UserRepository — provides CRUD operations for the User entity.
 * Spring Data JPA auto-generates the implementation at runtime.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Custom queries will be added here as needed
}
