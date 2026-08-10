package com.example.demo.controller;

import com.example.demo.config.SecurityConfig;
import com.example.demo.dto.ChatResponse;
import com.example.demo.dto.ConversationDetailResponse;
import com.example.demo.dto.ConversationResponse;
import com.example.demo.dto.MessageResponse;
import com.example.demo.exception.ConversationNotFoundException;
import com.example.demo.exception.UnauthorizedConversationAccessException;
import com.example.demo.security.JwtAuthenticationFilter;
import com.example.demo.security.JwtUtil;
import com.example.demo.service.ChatService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
        Mockito.when(chatService.chat("testuser", "Hello AI!", null, null))
                .thenReturn(new ChatResponse("Mocked AI reply", 5L, "Hello AI!", true, LocalDateTime.of(2026, 1, 1, 10, 0)));

        String token = jwtUtil.generateToken("testuser");

        mockMvc.perform(post("/api/chat")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"Hello AI!\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reply").value("Mocked AI reply"))
                .andExpect(jsonPath("$.conversationId").value(5))
                .andExpect(jsonPath("$.conversationTitle").value("Hello AI!"))
                .andExpect(jsonPath("$.newConversation").value(true));
    }

    @Test
    void whenExistingConversationIdProvided_thenPassesItToService() throws Exception {
        Mockito.when(chatService.chat("testuser", "Follow up", 5L, null))
                .thenReturn(new ChatResponse("Next reply", 5L, "Existing chat", false, LocalDateTime.of(2026, 1, 1, 10, 0)));

        String token = jwtUtil.generateToken("testuser");

        mockMvc.perform(post("/api/chat")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"Follow up\",\"conversationId\":5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conversationId").value(5))
                .andExpect(jsonPath("$.newConversation").value(false))
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

    @Test
    void listSessions_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/chat/sessions"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listSessions_withValidToken_returnsSessions() throws Exception {
        Mockito.when(chatService.listSessions("testuser"))
                .thenReturn(List.of(new ConversationResponse(
                        3L,
                        "Hello chat",
                        "gpt-3.5-turbo",
                        LocalDateTime.of(2026, 1, 1, 10, 0),
                        LocalDateTime.of(2026, 1, 1, 10, 5),
                        2L,
                        "Hello chat")));

        String token = jwtUtil.generateToken("testuser");

        mockMvc.perform(get("/api/chat/sessions")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(3))
                .andExpect(jsonPath("$[0].title").value("Hello chat"))
                .andExpect(jsonPath("$[0].messageCount").value(2));
    }

    @Test
    void getSessionMessages_withValidToken_returnsConversationDetail() throws Exception {
        Mockito.when(chatService.getConversationDetail("testuser", 3L))
                .thenReturn(new ConversationDetailResponse(
                        3L,
                        "Hello chat",
                        "gpt-3.5-turbo",
                        LocalDateTime.of(2026, 1, 1, 10, 0),
                        LocalDateTime.of(2026, 1, 1, 10, 1),
                        List.of(
                                new MessageResponse(1L, "Hi", "user", LocalDateTime.of(2026, 1, 1, 10, 0)),
                                new MessageResponse(2L, "Hello", "ai", LocalDateTime.of(2026, 1, 1, 10, 1)))));

        String token = jwtUtil.generateToken("testuser");

        mockMvc.perform(get("/api/chat/sessions/3")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.messageCount").value(2))
                .andExpect(jsonPath("$.messages[0].content").value("Hi"))
                .andExpect(jsonPath("$.messages[0].role").value("user"))
                .andExpect(jsonPath("$.messages[1].content").value("Hello"))
                .andExpect(jsonPath("$.messages[1].role").value("ai"));
    }

    @Test
    void getSessionMessages_whenConversationMissing_returns404() throws Exception {
        Mockito.when(chatService.getConversationDetail("testuser", 99L))
                .thenThrow(new ConversationNotFoundException("Conversation not found: 99"));

        String token = jwtUtil.generateToken("testuser");

        mockMvc.perform(get("/api/chat/sessions/99")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void getSessionMessages_whenNotOwned_returns403() throws Exception {
        Mockito.when(chatService.getConversationDetail("testuser", 99L))
                .thenThrow(new UnauthorizedConversationAccessException(
                        "User testuser is not authorized to access conversation 99"));

        String token = jwtUtil.generateToken("testuser");

        mockMvc.perform(get("/api/chat/sessions/99")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void getSessionMessages_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/chat/sessions/3"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteSession_withValidToken_returns204() throws Exception {
        String token = jwtUtil.generateToken("testuser");

        mockMvc.perform(delete("/api/chat/sessions/3")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        Mockito.verify(chatService).deleteConversation("testuser", 3L);
    }

    @Test
    void deleteSession_whenConversationMissing_returns404() throws Exception {
        Mockito.doThrow(new ConversationNotFoundException("Conversation not found: 99"))
                .when(chatService).deleteConversation("testuser", 99L);

        String token = jwtUtil.generateToken("testuser");

        mockMvc.perform(delete("/api/chat/sessions/99")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteSession_whenNotOwned_returns403() throws Exception {
        Mockito.doThrow(new UnauthorizedConversationAccessException(
                        "User testuser is not authorized to access conversation 99"))
                .when(chatService).deleteConversation("testuser", 99L);

        String token = jwtUtil.generateToken("testuser");

        mockMvc.perform(delete("/api/chat/sessions/99")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteSession_withoutToken_returns401() throws Exception {
        mockMvc.perform(delete("/api/chat/sessions/3"))
                .andExpect(status().isUnauthorized());
    }
}
