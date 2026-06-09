package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.url}")
    private String apiUrl;

    @PostMapping
    public ResponseEntity<?> chat(@RequestBody Map<String, String> request) {
        String userMessage = request.get("message");
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "model", "gpt-3.5-turbo",
                "messages", List.of(Map.of("role", "user", "content", userMessage)),
                "max_tokens", 150
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        try {
            ResponseEntity<Map> aiResponse = restTemplate.postForEntity(apiUrl, entity, Map.class);
            return ResponseEntity.ok(aiResponse.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "AI service unavailable: " + e.getMessage()));
        }
    }
}