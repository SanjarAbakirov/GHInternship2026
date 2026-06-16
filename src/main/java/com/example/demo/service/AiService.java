package com.example.demo.service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Map;

@Service
public class AiService {

//    @Value("${openai.api.key}")
//    private String apiKey;

    @Value("${openai.api.key:#{systemEnvironment['OPENAI_API_KEY']}}")
    private String apiKey;

    @Value("${openai.api.url}")
    private String apiUrl;

    public String getChatResponse(String userMessage) {
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
        ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, entity, Map.class);
        return response.getBody().get("choices").toString();
    }

}