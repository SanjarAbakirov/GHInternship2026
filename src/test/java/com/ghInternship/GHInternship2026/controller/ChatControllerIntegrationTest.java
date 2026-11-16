package com.ghInternship.GHInternship2026.controller;

import com.ghInternship.GHInternship2026.entity.ChatMessage;
import com.ghInternship.GHInternship2026.entity.ChatSession;
import com.ghInternship.GHInternship2026.repository.ChatMessageRepository;
import com.ghInternship.GHInternship2026.repository.ChatSessionRepository;
import com.ghInternship.GHInternship2026.repository.UserRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ChatControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatSessionRepository chatSessionRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @MockitoBean
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        chatMessageRepository.deleteAll();
        chatSessionRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/chat without authentication should be rejected with 401 Unauthorized")
    void shouldRejectUnauthenticatedRequestWith401() throws Exception {
        String requestJson = """
                {
                    "message": "Hello unauthenticated!"
                }
                """;

        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message", containsString("Full authentication is required")))
                .andExpect(jsonPath("$.path").value("/api/chat"))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    @WithMockUser(username = "chatUser")
    @DisplayName("POST /api/chat with authenticated user should process message and return 200 OK")
    void shouldProcessMessageWhenAuthenticated() throws Exception {
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok("AI response to: Hello from Authenticated User!"));

        String requestJson = """
                {
                    "message": "Hello from Authenticated User!"
                }
                """;

        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response", notNullValue()))
                .andExpect(jsonPath("$.response", containsString("Hello from Authenticated User!")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    @DisplayName("OPTIONS /api/chat should allow CORS preflight request without authentication from React frontend")
    void shouldAllowCorsPreflightForChatEndpointWithoutAuth() throws Exception {
        mockMvc.perform(options("/api/chat")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "Content-Type,Authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"))
                .andExpect(header().string("Access-Control-Allow-Methods", containsString("POST")))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

    @Test
    @DisplayName("End-to-End JWT Auth & Persistence: Register -> Login -> Send Message -> Persist Session and Messages")
    void shouldProcessMessageWithRealJwtFilterAndReachAiApi() throws Exception {
        // 1. Регистрация нового пользователя
        String registerJson = """
                {
                    "username": "jwtE2eUser",
                    "password": "StrongPassword123!"
                }
                """;
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isCreated());

        // 2. Вход в систему и получение реального JWT-токена
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andReturn();

        String jwtToken = JsonPath.read(loginResult.getResponse().getContentAsString(), "$.token");

        // 3. Мокируем внешний вызов AI API
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok("""
                        {
                            "choices": [
                                {
                                    "message": {
                                        "content": "Authenticated via real JWT filter!"
                                    }
                                }
                            ]
                        }
                        """));

        // 4. Отправка первого сообщения (без sessionId) — должна создаться новая сессия
        String chatRequestJson1 = """
                {
                    "message": "Hello via real JWT!"
                }
                """;

        MvcResult chatResult1 = mockMvc.perform(post("/api/chat")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(chatRequestJson1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chatSessionId", notNullValue()))
                .andExpect(jsonPath("$.response").value("Authenticated via real JWT filter!"))
                .andExpect(jsonPath("$.timestamp", notNullValue()))
                .andReturn();

        Number sessionIdNum = JsonPath.read(chatResult1.getResponse().getContentAsString(), "$.chatSessionId");
        Long sessionId = sessionIdNum.longValue();

        // Проверяем персистентность в БД
        assertThat(chatSessionRepository.findById(sessionId)).isPresent();
        ChatSession session = chatSessionRepository.findByIdWithUser(sessionId).orElseThrow();
        assertThat(session.getTitle()).isEqualTo("Hello via real JWT!");
        assertThat(session.getUser().getUsername()).isEqualTo("jwtE2eUser");

        List<ChatMessage> messages = chatMessageRepository.findBySessionOrderByTimestampAsc(session);
        assertThat(messages).hasSize(2);
        assertThat(messages.get(0).getSenderRole()).isEqualTo("user");
        assertThat(messages.get(0).getContent()).isEqualTo("Hello via real JWT!");
        assertThat(messages.get(1).getSenderRole()).isEqualTo("ai");
        assertThat(messages.get(1).getContent()).isEqualTo("Authenticated via real JWT filter!");

        // 5. Отправка второго сообщения с указанием chatSessionId — должно добавиться в ту же сессию
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok("""
                        {
                            "choices": [
                                {
                                    "message": {
                                        "content": "Second response in same session!"
                                    }
                                }
                            ]
                        }
                        """));

        String chatRequestJson2 = String.format("""
                {
                    "message": "Continuing conversation",
                    "chatSessionId": %d
                }
                """, sessionId);

        mockMvc.perform(post("/api/chat")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(chatRequestJson2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chatSessionId").value(sessionId))
                .andExpect(jsonPath("$.response").value("Second response in same session!"));

        // Проверяем, что в сессии теперь 4 сообщения
        List<ChatMessage> updatedMessages = chatMessageRepository.findBySessionOrderByTimestampAsc(session);
        assertThat(updatedMessages).hasSize(4);
        assertThat(updatedMessages.get(2).getContent()).isEqualTo("Continuing conversation");
        assertThat(updatedMessages.get(3).getContent()).isEqualTo("Second response in same session!");
    }

    @Test
    @DisplayName("Tenant Isolation: sending message to another user's session should return 404 Not Found")
    void shouldRejectAccessToOtherUsersChatSessionWith404() throws Exception {
        // Создаем пользователя А и его сессию через регистрацию/логин/чат
        String regA = """
                {"username":"userAlpha","password":"Password123!"}
                """;
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(regA))
                .andExpect(status().isCreated());
        MvcResult logA = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(regA))
                .andExpect(status().isOk()).andReturn();
        String tokenA = JsonPath.read(logA.getResponse().getContentAsString(), "$.token");

        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok("Response A"));

        MvcResult chatA = mockMvc.perform(post("/api/chat")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"User A first message\"}"))
                .andExpect(status().isOk()).andReturn();
        Number sessionNumA = JsonPath.read(chatA.getResponse().getContentAsString(), "$.chatSessionId");
        Long sessionIdA = sessionNumA.longValue();

        // Создаем пользователя Б
        String regB = """
                {"username":"userBeta","password":"Password123!"}
                """;
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(regB))
                .andExpect(status().isCreated());
        MvcResult logB = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(regB))
                .andExpect(status().isOk()).andReturn();
        String tokenB = JsonPath.read(logB.getResponse().getContentAsString(), "$.token");

        // Пользователь Б пытается отправить сообщение в сессию пользователя А
        String attackJson = String.format("{\"message\":\"Hijack attempt\",\"chatSessionId\":%d}", sessionIdA);
        mockMvc.perform(post("/api/chat")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(attackJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message", containsString("not found for user: userBeta")));
    }

    @Test
    @DisplayName("POST /api/chat with invalid or forged Bearer JWT token should be rejected with 401 Unauthorized")
    void shouldRejectForgedJwtTokenWith401() throws Exception {
        String chatRequestJson = """
                {
                    "message": "Attempting unauthorized chat access"
                }
                """;

        mockMvc.perform(post("/api/chat")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer forged.invalid.jwt.token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(chatRequestJson))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.path").value("/api/chat"));

        verifyNoInteractions(restTemplate);
    }

    @Test
    @DisplayName("POST /api/chat with non-Bearer Authorization header should be rejected with 401 Unauthorized")
    void shouldRejectBasicAuthHeaderWith401() throws Exception {
        String chatRequestJson = """
                {
                    "message": "Attempting Basic auth"
                }
                """;

        mockMvc.perform(post("/api/chat")
                        .header(HttpHeaders.AUTHORIZATION, "Basic dXNlcjpwYXNz")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(chatRequestJson))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));

        verifyNoInteractions(restTemplate);
    }
}
