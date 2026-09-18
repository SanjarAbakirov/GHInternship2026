package com.ghInternship.GHInternship2026.repository;

import com.ghInternship.GHInternship2026.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * UserRepository — data access layer for the User entity.
 *
 * By extending JpaRepository<User, Long>, we get the following
 * CRUD methods for FREE (Spring Data JPA generates them at runtime):
 *
 *   save(User)          → INSERT or UPDATE
 *   findById(Long)      → SELECT WHERE id = ?
 *   findAll()           → SELECT * FROM users
 *   deleteById(Long)    → DELETE WHERE id = ?
 *   count()             → SELECT COUNT(*) FROM users
 *   existsById(Long)    → checks if record exists
 *
 * No implementation needed — Spring generates everything automatically.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by their unique username.
     * Spring Data JPA generates the query: SELECT * FROM users WHERE username = ?
     *
     * @param username the login name to search for
     * @return Optional<User> — present if found, empty if not
     */
    Optional<User> findByUsername(String username);
}
