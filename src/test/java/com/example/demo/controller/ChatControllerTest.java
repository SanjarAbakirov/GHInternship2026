package com.example.demo.controller;

import com.example.demo.config.SecurityConfig;
import com.example.demo.dto.ChatResponse;
import com.example.demo.security.JwtAuthenticationFilter;
import com.example.demo.security.JwtUtil;
import com.example.demo.service.ChatService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtUtil.class})
@TestPropertySource(properties = "jwt.secret=my-super-secret-key-for-tests-1234567890")
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @MockitoBean
    private ChatService chatService;

    @Test
    void whenNoTokenProvided_thenReturns401() throws Exception {
        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"Hello AI!\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void whenValidTokenProvided_thenReturns200WithReplyAndSessionId() throws Exception {
        Mockito.when(chatService.chat("testuser", "Hello AI!", null))
                .thenReturn(new ChatResponse("Mocked AI reply", 5L));

        String token = jwtUtil.generateToken("testuser");

        mockMvc.perform(post("/api/chat")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"Hello AI!\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reply").value("Mocked AI reply"))
                .andExpect(jsonPath("$.chatSessionId").value(5));
    }

    @Test
    void whenExistingSessionIdProvided_thenPassesItToService() throws Exception {
        Mockito.when(chatService.chat("testuser", "Follow up", 5L))
                .thenReturn(new ChatResponse("Next reply", 5L));

        String token = jwtUtil.generateToken("testuser");

        mockMvc.perform(post("/api/chat")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"Follow up\",\"chatSessionId\":5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chatSessionId").value(5))
                .andExpect(jsonPath("$.reply").value("Next reply"));
    }

    @Test
    void whenMessageIsBlank_thenReturns400() throws Exception {
        String token = jwtUtil.generateToken("testuser");

        mockMvc.perform(post("/api/chat")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"\"}"))
                .andExpect(status().isBadRequest());
    }
}
