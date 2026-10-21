package com.ghInternship.GHInternship2026.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO для передачи учетных данных при аутентификации (входе).
 *
 * @param username имя пользователя
 * @param password пароль
 */
public record UserLoginDto(
        @NotBlank(message = "Username must not be blank")
        String username,

        @NotBlank(message = "Password must not be blank")
        String password
) {
}
