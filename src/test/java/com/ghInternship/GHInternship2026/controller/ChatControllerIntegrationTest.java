package com.ghInternship.GHInternship2026.controller;

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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    @MockitoBean
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
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
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

    @Test
    @WithMockUser(username = "chatUser")
    @DisplayName("POST /api/chat with authenticated user should return 400 Bad Request when message is empty")
    void shouldReturn400_WhenMessageIsBlankAndAuthenticated() throws Exception {
        String requestJson = """
                {
                    "message": ""
                }
                """;

        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.message", notNullValue()));
    }

    @Test
    @DisplayName("Public endpoints like GET /hello and POST /api/auth/register should remain accessible without authentication")
    void publicEndpointsShouldRemainAccessible() throws Exception {
        mockMvc.perform(get("/hello"))
                .andExpect(status().isOk());

        String validRegisterJson = """
                {
                    "username": "publicUser",
                    "password": "Password123!"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRegisterJson))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("E2E Authorization Flow: Register -> Login -> Obtain Real JWT -> Call POST /api/chat -> 200 OK")
    void shouldAuthenticateWithRealJwtFromLoginAndCallChatEndpoint() throws Exception {
        // 1. Регистрация пользователя
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

        // 4. Отправка запроса на /api/chat с реальным заголовком Authorization: Bearer <jwt>
        String chatRequestJson = """
                {
                    "message": "Hello via real JWT!"
                }
                """;

        mockMvc.perform(post("/api/chat")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(chatRequestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("Authenticated via real JWT filter!"))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
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
