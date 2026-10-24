package com.ghInternship.GHInternship2026.controller;

import com.ghInternship.GHInternship2026.dto.UserLoginDto;
import com.ghInternship.GHInternship2026.dto.UserRegistrationDto;
import com.ghInternship.GHInternship2026.entity.User;
import com.ghInternship.GHInternship2026.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Сквозные интеграционные тесты для эндпоинтов аутентификации {@link AuthController}.
 * Запускаются с полным контекстом Spring Boot и встроенной базой данных H2.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    // =========================================================================
    // POST /api/auth/register Integration Tests
    // =========================================================================

    @Test
    @DisplayName("POST /api/auth/register should successfully register a new user and return 201 Created")
    void register_ShouldReturn201_WhenDataIsValid() throws Exception {
        String requestJson = """
                {
                    "username": "alice",
                    "password": "Password123!"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        // Проверяем сохранение в базе и хэширование пароля
        User savedUser = userRepository.findByUsername("alice").orElseThrow();
        assertThat(savedUser.getPasswordHash()).isNotEqualTo("Password123!");
        assertThat(passwordEncoder.matches("Password123!", savedUser.getPasswordHash())).isTrue();
    }

    @Test
    @DisplayName("POST /api/auth/register should return 409 Conflict when username is already taken")
    void register_ShouldReturn409_WhenUsernameAlreadyExists() throws Exception {
        // Создаем существующего пользователя
        userRepository.save(new User("existingBob", passwordEncoder.encode("Secret123!")));

        String requestJson = """
                {
                    "username": "existingBob",
                    "password": "Password123!"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message", containsString("already taken")));
    }

    @Test
    @DisplayName("POST /api/auth/register should return 400 Bad Request when username is empty string")
    void register_ShouldReturn400_WhenUsernameIsEmpty() throws Exception {
        String requestJson = """
                {
                    "username": "",
                    "password": "ValidPassword123!"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.validationErrors.username", notNullValue()));
    }

    @Test
    @DisplayName("POST /api/auth/register should return 400 Bad Request when username contains only whitespace")
    void register_ShouldReturn400_WhenUsernameIsBlankSpaces() throws Exception {
        String requestJson = """
                {
                    "username": "     ",
                    "password": "ValidPassword123!"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.username", notNullValue()));
    }

    @Test
    @DisplayName("POST /api/auth/register should return 400 Bad Request when username is null/missing")
    void register_ShouldReturn400_WhenUsernameIsMissing() throws Exception {
        String requestJson = """
                {
                    "password": "ValidPassword123!"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.username", notNullValue()));
    }

    @Test
    @DisplayName("POST /api/auth/register should return 400 Bad Request when password is empty string")
    void register_ShouldReturn400_WhenPasswordIsEmpty() throws Exception {
        String requestJson = """
                {
                    "username": "validUser",
                    "password": ""
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.password", notNullValue()));
    }

    @Test
    @DisplayName("POST /api/auth/register should return 400 Bad Request when password contains only whitespace")
    void register_ShouldReturn400_WhenPasswordIsBlankSpaces() throws Exception {
        String requestJson = """
                {
                    "username": "validUser",
                    "password": "      "
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.password", notNullValue()));
    }

    @Test
    @DisplayName("POST /api/auth/register should return 400 Bad Request when password is null/missing")
    void register_ShouldReturn400_WhenPasswordIsMissing() throws Exception {
        String requestJson = """
                {
                    "username": "validUser"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.password", notNullValue()));
    }

    @Test
    @DisplayName("POST /api/auth/register should return 400 Bad Request when both username and password are empty")
    void register_ShouldReturn400_WhenBothFieldsAreEmpty() throws Exception {
        String requestJson = """
                {
                    "username": "",
                    "password": ""
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.username", notNullValue()))
                .andExpect(jsonPath("$.validationErrors.password", notNullValue()));
    }

    @Test
    @DisplayName("POST /api/auth/register should return 400 Bad Request when body is empty JSON object")
    void register_ShouldReturn400_WhenRequestBodyIsEmptyJson() throws Exception {
        String requestJson = "{}";

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.username", notNullValue()))
                .andExpect(jsonPath("$.validationErrors.password", notNullValue()));
    }

    @Test
    @DisplayName("POST /api/auth/register should return 400 Bad Request when request body is completely missing")
    void register_ShouldReturn400_WhenRequestBodyIsCompletelyMissing() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message", containsString("missing or invalid")));
    }

    // =========================================================================
    // POST /api/auth/login Integration Tests
    // =========================================================================

    @Test
    @DisplayName("POST /api/auth/login should return 200 OK when credentials are valid")
    void login_ShouldReturn200_WhenCredentialsAreValid() throws Exception {
        userRepository.save(new User("carol", passwordEncoder.encode("CarolPassword123!")));

        String requestJson = """
                {
                    "username": "carol",
                    "password": "CarolPassword123!"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login successful"));
    }

    @Test
    @DisplayName("POST /api/auth/login should return 401 Unauthorized when password is incorrect")
    void login_ShouldReturn401_WhenPasswordIsIncorrect() throws Exception {
        userRepository.save(new User("carol", passwordEncoder.encode("CarolPassword123!")));

        String requestJson = """
                {
                    "username": "carol",
                    "password": "WrongPassword!"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    @Test
    @DisplayName("POST /api/auth/login should return 401 Unauthorized when user does not exist")
    void login_ShouldReturn401_WhenUserNotFound() throws Exception {
        String requestJson = """
                {
                    "username": "unknownUser",
                    "password": "AnyPassword123!"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    @Test
    @DisplayName("POST /api/auth/login should return 400 Bad Request when validation fails")
    void login_ShouldReturn400_WhenValidationFails() throws Exception {
        String requestJson = """
                {
                    "username": "   ",
                    "password": ""
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.validationErrors.username", notNullValue()))
                .andExpect(jsonPath("$.validationErrors.password", notNullValue()));
    }

    // =========================================================================
    // CORS Preflight Test
    // =========================================================================

    @Test
    @DisplayName("OPTIONS /api/auth/register should allow CORS preflight request from React frontend")
    void shouldAllowCorsPreflightFromReactFrontend() throws Exception {
        mockMvc.perform(options("/api/auth/register")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }
}
