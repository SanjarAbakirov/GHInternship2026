package com.example.demo.controller;

import com.example.demo.dto.ChatRequest;
import com.example.demo.dto.ChatResponse;
import com.example.demo.dto.ConversationDetailResponse;
import com.example.demo.dto.ConversationResponse;
import com.example.demo.service.ChatService;
import jakarta.validation.Valid;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * Resolves the authenticated username from the security context. Spring Security's filter
     * chain ({@link com.example.demo.security.JwtAuthenticationFilter}) populates this before any
     * request reaches a controller method here, and every endpoint below requires authentication
     * (see {@code SecurityConfig}), so {@code authentication} is guaranteed non-null at this point.
     */
    private String getAuthenticatedUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    @PostMapping
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        String username = getAuthenticatedUsername();
        log.info("Received chat message from {} (conversation={}, length={})",
                username, request.getConversationId(), request.getMessage().length());
        ChatResponse response = chatService.chat(
                username, request.getMessage(), request.getConversationId(), request.getModelName());
        log.debug("AI reply generated for conversation {}", response.getConversationId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<ConversationResponse>> listSessions() {
        String username = getAuthenticatedUsername();
        log.info("Listing chat sessions for {}", username);
        return ResponseEntity.ok(chatService.listSessions(username));
    }

    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<ConversationDetailResponse> getSessionMessages(@PathVariable Long sessionId) {
        String username = getAuthenticatedUsername();
        log.info("Fetching conversation detail for session {} by {}", sessionId, username);
        return ResponseEntity.ok(chatService.getConversationDetail(username, sessionId));
    }

    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<Void> deleteSession(@PathVariable Long sessionId) {
        String username = getAuthenticatedUsername();
        log.info("Deleting conversation {} for {}", sessionId, username);
        chatService.deleteConversation(username, sessionId);
        return ResponseEntity.noContent().build();
    }
}
