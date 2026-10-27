package com.ghInternship.GHInternship2026.controller;

import com.ghInternship.GHInternship2026.dto.AuthResponseDto;
import com.ghInternship.GHInternship2026.dto.ErrorResponseDto;
import com.ghInternship.GHInternship2026.dto.UserLoginDto;
import com.ghInternship.GHInternship2026.dto.UserRegistrationDto;
import com.ghInternship.GHInternship2026.dto.UserResponseDto;
import com.ghInternship.GHInternship2026.entity.User;
import com.ghInternship.GHInternship2026.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Контроллер для аутентификации и регистрации пользователей.
 * Предоставляет публичные эндпоинты для создания аккаунтов и проверки учетных данных
 * с валидацией входных данных на основе Bean Validation (JSR 303/380) и BindingResult.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Эндпоинт регистрации нового пользователя.
     * Выполняет серверную валидацию UserRegistrationDto через JSR 303/380 (@Valid, @NotNull, @Size, @NotBlank).
     * Ошибки валидации перехватываются через BindingResult и возвращаются с кодом 400 Bad Request.
     *
     * @param registrationDto DTO с данными пользователя (логин и пароль)
     * @param bindingResult   результат валидации полей DTO
     * @param request         текущий HTTP-запрос
     * @return {@link ResponseEntity} с {@link UserResponseDto} (201 Created) или {@link ErrorResponseDto} (400 Bad Request)
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(
            @Valid @RequestBody UserRegistrationDto registrationDto,
            BindingResult bindingResult,
            HttpServletRequest request
    ) {
        log.info("Received request to register user: {}", registrationDto != null ? registrationDto.username() : "null");

        if (bindingResult.hasErrors()) {
            return handleValidationErrors(bindingResult, request, "/api/auth/register");
        }

        User createdUser = userService.registerUser(registrationDto);
        UserResponseDto response = new UserResponseDto(
                createdUser.getId(),
                createdUser.getUsername(),
                "User registered successfully"
        );

        log.info("User successfully created with id: {}", createdUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Эндпоинт аутентификации (входа) пользователя.
     * Проверяет валидацию UserLoginDto через BindingResult.
     * Проверяет существование пользователя по логину и совпадение пароля с сохранённым хэшем.
     *
     * @param loginDto      DTO с учетными данными (логин и пароль)
     * @param bindingResult результат валидации полей DTO
     * @param request       текущий HTTP-запрос
     * @return 200 OK при успехе, 400 Bad Request при невалидных данных, 401 Unauthorized при неверных учетных данных
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody UserLoginDto loginDto,
            BindingResult bindingResult,
            HttpServletRequest request
    ) {
        log.info("Received login attempt for user: {}", loginDto != null ? loginDto.username() : "null");

        if (bindingResult.hasErrors()) {
            return handleValidationErrors(bindingResult, request, "/api/auth/login");
        }

        Optional<User> optionalUser = userService.findByUsername(loginDto.username());

        if (optionalUser.isEmpty() || !passwordEncoder.matches(loginDto.password(), optionalUser.get().getPasswordHash())) {
            log.warn("Failed login attempt for user: {}", loginDto.username());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponseDto("Invalid username or password"));
        }

        log.info("User '{}' successfully authenticated", loginDto.username());
        return ResponseEntity.ok(new AuthResponseDto("Login successful"));
    }

    /**
     * Формирует понятный структурированный ответ об ошибках валидации (HTTP 400 Bad Request).
     *
     * @param bindingResult результат валидации с ошибками полей
     * @param request       текущий HTTP-запрос
     * @param defaultPath   дефолтный путь эндпоинта
     * @return {@link ResponseEntity} с {@link ErrorResponseDto} и кодом 400 Bad Request
     */
    private ResponseEntity<ErrorResponseDto> handleValidationErrors(
            BindingResult bindingResult,
            HttpServletRequest request,
            String defaultPath
    ) {
        log.warn("Validation failed for request: {} error(s) detected", bindingResult.getErrorCount());
        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
            log.debug("Field '{}' validation error: {}", fieldError.getField(), fieldError.getDefaultMessage());
        }

        String path = (request != null && request.getRequestURI() != null)
                ? request.getRequestURI()
                : defaultPath;

        ErrorResponseDto errorResponse = new ErrorResponseDto(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Validation failed for one or more fields",
                path,
                errors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}
