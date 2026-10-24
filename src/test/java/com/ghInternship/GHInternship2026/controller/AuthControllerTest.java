package com.ghInternship.GHInternship2026.controller;

import com.ghInternship.GHInternship2026.dto.UserRegistrationDto;
import com.ghInternship.GHInternship2026.entity.User;
import com.ghInternship.GHInternship2026.exception.GlobalExceptionHandler;
import com.ghInternship.GHInternship2026.exception.UserAlreadyExistsException;
import com.ghInternship.GHInternship2026.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Изолированные модульные тесты (Unit Tests) для {@link AuthController}.
 * Используют MockMvc (standaloneSetup) и мокируют {@link UserService} и {@link PasswordEncoder}.
 */
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // =========================================================================
    // POST /api/auth/register tests
    // =========================================================================

    @Test
    @DisplayName("POST /api/auth/register should successfully register user and return 201 Created")
    void register_ShouldReturn201_WhenDataIsValid() throws Exception {
        User registeredUser = new User(101L, "alice", "$2a$10$mockHashedPassword");
        when(userService.registerUser(any(UserRegistrationDto.class))).thenReturn(registeredUser);

        String requestJson = """
                {
                    "username": "alice",
                    "password": "ValidPassword123!"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(101))
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        verify(userService).registerUser(any(UserRegistrationDto.class));
    }

    @Test
    @DisplayName("POST /api/auth/register should return 409 Conflict when username is already taken")
    void register_ShouldReturn409_WhenUsernameAlreadyExists() throws Exception {
        when(userService.registerUser(any(UserRegistrationDto.class)))
                .thenThrow(new UserAlreadyExistsException("Username 'existingBob' is already taken"));

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
                .andExpect(jsonPath("$.message", containsString("Username 'existingBob' is already taken")));

        verify(userService).registerUser(any(UserRegistrationDto.class));
    }

    @Test
    @DisplayName("POST /api/auth/register should return 400 Bad Request when username is empty")
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

        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("POST /api/auth/register should return 400 Bad Request when username is blank whitespace")
    void register_ShouldReturn400_WhenUsernameIsBlankSpaces() throws Exception {
        String requestJson = """
                {
                    "username": "   ",
                    "password": "ValidPassword123!"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.username", notNullValue()));

        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("POST /api/auth/register should return 400 Bad Request when username is null/missing")
    void register_ShouldReturn400_WhenUsernameIsMissingOrNull() throws Exception {
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

        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("POST /api/auth/register should return 400 Bad Request when password is empty")
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

        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("POST /api/auth/register should return 400 Bad Request when password is blank whitespace")
    void register_ShouldReturn400_WhenPasswordIsBlankSpaces() throws Exception {
        String requestJson = """
                {
                    "username": "validUser",
                    "password": "   "
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.password", notNullValue()));

        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("POST /api/auth/register should return 400 Bad Request when password is null/missing")
    void register_ShouldReturn400_WhenPasswordIsMissingOrNull() throws Exception {
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

        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("POST /api/auth/register should return 400 Bad Request when both fields are empty")
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

        verifyNoInteractions(userService);
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

        verifyNoInteractions(userService);
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

        verifyNoInteractions(userService);
    }

    // =========================================================================
    // POST /api/auth/login tests
    // =========================================================================

    @Test
    @DisplayName("POST /api/auth/login should return 200 OK when credentials are valid")
    void login_ShouldReturn200_WhenCredentialsAreValid() throws Exception {
        User existingUser = new User(1L, "carol", "$2a$10$storedHashValue");
        when(userService.findByUsername("carol")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("CarolPassword123!", "$2a$10$storedHashValue")).thenReturn(true);

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

        verify(userService).findByUsername("carol");
        verify(passwordEncoder).matches("CarolPassword123!", "$2a$10$storedHashValue");
    }

    @Test
    @DisplayName("POST /api/auth/login should return 401 Unauthorized when password is incorrect")
    void login_ShouldReturn401_WhenPasswordIsIncorrect() throws Exception {
        User existingUser = new User(1L, "carol", "$2a$10$storedHashValue");
        when(userService.findByUsername("carol")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("WrongPassword!", "$2a$10$storedHashValue")).thenReturn(false);

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

        verify(userService).findByUsername("carol");
        verify(passwordEncoder).matches("WrongPassword!", "$2a$10$storedHashValue");
    }

    @Test
    @DisplayName("POST /api/auth/login should return 401 Unauthorized when username does not exist")
    void login_ShouldReturn401_WhenUsernameDoesNotExist() throws Exception {
        when(userService.findByUsername("unknownUser")).thenReturn(Optional.empty());

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

        verify(userService).findByUsername("unknownUser");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("POST /api/auth/login should return 400 Bad Request when credentials input is invalid")
    void login_ShouldReturn400_WhenInputIsInvalid() throws Exception {
        String invalidRequestJson = """
                {
                    "username": "   ",
                    "password": ""
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.validationErrors.username", notNullValue()))
                .andExpect(jsonPath("$.validationErrors.password", notNullValue()));

        verifyNoInteractions(userService);
        verifyNoInteractions(passwordEncoder);
    }
}
