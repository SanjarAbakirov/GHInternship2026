package com.example.demo.controller;

import com.example.demo.service.AiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final AiService aiService;
    public ChatController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping
    public ResponseEntity<?> chat(@RequestBody Map<String, String> request) {
        String userMessage = request.get("message");
        log.info("Chat request: {}", userMessage);
        try {
            String reply = aiService.getChatResponse(userMessage);
            log.debug("AI reply: {}", reply);
            return ResponseEntity.ok(Map.of("reply", reply));
        } catch (Exception e) {
            log.error("Error while calling AI service", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "AI service unavailable"));
        }
    }
}

//logging - need to check out logging
// create log messages APi controllers - gpt
// lgo-messages - когда сервер выдает инфо о себе что происходит