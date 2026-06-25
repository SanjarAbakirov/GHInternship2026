package com.example.demo.controller;

import com.example.demo.dto.ChatRequest;
import com.example.demo.dto.ChatResponse;
import com.example.demo.service.ChatService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

//@Slf4j
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);
    private final ChatService chatService;
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

//    private final AiService aiService;
//    public ChatController(AiService aiService) {
//        this.aiService = aiService;
//    }

//    @PostMapping
//    public ResponseEntity<?> chat(@RequestBody Map<String, String> request) {
//        String userMessage = request.get("message");
//        log.info("Chat request: {}", userMessage);
//        try {
//            String reply = aiService.getChatResponse(userMessage);
//            log.debug("AI reply: {}", reply);
//            return ResponseEntity.ok(Map.of("reply", reply));
//        } catch (Exception e) {
//            log.error("Error while calling AI service", e);
//            return ResponseEntity.internalServerError()
//                    .body(Map.of("error", "AI service unavailable"));
//        }
//    }
    @PostMapping
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        log.info("Received chat message: {}", request.getMessage());
        String aiReply = chatService.getChatReply(request.getMessage());
        log.debug("AI reply: {}", aiReply);
        return ResponseEntity.ok(new ChatResponse(aiReply));
    }
}

//logging - need to check out logging
// create log messages APi controllers - gpt
// lgo-messages - когда сервер выдает инфо о себе что происходит