package com.example.demo.repository;

import com.example.demo.model.ChatSession;
import com.example.demo.model.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {

    List<ChatSession> findByUserOrderByCreatedAtDesc(User user);

    List<ChatSession> findByUserIdOrderByCreatedAtDesc(Long userId);
}
