package com.ghInternship.GHInternship2026.controller;

import com.ghInternship.GHInternship2026.dto.ChatRequestDto;
import com.ghInternship.GHInternship2026.dto.ChatResponseDto;
import com.ghInternship.GHInternship2026.exception.GlobalExceptionHandler;
import com.ghInternship.GHInternship2026.service.ChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ChatControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ChatService chatService;

    @InjectMocks
    private ChatController chatController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(chatController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/chat should return 200 OK and ChatResponseDto when valid message is provided")
    void shouldReturn200_WhenValidMessage() throws Exception {
        ChatResponseDto expectedResponse = new ChatResponseDto("Mock AI answer", LocalDateTime.now());
        when(chatService.processMessage(any(ChatRequestDto.class))).thenReturn(expectedResponse);

        String json = """
                {
                    "message": "Hello AI!"
                }
                """;

        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("Mock AI answer"))
                .andExpect(jsonPath("$.timestamp", notNullValue()));

        verify(chatService).processMessage(any(ChatRequestDto.class));
    }

    @Test
    @DisplayName("POST /api/chat should return 400 Bad Request when message is blank")
    void shouldReturn400_WhenMessageIsBlank() throws Exception {
        String json = """
                {
                    "message": "   "
                }
                """;

        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.message", notNullValue()));
    }

    @Test
    @DisplayName("POST /api/chat should return 400 Bad Request when body is missing")
    void shouldReturn400_WhenBodyIsMissing() throws Exception {
        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}

