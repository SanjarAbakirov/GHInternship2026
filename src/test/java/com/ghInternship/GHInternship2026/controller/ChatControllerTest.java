package com.ghInternship.GHInternship2026.controller;

import com.ghInternship.GHInternship2026.config.SecurityConfig;
import com.ghInternship.GHInternship2026.config.WebConfig;
import com.ghInternship.GHInternship2026.dto.ChatRequestDto;
import com.ghInternship.GHInternship2026.dto.ChatResponseDto;
import com.ghInternship.GHInternship2026.service.ChatService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Изолированные срезовые тесты (Slice Tests) для {@link ChatController}.
 * Запускаются в веб-срезе через {@link WebMvcTest} с полноценной интеграцией со Spring Security.
 * Сервисный слой {@link ChatService} замокирован через {@link MockitoBean} (официальный аналог @MockBean в Spring Boot 4.0).
 */
@WebMvcTest(controllers = ChatController.class)
@Import({SecurityConfig.class, WebConfig.class})
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChatService chatService;

    // =========================================================================
    // 1. Тесты для неаутентифицированных запросов (Unauthorized User)
    // =========================================================================

    @Test
    @DisplayName("POST /api/chat without authentication should return 401 Unauthorized")
    void shouldRejectUnauthenticatedRequestWith401() throws Exception {
        String json = """
                {
                    "message": "Hello from unauthorized user!"
                }
                """;

        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message", containsString("Full authentication is required")))
                .andExpect(jsonPath("$.path").value("/api/chat"))
                .andExpect(jsonPath("$.timestamp", notNullValue()));

        verifyNoInteractions(chatService);
    }

    // =========================================================================
    // 2. Тесты для аутентифицированных запросов (Authorized User - Happy Path)
    // =========================================================================

    @Test
    @WithMockUser(username = "authorizedUser")
    @DisplayName("POST /api/chat with authorized user should return 200 OK and ChatResponseDto")
    void shouldReturn200_WhenAuthorizedAndValidMessage() throws Exception {
        ChatResponseDto expectedResponse = new ChatResponseDto("Mock AI response text", LocalDateTime.now());
        when(chatService.processMessage(any(ChatRequestDto.class))).thenReturn(expectedResponse);

        String json = """
                {
                    "message": "Explain Spring Security filter chain"
                }
                """;

        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("Mock AI response text"))
                .andExpect(jsonPath("$.timestamp", notNullValue()));

        verify(chatService).processMessage(any(ChatRequestDto.class));
    }

    // =========================================================================
    // 3. Тесты ошибок валидации для аутентифицированных запросов (Error Cases - 400 Bad Request)
    // =========================================================================

    @Test
    @WithMockUser(username = "authorizedUser")
    @DisplayName("POST /api/chat with blank message should return 400 Bad Request")
    void shouldReturn400_WhenMessageIsBlank() throws Exception {
        String json = """
                {
                    "message": "    "
                }
                """;

        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.validationErrors.message", notNullValue()));

        verifyNoInteractions(chatService);
    }

    @Test
    @WithMockUser(username = "authorizedUser")
    @DisplayName("POST /api/chat with null message should return 400 Bad Request")
    void shouldReturn400_WhenMessageIsNull() throws Exception {
        String json = """
                {
                    "message": null
                }
                """;

        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.message", notNullValue()));

        verifyNoInteractions(chatService);
    }

    @Test
    @WithMockUser(username = "authorizedUser")
    @DisplayName("POST /api/chat with message exceeding 2000 chars should return 400 Bad Request")
    void shouldReturn400_WhenMessageExceedsMaxLength() throws Exception {
        String longMessage = "a".repeat(2001);
        String json = """
                {
                    "message": "%s"
                }
                """.formatted(longMessage);

        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.message", notNullValue()));

        verifyNoInteractions(chatService);
    }

    @Test
    @WithMockUser(username = "authorizedUser")
    @DisplayName("POST /api/chat with empty request body should return 400 Bad Request")
    void shouldReturn400_WhenBodyIsMissing() throws Exception {
        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("Required request body is missing")));

        verifyNoInteractions(chatService);
    }

    // =========================================================================
    // 4. Проверка работы CORS preflight без аутентификации
    // =========================================================================

    @Test
    @DisplayName("OPTIONS /api/chat should allow CORS preflight request without authentication")
    void shouldAllowCorsPreflightWithoutAuth() throws Exception {
        mockMvc.perform(options("/api/chat")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "Content-Type,Authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }
}
