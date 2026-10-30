package com.ghInternship.GHInternship2026.controller;

import com.ghInternship.GHInternship2026.dto.AuthResponseDto;
import com.ghInternship.GHInternship2026.dto.UserLoginDto;
import com.ghInternship.GHInternship2026.dto.UserRegistrationDto;
import com.ghInternship.GHInternship2026.dto.UserResponseDto;
import com.ghInternship.GHInternship2026.entity.User;
import com.ghInternship.GHInternship2026.exception.InvalidCredentialsException;
import com.ghInternship.GHInternship2026.exception.ValidationException;
import com.ghInternship.GHInternship2026.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
     * Выполняет серверную валидацию UserRegistrationDto через JSR 303/380 (@Valid).
     * При ошибках валидации выбрасывает {@link ValidationException},
     * обрабатываемое централизованно в {@link com.ghInternship.GHInternship2026.exception.GlobalExceptionHandler}.
     *
     * @param registrationDto DTO с данными пользователя (логин и пароль)
     * @param bindingResult   результат валидации полей DTO
     * @return {@link ResponseEntity} с {@link UserResponseDto} (201 Created)
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(
            @Valid @RequestBody UserRegistrationDto registrationDto,
            BindingResult bindingResult
    ) {
        log.info("Received request to register user: {}", registrationDto != null ? registrationDto.username() : "null");

        if (bindingResult.hasErrors()) {
            log.warn("Validation failed for register request: {} error(s)", bindingResult.getErrorCount());
            throw new ValidationException(bindingResult);
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
     * При неверных данных выбрасывает {@link InvalidCredentialsException} (401 Unauthorized).
     *
     * @param loginDto      DTO с учетными данными (логин и пароль)
     * @param bindingResult результат валидации полей DTO
     * @return 200 OK с {@link AuthResponseDto} при успешной авторизации
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(
            @Valid @RequestBody UserLoginDto loginDto,
            BindingResult bindingResult
    ) {
        log.info("Received login attempt for user: {}", loginDto != null ? loginDto.username() : "null");

        if (bindingResult.hasErrors()) {
            log.warn("Validation failed for login request: {} error(s)", bindingResult.getErrorCount());
            throw new ValidationException(bindingResult);
        }

        Optional<User> optionalUser = userService.findByUsername(loginDto.username());

        if (optionalUser.isEmpty() || !passwordEncoder.matches(loginDto.password(), optionalUser.get().getPasswordHash())) {
            log.warn("Failed login attempt for user: {}", loginDto.username());
            throw new InvalidCredentialsException("Invalid username or password");
        }

        log.info("User '{}' successfully authenticated", loginDto.username());
        return ResponseEntity.ok(new AuthResponseDto("Login successful"));
    }
}
