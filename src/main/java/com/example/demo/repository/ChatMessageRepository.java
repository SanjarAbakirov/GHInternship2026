package com.example.demo.repository;

import com.example.demo.model.ChatMessage;
import com.example.demo.model.ChatSession;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findBySessionOrderByCreatedAtAsc(ChatSession session);

    List<ChatMessage> findBySessionIdOrderByCreatedAtAsc(Long sessionId);
}
