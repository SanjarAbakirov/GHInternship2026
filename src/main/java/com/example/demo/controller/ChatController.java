package com.example.demo.controller;

import com.example.demo.dto.ChatMessageResponse;
import com.example.demo.dto.ChatRequest;
import com.example.demo.dto.ChatResponse;
import com.example.demo.dto.ChatSessionResponse;
import com.example.demo.service.ChatService;
import jakarta.validation.Valid;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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

    @PostMapping
    public ResponseEntity<ChatResponse> chat(
            @Valid @RequestBody ChatRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        log.info("Received chat message from {} (session={}, length={})",
                username, request.getChatSessionId(), request.getMessage().length());
        ChatResponse response = chatService.chat(username, request.getMessage(), request.getChatSessionId());
        log.debug("AI reply generated for session {}", response.getChatSessionId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<ChatSessionResponse>> listSessions(Authentication authentication) {
        String username = authentication.getName();
        log.info("Listing chat sessions for {}", username);
        return ResponseEntity.ok(chatService.listSessions(username));
    }

    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<List<ChatMessageResponse>> getSessionMessages(
            @PathVariable Long sessionId,
            Authentication authentication) {
        String username = authentication.getName();
        log.info("Fetching messages for session {} by {}", sessionId, username);
        return ResponseEntity.ok(chatService.getSessionMessages(username, sessionId));
    }
}
