package com.ghInternship.GHInternship2026.controller;

import com.ghInternship.GHInternship2026.config.SecurityConfig;
import com.ghInternship.GHInternship2026.config.WebConfig;
import com.ghInternship.GHInternship2026.dto.ChatMessageResponseDto;
import com.ghInternship.GHInternship2026.dto.ChatRequestDto;
import com.ghInternship.GHInternship2026.dto.ChatResponseDto;
import com.ghInternship.GHInternship2026.dto.ChatSessionResponseDto;
import com.ghInternship.GHInternship2026.exception.ChatSessionNotFoundException;
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
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
        ChatResponseDto expectedResponse = new ChatResponseDto(77L, "Mock AI response text", LocalDateTime.now());
        when(chatService.processMessage(any(ChatRequestDto.class), any())).thenReturn(expectedResponse);

        String json = """
                {
                    "message": "Explain Spring Security filter chain",
                    "chatSessionId": 77
                }
                """;

        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chatSessionId").value(77))
                .andExpect(jsonPath("$.response").value("Mock AI response text"))
                .andExpect(jsonPath("$.timestamp", notNullValue()));

        verify(chatService).processMessage(any(ChatRequestDto.class), eq("authorizedUser"));
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

    // =========================================================================
    // 5. Тесты эндпоинтов истории диалогов (History Endpoints: /sessions, /sessions/{sessionId})
    // =========================================================================

    @Test
    @DisplayName("GET /api/chat/sessions without authentication should return 401 Unauthorized")
    void shouldRejectGetSessionsWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/chat/sessions"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));

        verifyNoInteractions(chatService);
    }

    @Test
    @WithMockUser(username = "alice")
    @DisplayName("GET /api/chat/sessions with authorized user should return 200 OK and list of sessions")
    void shouldReturnUserSessionsWhenAuthorized() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        List<ChatSessionResponseDto> mockSessions = List.of(
                new ChatSessionResponseDto(1L, "Диалог 1", now, now, 2L, "Превью 1"),
                new ChatSessionResponseDto(2L, "Диалог 2", now, now, 5L, "Превью 2")
        );
        when(chatService.getUserChatSessions("alice")).thenReturn(mockSessions);

        mockMvc.perform(get("/api/chat/sessions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Диалог 1"))
                .andExpect(jsonPath("$[0].messageCount").value(2))
                .andExpect(jsonPath("$[0].lastMessagePreview").value("Превью 1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].title").value("Диалог 2"));

        verify(chatService).getUserChatSessions("alice");
    }

    @Test
    @DisplayName("GET /api/chat/sessions/{sessionId} without authentication should return 401 Unauthorized")
    void shouldRejectGetSessionMessagesWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/chat/sessions/42"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));

        verifyNoInteractions(chatService);
    }

    @Test
    @WithMockUser(username = "bob")
    @DisplayName("GET /api/chat/sessions/{sessionId} with authorized user should return 200 OK and messages")
    void shouldReturnSessionMessagesWhenAuthorized() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        List<ChatMessageResponseDto> mockMessages = List.of(
                new ChatMessageResponseDto(10L, 42L, "user", "Вопрос", now),
                new ChatMessageResponseDto(11L, 42L, "ai", "Ответ", now.plusSeconds(5))
        );
        when(chatService.getSessionMessages(42L, "bob")).thenReturn(mockMessages);

        mockMvc.perform(get("/api/chat/sessions/42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].sessionId").value(42))
                .andExpect(jsonPath("$[0].senderRole").value("user"))
                .andExpect(jsonPath("$[0].content").value("Вопрос"))
                .andExpect(jsonPath("$[1].id").value(11))
                .andExpect(jsonPath("$[1].senderRole").value("ai"))
                .andExpect(jsonPath("$[1].content").value("Ответ"));

        verify(chatService).getSessionMessages(42L, "bob");
    }

    @Test
    @WithMockUser(username = "charlie")
    @DisplayName("GET /api/chat/sessions/{sessionId} for non-existent or foreign session should return 404 Not Found")
    void shouldReturn404WhenSessionNotFoundOrForeign() throws Exception {
        when(chatService.getSessionMessages(999L, "charlie"))
                .thenThrow(new ChatSessionNotFoundException(999L, "charlie"));

        mockMvc.perform(get("/api/chat/sessions/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message", containsString("999")))
                .andExpect(jsonPath("$.message", containsString("charlie")));

        verify(chatService).getSessionMessages(999L, "charlie");
    }
}
