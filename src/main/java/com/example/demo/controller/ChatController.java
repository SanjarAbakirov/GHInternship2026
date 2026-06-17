package com.example.demo.controller;
import com.example.demo.dto.ChatRequest;
import com.example.demo.dto.ChatResponse;
import com.example.demo.service.ChatService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        String aiReply = chatService.getChatReply(request.getMessage());
        return ResponseEntity.ok(new ChatResponse(aiReply));
    }
}
//logging - need to check out logging
// create log messages APi controllers - gpt
// lgo-messages - когда сервер выдает инфо о себе что происходит