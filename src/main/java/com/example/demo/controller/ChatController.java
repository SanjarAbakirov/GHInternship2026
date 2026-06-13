package com.example.demo.controller;
import com.example.demo.service.AiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

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
        try {
            String reply = aiService.getChatResponse(userMessage);
            return ResponseEntity.ok(Map.of("reply", reply));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "AI service unavailable"));
        }
    }
}