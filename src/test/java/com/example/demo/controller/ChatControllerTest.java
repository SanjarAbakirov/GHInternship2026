package com.example.demo.controller;

import com.example.demo.config.SecurityConfig;
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
    void whenValidTokenProvided_thenReturns200WithReply() throws Exception {
        Mockito.when(chatService.getChatReply("Hello AI!")).thenReturn("Mocked AI reply");

        String token = jwtUtil.generateToken("testuser");

        mockMvc.perform(post("/api/chat")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"Hello AI!\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reply").value("Mocked AI reply"));
    }
}