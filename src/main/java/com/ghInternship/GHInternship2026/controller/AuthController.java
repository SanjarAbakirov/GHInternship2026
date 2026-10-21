package com.ghInternship.GHInternship2026.controller;

import com.ghInternship.GHInternship2026.dto.AuthResponseDto;
import com.ghInternship.GHInternship2026.dto.UserLoginDto;
import com.ghInternship.GHInternship2026.dto.UserRegistrationDto;
import com.ghInternship.GHInternship2026.dto.UserResponseDto;
import com.ghInternship.GHInternship2026.entity.User;
import com.ghInternship.GHInternship2026.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * Контроллер для аутентификации и регистрации пользователей.
 * Предоставляет публичные эндпоинты для создания аккаунтов и проверки учетных данных.
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
     *
     * @param registrationDto DTO с данными пользователя (логин и пароль)
     * @return {@link ResponseEntity} с {@link UserResponseDto} и статусом 201 Created
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@Valid @RequestBody UserRegistrationDto registrationDto) {
        log.info("Received request to register user: {}", registrationDto.username());

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
     * Проверяет существование пользователя по логину и совпадение пароля с сохранённым хэшем.
     *
     * @param loginDto DTO с учетными данными (логин и пароль)
     * @return 200 OK при успехе, 401 Unauthorized при неверных учетных данных
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody UserLoginDto loginDto) {
        log.info("Received login attempt for user: {}", loginDto.username());

        Optional<User> optionalUser = userService.findByUsername(loginDto.username());

        if (optionalUser.isEmpty() || !passwordEncoder.matches(loginDto.password(), optionalUser.get().getPasswordHash())) {
            log.warn("Failed login attempt for user: {}", loginDto.username());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponseDto("Invalid username or password"));
        }

        log.info("User '{}' successfully authenticated", loginDto.username());
        return ResponseEntity.ok(new AuthResponseDto("Login successful"));
    }
}
